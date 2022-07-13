//package sanri;
//
//import org.jboss.shrinkwrap.api.Archive;
//import org.jboss.shrinkwrap.resolver.api.maven.Maven;
//import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
//import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
//import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
//import org.junit.Test;
//
//import java.io.File;
//import java.util.List;
//
//public class MavenMain {
//    @Test
//    public void testPomParseWithModules(){
//        final File[] files = Maven.resolver().loadPomFromFile("D:\\currentproject\\sanri-tools-maven\\tools-core/pom.xml")
//                .importDependencies(ScopeType.RUNTIME, ScopeType.PROVIDED)
//                .resolve("com.sanri.tools:tools-core:jar:1.0-SNAPSHOT").withTransitivity().asFile();
//        System.out.println(files);
//    }
//
//    @Test
//    public void execplugin(){
//        final BuiltProject builtProject = EmbeddedMaven.forProject("D:\\companyproject\\fssc_eam_dev\\eam-service\\fssc-eims/pom.xml")
//                .setGoals("clean")
//                .setUserSettingsFile(new File("C:\\Users\\Administrator\\.m2/settings.xml"))
//                .setLocalRepositoryDirectory(new File("d:/repository"))
//                .build();
//        final List<Archive> archives = builtProject.getArchives();
//        System.out.println(archives);
//    }
//
//    @Test
//    public void testValidate(){
//        final BuiltProject builtProject = EmbeddedMaven.forProject("D:\\companyproject\\fssc_eam_dev\\eam-service\\fssc-eims/fssc-eims-core/taxmpv2-sys/pom.xml")
//                .setGoals("clean","validate")
//                .setUserSettingsFile(new File("C:\\Users\\Administrator\\.m2/settings.xml"))
//                .setLocalRepositoryDirectory(new File("d:/repository"))
//                .build();
//        final List<Archive> archives = builtProject.getArchives();
//        System.out.println(archives);
//    }
//}
