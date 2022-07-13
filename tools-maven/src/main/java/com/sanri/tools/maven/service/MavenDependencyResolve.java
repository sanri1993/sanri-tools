package com.sanri.tools.maven.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * maven 依赖分析
 */
@Service
@Slf4j
public class MavenDependencyResolve {
    @Autowired
    private MavenSettingsResolve mavenSettingsResolve;

    private MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();

    /**
     * 直接读取 pom 文件, 不能解析变量和父子模块依赖分析 <br/>
     * 基本没啥用
     * @param pomFile
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    public Model resolvePomModel(File pomFile) throws IOException, XmlPullParserException {
        try(final FileInputStream fileInputStream = new FileInputStream(pomFile)){
            return mavenXpp3Reader.read(fileInputStream);
        }
    }

    /**
     * 解析有效的 pom, 把包的版本及依赖全部解析出来
     * @param pomFile
     * @return
     */
    public Model resolveEffectivePom(String connName,File pomFile) throws IOException, XmlPullParserException, ModelBuildingException {
        final RepositorySystem repositorySystem = mavenSettingsResolve.repositorySystem(false);

        final RepositorySystemSession session = mavenSettingsResolve.repositorySystemSession(connName, repositorySystem);

        final List<RemoteRepository> remoteRepositorys = mavenSettingsResolve.getRemoteRepositorys2(connName);

        final DefaultModelBuildingRequest request = new DefaultModelBuildingRequest()
                .setSystemProperties(System.getProperties())
                .setPomFile(pomFile);
        ModelBuilder builder = new DefaultModelBuilderFactory().newInstance();

        request.setModelResolver(new ModelResolverImpl(repositorySystem, session,remoteRepositorys));
        ModelBuildingResult result = builder.build(request);
        return result.getEffectiveModel();
    }
}
