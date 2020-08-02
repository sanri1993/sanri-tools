package com.sanri.tools.modules.mybatis.dtos;

import lombok.Data;

@Data
public class BoundSqlParam {
   private String project;
   private String statementId;
   private String className;
   private String classloaderName;
   private String arg;
}
