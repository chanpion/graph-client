package com.chenpp.graph.api.schema;

import lombok.Data;

import java.util.List;

/**
 * @author April.Chen
 * @date 2024/3/28 11:37
 */
@Data
public class GraphEntity {
    /**
     * 实体标识
     */
    private String name;

    /**
     * 属性列表
     */
    private List<GraphProperty> props;

    /**
     * 是否已发布
     */
    private Boolean deployed;
}
