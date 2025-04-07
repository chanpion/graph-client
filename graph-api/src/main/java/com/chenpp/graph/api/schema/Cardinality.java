package com.chenpp.graph.api.schema;

/**
 * 值类型定义
 *
 * @author April.Chen
 * @date 2023/10/11 4:37 下午
 **/
public enum Cardinality {

    /**
     * Only a single value may be associated with the given key.
     */
    SINGLE,

    /**
     * Multiple values and duplicate values may be associated with the given key.
     */
    LIST,

    /**
     * Multiple but distinct values may be associated with the given key.
     */
    SET,

    /**
     * key-value
     */
    MAP;
}