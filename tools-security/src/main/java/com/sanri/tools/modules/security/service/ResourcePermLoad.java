package com.sanri.tools.modules.security.service;

import com.sanri.tools.modules.core.security.dtos.ResourceInfo;
import com.sanri.tools.modules.core.security.dtos.RoleInfo;
import com.sanri.tools.modules.core.security.entitys.ToolResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ResourcePermLoad implements InitializingBean {
    private static final Map<String,ResourceInfo> resourceInfos = new HashMap<>();

    @Autowired
    private GroupService groupService;

    @Override
    public void afterPropertiesSet() throws Exception {
        final ClassLoader classLoader = UrlSecurityPermsLoad.class.getClassLoader();
        final Enumeration<URL> resources = classLoader.getResources("resources.conf");
        while (resources.hasMoreElements()){
            final URL url = resources.nextElement();
            UrlResource urlResource = new UrlResource(url);
            try(final InputStream inputStream = urlResource.getInputStream();){
                final List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
                for (String line : lines) {
                    final String[] splitLine = StringUtils.splitPreserveAllTokens(line, ":", 5);
                    if (splitLine.length != 5) {
                        log.warn("错误的资源权限配置:{}",line);
                        continue;
                    }
                    final ResourceInfo resource = new ResourceInfo(new ToolResource(splitLine[0], splitLine[1], splitLine[2], splitLine[3]));
                    if (StringUtils.isNotBlank(splitLine[4])){
                        final String[] groupArray = StringUtils.split(splitLine[4], ',');
                        resource.setGroups(Arrays.asList(groupArray));
                    }
                    resourceInfos.put(splitLine[0],resource);
                }
            }
        }

        // 资源读取完成, 需要添加分组
        final Set<String> collect = resourceInfos.values().stream().flatMap(resourceInfo -> resourceInfo.getGroups().stream()).collect(Collectors.toSet());
        for (String group : collect) {
            groupService.addGroup(group);
        }
    }

    /**
     * 初始化 admin 资源
     * 虚拟资源, 每次初始化, 不可被修改
     */
    void initAdmin(){
        log.info("初始化 admin 资源");
        final ToolResource toolResource = new ToolResource();
        toolResource.setResourceName("admin");
        final ResourceInfo resourceInfo = new ResourceInfo(toolResource);
        resourceInfo.addGroup("/");
        resourceInfos.put("admin",resourceInfo);
    }

    public static List<ResourceInfo> getResourceInfos() {
        return new ArrayList<>(resourceInfos.values());
    }

    public List<ResourceInfo> loadResourcesFromNames(Collection<String> resources) {
        return resources.stream().map(name -> resourceInfos.get(name)).collect(Collectors.toList());
    }

    /**
     * 查询当前分组可以授权的资源信息列表
     * @param group 组织名
     * @return
     */
    public Set<String> findCanGrantResources(String group){
        Set<String> resourceNames = new HashSet<>();

        final Iterator<ResourceInfo> iterator = resourceInfos.values().iterator();
        A:while (iterator.hasNext()){
            final ResourceInfo next = iterator.next();
            final List<String> groups = next.getGroups();
            for (String s : groups) {
                if (s.startsWith(group)){
                    resourceNames.add(next.getToolResource().getResourceName());
                    continue A;
                }
            }
        }

        return resourceNames;
    }
}
