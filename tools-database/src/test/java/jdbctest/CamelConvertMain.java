package jdbctest;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import org.junit.Test;

public class CamelConvertMain {
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
}
