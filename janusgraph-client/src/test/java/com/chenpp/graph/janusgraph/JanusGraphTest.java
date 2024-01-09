package com.chenpp.graph.janusgraph;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphVertex;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.PropertyKeyMaker;
import org.janusgraph.diskstorage.BackendException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.janusgraph.core.Multiplicity.MULTI;
import static org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration.STORAGE_BACKEND;
import static org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration.STORAGE_HOSTS;
import static org.janusgraph.graphdb.configuration.GraphDatabaseConfiguration.STORAGE_PORT;

/**
 * @author April.Chen
 * @date 2023/10/16 4:29 下午
 **/
public class JanusGraphTest {
    private static Logger log = LoggerFactory.getLogger(JanusGraphTest.class);

    public static JanusGraph connect() {
        Configuration configuration = new BaseConfiguration();
        configuration.setProperty(STORAGE_BACKEND.toStringWithoutRoot(), "cql");
        configuration.setProperty(STORAGE_HOSTS.toStringWithoutRoot(), "10.58.12.60");
        configuration.setProperty(STORAGE_PORT.toStringWithoutRoot(), "9042");
        configuration.setProperty("storage.cassandra.keyspace", "demo");
//        configuration.setProperty("index.demo.index-name", "demo");

        JanusGraph graph = JanusGraphFactory.open(configuration);
        System.out.println(graph.isOpen());
        log.info("graph open: {}", graph.isOpen());
        return graph;
    }


    public static void createSchema(JanusGraph graph) {
        JanusGraphManagement mgmt = graph.openManagement();
        VertexLabel person = mgmt.makeVertexLabel("person").make();
        VertexLabel company = mgmt.makeVertexLabel("company").make();
        EdgeLabel works = mgmt.makeEdgeLabel("works").multiplicity(MULTI).make();


        PropertyKey name = mgmt.makePropertyKey("name").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        PropertyKey age = mgmt.makePropertyKey("age").dataType(Integer.class).cardinality(Cardinality.SINGLE).make();
        PropertyKey startTime = mgmt.makePropertyKey("startTime").dataType(String.class).cardinality(Cardinality.SINGLE).make();
        mgmt.addProperties(person, name, age);
        mgmt.addProperties(company, name);
        mgmt.addProperties(works, startTime);

        mgmt.addConnection(works, person, company);

//        mgmt.buildIndex("nameIndex", Vertex.class).addKey(name).buildMixedIndex("search");
//        mgmt.addIndexKey(mgmt.getGraphIndex("nameIndex"), name);

        mgmt.commit();
    }

    public static void addVertex(JanusGraph graph) {
        graph.addVertex("person").property("name", "Tom").property("age", 20);
        graph.addVertex("person").property("name", "Lily").property("age", 18);
        graph.addVertex("person").property("name", "Joy").property("age", 30);
        graph.addVertex("company").property("name", "TD");
        graph.tx().commit();
    }

    public static void addEdge(JanusGraph graph) {
        GraphTraversalSource traversal = graph.traversal();
        Vertex person = traversal.V().hasLabel("person").has("name", "Tom").next();
        Vertex company = traversal.V().hasLabel("company").has("name", "TD").next();
        traversal.addE("works").property("startTime", "2021-04-12").from(person).to(company).iterate();
        traversal.V().hasLabel("person").has("name", "Lily").as("src").V().hasLabel("company").has("name", "TD").as("dst").addE("works").property("startTime", "2020-01-01").from("src").to("dst").next();
        traversal.V().hasLabel("person").has("name", "Joy").as("src").V().hasLabel("company").has("name", "TD").addE("works").property("startTime", "2022-01-01").from("src").next();
        traversal.tx().commit();
    }

    public void dropGraph(JanusGraph graph) {
        try {
            JanusGraphManagement mgmt = graph.openManagement();
            for (String openInstance : mgmt.getOpenInstances()) {
                if (!openInstance.contains("current")) {
                    mgmt.forceCloseInstance(openInstance);
                }
            }
            JanusGraphFactory.drop(graph);
        } catch (BackendException e) {
            e.printStackTrace();
        }
    }

    public static void queryVertex(JanusGraph graph) {
        GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V().limit(100);
        List<JSONObject> vertices = new ArrayList<>();
        while (traversal.hasNext()) {
            Vertex v = traversal.next();
            JSONObject node = new JSONObject();
            node.put("label", v.label());
            node.put("id", String.valueOf(v.id()));
            Map<String, Object> properties = new HashMap<>();
            Iterator<VertexProperty<Object>> iterator = v.properties();
            while (iterator.hasNext()) {
                VertexProperty<Object> vertexProperty = iterator.next();
                properties.put(vertexProperty.key(), vertexProperty.value());
            }
            node.put("properties", properties);
            vertices.add(node);
        }
        log.info("vertices: {}", vertices);
    }

    public static void queryEdge(JanusGraph graph) {
        GraphTraversal<Edge, Edge> traversal = graph.traversal().E().limit(100);
        List<JSONObject> edges = new ArrayList<>();
        while (traversal.hasNext()) {
            Edge e = traversal.next();
            JSONObject edge = new JSONObject();
            edge.put("label", e.label());
            edge.put("id", String.valueOf(e.id()));
            edge.put("from", String.valueOf(e.outVertex().id()));
            edge.put("to", String.valueOf(e.inVertex().id()));
            Map<String, Object> properties = new HashMap<>();
            Iterator<Property<Object>> iterator = e.properties();
            while (iterator.hasNext()) {
                Property<Object> property = iterator.next();
                properties.put(property.key(), property.value());
            }
            edge.put("properties", properties);
            edges.add(edge);
        }
        log.info("edges: {}", edges);
    }

    public static void clearGraphData(JanusGraph graph) {
        GraphTraversalSource traversal = graph.traversal();
        traversal.V().drop().iterate();
        traversal.E().drop().iterate();
        traversal.tx().commit();
    }
    public static void statGraphData(JanusGraph graph) {
        GraphTraversalSource traversal = graph.traversal();
        long vertexCount = traversal.V().count().next();
        long edgeCount = traversal.E().count().next();
        log.info("vertex count: {}, edge count: {}", vertexCount,edgeCount);
    }

    public static void main(String[] args) throws Exception {
        JanusGraph graph = connect();
//        queryVertex(graph);
//        queryEdge(graph);
//
//        graph = connect();
//        JanusGraphFactory.drop(graph);

//        createSchema(graph);
//        Set<String> graphs = JanusGraphFactory.getGraphNames();
//        log.info("graphs: {}", graphs);

        clearGraphData(graph);
        addVertex(graph);
        addEdge(graph);
        statGraphData(graph);
        queryVertex(graph);
        queryEdge(graph);

        graph.close();
    }
}
