package systest;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class JarAnalyse {
    private MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
    @Test
    public void pomAnalyse() throws IOException, XmlPullParserException {
        final Model model = mavenXpp3Reader.read(new FileInputStream(new File("D:\\currentproject\\sanri-tools-maven\\tools-core\\pom.xml")));
        MavenProject project = new MavenProject(model);


    }
}
