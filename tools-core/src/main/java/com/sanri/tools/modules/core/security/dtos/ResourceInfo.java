package com.sanri.tools.modules.core.security.dtos;

import com.sanri.tools.modules.core.security.entitys.ToolResource;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResourceInfo {
    /**
     * 资源信息
     */
    private ToolResource toolResource;
    /**
     * 资源所属分组列表
     */
    private List<String> groups = new ArrayList<>();

    public ResourceInfo(ToolResource toolResource) {
        this.toolResource = toolResource;
    }

    public void addGroup(String groupPath){
        groups.add(groupPath);
    }
}
