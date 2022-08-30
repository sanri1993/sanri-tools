package com.sanri.tools.modules.classloader.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Objects;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.security.krb5.internal.NetClient;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 方法信息
 */
@Getter
@Setter
@Slf4j
public class ClassMethodInfo implements Comparable<ClassMethodInfo> {
    private String methodName;
    private List<Arg> args = new ArrayList<>();
    private NestClass returnType;

    public ClassMethodInfo() {
    }

    public ClassMethodInfo(String methodName, List<Arg> args, NestClass returnType) {
        this.methodName = methodName;
        this.args = args;
        this.returnType = returnType;
    }

    @Data
    public static final class Arg{
        private String name;
        private NestClass type;

        public Arg() {
        }

        public Arg(String name) {
            this.name = name;
        }
    }

    @Setter
    @Getter
    public static final class NestClass{
        @JsonIgnore
        private Class<?> mainClass;
        private boolean array;
        private List<NestClass> genericClasses = new ArrayList<>();

        public NestClass() {
        }

        public NestClass(Class<?> mainClass) {
            this.mainClass = mainClass;
        }

        public NestClass(Class<?> mainClass, List<NestClass> genericClasses) {
            this.mainClass = mainClass;
            this.genericClasses = genericClasses;
        }

        /**
         * 将 type 类型转 NestClass
         * @param type
         * @return
         */
        public static NestClass convertTypeToNestClass(Type type) {
            if (type instanceof Class){
                return new NestClass((Class)type);
            }

            if (type instanceof ParameterizedTypeImpl){
                ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) type;
                final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                List<NestClass> genericClasses = new ArrayList<>();
                if (ArrayUtils.isNotEmpty(actualTypeArguments)){
                    for (Type actualTypeArgument : actualTypeArguments) {
                        genericClasses.add(convertTypeToNestClass(actualTypeArgument));
                    }
                }
                final Class<?> mainClass = parameterizedType.getRawType();
                return new NestClass(mainClass,genericClasses);
            }else if (type instanceof TypeVariable){
                TypeVariable typeVariable = (TypeVariable) type;

                log.error("typeVariable 暂不知道怎么处理");
            }else if (type instanceof GenericArrayType){
                GenericArrayType genericArrayType = (GenericArrayType) type;

                final Type genericComponentType = genericArrayType.getGenericComponentType();
                final NestClass nestClass = convertTypeToNestClass(genericArrayType);
                nestClass.setArray(true);
                return nestClass;
            }else if (type instanceof WildcardType){
                WildcardType wildcardType = (WildcardType) type;
                final Type[] lowerBounds = wildcardType.getLowerBounds();
                final Type[] upperBounds = wildcardType.getUpperBounds();
                if (ArrayUtils.isNotEmpty(lowerBounds)){
                    return convertTypeToNestClass(lowerBounds[0]);
                }
                return convertTypeToNestClass(upperBounds[0]);
            }
            return null;
        }

        public JavaType toClass() {
            if (CollectionUtils.isNotEmpty(genericClasses)) {
                List<JavaType> collect = genericClasses.stream().map(NestClass::toClass).collect(Collectors.toList());
                return TypeFactory.defaultInstance().constructParametricType(mainClass, collect.toArray(new JavaType[]{}));
            }
            return TypeFactory.defaultInstance().constructType(mainClass);
        }

        public String getSimpleName(){
            return mainClass.getSimpleName();
        }
        public String getClassName(){
            return mainClass.getName();
        }

        @Override
        public String toString() {
            return mainClass.getSimpleName();
        }

        /**
         * 获取完整限定名
         * @return
         */
        public String getQualifiedName(){
            return toQualifiedName(this);
        }

        public static final String toQualifiedName(NestClass nestClass){
            if (nestClass.array){
                if (CollectionUtils.isEmpty(nestClass.genericClasses)) {
                    return "[" + className(nestClass.mainClass) +"]";
                }
                StringBuffer buffer = new StringBuffer();
                buffer.append("[")
                        .append(className(nestClass.mainClass))
                        .append("<");
                for (int i = 0; i < nestClass.genericClasses.size(); i++) {
                    final NestClass genericClass = nestClass.genericClasses.get(i);
                    buffer.append(toQualifiedName(genericClass));
                    if (i != nestClass.genericClasses.size() - 1){
                        buffer.append(",");
                    }
                }
                buffer.append(">")
                        .append("]");
                return buffer.toString();
            }

            // 非数组类型时, 不用添加 ]
            if (CollectionUtils.isEmpty(nestClass.genericClasses)) {
                return className(nestClass.mainClass);
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append(className(nestClass.mainClass))
                    .append("<");
            for (int i = 0; i < nestClass.genericClasses.size(); i++) {
                final NestClass genericClass = nestClass.genericClasses.get(i);
                buffer.append(toQualifiedName(genericClass));
                if (i != nestClass.genericClasses.size() - 1){
                    buffer.append(",");
                }
            }
            buffer.append(">");
            return buffer.toString();
        }

        public static final String className(Class clazz){
            if (ClassUtils.isPrimitiveOrWrapper(clazz)){
                return clazz.getSimpleName();
            }
            if (clazz.isArray()){
                final Class componentType = clazz.getComponentType();
                return className(componentType)+"[]";
            }
            if (clazz.getPackage().getName().startsWith("java.")){
                return clazz.getSimpleName();
            }
            return clazz.getName();
        }
    }

    /**
     * 方法签名
     * @return
     */
    public String getSinature(){
        StringBuffer buffer = new StringBuffer();
//        buffer.append(returnType.toString());
//        buffer.append(" ");
        buffer.append(methodName);
        buffer.append("(");
        for (int i = 0; i < args.size(); i++) {
            Arg arg = args.get(i);
            buffer.append(arg.type.toString()).append(" ").append(arg.name);
            if (i != args.size() - 1){
                buffer.append(", ");
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    @Override
    public int compareTo(ClassMethodInfo o) {
        if (o == null){
            return 1;
        }
        return o.getSinature().compareTo(this.getSinature());
    }

    @Override
    public String toString() {
        return returnType.toString() + " " +getSinature();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassMethodInfo that = (ClassMethodInfo) o;
        return that.getSinature().equals(this.getSinature());
    }

    @Override
    public int hashCode() {
        return getSinature().hashCode();
    }
}
