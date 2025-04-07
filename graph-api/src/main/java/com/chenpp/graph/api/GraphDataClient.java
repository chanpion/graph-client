package com.chenpp.graph.api;

import com.chenpp.graph.api.model.GraphData;
import com.chenpp.graph.api.model.GraphEdge;
import com.chenpp.graph.api.model.GraphVertex;

/**
 * @author April.Chen
 * @date 2025/2/7 15:56
 */
public interface GraphDataClient {

    /**
     * 查询
     *
     * @param query 查询语句
     * @return 查询结果
     */
    GraphData query(String query);


    /**
     * 添加顶点
     *
     * @param vertex 顶点
     * @return
     */
    boolean addVertex(GraphVertex vertex);

    /**
     * 添加边
     *
     * @param edge 边
     */
    void addEdge(GraphEdge edge);
}
