package com.chenpp.graph.api.exception;

/**
 * @author April.Chen
 * @date 2024/5/13 19:36
 */
public class GraphDatabaseException extends RuntimeException {

    public GraphDatabaseException(String message) {
        super(message);
    }

    public GraphDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphDatabaseException(Throwable cause) {
        super(cause);
    }
}
