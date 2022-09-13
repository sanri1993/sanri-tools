package com.sanri.tools.maven.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.visitor.TreeDependencyVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanri.tools.maven.service.dtos.DependencyTree;

import lombok.extern.slf4j.Slf4j;

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

    /**
     * 解析某个工件的依赖树
     * @param connName
     * @param artifact
     */
    public DependencyTree dependencyTree(String connName, Artifact artifact) throws IOException, XmlPullParserException, DependencyResolutionException, DependencyCollectionException {
        final RepositorySystem repositorySystem = mavenSettingsResolve.repositorySystem(false);

        final RepositorySystemSession session = mavenSettingsResolve.repositorySystemSession(connName, repositorySystem);

        final List<RemoteRepository> remoteRepositorys = mavenSettingsResolve.getRemoteRepositorys2(connName);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(remoteRepositorys);

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

        List<ArtifactResult> artifactResults = repositorySystem.resolveDependencies(session, dependencyRequest).getArtifactResults();
        CollectResult collectResult = repositorySystem.collectDependencies(session, collectRequest);
        DependencyNode node = collectResult.getRoot();
        final TreeDependencyVisitorCustom treeDependencyVisitorCustom = new TreeDependencyVisitorCustom();
        node.accept(new TreeDependencyVisitor(treeDependencyVisitorCustom));

        return treeDependencyVisitorCustom.root;
    }

    public static final class TreeDependencyVisitorCustom implements DependencyVisitor {
        private Stack<DependencyTree> stack = new Stack<>();
        private DependencyTree root;
        @Override
        public boolean visitEnter(DependencyNode node) {
            final Artifact artifact = node.getArtifact();
            final DependencyTree dependencyTree = new DependencyTree(artifact);
            if (root == null){
                root = dependencyTree;
            }
            if (CollectionUtils.isNotEmpty(stack)){
                stack.peek().getChildren().add(dependencyTree);
                stack.push(dependencyTree);
            }else {
                stack.push(dependencyTree);
            }
            return true;
        }

        @Override
        public boolean visitLeave(DependencyNode node) {
            stack.pop();
            return true;
        }
    }


}
