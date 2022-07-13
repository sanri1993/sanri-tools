package com.sanri.tools.maven.service;

import com.sanri.tools.modules.core.controller.dtos.ListFileInfo;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 外部依赖 maven , 默认使用 3.6.3 版本
 */
@Service
@Slf4j
public class LocalMavenManager implements InitializingBean {

    @Autowired
    private FileManager fileManager;

    /**
     * maven 存储位置
     */
    private File mavenVersions;

    /**
     *  默认 maven 下载地址
     */
    public static final String DEFAULT_MAVEN_VERSION = "https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip";

    /**
     * maven 主目录
     */
    private File mavenHome;

    public List<ListFileInfo> listMavenVersions() {
        final File[] files = mavenVersions.listFiles();
        List<ListFileInfo> versions = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()){
                versions.add(new ListFileInfo(file));
            }
        }
        return versions;
    }

    /**
     * 上传一个 maven 版本
     * @param multipartFile
     */
    public void uploadMaven(MultipartFile multipartFile) throws IOException {
        final String originalFilename = multipartFile.getOriginalFilename();
        final String extension = FilenameUtils.getExtension(originalFilename);
        if (StringUtils.isBlank(extension) || !"zip".equalsIgnoreCase(extension)){
            throw new ToolException("仅支持 zip 格式文件");
        }

        // 复制到 maven 版本目录,并解压
        final File zipFile = new File(mavenVersions, originalFilename);
        FileCopyUtils.copy(multipartFile.getInputStream(),new FileOutputStream(zipFile));

        // 解压文件
        ZipUtil.unzip(zipFile,mavenVersions.getAbsoluteFile());

        // 删除压缩文件
        FileUtils.deleteQuietly(zipFile);
    }

    /**
     * 删除一个 maven
     * @param version
     * @throws IOException
     */
    public void deleteMavenVersion(String version) throws IOException {
        final File file = new File(mavenVersions, version);
        FileUtils.deleteDirectory(file);
    }

    /**
     * 将某个 maven 版本设置为默认
     * @param version
     */
    public void setDefault(String version){
        mavenHome = new File(mavenVersions, version);
    }

    /**
     * 下载默认 maven 并设置为默认
     */
    public void downloadDefaultMaven(String downloadAddress) throws IOException {
        if (StringUtils.isBlank(downloadAddress)){
            downloadAddress = DEFAULT_MAVEN_VERSION;
        }
        final String filename = new File(downloadAddress).getName();
        final File file = new File(mavenVersions, filename);
        if (file.exists()){
            log.warn("指定 maven 版本[{}]已经存在, 不需要下载",filename);
            return;
        }
        try {
            URL url = new URL(downloadAddress);
            try(final InputStream inputStream = url.openStream();
                final FileOutputStream fileOutputStream = new FileOutputStream(file)){
                IOUtils.copy(inputStream,fileOutputStream);

                // 解压文件
                ZipUtil.unzip(file,null);

                // 删除 zip 文件
                FileUtils.deleteQuietly(file);

                // 设置 mavenHome
                final String dirname = FilenameUtils.getBaseName(filename).replace("-bin", "");
                mavenHome = new File(mavenVersions,dirname);
            }
        } catch (MalformedURLException e) {
        }

    }

    public File getMavenHome(){
        return mavenHome;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mavenVersions = fileManager.mkDataDir("maven-versions");

        // 如果默认版本的 maven 环境存在, 则使用默认版本做为 mavenhome
        final File file = new File(mavenVersions, "apache-maven-3.6.3");
        if (file.exists()){
            mavenHome = file;
        }
    }
}
