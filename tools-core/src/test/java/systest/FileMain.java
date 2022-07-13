package systest;

import com.sanri.tools.modules.core.service.file.Tailf;
import com.sanri.tools.modules.core.service.file.TreeFile;
import com.sanri.tools.modules.core.utils.MybatisXNode;
import com.sanri.tools.modules.core.utils.MybatisXPathParser;
import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.core.utils.OnlyPaths;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FileMain {

    @Test
    public void fileAccessTimeRead() throws IOException {
        File file = new File("D:\\tmp\\classloader\\job\\com\\itstyle\\quartz\\job/ChickenJob.class");
        BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        FileTime fileTime = basicFileAttributes.lastAccessTime();
        System.out.println(new Date(fileTime.to(TimeUnit.MILLISECONDS)));
    }

    @Test
    public void testTailf() throws IOException, InterruptedException {
        final File file = new File("d:/test/1/1.txt");
        final Tailf tailf = new Tailf();
        final Tailf.InnerTail innerTail = tailf.startTail(file);
        innerTail.register(new Tailf.LineUpdateListener() {
            @Override
            public void update(String line) {
                System.out.println(line);
            }
        });
        Thread.sleep(20000);
        tailf.stopTail(innerTail);
    }

    @Test
    public void testTreeFile(){
        String [] paths = {"tools-core/src/main/java/com/sanri/tools/modules/core/service/data/RandomDataService.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/RegexRandomDataService.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/DigitLetter.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/Letter.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/LowerCaseLetter.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/RandomLetterPicker.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/RandomLetterPickers.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/RandomStringGenerator.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/RegexNormalizer.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/SymbolLetter.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/UpperCaseLetter.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/randomstring/UserDefinedLetterPickerScanner.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/BaseNode.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/LinkNode.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/Node.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/OptionalNode.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/OrdinaryNode.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/RepeatNode.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/SingleNode.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/exception/RegexpIllegalException.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/exception/TypeNotMatchException.java","tools-core/src/main/java/com/sanri/tools/modules/core/service/data/regex/exception/UninitializedException.java","tools-core/src/test/java/systest/RandomDataMain.java","tools-core/src/test/java/systest/ThirdPushRequest.java"};
        final List<OnlyPath> onlyPaths = Arrays.stream(paths).map(OnlyPath::new).collect(Collectors.toList());
        final OnlyPath root = new OnlyPath("tools-core");
        final TreeFile treeFile = OnlyPaths.treeFiles(onlyPaths, root);
        System.out.println(treeFile);
    }

    @Test
    public void testOnlyPath(){
        final OnlyPath onlyPath = new OnlyPath("d:/repository");
        System.out.println(onlyPath.isAbsolutePath());
        System.out.println(onlyPath.resolveFile(new File("/tmp/sanri/")));
        System.out.println(onlyPath.relativize(onlyPath));
        System.out.println(new OnlyPath("/pom.xml").getParent().getFileName());
        System.out.println(OnlyPath.ROOT.resolve(new OnlyPath("/pom.xml")));
        System.out.println();
    }
}
