package com.sanri.tools.modules.security.controller;

import com.sanri.tools.modules.core.security.dtos.RoleInfo;
import com.sanri.tools.modules.security.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@RestController
@RequestMapping("/security/role")
@Validated
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 查询角色信息
     * @param rolename 角色名称
     */
    @GetMapping("/{rolename}")
    public RoleInfo roleInfo(@NotBlank @PathVariable("rolename") String rolename){
        return roleService.getRole(rolename);
    }

    /**
     * 添加一个角色
     * @param rolename
     */
    @PostMapping("/add")
    public void addRole(@Validated @RequestBody RoleInfo roleInfo){
        roleService.addRole(roleInfo);
    }

    /**
     * 删除一个角色
     * @param rolename
     */
    @PostMapping("/del")
    public void delRole(@NotBlank String rolename){
        roleService.delRole(rolename);
    }

    /**
     * 角色有权限访问的资源列表
     * @param rolename 角色名称
     */
    @GetMapping("/{rolename}/accessResources")
    public Set<String> queryAccessResources(@NotBlank @PathVariable("rolename") String rolename){
       return roleService.queryAccessResources(rolename);
    }

    /**
     * 角色授权组织信息
     * @param rolename 角色名称
     * @param groups 组织信息
     */
    @PostMapping("/{rolename}/grantGroups")
    public void grantRoleGroups(@NotBlank @PathVariable("rolename") String rolename,String... groups){
        roleService.grantGroups(rolename,groups);
    }

    /**
     * 角色授权资源信息
     * @param rolename 角色名称
     * @param resources 资源信息
     */
    @PostMapping("/{rolename}/grantResources")
    public void grantRoleResources(@NotBlank @PathVariable("rolename") String rolename,String... resources){
        roleService.grantResources(rolename,resources);
    }
}
