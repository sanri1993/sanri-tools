package jdbctest;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DruidMain {
    @Test
    public void test1() throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("h23");
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/test");
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");

        druidDataSource.init();

        System.out.println(druidDataSource.getDbType());
        druidDataSource.close();
    }

    @Test
    public void test2(){

        class MethodClass{}
    }
}
