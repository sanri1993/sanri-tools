package systest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.junit.Test;

/**
 * 下载远程仓库的jar
 *
 * @author LZJ
 * @create 2018-08-18 18:47
 **/
public class DownloadMavenJar {

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
