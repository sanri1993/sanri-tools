package com.sanri.tools.modules.security.controller;

import com.sanri.tools.modules.core.security.dtos.GroupTree;
import com.sanri.tools.modules.core.security.dtos.ResourceInfo;
import com.sanri.tools.modules.core.security.entitys.ToolResource;
import com.sanri.tools.modules.core.security.entitys.ToolRole;
import com.sanri.tools.modules.core.security.entitys.ToolUser;
import com.sanri.tools.modules.security.service.*;
import com.sanri.tools.modules.security.service.dtos.SecurityUser;
import org.apache.catalina.startup.Tool;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限管理, 有权限的人可以调用
 */
@RestController
@RequestMapping("/security")
@Validated
public class SecurityController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private ProfileServiceImpl profileService;

    // 授权
    @PostMapping("/user/grantRoles")
    public void grantRoles(@NotBlank String username,String [] roles){
        securityService.grantRoles(username,roles);
    }

    /**
     * 授权分级, 这里 groups 应该是相对路径
     * @param username
     * @param groups
     */
    @PostMapping("/user/grantGroups")
    public void grantGroups(@NotBlank String username,String [] groups){
        final String mainGroup = profileService.currentUser().getMainGroup();
        String [] newGroups = new String[groups.length];
        for (int i = 0; i < groups.length; i++) {
            newGroups[i] = mainGroup + groups[i];
        }
        securityService.grantGroups(username,newGroups);
    }

    // 角色资源分组管理
    @PostMapping("/role/resource/reset")
    public void roleResetResource(@NotBlank String roleName, String [] resources){
        roleService.resetResource(roleName,resources);
    }

    /**
     *
     * @param roleName 角色名称
     * @param groups 相对分组的路径
     */
    @PostMapping("/role/group/reset")
    public void roleResetGroup(@NotBlank String roleName, String [] groups){
        final String mainGroup = profileService.currentUser().getMainGroup();
        String [] newGroups = new String[groups.length];
        for (int i = 0; i < groups.length; i++) {
            newGroups[i] = mainGroup + groups[i];
        }
        roleService.resetGroup(roleName,newGroups);
    }
}
