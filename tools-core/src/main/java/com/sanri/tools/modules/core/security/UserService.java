package com.sanri.tools.modules.core.security;

import com.sanri.tools.modules.core.security.dtos.FatUser;

import java.io.IOException;
import java.util.List;

/**
 * 这里只做一个接口, 实现由安全服务来实现, 然后注入到需要使用安全的服务里面
 */
public interface UserService {

    /**
     * 当前登录人名称
     */
    String username();

    /**
     * 用户详细信息
     */
    FatUser profile() throws IOException;

    /**
     * 当前登录人, 分组列表
     */
    List<String> groups();

    /**
     * 当前登录人角色列表
     */
    List<String> roles();

    /**
     * 查询可授权角色列表
     */
    List<String> queryAccessRoles();

    /**
     * 查询可授权资源列表
     */
    List<String> queryAccessResources();

    /**
     * 查询可授权用户列表
     */
    List<String> queryAccessUsers();

    /**
     * 查询可授权分组列表
     * @return
     */
    List<String> queryAccessGroups();
}
