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

package org.apache.ignite.internal.binary;

import java.util.Collection;
import java.util.HashMap;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.binary.BinaryType;

/**
 * Simple caching metadata handler. Used mainly in tests.
 */
public class BinaryCachingMetadataHandler implements BinaryMetadataHandler {
    /** Cached metadatas. */
    private final HashMap<Integer, BinaryType> metas = new HashMap<>();

    /**
     * Create new handler instance.
     *
     * @return New handler.
     */
    public static BinaryCachingMetadataHandler create() {
        return new BinaryCachingMetadataHandler();
    }

    /**
     * Private constructor.
     */
    private BinaryCachingMetadataHandler() {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public synchronized void addMeta(int typeId, BinaryType type, boolean failIfUnregistered)
        throws BinaryObjectException {
        BinaryType oldType = metas.put(typeId, type);

        if (oldType != null) {
            BinaryMetadata oldMeta = ((BinaryTypeImpl)oldType).metadata();
            BinaryMetadata newMeta = ((BinaryTypeImpl)type).metadata();

            BinaryMetadata mergedMeta = BinaryUtils.mergeMetadata(oldMeta, newMeta);

            BinaryType mergedType = mergedMeta.wrap(((BinaryTypeImpl)oldType).context());

            metas.put(typeId, mergedType);
        }
    }

    /** {@inheritDoc} */
    @Override public synchronized void addMetaLocally(int typeId, BinaryType meta, boolean failIfUnregistered)
        throws BinaryObjectException {
        addMeta(typeId, meta, failIfUnregistered);
    }

    /** {@inheritDoc} */
    @Override public synchronized BinaryType metadata(int typeId) throws BinaryObjectException {
        return metas.get(typeId);
    }

    /** {@inheritDoc} */
    @Override public synchronized BinaryMetadata metadata0(int typeId) throws BinaryObjectException {
        BinaryTypeImpl type = (BinaryTypeImpl)metas.get(typeId);

        return type != null ? type.metadata() : null;
    }

    /** {@inheritDoc} */
    @Override public synchronized BinaryType metadata(int typeId, int schemaId) throws BinaryObjectException {
        BinaryTypeImpl type = (BinaryTypeImpl)metas.get(typeId);
        return type != null && type.metadata().hasSchema(schemaId) ? type : null;
    }

    /** {@inheritDoc} */
    @Override public Collection<BinaryType> metadata() throws BinaryObjectException {
        return metas.values();
    }
}
