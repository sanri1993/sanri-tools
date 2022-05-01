package jgittest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.junit.Test;

import com.sanri.tools.modules.core.utils.URLUtil;

public class JGitMain {

    @Test
    public void test1() throws IOException {
        final File repositoryDir = new File("d:/test/sanri-tools-maven/.git");

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Repository repository = new RepositoryBuilder().setGitDir(repositoryDir).build();
        try (RevWalk walk = new RevWalk(repository)) {
            Ref head = repository.findRef("HEAD");
            walk.markStart(walk.parseCommit(head.getObjectId())); // 从HEAD开始遍历，
            for (RevCommit commit : walk) {
                RevTree tree = commit.getTree();

                TreeWalk treeWalk = new TreeWalk(repository, repository.newObjectReader());
                PathFilter f = PathFilter.create("pom.xml");
                treeWalk.setFilter(f);
                treeWalk.reset(tree);
                treeWalk.setRecursive(false);
                while (treeWalk.next()) {
                    PersonIdent authoIdent = commit.getAuthorIdent();
                    System.out.println("提交人： " + authoIdent.getName() + " <" + authoIdent.getEmailAddress() + ">");
                    System.out.println("提交SHA1： " + commit.getId().name());
                    System.out.println("提交信息： " + commit.getShortMessage());
                    System.out.println("提交时间： " + format.format(authoIdent.getWhen()));

//                    ObjectId objectId = treeWalk.getObjectId(0);
//                    ObjectLoader loader = repository.open(objectId);
//                    loader.copyTo(System.out);              //提取blob对象的内容
                }
            }
        }
    }

    @Test
    public void test2() throws MalformedURLException, URISyntaxException {
        final URL url = new URL("https://gitee.com/sanri/sanri-tools-maven.git");
        final String file = url.getFile();
        final String s = URLUtil.pathLast(url.toString());
        final String baseName = FilenameUtils.getBaseName(s);
        System.out.println(baseName);
    }

    @Test
    public void test31() throws IOException, GitAPIException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        git.branchCreate().setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).setName("newBranch").call();
    }
    /**
     * 分支列表
     * @throws IOException
     * @throws GitAPIException
     */
    @Test
    public void test3() throws IOException, GitAPIException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);
        // 本地分支
        final List<Ref> call = git.branchList().call();
        for (Ref ref : call) {
            System.out.println(ref.getName()+":"+ref.getObjectId()+":"+ref.getStorage());
        }
        // + 远程分支
        System.out.println("=====================================");
        final List<Ref> call1 = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (Ref ref : call1) {
            System.out.println(ref.getName()+":"+ref.getObjectId()+":"+ref.getStorage());
        }
    }

    /**
     * 当前分支
     */
    @Test
    public void test4() throws IOException {
        final File repositoryDir = new File("D:\\currentproject\\sanri-tools-maven");
        Git git = Git.open(repositoryDir);
        final String branch = git.getRepository().getBranch();
        System.out.println(branch);
    }

    /**
     * 切换分支(包含切换本地分支和切换远程分支两种)
     * https://my.oschina.net/u/4400687/blog/3660254
     */
    @Test
    public void test5() throws IOException, GitAPIException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);
        final String branch = git.getRepository().getBranch();
        System.out.println("当前分支名:"+branch);

        // 切换分支(本地)
        checkoutAndPull(git,"master");
        System.out.println("当前分支名:"+git.getRepository().getBranch());
    }

    /**
     * 获取提交记录(所有数据)
     */
    @Test
    public void test6() throws IOException, GitAPIException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        final Iterable<RevCommit> call = git.log().call();
        for (RevCommit revCommit : call) {
            System.out.println(revCommit.getCommitTime()+":"+revCommit.getShortMessage()+":"+revCommit.getCommitterIdent().getName()+":"+revCommit.getId());
        }
    }

    /**
     * 获取某次提交记录提交的文件
     */
    @Test
    public void test7() throws IOException, GitAPIException {
        String commitId = "80a809cb934f19429742f8bd52fcf9790d128cb8";
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        final Repository repository = git.getRepository();
        final ObjectId commitObjectId = repository.resolve(commitId);
        final RevWalk revWalk = new RevWalk(repository);
        final RevCommit revCommit = revWalk.parseCommit(commitObjectId);
        revWalk.markStart(revCommit);
        final RevCommit selfCommit = revWalk.next();
        final RevCommit nextCommit = revWalk.next();
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, nextCommit.getTree().getId());
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, revCommit.getTree().getId());

            final List<DiffEntry> call = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
            for (DiffEntry diffEntry : call) {
                System.out.println(diffEntry);
            }
        }
        revWalk.dispose();
    }

    /**
     * 获取某两次提交之间的修改文件
     */
    @Test
    public void test8() throws IOException, GitAPIException {
        String commitBeforeId = "1ddc6c071136d3980e51933d1791fd1511512c98";
        String commitAfterId = "05756d9e12eaa3c54a0edacdfb2ffcba450aa02f";
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        final Repository repository = git.getRepository();
        final RevWalk revWalk = new RevWalk(repository);
        final RevCommit revCommit = revWalk.parseCommit(repository.resolve(commitBeforeId));
        final RevCommit nextCommit = revWalk.parseCommit(repository.resolve(commitAfterId));
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, nextCommit.getTree().getId());
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, revCommit.getTree().getId());

            final List<DiffEntry> call = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
            for (DiffEntry diffEntry : call) {
                System.out.println(diffEntry);
            }
        }
        revWalk.dispose();
    }

//    /**
//     * 根据 diffEntry 打包编译好的文件
//     */
//    @Test
//    public void test9() throws IOException, GitAPIException {
//        String commitBeforeId = "80a809cb934f19429742f8bd52fcf9790d128cb8";
//        String commitAfterId = "85aec9d28be8366a91a2c1ec7b74fdf30bc4776b";
//        String dir = "D:\\test\\sanri-tools-maven";
//        final File repositoryDir = new File(dir);
//        Git git = Git.open(repositoryDir);
//
//        List<FileInfo> fileInfos = new ArrayList<>();
//
//        final Repository repository = git.getRepository();
//        final RevWalk revWalk = new RevWalk(repository);
//        final RevCommit revCommit = revWalk.parseCommit(repository.resolve(commitBeforeId));
//        final RevCommit nextCommit = revWalk.parseCommit(repository.resolve(commitAfterId));
//        try (ObjectReader reader = repository.newObjectReader()) {
//            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
//            oldTreeIter.reset(reader, nextCommit.getTree().getId());
//            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
//            newTreeIter.reset(reader, revCommit.getTree().getId());
//
//            final List<DiffEntry> diffEntries = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
//            for (DiffEntry diffEntry : diffEntries) {
//                List<FileInfo> currentChangeFiles = new ArrayList<>();
//
//                final DiffEntry.ChangeType changeType = diffEntry.getChangeType();
//                final String newPath = diffEntry.getNewPath();
//                final File file = new File(dir, newPath);
//                final String baseName = FilenameUtils.getBaseName(file.getName());
//                final String extension = FilenameUtils.getExtension(file.getName());
//                if ("java".equalsIgnoreCase(extension)) {
//                    // java 文件才需要去编译路径找,其它直接使用本文件
//                    File compilePath = findCompilePath(file, repositoryDir);
//                    // 在编译路径找到对应类, 需要包含内部类
//                    final WildcardFileFilter wildcardFileFilter = new WildcardFileFilter(baseName + "*");
//                    final SuffixFileFilter suffixFileFilter = new SuffixFileFilter("class");
//                    final AndFileFilter andFileFilter = new AndFileFilter(wildcardFileFilter, suffixFileFilter);
//                    final Collection<File> files = FileUtils.listFiles(compilePath, andFileFilter, TrueFileFilter.INSTANCE);
//
//                    final List<FileInfo> collect = files.stream().map(cur -> new FileInfo(cur, file,newPath)).collect(Collectors.toList());
//                    currentChangeFiles.addAll(collect);
//                }else{
//                    currentChangeFiles.add(new FileInfo(file,file,newPath));
//                }
//
//                switch (changeType){
//                    case ADD:
//                    case MODIFY:
//                        for (FileInfo currentChangeFile : currentChangeFiles) {
//                            currentChangeFile.setChangeType(FileInfo.ChangeType.MODIFY);
//                        }
//                        break;
//                    case DELETE:
//                        for (FileInfo currentChangeFile : currentChangeFiles) {
//                            currentChangeFile.setChangeType(FileInfo.ChangeType.DELETE);
//                        }
//
//                }
//
//                fileInfos.addAll(currentChangeFiles);
//            }
//
//            for (FileInfo fileInfo : fileInfos) {
//                System.out.println(fileInfo);
//            }
//        }
//        revWalk.dispose();
//        git.close();
//    }

    /**
     * TreeWalk
     */
    @Test
    public void test10() throws IOException, GitAPIException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        final Repository repository = git.getRepository();
        final RevWalk revWalk = new RevWalk(repository);

        Ref head = repository.findRef("HEAD");
        revWalk.markStart(revWalk.parseCommit(head.getObjectId()));
        final RevCommit revCommit = revWalk.next();
        final RevTree tree = revCommit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository, repository.newObjectReader());
        treeWalk.reset(tree);
        while (treeWalk.next()){
            System.out.println(treeWalk);
        }

        revWalk.dispose();
        git.close();
    }

    @Test
    public void test11() throws IOException, GitAPIException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        final PullCommand pull = git.pull();

        final List<RemoteConfig> call = git.remoteList().call();
        for (RemoteConfig remoteConfig : call) {
            final List<URIish> urIs = remoteConfig.getURIs();
            final List<RefSpec> fetchRefSpecs = remoteConfig.getFetchRefSpecs();
            for (RefSpec fetchRefSpec : fetchRefSpecs) {
                final String destination = fetchRefSpec.getDestination();
                System.out.println(destination);
            }
        }
    }

    /**
     * 测试下一个提交
     * @throws IOException
     */
    @Test
    public void test12() throws IOException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        final Repository repository = git.getRepository();
        final RevWalk revWalk = new RevWalk(repository);

        final RevCommit revCommit = repository.parseCommit(repository.resolve("9b3a4953f9938f9f63c013cbd43e98327947cac8"));
        revWalk.markStart(revCommit);
        revWalk.next();
        final RevCommit nextCommit = revWalk.next();
        System.out.println(nextCommit.getShortMessage());
    }

    /**
     * 测试初始化提交
     */
    @Test
    public void test13() throws IOException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        final Repository repository = git.getRepository();
        try(TreeWalk treeWalk = new TreeWalk(repository)){
            final RevCommit revCommit = repository.parseCommit(repository.resolve("cfc6a1b4100416428d90ac83872f3a521af290e7"));
            treeWalk.reset(revCommit.getTree().getId());
            treeWalk.setRecursive(true);
            while (treeWalk.next()){
                final String path = treeWalk.getPathString();
                final FileMode fileMode = treeWalk.getFileMode();
                System.out.println(fileMode+" "+path);
            }
        }
    }

    /**
     * 提交记录的差异, 多个提交 >2
     */
    @Test
    public void test14() throws IOException {
        String dir = "D:\\test\\sanri-tools-maven";
        final File repositoryDir = new File(dir);
        Git git = Git.open(repositoryDir);

        List<String> commitIds = Arrays.asList("4c70183e86b5fb8e927ada1123a934043a2eb52a","80a809cb934f19429742f8bd52fcf9790d128cb8");

        final Repository repository = git.getRepository();
        final RevWalk revWalk = new RevWalk(repository);

        List<List<DiffEntry>> allDiffEntries = new ArrayList<>();
        try(TreeWalk treeWalk = new TreeWalk(repository)){
            for (String commitId : commitIds) {
                final RevCommit revCommit = repository.parseCommit(repository.resolve(commitId));
                treeWalk.addTree(revCommit.getTree());
            }

            final List<DiffEntry> scan = DiffEntry.scan(treeWalk);
            System.out.println(scan);
        }
    }

    /**
     * 根据文件,一级一级往上找, 直到找到 target 目录
     * @param file
     * @return
     */
    private File findCompilePath(File file,File stop){
        String compilePath = "target/classes";
        if ("pom.xml".equals(file)){
            return new File(file.getParent(),compilePath);
        }
        File parent = file;
        while (!parent.equals(stop) && !ArrayUtils.contains(parent.list(),"target")){
            parent = parent.getParentFile();
        }
        if (parent.equals(stop)){
            if (ArrayUtils.contains(parent.list(),"target")){
                return new File(parent,compilePath);
            }
            final Path resolve = stop.toPath().relativize(file.toPath());
            throw new IllegalStateException("是否还未编译, 没有找到 target 目录,在文件:"+resolve);
        }
        return new File(parent,compilePath);
    }

    public void checkoutAndPull(Git git, String branchName) throws GitAPIException {
        try {
            if (this.branchNameExist(git, branchName)) {//如果分支在本地已存在，直接checkout即可。
                git.checkout().setCreateBranch(false).setName(branchName).call();
                git.pull().call();//拉取最新的提交
            } else {//如果分支在本地不存在，需要创建这个分支，并追踪到远程分支上面。
                git.checkout().setCreateBranch(true).setName(branchName).setStartPoint("origin/" + branchName).call();
            }

        } finally {
            git.close();
        }
    }

    public boolean branchNameExist(Git git, String branchName) throws GitAPIException {
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            if (ref.getName().contains(branchName)) {
                return true;
            }
        }
        return false;
    }
}
