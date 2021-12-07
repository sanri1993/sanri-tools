package com.sanri.tools.modules.security.controller;

import com.sanri.tools.modules.core.security.dtos.GroupTree;
import com.sanri.tools.modules.core.security.dtos.ResourceInfo;
import com.sanri.tools.modules.core.security.entitys.ToolResource;
import com.sanri.tools.modules.core.security.entitys.ToolRole;
import com.sanri.tools.modules.core.security.entitys.ToolUser;
import com.sanri.tools.modules.security.service.GroupService;
import com.sanri.tools.modules.security.service.ResourcePermLoad;
import com.sanri.tools.modules.security.service.RoleService;
import com.sanri.tools.modules.security.service.UserManagerService;
import com.sanri.tools.modules.security.service.dtos.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 这个类里的方法, 只允许管理员调用
 */
@RestController
@RequestMapping("/security/admin")
public class AdminController {
    @Autowired
    private UserManagerService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private GroupService groupService;

    /**
     * 所有用户信息
     * @return
     */
    @GetMapping("/users")
    public List<ToolUser> users(){
        return userService.users().stream().map(SecurityUser::getToolUser).collect(Collectors.toList());
    }

    /**
     * 所有角色信息
     * @return
     */
    @GetMapping("/roles")
    public Set<String> roles(){return roleService.roleList();}

    /**
     * 所有分组信息
     * @return
     */
    @GetMapping("/group/tree")
    public List<GroupTree> groupTrees(){return groupService.convertPathsToGroupTree(groupService.getGroups());}

    /**
     * 所有资源信息
     * @return
     */
    @GetMapping("/resources")
    public List<ToolResource> toolsResources(){
        return ResourcePermLoad.getResourceInfos().stream().map(ResourceInfo::getToolResource).collect(Collectors.toList());
    }

}
