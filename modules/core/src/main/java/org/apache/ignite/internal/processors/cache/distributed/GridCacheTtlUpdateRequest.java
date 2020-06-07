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

package org.apache.ignite.internal.processors.cache.distributed;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.GridDirectCollection;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.cache.GridCacheIdMessage;
import org.apache.ignite.internal.processors.cache.GridCacheSharedContext;
import org.apache.ignite.internal.processors.cache.KeyCacheObject;
import org.apache.ignite.internal.processors.cache.version.GridCacheVersion;
import org.apache.ignite.internal.util.tostring.GridToStringInclude;
import org.apache.ignite.internal.util.typedef.internal.CU;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.plugin.extensions.communication.MessageCollectionItemType;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;

/**
 *
 */
public class GridCacheTtlUpdateRequest extends GridCacheIdMessage {
    /** */
    private static final long serialVersionUID = 0L;

    /** Entries keys. */
    @GridToStringInclude
    @GridDirectCollection(KeyCacheObject.class)
    private List<KeyCacheObject> keys;

    /** Entries versions. */
    @GridDirectCollection(GridCacheVersion.class)
    private List<GridCacheVersion> vers;

    /** Near entries keys. */
    @GridToStringInclude
    @GridDirectCollection(KeyCacheObject.class)
    private List<KeyCacheObject> nearKeys;

    /** Near entries versions. */
    @GridDirectCollection(GridCacheVersion.class)
    private List<GridCacheVersion> nearVers;

    /** New TTL. */
    private long ttl;

    /** Topology version. */
    private AffinityTopologyVersion topVer;

    /**
     * Required empty constructor.
     */
    public GridCacheTtlUpdateRequest() {
        // No-op.
    }

    /**
     * @param cacheId Cache ID.
     * @param topVer Topology version.
     * @param ttl TTL.
     */
    public GridCacheTtlUpdateRequest(int cacheId, AffinityTopologyVersion topVer, long ttl) {
        assert ttl >= 0 || ttl == CU.TTL_ZERO : ttl;

        this.cacheId = cacheId;
        this.topVer = topVer;
        this.ttl = ttl;
    }

    /**
     * @return Topology version.
     */
    @Override public AffinityTopologyVersion topologyVersion() {
        return topVer;
    }

    /**
     * @return TTL.
     */
    public long ttl() {
        return ttl;
    }

    /**
     * @param key Key.
     * @param ver Version.
     */
    public void addEntry(KeyCacheObject key, GridCacheVersion ver) {
        if (keys == null) {
            keys = new ArrayList<>();

            vers = new ArrayList<>();
        }

        keys.add(key);

        vers.add(ver);
    }

    /**
     * @param key Key.
     * @param ver Version.
     */
    public void addNearEntry(KeyCacheObject key, GridCacheVersion ver) {
        if (nearKeys == null) {
            nearKeys = new ArrayList<>();

            nearVers = new ArrayList<>();
        }

        nearKeys.add(key);

        nearVers.add(ver);
    }

    /**
     * @return Keys.
     */
    public List<KeyCacheObject> keys() {
        return keys;
    }

    /**
     * @return Versions.
     */
    public List<GridCacheVersion> versions() {
        return vers;
    }

    /**
     * @param idx Entry index.
     * @return Version.
     */
    public GridCacheVersion version(int idx) {
        assert idx >= 0 && idx < vers.size() : idx;

        return vers.get(idx);
    }

    /**
     * @return Keys for near cache.
     */
    public List<KeyCacheObject> nearKeys() {
        return nearKeys;
    }

    /**
     * @return Versions for near cache entries.
     */
    public List<GridCacheVersion> nearVersions() {
        return nearVers;
    }

    /** {@inheritDoc} */
    @Override public void prepareMarshal(GridCacheSharedContext ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        GridCacheContext cctx = ctx.cacheContext(cacheId);

        prepareMarshalCacheObjects(keys, cctx);

        prepareMarshalCacheObjects(nearKeys, cctx);
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext ctx, ClassLoader ldr)
        throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        GridCacheContext cctx = ctx.cacheContext(cacheId);

        finishUnmarshalCacheObjects(keys, cctx, ldr);

        finishUnmarshalCacheObjects(nearKeys, cctx, ldr);
    }

    /** {@inheritDoc} */
    @Override public boolean addDeploymentInfo() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!super.writeTo(buf, writer))
            return false;

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 4:
                if (!writer.writeCollection("keys", keys, MessageCollectionItemType.MSG))
                    return false;

                writer.incrementState();

            case 5:
                if (!writer.writeCollection("nearKeys", nearKeys, MessageCollectionItemType.MSG))
                    return false;

                writer.incrementState();

            case 6:
                if (!writer.writeCollection("nearVers", nearVers, MessageCollectionItemType.MSG))
                    return false;

                writer.incrementState();

            case 7:
                if (!writer.writeAffinityTopologyVersion("topVer", topVer))
                    return false;

                writer.incrementState();

            case 8:
                if (!writer.writeLong("ttl", ttl))
                    return false;

                writer.incrementState();

            case 9:
                if (!writer.writeCollection("vers", vers, MessageCollectionItemType.MSG))
                    return false;

                writer.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        reader.setBuffer(buf);

        if (!reader.beforeMessageRead())
            return false;

        if (!super.readFrom(buf, reader))
            return false;

        switch (reader.state()) {
            case 4:
                keys = reader.readCollection("keys", MessageCollectionItemType.MSG);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 5:
                nearKeys = reader.readCollection("nearKeys", MessageCollectionItemType.MSG);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 6:
                nearVers = reader.readCollection("nearVers", MessageCollectionItemType.MSG);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 7:
                topVer = reader.readAffinityTopologyVersion("topVer");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 8:
                ttl = reader.readLong("ttl");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 9:
                vers = reader.readCollection("vers", MessageCollectionItemType.MSG);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return reader.afterMessageRead(GridCacheTtlUpdateRequest.class);
    }

    /** {@inheritDoc} */
    @Override public short directType() {
        return 20;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 10;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridCacheTtlUpdateRequest.class, this, "super", super.toString());
    }
}
