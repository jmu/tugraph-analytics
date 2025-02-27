/*
 * Copyright 2023 AntGroup CO., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.antgroup.geaflow.dsl.catalog.console;

import com.antgroup.geaflow.common.config.Configuration;
import com.antgroup.geaflow.dsl.catalog.Catalog;
import com.antgroup.geaflow.dsl.catalog.exception.ObjectAlreadyExistException;
import com.antgroup.geaflow.dsl.schema.GeaFlowFunction;
import com.antgroup.geaflow.dsl.schema.GeaFlowGraph;
import com.antgroup.geaflow.dsl.schema.GeaFlowTable;
import com.antgroup.geaflow.dsl.schema.GeaFlowView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.calcite.schema.Table;

public class ConsoleCatalog implements Catalog {

    public static final String CATALOG_TYPE = "console";

    private final Map<String, Map<String, Table>> allTables = new HashMap<>();

    private Set<String> allInstances;

    private Set<String> allGraphsAndTables;

    private ConsoleCatalogClient client;

    @Override
    public void init(Configuration config) {
        this.client = new ConsoleCatalogClient(config);
    }

    @Override
    public String getType() {
        return CATALOG_TYPE;
    }

    @Override
    public Set<String> listInstances() {
        if (allInstances == null) {
            allInstances = client.getInstances();
        }
        return allInstances;
    }

    @Override
    public boolean isInstanceExists(String instanceName) {
        Set<String> allInstances = listInstances();
        return allInstances.contains(instanceName);
    }

    @Override
    public Table getGraph(String instanceName, String graphName) {
        return allTables
            .computeIfAbsent(instanceName, k -> new HashMap<>())
            .computeIfAbsent(graphName, k -> client.getGraph(instanceName, graphName));
    }

    @Override
    public Table getTable(String instanceName, String tableName) {
        return allTables
            .computeIfAbsent(instanceName, k -> new HashMap<>())
            .computeIfAbsent(tableName, k -> client.getTable(instanceName, tableName));
    }

    @Override
    public GeaFlowFunction getFunction(String instanceName, String functionName) {
        return null;
    }

    @Override
    public Set<String> listGraphAndTable(String instanceName) {
        if (allGraphsAndTables == null) {
            allGraphsAndTables = new HashSet<>();
            allGraphsAndTables.addAll(client.getGraphs(instanceName));
            allGraphsAndTables.addAll(client.getTables(instanceName));
        }
        return allGraphsAndTables;
    }

    @Override
    public void createGraph(String instanceName, GeaFlowGraph graph)
        throws ObjectAlreadyExistException {
        if (getGraph(instanceName, graph.getName()) != null) {
            if (!graph.isIfNotExists()) {
                throw new ObjectAlreadyExistException(graph.getName());
            }
            return;
        }
        client.createGraph(instanceName, graph);
        allTables.get(instanceName).put(graph.getName(), graph);
    }

    @Override
    public void createTable(String instanceName, GeaFlowTable table)
        throws ObjectAlreadyExistException {
        if (getTable(instanceName, table.getName()) != null) {
            if (!table.isIfNotExists()) {
                throw new ObjectAlreadyExistException(table.getName());
            }
            // ignore if table exists.
            return;
        }
        client.createTable(instanceName, table);
        allTables.get(instanceName).put(table.getName(), table);
    }

    @Override
    public void createView(String instanceName, GeaFlowView view)
        throws ObjectAlreadyExistException {
        Map<String, Table> tableMap = allTables
            .computeIfAbsent(instanceName, k -> new HashMap<>());
        Table geaFlowView = tableMap.get(view.getName());
        if (geaFlowView != null) {
            if (!view.isIfNotExists()) {
                throw new ObjectAlreadyExistException(view.getName());
            }
            return;
        }
        tableMap.put(view.getName(), view);
    }

    @Override
    public void createFunction(String instanceName, GeaFlowFunction function)
        throws ObjectAlreadyExistException {

    }

    @Override
    public void dropGraph(String instanceName, String graphName) {
        client.deleteGraph(instanceName, graphName);
        allTables.computeIfAbsent(instanceName, k -> new HashMap<>()).remove(graphName);
    }

    @Override
    public void dropTable(String instanceName, String tableName) {
        client.deleteTable(instanceName, tableName);
        allTables.computeIfAbsent(instanceName, k -> new HashMap<>()).remove(tableName);
    }

    @Override
    public void dropFunction(String instanceName, String functionName) {

    }

    @Override
    public String describeGraph(String instanceName, String graphName) {
        Table graph = getGraph(instanceName, graphName);
        if (graph != null) {
            return graph.toString();
        }
        return null;
    }

    @Override
    public String describeTable(String instanceName, String tableName) {
        Table table = getTable(instanceName, tableName);
        if (table != null) {
            return table.toString();
        }
        return null;
    }

    @Override
    public String describeFunction(String instanceName, String functionName) {
        return null;
    }
}
