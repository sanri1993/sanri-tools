package com.sanri.tools.modules.core.security.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sanri.tools.modules.core.security.entitys.ToolGroup;
import com.sanri.tools.modules.core.security.entitys.ToolRole;
import com.sanri.tools.modules.core.security.entitys.ToolUser;

import lombok.Data;

@Data
public class ThinUser {
    protected ToolUser toolUser;
    protected List<String> roles = new ArrayList<>();
    protected List<String> groups = new ArrayList<>();

    public ThinUser() {
    }

    public ThinUser(ToolUser toolUser) {
        this.toolUser = toolUser;
    }

    public void addRole(String roleName){
        roles.add(roleName);
    }

    public void addGroup(String groupPath){
        groups.add(groupPath);
    }

    @JsonIgnore
    public ToolUser getToolUser() {
        return toolUser;
    }

    @JsonProperty
    public void setToolUser(ToolUser toolUser) {
        this.toolUser = toolUser;
    }
}
