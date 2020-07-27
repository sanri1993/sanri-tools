package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.database.dtos.TableDataParam;
import com.sanri.tools.modules.protocol.db.Column;
import com.sanri.tools.modules.protocol.db.Schema;
import com.sanri.tools.modules.protocol.db.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO 需要考虑类型映射 , 占位符冲突问题
@Service
@Slf4j
public class TableDataService {
    @Autowired
    private JdbcConnectionService jdbcConnectionService;
    ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 单表数据添加
     * @param tableDataParam
     */
    public void singleTableWriteRandomData(TableDataParam tableDataParam) {
        // 获取表元数据信息
        String connName = tableDataParam.getConnName();
        String schemaName = tableDataParam.getSchemaName();
        String tableName = tableDataParam.getTableName();
        ExConnection connection = jdbcConnectionService.getConnection(connName);
        try {
            Map<String, Table> tables = connection.getSchema(schemaName).getTables();
            if(tables.isEmpty()) {
                // 刷新数据表
                connection.tables(schemaName, true);

                // 刷新数据表的列
                connection.columns(schemaName,tableName,true);
            }
        } catch (SQLException e) {
            log.error("刷新数据表失败 {}",e.getMessage(),e);
            return ;
        }
        Table table = connection.getSchema(schemaName).getTable(tableName);

        List<TableDataParam.ColumnMapper> columnMappers = tableDataParam.getColumnMappers();
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("tableName",tableName);
        String columns = columnMappers.stream().map(TableDataParam.ColumnMapper::getColumnName).collect(Collectors.joining(","));
        paramMap.put("columns",columns);
        List<Object> values = new ArrayList<>();
        List<String> multiValues = new ArrayList<>();
        StringSubstitutor stringSubstitutor = new StringSubstitutor(paramMap, "${", "}");

        String insertSql = "insert into ${tableName}(${columns}) values ${values}";

        // 将数据分段插入
        int SEGMENT_SIZE = 100;
        int segments = (tableDataParam.getSize() - 1) / SEGMENT_SIZE + 1;
        for (int k = 0; k < segments; k++) {
            int start = k * SEGMENT_SIZE;
            int end = (k + 1) * SEGMENT_SIZE;
            if(end > tableDataParam.getSize()){
                end = tableDataParam.getSize();
            }

            multiValues.clear();
            for (int i = start; i < end; i++) {
                values.clear();
                for (TableDataParam.ColumnMapper columnMapper : columnMappers) {
                    String random = columnMapper.getRandom();
                    String columnName = columnMapper.getColumnName();

                    // 使用 spel 生成数据
                    Expression expression = expressionParser.parseExpression(random);
                    String value = expression.getValue(String.class);

                    Column column = table.getColumn(columnName);
                    String dataType = column.getColumnType().getDataType();
                    // 需要判断数据库字段类型,数字型和字符型的添加不一样的
                    values.add("'"+value+"'");
                }
                String columnValues = StringUtils.join(values, ',');
                multiValues.add('('+columnValues+')');
            }
            String columnValues = StringUtils.join(multiValues, ',');
            paramMap.put("values",columnValues);
            String finalSql = stringSubstitutor.replace(insertSql);
            log.info("将要执行的语句为 {}",finalSql);
            try {
                connection.execute(schemaName,finalSql);
            } catch (SQLException e) {
                log.error("当前 sql 执行错误 {}",finalSql);
            }
        }

    }


}
