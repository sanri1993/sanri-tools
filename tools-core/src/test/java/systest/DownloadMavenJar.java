package systest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;

/**
 * 下载远程仓库的jar
 *
 * @author LZJ
 * @create 2018-08-18 18:47
 **/
public class DownloadMavenJar {

    @Test
    public void testPomParseWithModules(){
        final File[] files = Maven.resolver().loadPomFromFile("D:\\currentproject\\sanri-tools-maven\\tools-core/pom.xml")
                .importDependencies(ScopeType.RUNTIME, ScopeType.PROVIDED)
                .resolve("com.sanri.tools:tools-core:jar:1.0-SNAPSHOT").withTransitivity().asFile();
        System.out.println(files);
    }

    @Test
    public void testResolveDependencies() throws DependencyResolutionException {
        RepositorySystem repoSystem = DependencyFactory.newRepositorySystem();
        RepositorySystemSession session = DependencyFactory.newSession(repoSystem, new File("d:/repository"));
        DependencyRequest dependencyRequest = new DependencyRequest();
        String repository = "https://mirrors.huaweicloud.com/repository/maven/";
        RemoteRepository central = new RemoteRepository.Builder("central", "default", repository).build();
        Artifact queryArtifact = new DefaultArtifact("org.apache.commons:commons-text:jar:1.5");
        Dependency dependency = new Dependency(queryArtifact, null);
        CollectRequest collectRequest = new CollectRequest(dependency, Arrays.asList(central));
        dependencyRequest.setCollectRequest(collectRequest);
        final DependencyResult dependencyResult = repoSystem.resolveDependencies(session, dependencyRequest);
        final List<ArtifactResult> artifactResults = dependencyResult.getArtifactResults();
        System.out.println(artifactResults);
    }

    /**
     * 从指定maven地址下载指定jar包
     *
     * @param params
     * @throws ArtifactResolutionException
     */
    public static void downLoadMavenJar(Params params) throws Exception {

        RepositorySystem repoSystem = DependencyFactory.newRepositorySystem();
        RepositorySystemSession session = DependencyFactory.newSession(repoSystem, params.getTarget());
        RemoteRepository central = DependencyFactory.newRemoteRepository(params);

        //下载该jar包及其全部依赖jar包
        DefaultArtifact defaultArtifact = DependencyFactory.newDefaultArtifact(params.getArtifactId(), params.getGroupId(), params.getVersion());
//        Map<String,String>  properties = new HashMap<>();
//        properties.put("type","jar");
//        defaultArtifact.setProperties(properties);
        Dependency dependency = new Dependency(defaultArtifact, null);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        collectRequest.addRepository(central);
        DependencyNode node = repoSystem.collectDependencies(session, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(node);

        repoSystem.resolveDependencies(session, dependencyRequest);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg);

        //此时就已经下载好了 打印出jars
//        System.out.println(nlg.getFiles());
        final List<File> files = nlg.getFiles();
        for (File file : files) {
            System.out.println(file);
        }
    }


}
