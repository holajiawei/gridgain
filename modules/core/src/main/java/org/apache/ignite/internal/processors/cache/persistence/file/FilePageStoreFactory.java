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

package org.apache.ignite.internal.processors.cache.persistence.file;

import java.io.File;
import java.nio.file.Path;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.pagemem.PageIdAllocator;
import org.apache.ignite.internal.pagemem.store.PageStore;
import org.apache.ignite.internal.processors.metric.impl.LongAdderMetric;
import org.apache.ignite.lang.IgniteOutClosure;

/**
 *
 */
public interface FilePageStoreFactory {
    /**
     * Creates instance of PageStore based on given file.
     *
     * @param type Data type, can be {@link PageIdAllocator#FLAG_IDX} or {@link PageIdAllocator#FLAG_DATA}.
     * @param file File Page store file.
     * @param allocatedTracker metrics updater.
     * @return page store
     * @throws IgniteCheckedException if failed.
     */
    default PageStore createPageStore(byte type, File file, LongAdderMetric allocatedTracker)
        throws IgniteCheckedException {
        return createPageStore(type, file::toPath, allocatedTracker);
    }

    /**
     * Creates instance of PageStore based on file path provider.
     *
     * @param type Data type, can be {@link PageIdAllocator#FLAG_IDX} or {@link PageIdAllocator#FLAG_DATA}
     * @param pathProvider File Page store path provider.
     * @param allocatedTracker metrics updater
     * @return page store
     * @throws IgniteCheckedException if failed
     */
    PageStore createPageStore(byte type, IgniteOutClosure<Path> pathProvider, LongAdderMetric allocatedTracker)
        throws IgniteCheckedException;
}
