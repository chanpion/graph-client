package com.chenpp.graph.api.schema;

import lombok.Data;

import java.util.List;

/**
 * @author April.Chen
 * @date 2024/5/14 17:34
 */
@Data
public class GraphSchema {
    private String graphCode;
    private List<GraphEntity> entities;
    private List<GraphRelation> relations;
    private List<GraphIndex> indexes;
}
