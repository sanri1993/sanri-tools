package com.sanri.tools.modules.core.security.entitys;

import lombok.Data;

@Data
public class ToolRole {
    private String rolename;

    public ToolRole() {
    }

    public ToolRole(String rolename) {
        this.rolename = rolename;
    }
}
