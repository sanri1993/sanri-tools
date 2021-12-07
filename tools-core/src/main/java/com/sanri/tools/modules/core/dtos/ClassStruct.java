package com.sanri.tools.modules.core.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 简化版的类结构
 * 类部类不做处理,只处理最简单的 dto 结构
 */
@Data
public class ClassStruct {
    /**
     * 字段列表
     */
    private List<Field> fields = new ArrayList<>();
    /**
     * 方法列表
     */
    private List<Method> methods = new ArrayList<>();
    /**
     * 类名
     */
    private String name;
    /**
     * 包名
     */
    private String packageName;

    public ClassStruct() {
    }

    public ClassStruct(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    @Data
    public static abstract class Member{
        /**
         * 修饰符
         */
        protected int modifys;
        /**
         * 名称
         */
        protected String name;

        public Member(int modifys, String name) {
            this.modifys = modifys;
            this.name = name;
        }

        public Member() {
        }
    }

    @Data
    public static class Method extends Member{
        /**
         * 返回类型
         */
        private String returnType;
        /**
         * 参数列表
         */
        private List<Arg> args = new ArrayList<>();

        public Method() {
        }

        public Method(int modifys, String name, String returnType, List<Arg> args) {
            super(modifys, name);
            this.returnType = returnType;
            this.args = args;
        }

        public Method(int modifys, String name, String returnType) {
            super(modifys, name);
            this.returnType = returnType;
        }
    }

    @Data
    public static class Arg{
        /**
         * 参数类型
         */
        private String type;
        /**
         * 参数名称
         */
        private String name;

        public Arg() {
        }

        public Arg(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    @Data
    public static class Field extends Member{
        /**
         * 字段类型
         */
        private String type;

        public Field() {
        }

        public Field(int modifys, String name, String type ) {
            super(modifys,name);
            this.type = type;
        }
    }
}
