package com.sanri.tools.modules.database.service.code;

import com.sanri.tools.modules.database.service.JdbcMetaService;
import com.sanri.tools.modules.database.service.meta.aspect.JdbcConnection;
import com.sanri.tools.modules.database.service.meta.dtos.Namespace;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.List;

/**
 * 一句 sql 生成相应代码
 */
@Service
@Slf4j
public class CodeSqlService {

    @Autowired
    private JdbcMetaService jdbcMetaService;

    // jsqlparser 解析
    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @JdbcConnection
    public void generateCode(String connName, Namespace namespace,String sql) throws JSQLParserException {
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        final List<SelectItem> selectItems = plainSelect.getSelectItems();
        final Expression where = plainSelect.getWhere();

        // 生成输出项
        generateOutputDto(selectItems,connName,namespace);

        // 生成查询项
        generateQueryParamDto(where,connName,namespace);
    }

    private void generateQueryParamDto(Expression where, String connName, Namespace namespace) {

    }

    private void generateOutputDto(List<SelectItem> selectItems, String connName, Namespace namespace) {
        for (SelectItem selectItem : selectItems) {

        }
    }
}
