package com.sanri.tools.modules.mybatis.dtos;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class BoundSqlParam {
   private String project;
   private String statementId;
   private String className;
   private String classloaderName;
   private JSONObject arg;
   private String connName;
}
