package com.sanri.tools.modules.database.service.code.dtos;

import com.sanri.tools.modules.database.service.meta.dtos.Namespace;
import lombok.Data;

@Data
public class CodeFromSqlParam {
    private String connName;
    private Namespace namespace;
    private String sql;
    private ProjectGenerateConfig.PackageConfig packageConfig;
    private String bizName;
}
