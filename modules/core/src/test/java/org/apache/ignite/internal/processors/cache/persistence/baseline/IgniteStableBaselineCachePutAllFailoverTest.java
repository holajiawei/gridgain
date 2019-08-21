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
package org.apache.ignite.internal.processors.cache.persistence.baseline;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.TestDataStorageConfiguration;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.processors.cache.distributed.CachePutAllFailoverAbstractTest;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.GridTestUtils;

/**
 * Failover cache test with putAll operations executed with presence of BaselineTopology
 * when one random node from BLT is constantly restarted during the load.
 */
public class IgniteStableBaselineCachePutAllFailoverTest extends CachePutAllFailoverAbstractTest {
    /** */
    private static final int GRIDS_COUNT = 3;

    /** */
    private static final int OUT_OF_BASELINE_GRID_ID = GRIDS_COUNT;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setDataStorageConfiguration(
            new TestDataStorageConfiguration()
                .setDefaultDataRegionConfiguration(
                    new DataRegionConfiguration()
                        .setPersistenceEnabled(true)
                        .setInitialSize(200L * 1024 * 1024)
                        .setMaxSize(200L * 1024 * 1024)
                    .setCheckpointPageBufferSize(200L * 1024 * 1024)
                )
        );

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected IgniteInternalFuture createAndRunConcurrentAction(final AtomicBoolean finished, final long endTime) {
        return GridTestUtils.runAsync(new Callable() {
            @Override public Object call() throws Exception {
                Thread.currentThread().setName("restart-thread");

                while (!finished.get() && System.currentTimeMillis() < endTime) {
                    ThreadLocalRandom tlr = ThreadLocalRandom.current();

                    int idx = tlr.nextInt(1, GRIDS_COUNT + 1);

                    log.info("Stopping node " + idx);

                    stopGrid(idx);

                    U.sleep(tlr.nextInt(500, 700));

                    log.info("Restarting node " + idx);

                    startGrid(idx);
                }

                return null;
            }
        });
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        cleanPersistenceDir();

        startGrids(GRIDS_COUNT);

        grid(0).active(true);

        startGrid(OUT_OF_BASELINE_GRID_ID);

        awaitPartitionMapExchange();
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        cleanPersistenceDir();
    }
}
