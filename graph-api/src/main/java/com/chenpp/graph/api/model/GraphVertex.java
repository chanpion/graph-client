package com.chenpp.graph.api.model;

import java.io.Serializable;
import java.util.Map;

/**
 * @author April.Chen
 * @date 2023/10/11 3:51 下午
 **/
public class GraphVertex implements Serializable {
    private String id;
    private String label;
    private Map<String, Object> properties;
}
