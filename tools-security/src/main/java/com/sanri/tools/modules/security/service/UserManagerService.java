package com.sanri.tools.modules.security.service;

import com.sanri.tools.modules.core.aspect.SerializerToFile;
import com.sanri.tools.modules.core.exception.SystemMessage;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.security.entitys.ToolUser;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.security.service.dtos.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserManagerService implements InitializingBean {
    @Autowired
    private FileManager fileManager;
    @Autowired
    private ProfileServiceImpl profileService;

    // 所有的用户信息
    static final Map<String, SecurityUser> USERS = new ConcurrentHashMap<>();

    @SerializerToFile
    public void addUser(String username,String password,String mainGroup){
        if (existUser(username)){
            log.error("已经存在用户:{}",username);
            throw new ToolException("已经存在用户;"+username);
        }
        final ToolUser toolUser = new ToolUser(username, password);
        final SecurityUser thinUser = new SecurityUser(toolUser);
        thinUser.addGroup(mainGroup);
        USERS.put(username,thinUser);
    }

    public void deleteUser(String username) throws IOException {
        checkAccess(username);

        USERS.remove(username);

        // 删除用户文件夹
        final File usersDir = fileManager.mkConfigDir("security/users");
        FileUtils.deleteDirectory(new File(usersDir,username));
    }

    public void checkAccess(String username) {
        // 需要验证当前用户是否可以访问这个用户
        final List<String> accessUsers = profileService.queryAccessUsers();
        if (!accessUsers.contains(username)){
            throw SystemMessage.ACCESS_DENIED.exception();
        }
    }

    public boolean existUser(String username){
        return USERS.containsKey(username);
    }

    @SerializerToFile
    public void changePassword(String username,String password){
        final SecurityUser thinUser = USERS.get(username);
        thinUser.getToolUser().setPassword(password);
    }

    public void serializer() throws IOException {
        final File usersDir = fileManager.mkConfigDir("security/users");
        for (SecurityUser value : USERS.values()) {
            final String username = value.getUsername();
            final File baseFile = new File(usersDir, username+"/base");

            final String password = value.getPassword();
            final List<String> roles = value.getRoles();
            final List<String> groups = value.getGroups();

            String userBaseInfo = username + ":" + password +
                    ":" +
                    groups.stream().collect(Collectors.joining(",")) +
                    ":" +
                    roles.stream().collect(Collectors.joining(","));
            FileUtils.writeStringToFile(baseFile, userBaseInfo,StandardCharsets.UTF_8);
        }
    }

    /**
     * $configDir[Root]
     *   security[Dir]
     *    users[Dir]          所有用户的目录
     *     user1[Dir]        用户名
     *       base[File]      基础信息 用户名:密码:分组路径列表:角色列表 例 user1:123:sanri/dev,hd/dev,hd/test:role1,admin
     *       profile[File]    自定义信息,暂无
     *       ...
     *     user2[Dir]
     *       base[File]
     *       profile[File]
     *
     * 一个用户可以有多个分组, 多个角色
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 读取所有用户信息
        final File usersDir = fileManager.mkConfigDir("security/users");

        final File[] files = usersDir.listFiles();
        for (File userDir : files) {
            final String username = userDir.getName();
            // 初始化只加载 base 信息
            final File base = new File(userDir, "base");
            final String[] userFields = StringUtils.splitPreserveAllTokens(FileUtils.readFileToString(base, StandardCharsets.UTF_8), ":", 4);
            final ToolUser toolUser = new ToolUser(userFields[0], userFields[1]);
            final SecurityUser securityUser = new SecurityUser(toolUser);

            // 用户分组信息添加
            if (StringUtils.isNotBlank(userFields[2])){
                final String[] groups = StringUtils.splitPreserveAllTokens(userFields[2], ",");
                for (String groupPath : groups) {
                    securityUser.addGroup(groupPath);
                }
            }

            // 用户角色信息添加
            if (StringUtils.isNotBlank(userFields[3])){
                final String[] roles = StringUtils.splitPreserveAllTokens(userFields[3], ",");
                for (String role : roles) {
                    securityUser.addRole(role);
                }
            }

            USERS.put(username,securityUser);
        }
    }

    public List<SecurityUser> users() {
        return new ArrayList<>(USERS.values());
    }

    /**
     * 查询当前分组可授权的用户列表
     * @param group 分组名
     * @return
     */
    public Set<String> findCanGrantUsers(String group){
        Set<String> usernames = new HashSet<>();

        final Iterator<SecurityUser> iterator = USERS.values().iterator();
        A:while (iterator.hasNext()){
            final SecurityUser securityUser = iterator.next();
            final List<String> groups = securityUser.getGroups();
            for (String s : groups) {
                if (s.startsWith(group)){
                    usernames.add(securityUser.getUsername());
                    continue A;
                }
            }
        }

        return usernames;
    }

    @SerializerToFile
    void initAdmin(){
        log.info("初始化 admin 用户, 密码 0, 角色 admin, 分组 / ");
        final ToolUser toolUser = new ToolUser("admin", "0");
        final SecurityUser thinUser = new SecurityUser(toolUser);
        thinUser.addGroup("/");
        thinUser.addRole("admin");
        USERS.put("admin",thinUser);
    }
}
