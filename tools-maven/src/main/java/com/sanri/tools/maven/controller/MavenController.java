package com.sanri.tools.maven.controller;

import java.io.*;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import com.sanri.tools.maven.service.MavenDependencyResolve;
import com.sanri.tools.maven.service.dtos.DependencyTree;
import com.sanri.tools.modules.core.utils.OnlyPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.building.ModelBuildingException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.sanri.tools.maven.service.MavenJarResolve;
import com.sanri.tools.maven.service.dtos.JarCollect;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.StreamResponse;
import com.sanri.tools.modules.core.utils.ZipUtil;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

/**
 * Maven 工具
 * @author sanri
 */
@RestController
@RequestMapping("/maven")
@Slf4j
public class MavenController {
    @Autowired
    private MavenJarResolve mavenJarResolve;
    @Autowired
    private FileManager fileManager;
    @Autowired
    private StreamResponse streamUtil;
    @Autowired
    private MavenDependencyResolve mavenDependencyResolve;

    /**
     * 下载依赖的 jar 包列表
     * @param settings maven 配置文件名
     * @param pomFile pom 文件
     */
    @PostMapping("/resolveJars")
    public String resolveJars(String settings, @RequestParam("file") MultipartFile pomFile) throws IOException, DependencyCollectionException, XmlPullParserException, DependencyResolutionException, ModelBuildingException {
        // 移动文件到临时目录
        final File uploadTemp = fileManager.mkTmpDir("uploadTemp/" + System.currentTimeMillis());
        final File targetFile = new File(uploadTemp, pomFile.getOriginalFilename());

        final File zipFile = new File(uploadTemp + "/rsolvejars.zip");
        try(final InputStream inputStream = pomFile.getInputStream();
            final FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            final ZipUtil.ZipFile zip = new ZipUtil.ZipFile(zipFile)){
            FileCopyUtils.copy(inputStream,fileOutputStream);
            final JarCollect jarCollect = mavenJarResolve.resolveJarFiles(settings, targetFile);
            final Collection<File> files = jarCollect.getFiles();

            // 压缩文件
            zip.addFilesAndFinish(files.toArray(new File[]{}));
        }

        return fileManager.relativePath(new OnlyPath(zipFile)).toString();
    }

    /**
     * 通过 gav 坐标下载 jar
     * @param settings 配置文件, 即 maven 连接名
     * @param gav 工件坐标
     */
    @GetMapping("/resolveJarsByGAV")
    public String resolveJarsByGAV(String settings,String gav) throws IOException, TemplateException, DependencyCollectionException, XmlPullParserException, DependencyResolutionException, ModelBuildingException {
        final String traceId = MDC.get("traceId");
        final File uploadTemp = fileManager.mkTmpDir("uploadTemp/" + traceId);

        // 解析 gav 坐标
        Artifact artifact = new DefaultArtifact(gav);
        // 解析依赖 jar
        final JarCollect jarCollect = mavenJarResolve.resolveArtifact("test", artifact);

        // 下载 jar
        final Collection<File> files = jarCollect.getFiles();

        if (CollectionUtils.isEmpty(files)){
            throw new ToolException("依赖解析失败, 未找到任何包");
        }

        for (File file : files) {
            FileUtils.copyFileToDirectory(file,uploadTemp);
        }
        return fileManager.relativePath(new OnlyPath(uploadTemp)).toString();
    }

    /**
     * 解决某一个坐标的依赖树
     * @param settings 配置文件, 即 maven 连接名
     * @param gav 工件坐标
     * @return
     */
    @GetMapping("/collectDependencyTree")
    public DependencyTree collectDependencyTree(String settings,String gav) throws DependencyCollectionException, XmlPullParserException, DependencyResolutionException, IOException {
        return mavenDependencyResolve.dependencyTree(settings,new DefaultArtifact(gav));
    }
}
