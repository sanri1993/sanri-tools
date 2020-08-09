package com.sanri.tools.modules.database.dtos;

import lombok.Data;

import java.util.List;

@Data
public class CodeGeneratorParam {
   private List<String> templates;
   private CodeGeneratorConfig.DataSourceConfig dataSourceConfig;
   private CodeGeneratorConfig.PackageConfig packageConfig;
   private String renameStrategyName;
}
