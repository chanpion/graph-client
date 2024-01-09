package com.chenpp.graph.janus;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.JanusGraphTransaction;

/**
 * @author pengpeng.chen
 * @date 2021/4/16
 */
public class JanusGraphClient {

    public static void main(String[] args)
    {
        createVertexAndEdge();
    }

    public static void createVertexAndEdge()
    {
        //First configure the graph
        JanusGraphFactory.Builder builder = JanusGraphFactory.build();
        builder.set("storage.backend", "cassandrathrift");
        builder.set("storage.hostname", "192.168.192.128");
        builder.set("storage.port", "9160");
        //ip address where cassandra is installed
        //builder.set("storage.username", “cassandra”);
        //buder.set("storage.password", “cassandra”);
        //builder.set("storage.cassandra.keyspace", "testing");

        //open a graph database
        JanusGraph graph = builder.open();
        //Open a transaction
        JanusGraphTransaction tx = graph.newTransaction();
        //Create a vertex v1 with label student, add property to the vertex
        Vertex v1 = tx.addVertex(T.label, "student");
        v1.property("id", 1);
        //create a vertex v2 without label and property
        Vertex v2 = tx.addVertex();
        //create a vertex v3 with label student, then add property to the vertex
        Vertex v3 = tx.addVertex(T.label, "student");
        v3.property("id", 2);
        tx.commit();

        //Create edge between 2 vertices
        Edge edge12 = v1.addEdge("friends", v2);
        Edge edge13 = v1.addEdge("friends", v3);
        //Finally commit the transaction
        tx.commit();

        System.out.println(graph.traversal().V());
        System.out.println(graph.traversal().E());
    }
}
