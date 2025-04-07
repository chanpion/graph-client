package com.chenpp.graph.api;

import com.chenpp.graph.api.exception.GraphException;
import com.chenpp.graph.api.model.GraphConf;
import com.chenpp.graph.api.schema.GraphSchema;

/**
 * @author April.Chen
 * @date 2024/5/13 17:07
 */
public interface GraphClient {

    /**
     * 创建图
     *
     * @param graphConf 图配置信息
     */
    void createGraph(GraphConf graphConf);

    /**
     * 删除图
     *
     * @param graphConf 图配置信息
     * @throws GraphException
     */
    void dropGraph(GraphConf graphConf) throws GraphException;


    /**
     *
     * @param graphConf
     * @param graphSchema
     * @throws GraphException
     */
    void deploySchema(GraphConf graphConf, GraphSchema graphSchema) throws GraphException;
}
