package com.sanri.tools.modules.core.security.entitys;

import lombok.Data;

@Data
public class ToolResource {
    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 资源地址
     */
    private String url;
    /**
     * 资源类型
     * Menu,SubMenu , Button
     */
    private String type;
    /**
     * 父级资源名称
     */
    private String parentResourceName;

    /**
     * 前端展示路由名, 必须由  / 开头
     */
    private String routeName;

    public ToolResource() {
    }

    public ToolResource(String resourceName, String url, String type, String parentResourceName) {
        this.resourceName = resourceName;
        this.url = url;
        this.type = type;
        this.parentResourceName = parentResourceName;
    }
}
