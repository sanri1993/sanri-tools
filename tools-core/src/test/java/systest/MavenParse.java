package systest;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.List;

public class MavenParse {
    @Test
    public void testParseMaven() throws Exception {
        File pomfile = new File("d:/test/classloaderdefault.pom");
        Model model = null;
        FileReader reader = null;
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        try {
            reader = new FileReader(pomfile);
            model = mavenreader.read(reader);
//            model.setPomFile(pomfile);
        }catch(Exception ex){}
        MavenProject project = new MavenProject(model);

        final List<Dependency> dependencies = project.getDependencies();
        String repo = "https://mirrors.huaweicloud.com/repository/maven/";
        for (Dependency dependency : dependencies) {
            final Params testmaven = new Params(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), repo, new File("D:\\repository"));
            DownloadMavenJar.downLoadMavenJar(testmaven);
        }
    }
}
