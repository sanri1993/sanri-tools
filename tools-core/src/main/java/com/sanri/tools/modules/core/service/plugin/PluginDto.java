package com.sanri.tools.modules.core.service.plugin;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class PluginDto {
    // 模块名,作者,logo ,使用环境列表逗号分隔,模块描述,帮助文档地址,依赖列表
    private String module;
    private String author;
    private String logo;
    private String envs;
    private String desc;
    private String help;
    private String dependencies;

    @Tolerate
    public PluginDto() {
    }
}
