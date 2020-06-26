/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
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

package org.apache.ignite.internal.commandline.diagnostic;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.client.GridClient;
import org.apache.ignite.internal.client.GridClientConfiguration;
import org.apache.ignite.internal.client.GridClientNode;
import org.apache.ignite.internal.commandline.Command;
import org.apache.ignite.internal.commandline.TaskExecutor;
import org.apache.ignite.internal.visor.diagnostic.availability.VisorConnectivityArgs;
import org.apache.ignite.internal.visor.diagnostic.availability.VisorConnectivityResult;
import org.apache.ignite.internal.visor.diagnostic.availability.ConnectivityStatus;
import org.apache.ignite.internal.visor.diagnostic.availability.VisorConnectivityTask;

import static org.apache.ignite.internal.commandline.CommandHandler.UTILITY_NAME;
import static org.apache.ignite.internal.commandline.CommandList.DIAGNOSTIC;
import static org.apache.ignite.internal.commandline.CommandLogger.join;
import static org.apache.ignite.internal.commandline.diagnostic.DiagnosticSubCommand.CONNECTIVITY;

/**
 * Command to check connectivity between every node
 */
public class ConnectivityCommand implements Command<Void> {
    /**
     * Logger
     */
    private Logger logger;

    /** {@inheritDoc} */
    @Override public Object execute(GridClientConfiguration clientCfg, Logger logger) throws Exception {
        this.logger = logger;

        Map<ClusterNode, VisorConnectivityResult> result;

        try (GridClient client = Command.startClient(clientCfg)) {
            Set<UUID> nodeIds = client.compute().nodes().stream().map(GridClientNode::nodeId).collect(Collectors.toSet());

            VisorConnectivityArgs taskArg = new VisorConnectivityArgs(nodeIds);

            result = TaskExecutor.executeTask(
                client,
                VisorConnectivityTask.class,
                taskArg,
                clientCfg
            );
        }

        printResult(result);

        return result;
    }

    /**
     * @param res Result.
     */
    private void printResult(Map<ClusterNode, VisorConnectivityResult> res) {
        final boolean[] hasFailed = {false};

        StringBuilder sb = new StringBuilder();

        Integer longestId = res.keySet().stream().map(ClusterNode::id).map(UUID::toString).map(String::length).max(Integer::compareTo).orElse(0);

        for (Map.Entry<ClusterNode, VisorConnectivityResult> entry : res.entrySet()) {
            ClusterNode key = entry.getKey();

            String id = key.id().toString();

            VisorConnectivityResult value = entry.getValue();

            Map<String, ConnectivityStatus> statuses = value.getNodeIds();

            String connectionStatus = statuses.entrySet().stream()
                .map(nodeStat -> {
                    String node = nodeStat.getKey();

                    ConnectivityStatus status = nodeStat.getValue();

                    if (status != ConnectivityStatus.OK) {
                        hasFailed[0] = true;

                        // id1 {spaces to align all ids according to the longest} {tab sign} id2
                        return String.format("%-" + longestId + "s\t%s", id, node);
                    }

                    return "";
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));

            sb.append(connectionStatus);
        }

        if (hasFailed[0])
            // SOURCE {spaces to align all ids according to the longest} {tab sign} DESTINATION
            logger.info(String.format("There is no connectivity between the following nodes:\n%-"
                    + longestId + "s\t%s\n%s", "SOURCE", "DESTINATION", sb));
        else
            logger.info("There are no connectivity problems.");
    }

    /** {@inheritDoc} */
    @Override public Void arg() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void printUsage(Logger logger) {
        logger.info("View connectvity state of all nodes in cluster");
        logger.info(join(" ",
            UTILITY_NAME, DIAGNOSTIC, CONNECTIVITY,
            "// Prints info about nodes' connectivity"));
        logger.info("");
    }

    /** {@inheritDoc} */
    @Override public String name() {
        return CONNECTIVITY.name();
    }
}
