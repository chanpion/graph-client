package com.chenpp.graph.api.schema;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author April.Chen
 * @date 2023/10/11 4:29 下午
 **/
@Data
public class GraphLabel {
    private String name;
    private String label;
    private List<GraphProperty> properties;
}
