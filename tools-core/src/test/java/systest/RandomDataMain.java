package systest;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

import org.junit.Test;

import com.sanri.tools.modules.core.service.data.RandomDataService;

import lombok.Data;

public class RandomDataMain {
    RandomDataService randomDataService = new RandomDataService();

    /**
     * 简单类型数据生成
     */
    @Test
    public void test1(){
        System.out.println(randomDataService.populateDataStart(String.class));
        System.out.println(randomDataService.populateDataStart(Integer.class));
        System.out.println(randomDataService.populateDataStart(int.class));
        System.out.println(randomDataService.populateDataStart(int[].class));
        System.out.println(randomDataService.populateDataStart(boolean.class));
        System.out.println(randomDataService.populateDataStart(Date.class));
        System.out.println(randomDataService.populateDataStart(BigDecimal.class));
    }

    /**
     * 集合类型支持
     * Bean 类型
     * 泛型
     */
    @Test
    public void test2(){
        final Example example = (Example) randomDataService.populateDataStart(Example.class);
        System.out.println(example);
    }

    /**
     * 继承
     */
    @Test
    public void test3(){
        final ExampleExtend exampleExtend = (ExampleExtend) randomDataService.populateDataStart(ExampleExtend.class);
        System.out.println(exampleExtend);
    }

    /**
     * 自定义泛型
     */
    @Test
    public void test4(){
        final CustomInput customInput = (CustomInput) randomDataService.populateDataStart(CustomInput.class);
        System.out.println(customInput);
    }



    @Test
    public void test6() throws NoSuchFieldException {
        final Class<CustomInput> customInputClass = CustomInput.class;
        final Field exampleBaseInput = customInputClass.getDeclaredField("exampleBaseInput");
        final AnnotatedParameterizedType annotatedType = (AnnotatedParameterizedType) exampleBaseInput.getAnnotatedType();
        final Type type = annotatedType.getAnnotatedActualTypeArguments()[0].getType();
        System.out.println(type);
    }

    @Test
    public void test7(){
        final A a = (A) randomDataService.populateDataStart(A.class);
        System.out.println(a);
    }

    @Data
    public static class A {
        private GenericArray<ExampleExtend> integerGenericArray;
    }

    @Data
    public static class Example{
        protected List<String> list = new ArrayList<>();
        protected List<Integer> list2 = new ArrayList<>();
        protected Set<String> set = new HashSet<>();
        protected Set<Integer> set2 = new HashSet<>();
        protected Map<String,String> map = new HashMap<>();
        protected Map<String,Integer> map2 = new HashMap<>();
    }

    @Data
    public static class ExampleExtend extends Example{
        private int num;
    }

    @Data
    public static class CustomInput {
        private BaseInput<Example> exampleBaseInput;
        private Custom<Integer> custom;
        private BaseInput<Date> baseInput;
    }

    @Data
    public static class Custom<T>{
        protected T dataInfo;
    }

    /**
     * 泛型数组
     * @param <T>
     */
    @Data
    public static class GenericArray<T>{
        private T[] datas;
    }


    @Data
    public static class BaseInput<T> {
        private T data;
        private String optUser;
    }
}
