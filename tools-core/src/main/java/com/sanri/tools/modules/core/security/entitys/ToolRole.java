package com.sanri.tools.modules.core.security.entitys;

import lombok.Data;

@Data
public class ToolRole {
    private String roleName;

    public ToolRole() {
    }

    public ToolRole(String roleName) {
        this.roleName = roleName;
    }
}
