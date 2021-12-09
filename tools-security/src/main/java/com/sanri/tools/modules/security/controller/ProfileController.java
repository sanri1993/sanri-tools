package com.sanri.tools.modules.security.controller;

import com.sanri.tools.modules.core.dtos.TreeResponseDto;
import com.sanri.tools.modules.core.security.dtos.FatUser;
import com.sanri.tools.modules.core.security.dtos.GroupTree;
import com.sanri.tools.modules.core.security.dtos.ResourceInfo;
import com.sanri.tools.modules.core.security.entitys.ToolUser;
import com.sanri.tools.modules.security.service.*;
import com.sanri.tools.modules.security.service.dtos.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;

/**
 * 当前用户信息,个人用户可以调用
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private ProfileServiceImpl profileService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ResourcePermLoad resourcePermLoad;
    @Autowired
    private UserManagerService userService;
    @Autowired
    private RoleService roleService;

    @GetMapping("/username")
    public String username(){
        return profileService.currentUser().getUsername();
    }

    @GetMapping("/user")
    public SecurityUser user(){
        return profileService.currentUser();
    }

    @GetMapping
    public FatUser profile() throws IOException {
        return profileService.profile();
    }

    /**
     * 查询当前用户可以看到哪些分组信息
     * @return
     */
    @GetMapping("/query/groups")
    public List<GroupTree> queryGroups(){
        final List<String> queryGroups = profileService.groups();
        return groupService.convertPathsToGroupTree(queryGroups);
    }

    /**
     * 查询当前用户可以授权哪些用户
     * @return
     */
    @GetMapping("/query/users")
    public List<String> queryUsers(){
        return profileService.queryAccessUsers();
    }

    /**
     * 可授权角色信息查询
     * @return
     */
    @GetMapping("/query/roles")
    public List<String> queryRoles(){
        return profileService.queryAccessRoles();
    }

    /**
     * 可授权资源信息查询
     * @return
     */
    @GetMapping("/query/resources")
    public List<ResourceInfo> queryResources(){
        final List<String> resources = profileService.queryAccessResources();
        return resourcePermLoad.loadResourcesFromNames(resources);
    }

    /**
     * 加载出所有有权限访问的菜单列表
     * @return
     */
    @GetMapping("/query/menus")
    public List<String> queryMenuNames(){
        final List<String> resources = profileService.queryAccessResources();
        return resourcePermLoad.loadMenusFromNames(resources);
    }

    // 安全信息添加
    @PostMapping("/user/add")
    public void addUser(@NotBlank String username){
        profileService.addUser(new ToolUser(username,""));
    }

    @PostMapping("/role/add")
    public void addRole(@Validated String roleName){
        profileService.addRole(roleName);
    }

    /**
     * 添加分组
     * @param parentGroup 父级组织
     * @param relativePath 相对路径
     */
    @PostMapping("/group/add")
    public void addGroup(@NotBlank String relativePath){
        final SecurityUser securityUser = profileService.currentUser();
        final String mainGroup = securityUser.getMainGroup();
        groupService.addGroup(mainGroup + relativePath);
    }

    // 安全信息删除
    @PostMapping("/user/delete")
    public void deleteUser(@NotBlank String username) throws IOException {userService.deleteUser(username);}

    @PostMapping("/role/delete")
    public void deleteRole(@NotBlank String role){
        roleService.deleteRole(role);
    }

    @PostMapping("/group/delete")
    public void deleteGroup(@NotBlank String relativePath){
        final String mainGroup = profileService.currentUser().getMainGroup();
        groupService.deleteGroup(mainGroup + relativePath);
    }
}
