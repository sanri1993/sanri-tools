package com.sanri.tools.modules.security.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.sanri.tools.modules.core.exception.SystemMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.core.aspect.SerializerToFile;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.security.dtos.GroupTree;
import com.sanri.tools.modules.core.service.file.FileManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 分组管理
 */
@Service
@Slf4j
public class GroupService implements InitializingBean {
    private final FileManager fileManager;

    // 存储还是线性结构, 获取的时候再转化
    private List<String> groups = new ArrayList<>();

    @Autowired
    private ProfileServiceImpl profileService;

//    // 根路径 /
//    private static final URI ROOT = URI.create("/");
//    // 上级路径
//    private static final URI PARENT = URI.create("..");
//    // 空路径
//    private static final URI EMPTY = URI.create("");

    public GroupService(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * 判断当前分组是否已经存在
     * @param group 分组路径
     * @return
     */
    public boolean existsGroup(String group){
        if (!group.endsWith("/")){group += "/";}

        for (String curGroup : groups) {
            if (curGroup.startsWith(group)){
                return true;
            }
        }
        return false;
    }

    /**
     * 用户添加分组
     * @param prefix
     * @param group
     */
    @SerializerToFile
    public void addGroup(String group){
        if (!group.endsWith("/")){group += "/";}

        if (existsGroup(group)){
           log.warn("已经存在分组:{}",group);
           return ;
        }
        groups.add(group);
    }

    @SerializerToFile
    public void deleteGroup(String group){
        if (!group.endsWith("/")){group += "/";}
        // 鉴定当前用户是否有访问这个分组的权限
        final List<String> groups = profileService.groups();
        boolean find = false;
        for (String userGroup : groups) {
            if (group.startsWith(userGroup)){
                find = true;
                break;
            }
        }
        if (!find){
            throw SystemMessage.ACCESS_DENIED.exception();
        }

        final String deleteGroup = group;
        final boolean removeIf = this.groups.removeIf(next -> next.startsWith(deleteGroup));
        if (removeIf) {
            this.groups.add(group);
        }
    }

    /**
     * 分组信息序列化到文件
     */
    public void serializer() throws IOException {
        fileManager.writeConfig("security","groups",StringUtils.join(groups,'\n'));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 读取分组信息
        final String readConfig = fileManager.readConfig("security", "groups");
        if (StringUtils.isNotBlank(readConfig)) {
            final String[] lines = StringUtils.split(readConfig, '\n');
            this.groups = new ArrayList<>(Arrays.asList(lines));
        }
    }

    public List<String> getGroups() {
        return groups;
    }

    /**
     * 过滤出顶层 group 列表
     * @param groups
     * @return
     */
    public Set<String> filterTopGroups(Set<String> groups){
        Set<String> topGroups = new HashSet<>();

        A: for (String group : groups) {
            final Iterator<String> iterator = topGroups.iterator();
            while (iterator.hasNext()){
                final String next = iterator.next();
                if (next.startsWith(group)){
                    iterator.remove();
                    topGroups.add(group);
                    continue A;
                }
                if (group.startsWith(next)){
                    continue A;
                }
            }
            topGroups.add(group);
        }

        return topGroups;
    }

    /**
     * 将一批路径转成树结构
     * @param groups 分组列表
     * @return
     */
    public List<GroupTree> convertPathsToGroupTree(List<String> groups){
        final List<Path> groupPaths = groups.stream().map(Paths::get).collect(Collectors.toList());

        GroupTree groupTree = new GroupTree("顶层");
        for (Path groupPath : groupPaths) {
            convertToGroupTree(groupPath, groupTree, 0);
        }
        return groupTree.getChildes();
    }

    private void convertToGroupTree(Path path, GroupTree root, int deep){
        final int nameCount = path.getNameCount();
        if (deep >= nameCount){
            return ;
        }
        final String pathName = path.getName(deep).toString();
        final List<GroupTree> children = root.getChildes();
        for (GroupTree child : children) {
            final String childName = child.getName();
            if (pathName.equals(childName)){
                convertToGroupTree(path,child,++deep);
                return ;
            }
        }

        final GroupTree groupTree = new GroupTree(pathName);
        groupTree.setPath(path.subpath(0, deep + 1).toString());
        root.addChild(groupTree);
        groupTree.setParent(root);
        convertToGroupTree(path,groupTree,++deep);
    }

    @SerializerToFile
    void initAdmin(){
        log.info("初始化 amdin 组织 / ");
        addGroup("/");
    }
}
