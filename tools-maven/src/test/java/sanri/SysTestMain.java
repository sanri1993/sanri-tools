package sanri;

import com.sanri.tools.maven.service.Booter;
import com.sanri.tools.maven.service.MavenDependencyResolve;
import com.sanri.tools.maven.service.MavenJarResolve;
import com.sanri.tools.maven.service.MavenSettingsResolve;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.model.Model;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.eclipse.aether.util.graph.visitor.TreeDependencyVisitor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SysTestMain {

    @Test
    public void testModel() throws IOException, XmlPullParserException {
        MavenDependencyResolve mavenDependencyResolve = new MavenDependencyResolve();
        final Model model = mavenDependencyResolve.resolvePomModel(MavenCoreMain.pomFile);
        System.out.println(model);
    }

    /**
     * 依赖树分析
     * @throws IOException
     * @throws XmlPullParserException
     * @throws DependencyResolutionException
     * @throws DependencyCollectionException
     */
    @Test
    public void testJarResolveTree() throws IOException, XmlPullParserException, DependencyResolutionException, DependencyCollectionException {
        System.out.println("------------------------------------------------------------");


        RepositorySystem system = Booter.newRepositorySystem(false);

        RepositorySystemSession session = Booter.newRepositorySystemSession(system,"d:/repository");

        Artifact artifact = new DefaultArtifact("com.dtflys.forest:forest-spring-boot-starter:1.5.26");

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
//        collectRequest.setRepositories(Booter.newRepositories(system, session));
        RemoteRepository remoteRepository = new RemoteRepository.Builder("hw", "default", "https://mirrors.huaweicloud.com/repository/maven/").build();
        collectRequest.setRepositories(Arrays.asList(remoteRepository));

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

        List<ArtifactResult> artifactResults =
                system.resolveDependencies(session, dependencyRequest).getArtifactResults();

//        for (ArtifactResult artifactResult : artifactResults) {
//            System.out.println(artifactResult.getArtifact() + " resolved to " + artifactResult.getArtifact().getFile());
//        }

        //use collectDependencies to collect
        CollectResult collectResult = system.collectDependencies(session, collectRequest);
        DependencyNode node = collectResult.getRoot();
        final MavenDependencyResolve.TreeDependencyVisitorCustom treeDependencyVisitorCustom = new MavenDependencyResolve.TreeDependencyVisitorCustom();
        node.accept(new TreeDependencyVisitor(treeDependencyVisitorCustom));
        System.out.println(treeDependencyVisitorCustom);
//        node.accept(new TreeDependencyVisitor(new DependencyVisitor() {
//            String indent = "";
//            @Override
//            public boolean visitEnter(DependencyNode dependencyNode) {
//                System.out.println(indent + dependencyNode.getArtifact());
//                indent += "    ";
//                return true;
//            }
//
//            @Override
//            public boolean visitLeave(DependencyNode dependencyNode) {
//                indent = indent.substring(0, indent.length() - 4);
//                return true;
//            }
//        }));
    }
}
