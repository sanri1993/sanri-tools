package com.sanri.tools.modules.security.service;

import com.google.common.collect.ArrayListMultimap;
import com.sanri.tools.modules.core.aspect.SerializerToFile;
import com.sanri.tools.modules.core.exception.SystemMessage;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.security.dtos.RoleInfo;
import com.sanri.tools.modules.core.security.entitys.ToolGroup;
import com.sanri.tools.modules.core.security.entitys.ToolResource;
import com.sanri.tools.modules.core.security.entitys.ToolRole;
import com.sanri.tools.modules.core.service.file.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleService implements InitializingBean {
    @Autowired
    private FileManager fileManager;
    @Autowired
    private ProfileServiceImpl profileService;

    private static final Map<String,RoleInfo> roleInfoMap = new HashMap<>();


    @SerializerToFile
    public void addRole(String rolename,String mainGroup){
        if (roleInfoMap.containsKey(rolename)){
            throw new ToolException("已经存在角色:"+rolename);
        }
        final RoleInfo roleInfo = new RoleInfo(new ToolRole(rolename));
        roleInfo.addGroup(mainGroup);
        roleInfoMap.put(rolename, roleInfo);
    }

    @SerializerToFile
    public void deleteRole(String role){
        checkAccess(role);
        roleInfoMap.remove(role);
    }

    public void checkAccess(String role) {
        // 检查当前用户是否有权限删除这个角色信息
        final List<String> roles = profileService.queryAccessRoles();
        if (!role.contains(role)){
            throw SystemMessage.ACCESS_DENIED.exception();
        }
    }

    public RoleInfo getRole(String role){
        return roleInfoMap.get(role);
    }

    @SerializerToFile
    public void resetGroup(String role, String... groups){
        checkAccess(role);
        final List<String> accessGroups = profileService.groups();
        A: for (String group : groups) {
            for (String accessGroup : accessGroups) {
                if (group.startsWith(accessGroup)){
                    continue A;
                }
            }
            throw SystemMessage.ACCESS_DENIED_ARGS.exception("不可授权分组:"+group);
        }

        final RoleInfo roleInfo = roleInfoMap.get(role);
        if (roleInfo != null) {
            roleInfo.getGroups().clear();
            for (String group : groups) {
                roleInfo.addGroup(group);
            }
        }
    }

    @SerializerToFile
    public void resetResource(String role, String... resources){
        checkAccess(role);
        final List<String> canAccessResources = profileService.queryAccessResources();
        for (String resource : resources) {
            if (!canAccessResources.contains(resource)){
                throw SystemMessage.ACCESS_DENIED_ARGS.exception("不可授权资源:"+resource);
            }
        }

        final RoleInfo roleInfo = roleInfoMap.get(role);
        if (roleInfo != null){
            roleInfo.getResources().clear();
            for (String resource : resources) {
                roleInfo.addResource(resource);
            }
        }
    }

    public Set<String> roleList(){
        return roleInfoMap.keySet();
    }

    public void serializer() throws IOException {
        List<String> roleSerializer = new ArrayList<>();
        for (RoleInfo value : roleInfoMap.values()) {
            final ToolRole toolRole = value.getToolRole();
            final List<String> groups = value.getGroups();
            final List<String> resources = value.getResources();
            roleSerializer.add(toolRole.getRoleName()+":"+StringUtils.join(groups,",")+":"+StringUtils.join(resources,","));
        }
        fileManager.writeConfig("security","roles",StringUtils.join(roleSerializer,'\n'));
    }

    /**
     * $configDir[Root]
     *   security[Dir]
     *     roles[File]:     role1:group1,group2:resource1,resource2
     *                      role2:group1,group2:resource1,resource2
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        final String readConfig = fileManager.readConfig("security", "roles");
        if (StringUtils.isNotBlank(readConfig)) {
            final String[] roles = StringUtils.split(readConfig, '\n');
            for (String role : roles) {
                final String[] splitPreserveAllTokens = StringUtils.splitPreserveAllTokens(role, ":");
                if (splitPreserveAllTokens.length != 3){
                    log.warn("角色配置错误:{}",role);
                    continue;
                }
                final ToolRole toolRole = new ToolRole(splitPreserveAllTokens[0]);

                final RoleInfo roleInfo = new RoleInfo(toolRole);

                // 角色组织关联
                final String[] groups = StringUtils.split(splitPreserveAllTokens[1],',');
                for (String groupPath : groups) {
                    roleInfo.addGroup(groupPath);
                }

                // 角色资源关联
                final String[] resources = StringUtils.split(splitPreserveAllTokens[2],',');
                for (String resourceName : resources) {
                    roleInfo.addResource(resourceName);
                }

                roleInfoMap.put(toolRole.getRoleName(),roleInfo);
            }
        }
    }

    /**
     * 查询分组可以授权的角色列表
     * @param group
     * @return
     */
    public Set<String> findCanGrantRoles(String group){
        Set<String> rolenames = new HashSet<>();

        final Iterator<RoleInfo> iterator = roleInfoMap.values().iterator();
        A:while (iterator.hasNext()){
            final RoleInfo next = iterator.next();
            final List<String> groups = next.getGroups();
            for (String s : groups) {
                if (s.startsWith(group)){
                    rolenames.add(next.getToolRole().getRoleName());
                    continue A;
                }
            }
        }

        return rolenames;
    }

    @SerializerToFile
    void initAdmin(){
        log.info("初始化 admin 角色 admin, 分组 /, 拥有资源 admin ");
        final RoleInfo roleInfo = new RoleInfo(new ToolRole("admin"));
        roleInfo.addGroup("/");
        roleInfo.addResource("admin");
        roleInfoMap.put("admin", roleInfo);
    }
}
