package com.chenpp.graph.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import static org.neo4j.driver.Values.parameters;

/**
 * @author April.Chen
 * @date 2023/10/16 11:54 上午
 **/
public class Neo4jExample implements AutoCloseable {
    private final Driver driver;

    public Neo4jExample(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public void printGreeting(final String message) {
        try (Session session = driver.session()) {
            String greeting = session.executeWrite(tx -> {
                Query query = new Query("CREATE (a:Greeting) SET a.message = $message RETURN a.message + ', from node ' + id(a)",
                        parameters("message", message));
                Result result = tx.run(query);
                return result.single().get(0).asString();
            });
            System.out.println(greeting);
        }
    }

    public static void main(String... args) throws Exception {
        try (Neo4jExample greeter = new Neo4jExample("bolt://localhost:7687", "neo4j", "password")) {
            greeter.printGreeting("hello, world");
        }
    }
}