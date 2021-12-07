package test;

import com.sanri.tools.modules.core.security.dtos.GroupTree;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class NoSpringTest {
    @Test
    public void test1(){
        final Path path = Paths.get("a/b/c/d");
        final Iterator<Path> iterator = path.iterator();
        while (iterator.hasNext()){
            final Path next = iterator.next();
            System.out.println(next.toString());
        }

        final int nameCount = path.getNameCount();
        System.out.println(nameCount);

        final Path name = path.getName(3);
        System.out.println(name);
    }

    @Test
    public void test2() throws URISyntaxException {
        String group = "/a/b";
        String [] groups = {"/a/c","/a/b/c/d","/a/b/c"};


        final URI prefixPath = new URI(group);

        Set<String> subChildGroups = new HashSet<>();
        final List<String> supportChildGroup = Arrays.stream(groups).filter(g -> g.startsWith(group)).collect(Collectors.toList());
        for (String support : supportChildGroup) {
            final URI wholePath = new URI(support);
            final URI relativize = prefixPath.relativize(wholePath);

            URI path = relativize.resolve("..");

        }
    }

    @Test
    public void test3() throws URISyntaxException {
        URI uri = new URI("/a/b/c/");
        System.out.println(uri.resolve(".."));
        System.out.println(uri.resolve("..").resolve(".."));
        System.out.println(uri.resolve("..").resolve("..").resolve(".."));
        System.out.println(uri.resolve("..").resolve("..").resolve("..").equals(URI.create("/")));

        final URI relativize = uri.relativize(URI.create("/a/b/c/d/e/"));
        System.out.println(relativize);

        final URI relativize1 = uri.relativize(URI.create("/a/b/c/"));
        System.out.println(relativize1);
    }


    @Test
    public void testGroupsToTree(){
        final List<Path> paths = Arrays.asList(
                Paths.get("/a/b/c"),
                Paths.get("/d/e/f"),
                Paths.get("/a/c/m")
        );

        GroupTree groupTree = new GroupTree("顶层");
        for (Path path : paths) {
            convertToGroupTree(path,groupTree,0);
        }

//        appendPath(Paths.get("/a/b/c/d"),0,groupTree);
        appendPath(Paths.get("/m/c/b"),0,groupTree);

        System.out.println(groupTree);
    }

    public void convertToGroupTree(Path path, GroupTree root, int deep){
        final int nameCount = path.getNameCount();
        if (deep >= nameCount){
            return ;
        }
        final String pathName = path.getName(deep).toString();
        final List<GroupTree> childs = root.getChildes();
        for (GroupTree child : childs) {
            final String childName = child.getName();
            if (pathName.equals(childName)){
                convertToGroupTree(path,child,++deep);
                return ;
            }
        }

        final GroupTree groupTree = new GroupTree(pathName);
        groupTree.setPath(path.subpath(0, deep + 1).toString());
        root.addChild(groupTree);
        convertToGroupTree(path,groupTree,++deep);
    }

    /**
     * 将路径追加到分组
     * @param path 路径信息
     * @param deep 深度
     */
    private void appendPath(Path path, int deep,GroupTree parent) {
        if (deep >= path.getNameCount()){
            return ;
        }
        final String pathName = path.getName(deep).toString();
        for (GroupTree groupTree : parent.getChildes()) {
            final String name = groupTree.getName();
            if (name.equals(pathName)){
                appendPath(path,++deep,groupTree);
                return ;
            }
        }
        addNewPath(path,deep,parent);
    }

    /**
     * 添加一条新路径
     * @param path 路径
     * @param deep 开始添加的深度
     * @param parent 父级节点
     */
    private void addNewPath(Path path,int deep,GroupTree parent){
        final int nameCount = path.getNameCount();
        if (deep >= nameCount){
            return ;
        }
        for (int i = deep; i < nameCount; i++) {
            final String pathName = path.getName(i).toString();
            final GroupTree groupTree = new GroupTree(pathName);
            groupTree.setPath(path.subpath(0,i + 1).toString());
            parent.addChild(groupTree);
            parent = groupTree;
        }
    }
}
