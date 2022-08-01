package com.sanri.tools.modules.versioncontrol.project;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.versioncontrol.dtos.ProjectLocation;
import com.sanri.tools.modules.versioncontrol.git.RepositoryMetaService;
import com.sanri.tools.modules.versioncontrol.project.dtos.ProjectMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class ProjectMetaService {
    @Autowired
    private RepositoryMetaService repositoryMetaService;

    /**
     * 添加或者获取一个项目, 在仓库中
     * @param projectName
     * @param path
     * @return
     */
    public ProjectMeta computeIfAbsent(ProjectLocation projectLocation) throws IOException {
        final RepositoryMetaService.RepositoryMeta repositoryMeta = repositoryMetaService.repositoryMeta(projectLocation.getGroup(), projectLocation.getRepository());

        final String projectName = projectLocation.getProjectName();

        final Map<String, ProjectMeta> projectMetaMap = repositoryMeta.getProjectMetaMap();
        if (projectMetaMap.containsKey(projectName)){
            return projectMetaMap.get(projectName);
        }
        final ProjectMeta projectMeta = new ProjectMeta(projectName,projectLocation.getPath());
        repositoryMeta.addProjectMeta(projectMeta);
        return projectMeta;
    }


}
