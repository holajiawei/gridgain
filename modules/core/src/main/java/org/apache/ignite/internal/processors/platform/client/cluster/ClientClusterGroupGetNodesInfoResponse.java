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

package org.apache.ignite.internal.processors.platform.client.cluster;

import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.binary.BinaryRawWriterEx;
import org.apache.ignite.internal.processors.platform.client.ClientConnectionContext;
import org.apache.ignite.internal.processors.platform.client.ClientResponse;
import org.apache.ignite.internal.processors.platform.utils.PlatformUtils;

import java.util.Collection;

/**
 * Cluster group get nodes information response.
 */
public class ClientClusterGroupGetNodesInfoResponse extends ClientResponse {
    /** Nodes collection. */
    private final Collection<ClusterNode> nodes;

    /**
     * Constructor.
     *
     * @param reqId Request identifier.
     */
    public ClientClusterGroupGetNodesInfoResponse(long reqId, Collection<ClusterNode> nodes) {
        super(reqId);
        this.nodes = nodes;
    }

    @Override
    public void encode(ClientConnectionContext ctx, BinaryRawWriterEx writer) {
        super.encode(ctx, writer);

        writer.writeInt(nodes.size());

        for (ClusterNode node: nodes){
            PlatformUtils.writeNodeInfo(writer, node);
        }
    }
}
