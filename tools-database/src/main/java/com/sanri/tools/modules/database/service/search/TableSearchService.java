package com.sanri.tools.modules.database.service.search;

import com.sanri.tools.modules.database.dtos.ExtendTableMetaData;
import com.sanri.tools.modules.database.dtos.meta.TableMetaData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据表搜索服务
 */
public interface TableSearchService {
    /**
     * 搜索数据表
     * @param connName
     * @param catalog
     * @param schemas
     * @param keyword
     * @return
     */
    List<TableMetaData> searchTables(String connName, String catalog, String[] schemas, String keyword) throws Exception;
}
