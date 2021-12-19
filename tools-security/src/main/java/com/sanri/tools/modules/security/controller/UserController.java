package com.sanri.tools.modules.security.controller;

import java.io.IOException;
import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.sanri.tools.modules.core.security.dtos.ThinUser;
import com.sanri.tools.modules.security.service.ProfileService;
import com.sanri.tools.modules.security.service.UserManagerService;

/**
 * 当前用户管理另一个用户的权限信息管理
 */
@RestController
@RequestMapping("/security/user")
@Validated
public class UserController {

    @Autowired
    private UserManagerService userManagerService;
    @Autowired
    private ProfileService profileService;

    /**
     * 获取用户信息
     * @param username 用户名
     */
    @GetMapping("/{username}")
    public ThinUser userInfo(@Validated @PathVariable("username") String username){
        return userManagerService.getUser(username);
    }

    /**
     * 重置密码
     * @param username 用户名
     */
    @PostMapping("/{username}/resetPassword")
    public void resetPassword(@Validated @PathVariable("username") String username){
        userManagerService.resetPassword(username);
    }

    /**
     * 添加用户
     * @param user
     */
    @PostMapping("/add")
    public void addUser(@RequestBody ThinUser user){
        userManagerService.addUser(user);
    }

    /**
     * 删除用户
     * @param username
     */
    @PostMapping("/del")
    public void delUser(@NotBlank String username) throws IOException {
        userManagerService.delUser(username);
    }

    /**
     * 查询用户的可授权组织列表
     * @param username 用户名
     */
    @GetMapping("/{username}/accessGroups")
    public List<String> queryAccessGroups(@NotBlank @PathVariable("username") String username){
        profileService.checkUserAccess(username);
        return userManagerService.queryAccessGroups(username);
    }

    /**
     * 查询用户的可授权用户
     * @param username 用户名
     * @return
     */
    @GetMapping("/{username}/accessUsers")
    public List<String> queryAccessUsers(@NotBlank @PathVariable("username") String username){
        profileService.checkUserAccess(username);
        return userManagerService.queryAccessUsers(username);
    }

    /**
     * 查询用户的可授权角色
     * @param username 用户名
     * @return
     */
    @GetMapping("/{username}/accessRoles")
    public List<String> queryAccessRoles(@NotBlank @PathVariable("username") String username){
        profileService.checkUserAccess(username);
        return userManagerService.queryAccessRoles(username);
    }

    /**
     * 查询用户的可授权资源
     * @param username 用户名
     * @return
     */
    @GetMapping("/{username}/accessResources")
    public List<String> queryAccessResources(@NotBlank @PathVariable("username") String username){
        profileService.checkUserAccess(username);
        return userManagerService.queryAccessResources(username);
    }

    /**
     * 用户授权角色信息
     * @param username 用户名
     * @param roles 角色名列表
     */
    @PostMapping("/{username}/grantRoles")
    public void grantUserRoles(@Validated @PathVariable("username") String username,String...roles){
        userManagerService.grantRoles(username,roles);
    }

    /**
     * 用户授权分组
     * @param username 用户名
     * @param groups 分组信息
     */
    @PostMapping("/{username}/grantGroups")
    public void grantUserGroups(@Validated @PathVariable("username") String username,String...groups){
        userManagerService.grantGroups(username,groups);
    }
}
