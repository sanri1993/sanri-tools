package systest;

import com.sanri.tools.modules.core.utils.JarArchiveUtil;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JarUtilTest {
	String classpath ;

	@Before
	public void setup() throws IOException {
		final InputStream resourceAsStream = JarUtilTest.class.getResourceAsStream("/classpath");
		classpath = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
	}

	@Test
	public void testJarUtil() throws IOException {
		final Manifest manifest = new Manifest();
		final Attributes mainAttributes = manifest.getMainAttributes();
		mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainAttributes.put(Attributes.Name.CLASS_PATH, JarArchiveUtil.standardClasspath(classpath));

		JarArchiveUtil.createManifestFile(new File("d:/test"), manifest);
	}

	@Test
	public void testJar() throws IOException {
		File jarFileDir = new File("C:\\Users\\Administrator\\Desktop\\back\\patchcode");

		final Manifest manifest = new Manifest();
		final Attributes mainAttributes = manifest.getMainAttributes();
		mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainAttributes.put(Attributes.Name.CLASS_PATH, JarArchiveUtil.standardClasspath(classpath));
		JarArchiveUtil.createManifestFile(jarFileDir, manifest);

		// 打包 jar 包
		final File jar = JarArchiveUtil.jar(new File("d:/test/test.jar"), jarFileDir);
		System.out.println(jar);
	}



}
