package com.sanri.tools.maven.service;

import com.sanri.tools.maven.service.dtos.JarCollect;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.tomcat.Jar;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.filter.ExclusionsDependencyFilter;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 获取 jar 路径列表, 下载依赖 jar 包 能力
 */
@Service
@Slf4j
public class MavenJarResolve {
    @Autowired
    private MavenSettingsResolve mavenSettingsResolve;
    @Autowired
    private MavenDependencyResolve mavenDependencyResolve;

    /**
     * 解析一个工件, 得到 jar 包信息
     * @param settingsName
     * @param artifact
     * @return
     */
    public JarCollect resolveArtifact(String settingsName, Artifact artifact) throws IOException, XmlPullParserException, DependencyResolutionException {
        // 获取仓库系统
        final RepositorySystem repositorySystem = mavenSettingsResolve.repositorySystem(false);

        // 映射远程仓库
        final List<RemoteRepository> remoteRepositorys = mavenSettingsResolve.getRemoteRepositorys2(settingsName);

        // session
        final RepositorySystemSession session = mavenSettingsResolve.repositorySystemSession(settingsName, repositorySystem);

        DependencyRequest dependencyRequest = new DependencyRequest();
        Dependency dependency = new Dependency(artifact, null);
        CollectRequest collectRequest = new CollectRequest(dependency, remoteRepositorys);
        dependencyRequest.setCollectRequest(collectRequest);
        final DependencyResult dependencyResult = repositorySystem.resolveDependencies(session, dependencyRequest);
        PreorderNodeListGenerator preorderNodeListGenerator = new PreorderNodeListGenerator();
        dependencyResult.getRoot().accept(preorderNodeListGenerator);
        return new JarCollect(preorderNodeListGenerator.getFiles(),preorderNodeListGenerator.getClassPath());
    }

    /**
     * 解析 jar 文件列表, 根据 pom 文件
     * @param settingsName settings 文件名
     * @param pomFile pom文件
     * @return
     */
    public JarCollect resolveJarFiles(String settingsName, File pomFile) throws IOException, XmlPullParserException, DependencyCollectionException, DependencyResolutionException, ModelBuildingException {
        // 获取仓库系统
        final RepositorySystem repositorySystem = mavenSettingsResolve.repositorySystem(false);

        // 映射远程仓库
        final List<RemoteRepository> remoteRepositorys = mavenSettingsResolve.getRemoteRepositorys2(settingsName);

        // 获取要解析的依赖
        final Model model = mavenDependencyResolve.resolveEffectivePom(settingsName,pomFile);

        // session
        final RepositorySystemSession session = mavenSettingsResolve.repositorySystemSession(settingsName, repositorySystem);

        // 收集依赖项
        final CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRepositories(remoteRepositorys);

        final DefaultArtifact defaultArtifact = new DefaultArtifact(model.getGroupId(),model.getArtifactId(),model.getPackaging(),model.getVersion());
        collectRequest.setRoot(new Dependency(defaultArtifact,"compile"));

        final CollectResult collectResult = repositorySystem.collectDependencies(session, collectRequest);
        final DependencyNode root = collectResult.getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(root);

        // 排除自身
        String coodId = root.getArtifact().getGroupId()+":"+root.getArtifact().getArtifactId();
        final ExclusionsDependencyFilter exclusionsDependencyFilter = new ExclusionsDependencyFilter(Arrays.asList(coodId));
        dependencyRequest.setFilter(exclusionsDependencyFilter);

        repositorySystem.resolveDependencies(session, dependencyRequest);

        PreorderNodeListGenerator preorderNodeListGenerator = new PreorderNodeListGenerator();
        root.accept(preorderNodeListGenerator);

        final List<File> files = preorderNodeListGenerator.getFiles();
        final Set<File> noRepeatFiles = new HashSet<>(files);
        final String classpath = noRepeatFiles.stream().map(File::getAbsolutePath).collect(Collectors.joining(";"));
        return new JarCollect(noRepeatFiles,classpath);
    }

}
