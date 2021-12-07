package com.sanri.tools.modules.security.service;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.security.UserService;
import com.sanri.tools.modules.core.security.dtos.FatUser;
import com.sanri.tools.modules.core.security.dtos.GroupTree;
import com.sanri.tools.modules.core.security.dtos.ResourceInfo;
import com.sanri.tools.modules.core.security.dtos.RoleInfo;
import com.sanri.tools.modules.core.security.entitys.ToolGroup;
import com.sanri.tools.modules.core.security.entitys.ToolRole;
import com.sanri.tools.modules.core.security.entitys.ToolUser;
import com.sanri.tools.modules.core.security.entitys.UserProfile;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.security.service.dtos.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户个人信息
 */
@Service
@Slf4j
public class ProfileServiceImpl implements UserService {

    @Autowired
    private FileManager fileManager;
    @Autowired
    private GroupService groupService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ResourcePermLoad resourcePermLoad;
    @Autowired
    private UserManagerService userManagerService;

    /**
     * @return 当前用户
     */
    public SecurityUser currentUser(){
        final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (SecurityUser) principal;
    }

    public FatUser profile() throws IOException {
        final SecurityUser thinUser = currentUser();
        final FatUser fatUser = new FatUser(thinUser);

        final String username = thinUser.getUsername();
        final File usersDir = fileManager.mkConfigDir("security/users");
        final File file = new File(usersDir, username + "/profile");
        if (file.exists()){
            final String profile = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            final UserProfile userProfile = JSON.parseObject(profile, UserProfile.class);
            fatUser.setProfile(userProfile);
        }

        return fatUser;
    }

    @Override
    public String username() {
        final SecurityUser thinUser = currentUser();
        if (thinUser != null) {
            return thinUser.getUsername();
        }
        return null;
    }

    @Override
    public List<String> groups() {
        final SecurityUser thinUser = currentUser();
        if (thinUser != null){
            return thinUser.getGroups();
        }
        return null;
    }

    @Override
    public List<String> roles() {
        final SecurityUser thinUser = currentUser();
        if (thinUser != null){
            return thinUser.getRoles();
        }
        return null;
    }

    /**
     * 当前用户拥有的角色所在的组织及其下面组织的角色信息
     * @return
     */
    public List<String> queryAccessRoles(){
        // 当前分配的角色
        final List<String> roles = roles();

        Set<String> allRoles = new HashSet<>(roles);

        // 角色所在组织及其以下的组织中的角色
        Set<String> groupsInRoles = new HashSet<>();
        for (String role : roles) {
            final RoleInfo roleInfo = roleService.getRole(role);
            if (roleInfo == null){
                log.info("角色信息不存在[{}]",role);
                continue;
            }
            final List<String> groups = roleInfo.getGroups();
            groupsInRoles.addAll(groups);
        }

        final Set<String> topGroups = groupService.filterTopGroups(groupsInRoles);
        for (String topGroup : topGroups) {
            final Set<String> canGrantRoles = roleService.findCanGrantRoles(topGroup);
            allRoles.addAll(canGrantRoles);
        }

        return new ArrayList<>(allRoles);
    }

    /**
     * 当前用户可授权的资源
     *
     * 当前用户拥有的角色
     * 角色拥有的资源列表
     * 查到资源的分组
     * 找到顶层分组列表
     * 查询可授权资源列表
     *
     * @return
     */
    public List<String> queryAccessResources(){
        final List<String> roles = roles();

        Set<String> resources = new HashSet<>();
        for (String roleName : roles) {
            final RoleInfo roleInfo = roleService.getRole(roleName);
            if (roleInfo == null){
                log.warn("当前角色信息不存在或已被删除:{}",roleName);
                continue;
            }
            resources.addAll(roleInfo.getResources());
        }

        // 找到资源的顶层组织列表
        final List<ResourceInfo> resourceInfos = resourcePermLoad.loadResourcesFromNames(resources);
        Set<String> groups = new HashSet<>();
        for (ResourceInfo resourceInfo : resourceInfos) {
            groups.addAll(resourceInfo.getGroups());
        }
        final Set<String> topGroups = groupService.filterTopGroups(groups);

        // 查询可授权资源列表
        Set<String> canGrantResources = new HashSet<>();
        for (String topGroup : topGroups) {
            final Set<String> canGrantResourcesPart = resourcePermLoad.findCanGrantResources(topGroup);
            canGrantResources.addAll(canGrantResourcesPart);
        }
        return new ArrayList<>(canGrantResources);
    }

    /**
     * 当前用户可以查询到的用户列表
     * @return
     */
    public List<String> queryAccessUsers() {
        final List<String> groups = currentUser().getGroups();
        Set<String> usernames = new HashSet<>();
        for (String group : groups) {
            final Set<String> canGrantUsers = userManagerService.findCanGrantUsers(group);
            usernames.addAll(canGrantUsers);
        }
        return new ArrayList<>(usernames);
    }

    @Override
    public List<String> queryAccessGroups() {
        return groups();
    }

    /**
     * 添加用户
     * @param toolUser 用户信息
     */
    public void addUser(ToolUser toolUser) {
        final List<String> groups = groups();
        final String group0 = groups.get(0);
        String mainGroup = group0 + toolUser.getUsername()+"/";
        groupService.addGroup(mainGroup);
        // 初始密码 0
        toolUser.setPassword("0");
        userManagerService.addUser(toolUser.getUsername(),toolUser.getPassword(),mainGroup);
    }

    /**
     * 添加角色
     * @param rolename 角色名称
     */
    public void addRole(String rolename) {
        final List<String> groups = groups();
        final String group0 = groups.get(0);
        String mainGroup = group0 + rolename + "/";
        groupService.addGroup(mainGroup);
        roleService.addRole(rolename,mainGroup);
    }
}
