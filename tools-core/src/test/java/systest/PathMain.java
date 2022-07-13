package systest;

import com.sanri.tools.modules.core.utils.OnlyPath;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathMain {
    @Test
    public void test1(){
        final OnlyPath onlyPath1 = new OnlyPath("a/b///d/c.xml");
        final OnlyPath onlyPath2 = new OnlyPath("a/b");
        final OnlyPath onlyPath3 = new OnlyPath("/pom.xml");
        final OnlyPath onlyPath4 = new OnlyPath("pom.xml");

        final Path path1 = Paths.get("a/b///d/c.xml");
        final Path path2 = Paths.get("a/b");
        final Path path3 = Paths.get("/pom.xml");
        final Path path4 = Paths.get("pom.xml");


        System.out.println(onlyPath2.relativize(onlyPath1));
        System.out.println(onlyPath1.getParent());
        System.out.println(onlyPath4.toAbsolutePath());
        System.out.println(onlyPath4.getParent());
        System.out.println(onlyPath1.startsWith("a/b"));
        System.out.println(onlyPath1.startsWith("/a/b"));
        System.out.println(onlyPath2.getParent().getParent());
        System.out.println(onlyPath1.getNameCount());
        System.out.println(onlyPath1.subpath(1,4));
        System.out.println(onlyPath1.getFileName());

        System.out.println("-----------------------------------------------");
        System.out.println(path2.relativize(path1));
        System.out.println(path1.getParent());
        System.out.println(path4.toAbsolutePath());
        System.out.println(path4.getParent());
        System.out.println(path1.startsWith("a/b"));
        System.out.println(path1.startsWith("/a/b"));
        System.out.println(path2.getParent().getParent());
        System.out.println(path1.getNameCount());
        System.out.println(path1.subpath(1,4));
        System.out.println(path1.getFileName());

    }
}
