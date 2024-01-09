package com.chenpp.graph.nebula;

import com.vesoft.nebula.Date;
import com.vesoft.nebula.Value;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.BindSpaceFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Query;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author April.Chen
 * @date 2023/10/11 3:14 下午
 **/
public class NebulaTest {
    private static Logger log = LoggerFactory.getLogger(NebulaTest.class);


    public static SessionPool initSessionPool() {
        List<HostAddress> addresses = Arrays.asList(new HostAddress("10.57.36.17", 9660),
                new HostAddress("10.57.36.18", 9660), new HostAddress("10.57.36.19", 9660));
        String spaceName = "cpp_test_20231017";
        String user = "root";
        String password = "nebula";
        SessionPoolConfig sessionPoolConfig = new SessionPoolConfig(addresses, spaceName, user, password);
        SessionPool sessionPool = new SessionPool(sessionPoolConfig);
        if (!sessionPool.isActive()) {
            log.error("session pool init failed.");
            System.exit(1);
        }
        return sessionPool;
    }

    public static NebulaPool initNebulaPool() {
        NebulaPoolConfig nebulaPoolConfig = new NebulaPoolConfig();
        nebulaPoolConfig.setMaxConnSize(10);
        List<HostAddress> addresses = Arrays.asList(new HostAddress("10.57.36.17", 9660),
                new HostAddress("10.57.36.18", 9660), new HostAddress("10.57.36.19", 9660));
        NebulaPool pool = new NebulaPool();

        Session session = null;
        try {
            boolean initResult = pool.init(addresses, nebulaPoolConfig);
            if (!initResult) {
                log.error("pool init failed.");
                System.exit(1);
            }
            session = pool.getSession("root", "nebula", false);
        } catch (Exception e) {
            log.error("init nebula session error", e);
        }
        return pool;
    }

    public static Session connect(NebulaPool pool) {
        Session session = null;
        try {
            session = pool.getSession("root", "nebula", false);
        } catch (Exception e) {
            log.error("init nebula session error", e);
        }
        return session;
    }

    public static void createSchema(Session session) {
        String createSchema = "CREATE SPACE IF NOT EXISTS cpp_test_20231017(vid_type=fixed_string(20)); "
                + "USE cpp_test_20231017;"
                + "CREATE TAG IF NOT EXISTS person(name string, age int);"
                + "CREATE EDGE IF NOT EXISTS like(likeness double)";
        ResultSet resp = null;
        try {
            resp = session.execute(createSchema);
            if (!resp.isSucceeded()) {
                log.error(String.format("Execute: `%s', failed: %s", createSchema, resp.getErrorMessage()));
                System.exit(1);
            }
        } catch (IOErrorException e) {
            log.error("create schema error", e);
        }
    }

    public static void insertVertex(Session session) {
        String insertVertexes = "INSERT VERTEX person(name, age) VALUES "
                + "'Bob':('Bob', 10), "
                + "'Lily':('Lily', 9), "
                + "'Tom':('Tom', 10), "
                + "'Jerry':('Jerry', 13), "
                + "'John':('John', 11);";
        ResultSet resp = null;
        try {
            resp = session.execute(insertVertexes);
            if (!resp.isSucceeded()) {
                log.error(String.format("Execute: `%s', failed: %s", insertVertexes, resp.getErrorMessage()));
                System.exit(1);
            }
        } catch (IOErrorException e) {
            log.error("insert vertex error", e);
        }
    }

    public static void insertEdge(Session session) {
        try {
            String insertEdges = "INSERT EDGE like(likeness) VALUES "
                    + "'Bob'->'Lily':(80.0), "
                    + "'Bob'->'Tom':(70.0), "
                    + "'Lily'->'Jerry':(84.0), "
                    + "'Tom'->'Jerry':(68.3), "
                    + "'Bob'->'John':(97.2);";
            ResultSet resp = session.execute(insertEdges);
            if (!resp.isSucceeded()) {
                log.error(String.format("Execute: `%s', failed: %s",
                        insertEdges, resp.getErrorMessage()));
                System.exit(1);
            }
        } catch (IOErrorException e) {
            log.error("insert edge error", e);
        }
    }

    public static void query(Session session) {
        try {
            String query = "GO FROM \"Bob\" OVER like "
                    + "YIELD $^.person.name, $^.person.age, like.likeness";
            ResultSet resp = session.execute(query);
            if (!resp.isSucceeded()) {
                log.error(String.format("Execute: `%s', failed: %s",
                        query, resp.getErrorMessage()));
                System.exit(1);
            }
            printResult(resp);
        } catch (Exception e) {
            log.error("query error", e);
        }
    }

    public static void matchQuery(SessionPool sessionPool){
        ResultSet resultSet;
        try {
            resultSet = sessionPool.execute("match (v:person) return v limit 10;");
            System.out.println(resultSet.toString());
        } catch (IOErrorException | ClientServerIncompatibleException | AuthFailedException
                | BindSpaceFailedException e) {
            e.printStackTrace();
            sessionPool.close();
            System.exit(1);
        }
    }
    public static void queryWithParam(Session session) {
        // prepare parameters
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("p1", 3);
        paramMap.put("p2", true);
        paramMap.put("p3", 3.3);
        Value nvalue = new Value();
        Date date = new Date();
        date.setYear((short) 2021);
        nvalue.setDVal(date);
        List<Object> list = new ArrayList<>();
        list.add(1);
        list.add(true);
        list.add(nvalue);
        list.add(date);
        paramMap.put("p4", list);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", true);
        map.put("c", nvalue);
        map.put("d", list);
        paramMap.put("p5", map);
        String query = "RETURN abs($p1+1),toBoolean($p2) and false,$p3,$p4[2],$p5.d[3]";

        try {
            ResultSet resp = session.executeWithParameter(query, paramMap);
            if (!resp.isSucceeded()) {
                log.error(String.format("Execute: `%s', failed: %s",
                        query, resp.getErrorMessage()));
                System.exit(1);
            }
            printResult(resp);
        } catch (Exception e) {
            log.error("query error", e);
        }


    }

    private static void printResult(ResultSet resultSet) throws UnsupportedEncodingException {
        List<String> colNames = resultSet.keys();
        for (String name : colNames) {
            System.out.printf("%15s |", name);
        }
        System.out.println();
        for (int i = 0; i < resultSet.rowsSize(); i++) {
            ResultSet.Record record = resultSet.rowValues(i);
            for (ValueWrapper value : record.values()) {
                if (value.isLong()) {
                    System.out.printf("%15s |", value.asLong());
                }
                if (value.isBoolean()) {
                    System.out.printf("%15s |", value.asBoolean());
                }
                if (value.isDouble()) {
                    System.out.printf("%15s |", value.asDouble());
                }
                if (value.isString()) {
                    System.out.printf("%15s |", value.asString());
                }
                if (value.isTime()) {
                    System.out.printf("%15s |", value.asTime());
                }
                if (value.isDate()) {
                    System.out.printf("%15s |", value.asDate());
                }
                if (value.isDateTime()) {
                    System.out.printf("%15s |", value.asDateTime());
                }
                if (value.isVertex()) {
                    System.out.printf("%15s |", value.asNode());
                }
                if (value.isEdge()) {
                    System.out.printf("%15s |", value.asRelationship());
                }
                if (value.isPath()) {
                    System.out.printf("%15s |", value.asPath());
                }
                if (value.isList()) {
                    System.out.printf("%15s |", value.asList());
                }
                if (value.isSet()) {
                    System.out.printf("%15s |", value.asSet());
                }
                if (value.isMap()) {
                    System.out.printf("%15s |", value.asMap());
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NebulaPool nebulaPool = initNebulaPool();
        Session session = connect(nebulaPool);
        System.out.println(session.ping());

        createSchema(session);

        TimeUnit.SECONDS.sleep(5);
        insertVertex(session);
        insertEdge(session);

        query(session);
        session.close();
        nebulaPool.close();
        System.out.println("finish.");

        SessionPool sessionPool = initSessionPool();
        matchQuery(sessionPool);
        sessionPool.close();
    }
}
