package com.sanri.tools.modules.database.service.code.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CodeGeneratorParam {
   private List<String> templates = new ArrayList<>();
   private ProjectGenerateConfig.DataSourceConfig dataSourceConfig;
   private ProjectGenerateConfig.PackageConfig packageConfig;
   private String renameStrategyName;
   // 单一文件 , 这时只会使用 dataSourceConfig
   private boolean single;
}
