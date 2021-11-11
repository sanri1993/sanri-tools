package com.sanri.tools.modules.core.dtos;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class PluginDto {
    /**
     * 模块标识信息
     */
    private String module;
    private String name;

    /**
     * 插件详细信息
     */
    private String author;
    private String logo;
    private String title;
    private String desc;
    private String help;
    private String helpContent;

    /**
     * 环境和依赖项
     */
    private String envs;
    private String dependencies;

    @Tolerate
    public PluginDto() {
    }

    /**
     * 模块唯一主键
     * @return
     */
    public String key(){
        return module+":"+name;
    }
}
