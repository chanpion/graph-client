package com.chenpp.graph.api.exception;

/**
 * @author April.Chen
 * @date 2024/5/13 17:04
 */
public class GraphException extends RuntimeException{

    public GraphException() {
    }

    public GraphException(String message) {
        super(message);
    }

    public GraphException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphException(Throwable cause) {
        super(cause);
    }
}
