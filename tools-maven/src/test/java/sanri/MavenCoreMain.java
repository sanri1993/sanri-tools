package sanri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.filter.ExclusionsDependencyFilter;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.junit.Test;

public class MavenCoreMain {

    SettingsXpp3Reader settingsXpp3Reader = new SettingsXpp3Reader();

    MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();

    @Test
    public void testSettingsReader() throws IOException, XmlPullParserException {
        final File file = new File("C:\\pathdev\\apache-maven-3.6.3\\conf/settings.xml");
        try(final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
            final Settings settings = settingsXpp3Reader.read(inputStreamReader);
            System.out.println(settings);
        }
    }

    @Test
    public void testPomReader() throws IOException, XmlPullParserException {
        final File file = new File("d:/test/classloaderdefault.pom");
        try(final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
            final Model model = mavenXpp3Reader.read(inputStreamReader);
            System.out.println(model);
        }
    }

    /**
     * 这个读取的 pom 没有解析父子模块的能力
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Test
    public void test3() throws IOException, XmlPullParserException {
        final File file = new File("d:/test/classloaderdefault.pom");
        try(final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
            final Model model = mavenXpp3Reader.read(inputStreamReader);
            final MavenProject mavenProject = new MavenProject(model);
        }
    }

    /**
     * maven 依赖分析, 只能分析一个在仓库中的 maven 坐标
     * @throws DependencyResolutionException
     */
    @Test
    public void testResolveDependencies() throws DependencyResolutionException {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        final RepositorySystem repositorySystem = locator.getService(RepositorySystem.class);

        LocalRepository localRepo = new LocalRepository("d:/repository");
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);
        session.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true);
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));

        DependencyRequest dependencyRequest = new DependencyRequest();
        String repository = "https://mirrors.huaweicloud.com/repository/maven/";
        RemoteRepository central = new RemoteRepository.Builder("central", "default", repository).build();
        Artifact queryArtifact = new DefaultArtifact("org.apache.commons:commons-text:jar:1.5");
        Dependency dependency = new Dependency(queryArtifact, null);
        CollectRequest collectRequest = new CollectRequest(dependency, Arrays.asList(central));
        dependencyRequest.setCollectRequest(collectRequest);
        final DependencyResult dependencyResult = repositorySystem.resolveDependencies(session, dependencyRequest);
//        final List<ArtifactResult> artifactResults = dependencyResult.getArtifactResults();
//        System.out.println(artifactResults);
        PreorderNodeListGenerator preorderNodeListGenerator = new PreorderNodeListGenerator();
        dependencyResult.getRoot().accept(preorderNodeListGenerator);
        final List<File> files = preorderNodeListGenerator.getFiles();
        System.out.println(files);
    }

    /**
     * 在本地仓库中查找 jar 包, 如果需要在网络上查找 , 需要添加  HttpTransporterFactory.class
     * 这个解析的依赖包信息会有重复的文件, 需要去重
     * @throws DependencyCollectionException
     * @throws DependencyResolutionException
     */
    @Test
    public void testResolverJars() throws DependencyCollectionException, DependencyResolutionException {
        long startTime = System.currentTimeMillis();
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        final RepositorySystem repositorySystem = locator.getService(RepositorySystem.class);

        LocalRepository localRepo = new LocalRepository("d:/repository");
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);
        session.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true);
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));

        String repository = "http://192.168.12.172:9081/repository/maven-public/";
        RemoteRepository central = new RemoteRepository.Builder("central", "default", repository).build();

        final CollectRequest collectRequest = new CollectRequest();
        collectRequest.addRepository(central);
        final DefaultArtifact defaultArtifact = new DefaultArtifact("com.wenjing:utax-eam-base:dev");
        collectRequest.setRoot(new Dependency(defaultArtifact,"compile"));

        final CollectResult collectResult = repositorySystem.collectDependencies(session, collectRequest);
        final DependencyNode root = collectResult.getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(root);
        String coodId = root.getArtifact().getGroupId()+":"+root.getArtifact().getArtifactId();
        final ExclusionsDependencyFilter exclusionsDependencyFilter = new ExclusionsDependencyFilter(Arrays.asList(coodId));
//        dependencyRequest.setFilter(exclusionsDependencyFilter);
        repositorySystem.resolveDependencies(session, dependencyRequest);

        PreorderNodeListGenerator preorderNodeListGenerator = new PreorderNodeListGenerator();
        root.accept(preorderNodeListGenerator);

        final List<File> files = preorderNodeListGenerator.getFiles();
        final Map<String, List<File>> collect = files.stream().collect(Collectors.groupingBy(File::getName));
        final String classPath = preorderNodeListGenerator.getClassPath();
//        System.out.println(classPath);
        System.out.println("花费时间: "+ (System.currentTimeMillis() - startTime));
    }

    static File pomFile = new File("D:\\currentproject\\sanri-tools-maven/tools-core/pom.xml");


    /**
     * 解析 pom 文件, 得到依赖项, 这个只会解析变量, 不会去递归查找依赖项
     * @throws Exception
     */
    @Test
    public void testPomResolve() throws Exception {
        final File file = new File("C:\\pathdev\\apache-maven-3.6.3\\conf/settings.xml");
        try(final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
            DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
            locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
            locator.addService(TransporterFactory.class, FileTransporterFactory.class);
            locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
            final RepositorySystem repositorySystem = locator.getService(RepositorySystem.class);

            LocalRepository localRepo = new LocalRepository("d:/repository");
            DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
            session.setConfigProperty(ConflictResolver.CONFIG_PROP_VERBOSE, true);
            session.setConfigProperty(DependencyManagerUtils.CONFIG_PROP_VERBOSE, true);
            session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));

            final Settings settings = settingsXpp3Reader.read(inputStreamReader);

            String repository = "http://192.168.12.172:9081/repository/maven-public/";
            RemoteRepository central = new RemoteRepository.Builder("central", "default", repository).build();

            final DefaultModelBuildingRequest request = new DefaultModelBuildingRequest()
                    .setSystemProperties(System.getProperties())
                    .setPomFile(pomFile);
//                    .setActiveProfileIds(settings.getActiveProfiles());
            ModelBuilder builder = new DefaultModelBuilderFactory().newInstance();

            request.setModelResolver(new MavenModelResolver(repositorySystem, session,Arrays.asList(central)));
            ModelBuildingResult result = builder.build(request);

            final Model effectiveModel = result.getEffectiveModel();
            System.out.println(effectiveModel);
        }
    }

    /**
     * 执行插件
     */
    @Test
    public void testPluginExecute() throws MojoExecutionException, IOException, XmlPullParserException {
//        try(final FileInputStream fileInputStream = new FileInputStream(pomFile);) {
//            final Model read = mavenXpp3Reader.read(fileInputStream);
//            final MavenProject mavenProject = new MavenProject(read);
//
//            try {
//                MojoExecutor
//                        .executeMojo(MojoExecutor.plugin("org.codehaus.mojo",
//                                "wagon-maven-plugin"), MojoExecutor
//                                .goal("upload-single"), MojoExecutor
//                                .configuration(MojoExecutor.element("outputDirectory","${project.build.directory}/foo")),
//                        new MojoExecutor.ExecutionEnvironment(mavenProject,null, null));
//            } catch (Exception ex) {
//                throw new MojoExecutionException("Error while uploading file: "
//                        + ex.getMessage(), ex);
//            }
//        }
    }

    @Test
    public void testExecuteGoals() throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( pomFile);
        request.setGoals( Collections.singletonList( " compile" ) );
//        request.setUserSettingsFile(new File(""));
        request.setOutputHandler(new InvocationOutputHandler() {
            @Override
            public void consumeLine(String line) throws IOException {
                System.out.println("out:"+line);
            }
        });

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("C:\\pathdev\\apache-maven-3.6.3"));
        final InvocationResult execute = invoker.execute(request);
        System.out.println("同步");
    }

}
