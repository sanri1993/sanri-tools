package sqlparser;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sanri.tools.modules.database.service.dtos.data.ExtendTableRelation;
import com.sanri.tools.modules.database.service.dtos.meta.TableRelation;
import com.sanri.tools.modules.database.service.sqlparser.FindTable;
import com.sanri.tools.modules.database.service.sqlparser.TablesFinder;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.text.StringSubstitutor;
import org.junit.Test;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.sanri.tools.modules.database.service.meta.dtos.ActualTableName;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.SelectUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;

import static com.sanri.tools.modules.database.service.TableDataService.*;

public class JSqlParserMain {
    // jsqlparser 解析
    CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void tes22t() throws JSQLParserException {
//        String sql = "select * from mct.mct_event_handler";
        String sql = "select count(*) from aaa";
        Select select = (Select) parserManager.parse(new StringReader(sql));
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        System.out.println(tableList);
    }

    @Test
    public void testTableNamesFinder() throws JSQLParserException {
        String sql = "select t1.*,t2.roleId from t_sys_user t1 inner join t_sys_user_role t2 on t1.userId = t2.userId where username = '张三' and t2.rolename like '%管理员%' limit 10";
        Select select = (Select) parserManager.parse(new StringReader(sql));
        TablesFinder tablesNamesFinder = new TablesFinder();
        List<FindTable> tableList = tablesNamesFinder.getTableList(select);
        System.out.println(tableList);
    }

    @Test
    public void test33(){
        final BigDecimal bigDecimal = new BigDecimal("480.00");
        final String s = bigDecimal.toString();
        System.out.println(s);
    }

    @Test
    public void testBuildSelect(){
        String catalog = "xxdb";
        ActualTableName main = new ActualTableName(catalog,"mct","mct_event_common");
        ActualTableName sub = new ActualTableName(catalog,"mct","mct_event_handler");

        Table mainTable = new Table(main.getSchema(),main.getTableName());
        mainTable.setAlias(new Alias("a"));
        Table subTable = new Table(sub.getSchema(),sub.getTableName());
        subTable.setAlias(new Alias("b"));

        Select select = new Select();
        PlainSelect plainSelect = new PlainSelect();
        SelectItem selectItem = new AllTableColumns(mainTable);
        plainSelect.setSelectItems(Collections.singletonList(selectItem));
        plainSelect.setFromItem(mainTable);
        System.out.println(plainSelect.toString());
    }

    @Test
    public void testBuilsssdSelect(){
        String catalog = "xxdb";
        ActualTableName main = new ActualTableName(catalog,"mct","mct_event_common");
        ActualTableName sub = new ActualTableName(catalog,"mct","mct_event_handler");

        Table mainTable = new Table(main.getSchema(),main.getTableName());
        mainTable.setAlias(new Alias("a"));
        Table subTable = new Table(sub.getSchema(),sub.getTableName());
        subTable.setAlias(new Alias("b"));

        Select select = SelectUtils.buildSelectFromTableAndExpressions(mainTable, new Column("b.*"));
        System.out.println(select);

        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column("a.uuid"));
        equalsTo.setRightExpression(new Column("b.event_record_id"));
        SelectUtils.addJoin(select,subTable,equalsTo);

        System.out.println(select);
    }

    @Test
    public void test(){
        String orderColumn = "orderColumn";
        //输入是LOWER_CAMEL，输出是LOWER_UNDERSCORE
        orderColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, orderColumn);
        System.out.println(orderColumn);//order_column

        orderColumn = "orderColumn";
        //输入是LOWER_CAMEL，输出是UPPER_CAMEL
        orderColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL,orderColumn);
        System.out.println(orderColumn);//OrderColumn

        orderColumn = "order_column";
        //输入是LOWER_UNDERSCORE，输出是LOWER_CAMEL
        orderColumn = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,orderColumn);
        System.out.println(orderColumn);//orderColumn

        Converter<String, String> stringStringConverter = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL);
        String order_column = stringStringConverter.convert("order_column");
        System.out.println(order_column);

        String order_column1 = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL).convert("order_column");
        System.out.println(order_column1);
    }

    @Test
    public void testBeanTomap(){
        final TableRelation child = new TableRelation("eims_image","eims_invoices","id","imageId","ONE_ONE");
        final ExtendTableRelation extendTableRelation = new ExtendTableRelation(child,"id","id");

        StringSubstitutor stringSubstitutor = new StringSubstitutor(beanToMap(extendTableRelation));
        final String replace = stringSubstitutor.replace(sqlLeftJoinTemplate);
        System.out.println(replace);

        System.out.println(stringSubstitutor.replace(sqlRightJoinTemplate));
//        StringSubstitutor stringSubstitutor2 = new StringSubstitutor(beanToMap(extendTableRelation.reverse()));
//        final String replace2 = stringSubstitutor2.replace(oneToOneSqlLeftJoinTemplate);
//        System.out.println(replace2);
    }

    @Test
    public void testBuildSelect2(){
        final Table table1 = new Table("eims_image");

        final Table table2 = new Table("eims_invoices");

        final Column column = new Column(table2,"id");
        SelectExpressionItem selectItem = new SelectExpressionItem();
        selectItem.setExpression(column);

        EqualsTo on = new EqualsTo();
        on.setLeftExpression(new Column(table1,"id"));
        on.setRightExpression(new Column(table2,"imageId"));

        IsNullExpression isNullExpression = new IsNullExpression();
        isNullExpression.setLeftExpression(new Column(table1,"id"));

        final Select select = SelectUtils.buildSelectFromTableAndSelectItems(table2, selectItem);
        final Join join = SelectUtils.addJoin(select, table1, on);
        join.setLeft(true);
        final PlainSelect selectBody = (PlainSelect) select.getSelectBody();
        selectBody.setWhere(isNullExpression);

        System.out.println(select.toString());
    }

    @Test
    public void testRelation() throws JSQLParserException {
//        String sql = "SELECT * FROM EIMS_IMAGE EI LEFT JOIN EIMS_INVOICES EI2 ON ei.ID  = ei2.IMAGEID";
//        String sql = "select * from eims_scantask inner join eims_image on document_code = billnumber";
        String sql = "SELECT * FROM EIMS_IMAGE EI \n" +
                "LEFT JOIN EIMS_INVOICES EI2 ON ei.ID  = ei2.IMAGEID \n" +
                "LEFT JOIN EIMS_INVOICES_DETAIL EID ON eid.INVOICE_NUM  = ei2.INVOICENUMBER \n" +
                "inner JOIN EIMS_SCANTASK ES ON es.BILLNUMBER  = ei.DOCUMENT_CODE ";
        Map<String,String> tableAlaisMap = new HashMap<>();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        final PlainSelect selectBody = (PlainSelect) select.getSelectBody();
        final Table fromItem = (Table) selectBody.getFromItem();
        if (fromItem.getAlias() != null) {
            tableAlaisMap.put(fromItem.getAlias().getName(), fromItem.getName());
        }
        tableAlaisMap.put(fromItem.getName(), fromItem.getName());
        final List<Join> joins = selectBody.getJoins();
        for (Join join : joins) {
            final Table rightItem = (Table) join.getRightItem();
            if (rightItem.getAlias() != null) {
                tableAlaisMap.put(rightItem.getAlias().getName(), rightItem.getName());
            }
            tableAlaisMap.put(rightItem.getName(),rightItem.getName());
            final EqualsTo equalsTo = (EqualsTo) join.getOnExpression();
            final Column leftExpression = (Column) equalsTo.getLeftExpression();
            final Column rightExpression = (Column) equalsTo.getRightExpression();

            String sourceTableName = tableAlaisMap.get(leftExpression.getTable().getName().toUpperCase());
            String targetTableName = tableAlaisMap.get(rightExpression.getTable().getName().toUpperCase());
            String sourceColumnName = leftExpression.getColumnName();
            String targetColumnName = rightExpression.getColumnName();

            System.out.println(sourceTableName+"."+sourceColumnName + " => "+targetTableName+"."+targetColumnName);
        }
    }

    @Test
    public void testCaseMap(){
        Map<String,String> aa = new HashMap<>();
        aa.put("aa","b");
        aa = new CaseInsensitiveMap<>(aa);
        final String aa1 = aa.get("Aa");
        System.out.println(aa1);
        final String aa2 = aa.remove("AA");
        System.out.println(aa2);
    }
}
