package com.sanri.tools.modules.core.service.plugin;

import com.sanri.tools.modules.core.service.file.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class PluginRepoManager implements InitializingBean {

    @Autowired
    private FileManager fileManager;

    private static Set<String> REPOS = new LinkedHashSet<>();

    public Set<String> list(){
        return REPOS;
    }

    public String current(){
        if (CollectionUtils.isNotEmpty(REPOS)){
            return REPOS.iterator().next();
        }
        return null;
    }

    public void add(String repo){
        REPOS.add(repo);
    }

    public void remove(String repo){
        final Iterator<String> iterator = REPOS.iterator();
        while (iterator.hasNext()){
            final String next = iterator.next();
            if (next.equals(repo)){
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final File plugin = fileManager.mkConfigDir("plugin");
        final File repos = new File(plugin, "repos");
        if (repos.exists()) {
            REPOS = new LinkedHashSet<>(FileUtils.readLines(repos, StandardCharsets.UTF_8));
        }
    }
}
