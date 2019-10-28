/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.platform.client.cluster;

import org.apache.ignite.IgniteCluster;
import org.apache.ignite.binary.BinaryRawReader;
import org.apache.ignite.internal.processors.platform.client.ClientBooleanResponse;
import org.apache.ignite.internal.processors.platform.client.ClientConnectionContext;
import org.apache.ignite.internal.processors.platform.client.ClientRequest;
import org.apache.ignite.internal.processors.platform.client.ClientResponse;

/**
 * Get cache WAL state request.
 */
public class ClientClusterWalGetStateRequest extends ClientRequest {
    /** Cache name. */
    private final String cacheName;

    /**
     * Constructor.
     *
     * @param reader Reader/
     */
    public ClientClusterWalGetStateRequest(BinaryRawReader reader) {
        super(reader);
        cacheName = reader.readString();
    }

    /** {@inheritDoc} */
    @Override
    public ClientResponse process(ClientConnectionContext ctx) {
        IgniteCluster cluster = ctx.kernalContext().grid().cluster();
        return new ClientBooleanResponse(requestId(), cluster.isWalEnabled(cacheName));
    }
}