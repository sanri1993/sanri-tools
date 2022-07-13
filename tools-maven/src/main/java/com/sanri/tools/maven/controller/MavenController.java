package com.sanri.tools.maven.controller;

import com.sanri.tools.maven.service.dtos.JarCollect;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.StreamResponse;
import com.sanri.tools.modules.core.utils.ZipUtil;
import org.apache.maven.model.building.ModelBuildingException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import com.sanri.tools.maven.service.MavenJarResolve;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @GetMapping("/test")
    public void test(){
        int i = 1/0;
    }

    /**
     * 下载依赖的 jar 包列表
     * @param settings maven 配置文件名
     * @param pomFile pom 文件
     */
    @PostMapping("/resolveJars")
    public void resolveJars(String settings, @RequestParam("file") MultipartFile pomFile, HttpServletResponse response) throws IOException, DependencyCollectionException, XmlPullParserException, DependencyResolutionException, ModelBuildingException {
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

        final FileInputStream fileInputStream = new FileInputStream(zipFile) ;
        streamUtil.download(fileInputStream, MediaType.APPLICATION_OCTET_STREAM,zipFile.getName(),response);
    }

}
