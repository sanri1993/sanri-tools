package sanri;

import com.sanri.tools.maven.service.MavenDependencyResolve;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class SysTestMain {

    @Test
    public void testModel() throws IOException, XmlPullParserException {
        MavenDependencyResolve mavenDependencyResolve = new MavenDependencyResolve();
        final Model model = mavenDependencyResolve.resolvePomModel(MavenCoreMain.pomFile);
        System.out.println(model);
    }
}
