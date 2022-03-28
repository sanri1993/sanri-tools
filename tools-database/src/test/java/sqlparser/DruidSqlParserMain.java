package sqlparser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class DruidSqlParserMain {

    @Test
    public void test1(){
        MySqlStatementParser mySqlStatementParser = new MySqlStatementParser("select * from t_sys_user t1 inner join t_sys_user_role t2 on t1.userId = t2.userId where username = 'abc' limit 10");
        SQLSelectStatement sqlStatement = (SQLSelectStatement) mySqlStatementParser.parseSelect();
        final SQLSelect select = sqlStatement.getSelect();
        final SQLSelectQuery query = select.getQuery();
        System.out.println(query);
    }

}
