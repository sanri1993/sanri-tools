package com.sanri.tools.modules.versioncontrol.project;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.versioncontrol.dtos.ProjectLocation;
import com.sanri.tools.modules.versioncontrol.project.dtos.ProjectMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ModuleMetaService {
    @Autowired
    private ProjectMetaService projectMetaService;

    @Autowired
    private FileManager fileManager;

    /**
     * 获取或者创建一个模块元数据
     * @param projectLocation
     * @param modulePomPath
     * @return
     */
    public ProjectMeta.ModuleMeta computeIfAbsent(ProjectLocation projectLocation,String relativePomFile) throws IOException {
        final ProjectMeta projectMeta = projectMetaService.computeIfAbsent(projectLocation);
        final OnlyPath parent = new OnlyPath(relativePomFile).getParent();
        String moduleName = projectLocation.getRepository();
        if (parent != null){
            // 当相对 pom 文件为 pom.xml 时, 模块使用项目名
            moduleName = parent.getFileName();
        }

        ProjectMeta.ModuleMeta moduleCompileMeta = projectMeta.getModuleCompileMetas().get(moduleName);
        if (moduleCompileMeta != null){
            return moduleCompileMeta;
        }
        moduleCompileMeta = new ProjectMeta.ModuleMeta(moduleName,relativePomFile);
        projectMeta.getModuleCompileMetas().put(moduleName,moduleCompileMeta);
        return moduleCompileMeta;
    }

    /**
     * 写入模块元数据的 classpath
     * @param moduleCompileMeta
     * @param classpath classpath 路径列表
     */
    public void writeModuleClasspath(ProjectLocation projectLocation,String relativePomFile, String classpath) throws IOException {
        final ProjectMeta.ModuleMeta moduleMeta = computeIfAbsent(projectLocation, relativePomFile);

        final String pomFileRelativePath = moduleMeta.getPomFileRelativePath();
        final String relativePath = new OnlyPath(pomFileRelativePath).getParent().toString();
        final File classPathDir = fileManager.mkTmpDir("classpath/" + relativePath);
        final File classPathFile = new File(classPathDir, System.currentTimeMillis() + "");
        final OnlyPath relativize = new OnlyPath(fileManager.getTmpBase()).relativize(new OnlyPath(classPathFile));
        FileUtils.writeStringToFile(classPathFile,classpath, StandardCharsets.UTF_8);
        moduleMeta.setClasspath(relativize.toString());
    }

    /**
     * 读取模块元数据的 classpath
     * @param moduleCompileMeta
     */
    public String readModuleClasspath(ProjectLocation projectLocation,String relativePomFile) throws IOException {
        final ProjectMeta.ModuleMeta moduleMeta = computeIfAbsent(projectLocation, relativePomFile);

        final String classpath = moduleMeta.getClasspath();
        if (StringUtils.isBlank(classpath)){
            return "";
        }
        final File file = new OnlyPath(classpath).resolveFile(fileManager.getTmpBase());
        if (file.exists()){
            return FileUtils.readFileToString(file,StandardCharsets.UTF_8);
        }
        // 如果指定的 classpath 文件被删除, 则需要重新创建, 将classpath 字段置空
        moduleMeta.setClasspath(null);

        return null;
    }

    /**
     * 获取模块 classpath 最后更新时间
     * @param moduleCompileMeta
     * @return
     */
    public Long readModuleClassPathLastUpdateTime(ProjectLocation projectLocation,String relativePomFile) throws IOException {
        final ProjectMeta.ModuleMeta moduleMeta = computeIfAbsent(projectLocation, relativePomFile);

        final String classpath = moduleMeta.getClasspath();
        if (StringUtils.isBlank(classpath)){
            return null;
        }
        final File file = new OnlyPath(classpath).resolveFile(fileManager.getTmpBase());
        if (!file.exists()){
            return null;
        }
        return file.lastModified();
    }
}
