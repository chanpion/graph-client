package com.chenpp.graph.api.model;

import com.chenpp.graph.api.schema.Cardinality;
import com.chenpp.graph.api.schema.DataType;

/**
 * @author April.Chen
 * @date 2023/10/11 4:25 下午
 **/
public class GraphIndex {
    /**
     * 属性标识
     */
    private String name;

    /**
     * 数据类型
     */
    private DataType dataType;

    /**
     * 基数类型
     */
    private Cardinality cardinality;
}
