package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import com.sanri.tools.modules.database.dtos.meta.Column;
import com.sanri.tools.modules.database.dtos.TableDataParam;
import com.sanri.tools.modules.database.dtos.meta.TableMetaData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

// TODO 需要考虑类型映射 , 占位符冲突问题
@Service
@Slf4j
public class TableDataService {
    @Autowired
    private JdbcService jdbcService;
    ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 单表数据添加
     * @param tableDataParam
     */
    public void singleTableWriteRandomData(TableDataParam tableDataParam) {
        // 获取表元数据信息
        String connName = tableDataParam.getConnName();
        ActualTableName actualTableName = tableDataParam.getActualTableName();
        TableMetaData tableMetaData = jdbcService.findTable(connName,actualTableName);

        List<TableDataParam.ColumnMapper> columnMappers = tableDataParam.getColumnMappers();
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("tableName",actualTableName.getTableName());
        String columns = columnMappers.stream().map(TableDataParam.ColumnMapper::getColumnName).collect(Collectors.joining(","));
        paramMap.put("columns",columns);
        List<Object> values = new ArrayList<>();
        List<String> multiValues = new ArrayList<>();
        StringSubstitutor stringSubstitutor = new StringSubstitutor(paramMap, "${", "}");

        String insertSql = "insert into ${tableName}(${columns}) values ${values}";

        // column 映射成 map
        Map<String, Column> columnMap = tableMetaData.getColumns().stream().collect(Collectors.toMap(Column::getColumnName, column -> column));

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

                    Column column = columnMap.get(columnName);
                    String dataType = column.getTypeName();
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
                List<Integer> executeUpdate = jdbcService.executeUpdate(connName, Arrays.asList(finalSql));
                log.info("影响行数 {}",executeUpdate);
            } catch (SQLException | IOException e) {
                log.error("当前 sql 执行错误 {}",finalSql);
            }
        }

    }


}
