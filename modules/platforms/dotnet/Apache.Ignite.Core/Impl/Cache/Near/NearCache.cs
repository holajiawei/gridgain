/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

namespace Apache.Ignite.Core.Impl.Cache.Near
{
    using System;
    using System.Collections.Concurrent;
    using System.Diagnostics;
    using System.Threading;
    using Apache.Ignite.Core.Impl.Binary;
    using Apache.Ignite.Core.Impl.Binary.IO;
    using Apache.Ignite.Core.Impl.Memory;

    /// <summary>
    /// Holds near cache data for a given cache, serves one or more <see cref="CacheImpl{TK,TV}"/> instances.
    /// </summary>
    internal sealed class NearCache<TK, TV> : INearCache
    {
        /** Generic map, used by default, should fit most use cases. */
        private volatile ConcurrentDictionary<TK, NearCacheEntry<TV>> _map = 
            new ConcurrentDictionary<TK, NearCacheEntry<TV>>();

        /** Non-generic map. Switched to when same cache is used with different generic arguments.
         * Less efficient because of boxing and casting. */
        private volatile ConcurrentDictionary<object, NearCacheEntry<object>> _fallbackMap;

        /** Entry ID counter. */
        // ReSharper disable once StaticMemberInGenericType (reviewed).
        private static long _entryId;

        public bool TryGetValue<TKey, TVal>(TKey key, out TVal val)
        {
            // ReSharper disable once SuspiciousTypeConversion.Global (reviewed)
            var map = _map as ConcurrentDictionary<TKey, NearCacheEntry<TVal>>;
            if (map != null)
            {
                NearCacheEntry<TVal> entry;
                if (map.TryGetValue(key, out entry) && entry.HasValue)
                {
                    val = entry.Value;
                    return true;
                }
            }
            
            if (_fallbackMap != null)
            {
                NearCacheEntry<object> fallbackEntry;
                if (_fallbackMap.TryGetValue(key, out fallbackEntry) && fallbackEntry.HasValue)
                {
                    val = (TVal) fallbackEntry.Value;
                    return true;
                }
            }

            val = default(TVal);
            return false;
        }

        public void Put<TKey, TVal>(TKey key, TVal val)
        {
            // ReSharper disable once SuspiciousTypeConversion.Global (reviewed)
            var map = _map as ConcurrentDictionary<TKey, NearCacheEntry<TVal>>;
            if (map != null)
            {
                map[key] = new NearCacheEntry<TVal>(true, val);
                return;
            }

            EnsureFallbackMap();
            _fallbackMap[key] = new NearCacheEntry<object>(true, val);
        }

        public object GetOrCreateEntry<TKey, TVal>(TKey key)
        {
            // Near cache on Java side works as a "subscription":
            // When you first get or put the given key, subscription to the changes is established.
            // This subscription can be later removed by eviction.
            // The problem on .NET side is that we want to store the value for a new subscription
            // in the .NET near cache, but we should account for concurrent invalidation or eviction.
            // Possibilities:
            // - Subscription already exists => .NET near cache already has the value, it is returned.
            // - Subscription does not exist
            // -- Trivial case: subscription is created, store retrieved value in .NET Near Cache
            // -- Concurrent eviction: near cache entry is evicted in parallel, and subscription is removed.
            //    We should not store retrieved value in .NET near cache, since it will become stale.
            // -- Concurrent invalidation: another value is set for the given key, we should not overwrite it,
            //    our value is potentially old
            //
            // Concurrent eviction is what forces us to use a wrapper: NearCacheEntry.
            // Since NearCacheEntry is a struct (to reduce allocations), we have to use a unique counter
            // to avoid stale values in case when update happens concurrently between calls to GetOrCreateEntry and
            // SetEntryValue
            
            // ReSharper disable once SuspiciousTypeConversion.Global (reviewed)
            var map = _map as ConcurrentDictionary<TKey, NearCacheEntry<TVal>>;
            if (map != null)
            {
                return map.GetOrAdd(key, _ => new NearCacheEntry<TVal>(false));
            }
            
            EnsureFallbackMap();
            return _fallbackMap.GetOrAdd(key, _ => new NearCacheEntry<object>(false));
        }

        public void SetEntryValue<TKey, TVal>(object entry, TKey key, TVal val)
        {
            // ReSharper disable once SuspiciousTypeConversion.Global (reviewed)
            var map = _map as ConcurrentDictionary<TKey, NearCacheEntry<TVal>>;
            if (map != null)
            {
                map.TryUpdate(key, new NearCacheEntry<TVal>(true, val), (NearCacheEntry<TVal>) entry);
                return;
            }
            
            EnsureFallbackMap();
            _fallbackMap.TryUpdate(key, new NearCacheEntry<object>(true, val), (NearCacheEntry<object>) entry);
        }

        public void Update(IBinaryStream stream, Marshaller marshaller)
        {
            Debug.Assert(stream != null);
            Debug.Assert(marshaller != null);

            var reader = marshaller.StartUnmarshal(stream);

            var key = reader.ReadObject<object>();
            var hasVal = reader.ReadBoolean();
            var val = hasVal ? reader.ReadObject<object>() : null;

            var map = _map;
            if (map != null && key is TK)
            {
                if (hasVal)
                {
                    if (val is TV)
                    {
                        _map[(TK) key] = new NearCacheEntry<TV>(true, (TV) val);
                        return;
                    }
                }
                else
                {
                    NearCacheEntry<TV> unused;
                    _map.TryRemove((TK) key, out unused);
                    return;
                }
            }

            EnsureFallbackMap();

            if (hasVal)
            {
                _fallbackMap[key] = new NearCacheEntry<object>(true, val);
            }
            else
            {
                NearCacheEntry<object> unused;
                _fallbackMap.TryRemove(key, out unused);
            }
        }

        public void Evict(PlatformMemoryStream stream, Marshaller marshaller)
        {
            // Eviction callbacks from Java work for 2 out of 3 cases:
            // + Client node (all keys)
            // + Server node (non-primary keys)
            // - Server node (primary keys) - because there is no need to store primary keys in near cache

            Debug.Assert(stream != null);
            Debug.Assert(marshaller != null);
            
            var reader = marshaller.StartUnmarshal(stream);
            var key = reader.ReadObject<object>();

            var map = _map;
            if (map != null && key is TK)
            {
                NearCacheEntry<TV> unused;
                _map.TryRemove((TK) key, out unused);
            }

            if (_fallbackMap != null)
            {
                NearCacheEntry<object> unused;
                _fallbackMap.TryRemove(key, out unused);
            }
        }

        public void Clear()
        {
            if (_fallbackMap != null)
            {
                _fallbackMap.Clear();
            }
            else
            {
                var map = _map;
                if (map != null)
                {
                    map.Clear();
                }
            }
        }

        public void Remove<TKey, TVal>(TKey key)
        {
            // ReSharper disable once SuspiciousTypeConversion.Global (reviewed)
            var map = _map as ConcurrentDictionary<TKey, NearCacheEntry<TVal>>;
            if (map != null)
            {
                NearCacheEntry<TVal> unused;
                map.TryRemove(key, out unused);
            }

            if (_fallbackMap != null)
            {
                NearCacheEntry<object> unused;
                _fallbackMap.TryRemove(key, out unused);
            }
        }

        /// <summary>
        /// Switches this instance to fallback mode (generic downgrade).
        /// </summary>
        private void EnsureFallbackMap()
        {
            _fallbackMap = _fallbackMap ?? new ConcurrentDictionary<object, NearCacheEntry<object>>();
            _map = null;
        }
        
        private struct NearCacheEntry<T> : IEquatable<NearCacheEntry<T>>
        {
            public readonly T Value;

            public readonly bool HasValue;

            private readonly long _id;

            public NearCacheEntry(bool hasValue, T val = default(T))
            {
                HasValue = hasValue;
                Value = val;
                _id = Interlocked.Increment(ref _entryId);
            }

            public bool Equals(NearCacheEntry<T> other)
            {
                return _id == other._id;
            }

            public override bool Equals(object obj)
            {
                if (ReferenceEquals(null, obj))
                {
                    return false;
                }

                if (obj.GetType() != GetType())
                {
                    return false;
                }
                
                return Equals((NearCacheEntry<T>) obj);
            }

            public override int GetHashCode()
            {
                return _id.GetHashCode();
            }
        }
    }
}