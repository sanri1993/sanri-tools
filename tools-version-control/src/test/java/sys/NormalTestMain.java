package sys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import javax.tools.*;

import lombok.Lombok;
import org.apache.commons.lang3.RegExUtils;
import org.junit.Test;

public class NormalTestMain {
    /**
     * 使用 url 获取仓库名
     */
    @Test
    public void test1(){
        final File file = new File("https://gitee.com/sanri/sanri-tools-maven.git");
        System.out.println(file.getName());

        final File file1 = new File("git@gitee.com:sanri/sanri-tools-maven.git");
        System.out.println(file1.getName());
    }

    @Test
    public void test2(){
        final File file = new File("d:/test");
        System.out.println(new File(file,"/abc"));
    }

    @Test
    public void test30() throws IOException {
        final File file = new File("D:\\tmp\\sanritools\\temp\\tmpjars\\1653448924249\\META-INF\\MANIFEST.MF");
        Manifest manifest = new Manifest();
        try(final FileInputStream fileInputStream = new FileInputStream(file)){
            manifest.read(fileInputStream);

            final String classpath = (String) manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH);

            System.out.println(RegExUtils.replaceAll(classpath," ",";"));
        }
    }

}
