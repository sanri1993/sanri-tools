package com.sanri.tools.modules.mybatis.dtos;

import com.sanri.tools.modules.database.dtos.DynamicQueryDto;
import lombok.Data;
import org.apache.ibatis.mapping.SqlCommandType;

@Data
public class BoundSqlResponse {
    private SqlCommandType sqlCommandType;
    private DynamicQueryDto dynamicQueryDto;
    private String sql;

    public BoundSqlResponse() {
    }

    public BoundSqlResponse(SqlCommandType sqlCommandType, DynamicQueryDto dynamicQueryDto, String sql) {
        this.sqlCommandType = sqlCommandType;
        this.dynamicQueryDto = dynamicQueryDto;
        this.sql = sql;
    }
}
