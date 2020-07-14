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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        final List<List<String>> table = new ArrayList<>();
        table.add(Arrays.asList("SOURCE", "SOURCE_CONS_ID", "SOURCE_CLIENT", "DESTINATION", "DESTINATION_CONS_ID",
                "DESTINATION_CLIENT"));

        for (Map.Entry<ClusterNode, VisorConnectivityResult> entry : res.entrySet()) {
            ClusterNode key = entry.getKey();

            String id = key.id().toString();
            String consId = key.consistentId().toString();
            String isClient = String.valueOf(key.isClient());

            VisorConnectivityResult value = entry.getValue();

            Map<ClusterNode, Boolean> statuses = value.getNodeIds();

            List<List<String>> row = statuses.entrySet().stream().map(nodeStat -> {
                ClusterNode remoteNode = nodeStat.getKey();

                String remoteId = remoteNode.id().toString();
                String remoteConsId = remoteNode.consistentId().toString();
                String remoteIsClient = String.valueOf(remoteNode.isClient());

                Boolean status = nodeStat.getValue();

                if (!status) {
                    hasFailed[0] = true;
                    return Arrays.asList(id, consId, isClient, remoteId, remoteConsId, remoteIsClient);
                }

                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

            table.addAll(row);
        }

        if (hasFailed[0])
            logger.info("There is no connectivity between the following nodes:\n" + formatAsTable(table));
        else
            logger.info("There are no connectivity problems.");
    }

    /**
     * Format output as a table
     * @param rows table rows.
     * @return formatted string.
     */
    public static String formatAsTable(List<List<String>> rows) {
        int[] maxLengths = new int[rows.get(0).size()];

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++)
                maxLengths[i] = Math.max(maxLengths[i], row.get(i).length());
        }

        StringBuilder formatBuilder = new StringBuilder();

        for (int maxLength : maxLengths)
            formatBuilder.append("%-").append(maxLength + 2).append("s");

        String format = formatBuilder.toString();

        StringBuilder result = new StringBuilder();

        for (List<String> row : rows)
            result.append(String.format(format, row.toArray(new String[0]))).append("\n");

        return result.toString();
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
