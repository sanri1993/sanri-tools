package com.sanri.tools.modules.security.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sanri.tools.modules.core.aspect.SerializerToFile;
import com.sanri.tools.modules.core.exception.SystemMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.security.service.dtos.SecurityUser;

import lombok.extern.slf4j.Slf4j;

/**
 * 权限相关信息管理
 */
@Service
@Slf4j
public class SecurityService{
    @Autowired
    private FileManager fileManager;
    @Autowired
    private UserManagerService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ProfileServiceImpl profileService;

    /**
     * 用户授权角色信息
     * @param username 用户名
     * @param roleNames 角色名列表
     */
    @SerializerToFile
    public void grantRoles(String username,String... roleNames){
        // 检查是否有权限操作这个用户
        userService.checkAccess(username);
        final List<String> roles = profileService.queryAccessRoles();
        for (String roleName : roleNames) {
            if (!roles.contains(roleName)){
                throw SystemMessage.ACCESS_DENIED_ARGS.exception("不可授权角色:"+roleName);
            }
        }

        final SecurityUser thinUser = UserManagerService.USERS.get(username);
        if (thinUser != null) {
            thinUser.getRoles().clear();
            for (String roleName : roleNames) {
                thinUser.addRole(roleName);
            }
        }
    }

    /**
     * 用户授权分组信息
     * @param username 用户名
     * @param groups 分组列表
     */
    @SerializerToFile
    public void grantGroups(String username,String... groups){
        // 检查是否有权限操作这个用户
        userService.checkAccess(username);
        final List<String> accessGroups = profileService.groups();
        A: for (String group : groups) {
            for (String accessGroup : accessGroups) {
                if (group.startsWith(accessGroup)){
                    continue A;
                }
            }
            throw SystemMessage.ACCESS_DENIED_ARGS.exception("不可授权分组:"+group);
        }

        final SecurityUser thinUser = UserManagerService.USERS.get(username);
        if (thinUser != null) {
            thinUser.getGroups().clear();
            // 路径清理, 如果有重复路径, 只取路径短的, 例: /a/b/c/d /a/b/ 时, 只会取 /a/b/
            List<String> finalGroup = new ArrayList<>();
            A:for (String group : groups) {
                final Iterator<String> iterator = finalGroup.iterator();
                while (iterator.hasNext()){
                    final String next = iterator.next();
                    if (next.startsWith(group)){
                        iterator.remove();
                        finalGroup.add(group);
                        continue A;
                    }
                    if (group.startsWith(next)){
                        continue A;
                    }
                }
                finalGroup.add(group);
            }
            for (String s : finalGroup) {
                thinUser.addGroup(s);
            }
        }
    }

    public void serializer() throws IOException {
        userService.serializer();
    }
}
