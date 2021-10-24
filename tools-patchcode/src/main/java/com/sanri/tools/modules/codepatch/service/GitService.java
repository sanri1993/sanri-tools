package com.sanri.tools.modules.codepatch.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.jcraft.jsch.Session;
import com.sanri.tools.modules.codepatch.service.dtos.*;
import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.dtos.param.AuthParam;
import com.sanri.tools.modules.core.dtos.param.GitParam;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.core.utils.URLUtil;
import com.sanri.tools.modules.core.utils.ZipUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GitService {

    @Autowired
    private FileManager fileManager;
    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ConnectService connectService;

    private String baseDirName = "gitrepositorys";

    private static final String module = "git";

    @Autowired
    private WebSocketCompileService webSocketService;

    /**
     * @param group 表示连接管理中的连接名
     * @param url
     * @throws URISyntaxException
     * @throws GitAPIException
     */
    public void cloneRepository(String group,String url) throws URISyntaxException, GitAPIException, IOException {
        final URL repositoryURL = new URL(url);

        final File baseDir = fileManager.mkTmpDir(baseDirName);

        // 获取当前要创建的目录名,并检测是否存在
        final String repositoryName = FilenameUtils.getBaseName(URLUtil.pathLast(url));
        final File repositoryDir = new File(baseDir, group+"/"+repositoryName);
        if (repositoryDir.exists() && !ArrayUtils.isEmpty(repositoryDir.list())){
            log.error("仓库[{}]已经存在",repositoryDir.getAbsolutePath());
            throw new ToolException("仓库"+repositoryName+"已经存在");
        }
        repositoryDir.mkdirs();

        final CloneCommand cloneCommand = Git.cloneRepository().setURI(url).setDirectory(repositoryDir);

        addAuth(group, cloneCommand);
        cloneCommand.call();
    }

    public String[] groups(){
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        return baseDir.list();
    }

    public String[] repositorys(String group){
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        final File file = new File(baseDir, group);
        return file.list();
    }

    /**
     * 加载当前仓库所有的 pom 文件
     * @param group
     * @param repository
     * @return
     */
    public List<PomFile> loadAllPomFile(String group, String repository){
        final File repositoryDir = repositoryDir(group, repository);
        final Collection<File> files = FileUtils.listFiles(repositoryDir, new NameFileFilter("pom.xml"), TrueFileFilter.INSTANCE);
        List<PomFile> pomFiles = new ArrayList<>();
        for (File file : files) {
            final Path relativize = repositoryDir.toPath().relativize(file.toPath());
            final String moduleName = file.getParentFile().getName();
            pomFiles.add(new PomFile(repositoryDir,relativize.toString(),moduleName));
        }
        return pomFiles;
    }

    /**
     * 获取模块信息
     * @param group
     * @param repository
     * @return
     */
    public List<Module> modules(String group,String repository){
        final List<PomFile> pomFiles = loadAllPomFile(group, repository);
        if (CollectionUtils.isNotEmpty(pomFiles)) {
            Collections.sort(pomFiles);

            Function<String,Path> relativeToPath = relativePath -> {
                Path parent = Paths.get(relativePath).getParent();
                if (parent == null){
                    parent = Paths.get("/");
                }
                return parent;
            };

            final List<Path> pathList = pomFiles.stream().map(PomFile::getRelativePath).map(relativeToPath).collect(Collectors.toList());

            // 先映射成模块
            final Map<Path, Module> moduleMap = pomFiles.stream().map(Module::new).collect(Collectors.toMap(cur -> relativeToPath.apply(cur.getRelativePath()), Function.identity()));

            final Iterator<Path> iterator = pathList.iterator();
            while (iterator.hasNext()){
                final Path path = iterator.next();
                final Module findChildModule = moduleMap.get(path);
                for (Path curPath : pathList) {
                    final Module module = moduleMap.get(curPath);
                    final boolean isChildren = curPath.startsWith(path) && curPath != path && Math.abs(curPath.getNameCount() - path.getNameCount()) < 2;
                    if (isChildren){
                        findChildModule.getChildrens().add(module);
                    }
                }
            }

            // 查找顶部元素(同一方向路径最短)
            List<Module> tops = new ArrayList<>();
            Set<Path> routePaths = new HashSet<>();
            A: for (Path parent : pathList) {
                final Iterator<Path> routeIterator = routePaths.iterator();
                while (routeIterator.hasNext()){
                    Path path = routeIterator.next();
                    if (path.startsWith(parent)){
                        routeIterator.remove();
                        routePaths.add(parent);
                        continue A;
                    }
                    if (parent.startsWith(path)){
                        continue A;
                    }
                }
                routePaths.add(parent);
            }
            for (Path routePath : routePaths) {
                final Module topModule = moduleMap.get(routePath);
                tops.add(topModule);
            }

            return tops;
        }
        return new ArrayList<>();
    }


    /**
     * 编译文件
     * @param group
     * @param repository
     * @param pomRelativePath
     * @return 执行编译 mvn 退出码
     */
    public void compile(String websocketId,String group, String repository, String pomRelativePath) throws IOException, InterruptedException {
        final File repositoryDir = repositoryDir(group, repository);
        final File pomFile = repositoryDir.toPath().resolve(pomRelativePath).toFile();
        final Process cleanCompile = RuntimeUtil.exec(pomFile.getParentFile(), "C:\\pathdev\\apache-maven-3.6.3\\bin\\mvn.cmd clean compile");
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(cleanCompile.getInputStream(), StandardCharsets.UTF_8));
        String line = "";
        while ((line = bufferedReader.readLine()) != null){
            System.out.println(line);
            webSocketService.sendMessage(websocketId,line);
        }
        final int waitFor = cleanCompile.waitFor();
        webSocketService.sendMessage(websocketId,waitFor+"");
        RuntimeUtil.destroy(cleanCompile);
    }

    public void pull(String group, String repositoryName) throws IOException, GitAPIException, URISyntaxException {
        Git git = openGit(group, repositoryName);
        final PullCommand pullCommand = git.pull();
        final Collection<Ref> call = git.lsRemote().call();
        addAuth(group,pullCommand);
        final PullResult pullResult = pullCommand.call();
        log.info("拉取数据结果",pullResult);
    }

    public List<Branchs.Branch> branchs(String group, String repositoryName) throws IOException, GitAPIException {
        Git git = openGit(group, repositoryName);
        final List<Ref> call = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

        List<Branchs.Branch> branches = new ArrayList<>();
        for (Ref ref : call) {
            final Branchs.Branch branch = new Branchs.Branch(ref.getName(),ref.getObjectId().name());
            branches.add(branch);
        }
        return branches;
    }

    public String currentBranch(String group,String repositoryName) throws IOException {
        Git git = openGit(group, repositoryName);

        final Repository repository = git.getRepository();
        return repository.getBranch();
    }

    public String switchBranch(String group, String repositoryName, String branchName) throws IOException, GitAPIException, URISyntaxException {
        final Git git = openGit(group, repositoryName);
        //如果分支在本地已存在，直接checkout即可。
        if (this.branchNameExist(git, branchName)) {
            git.checkout().setCreateBranch(false).setName(branchName).call();
            final PullCommand pull = git.pull();
            addAuth(group,pull);
            pull.call();
        } else {
            //如果分支在本地不存在，需要创建这个分支，并追踪到远程分支上面。
            final Path fileName = Paths.get(branchName).getFileName();
            git.checkout().setCreateBranch(true).setName(fileName.toString()).setStartPoint(branchName).call();
        }

        return branchName;
    }

    public List<Commit> listCommits(String group,String repositoryName,int maxCount) throws IOException, GitAPIException {
        final Git git = openGit(group, repositoryName);
        List<Commit> commits = new ArrayList<>();

        final Iterable<RevCommit> revCommits = git.log().setMaxCount(maxCount).call();
        final Iterator<RevCommit> iterator = revCommits.iterator();
        while (iterator.hasNext()){
            final RevCommit revCommit = iterator.next();
            final String shortMessage = revCommit.getShortMessage();
            final String author = revCommit.getAuthorIdent().getName();
            final int commitTime = revCommit.getCommitTime();
            final ObjectId objectId = revCommit.getId();

            final Commit commit = new Commit(shortMessage, author, new String(objectId.name()), new Date(((long)commitTime) * 1000));
            commits.add(commit);
        }
        return commits;
    }

    public List<DiffEntry> loadChangeFiles(String group,String repositoryName,String commitBeforeId,String commitAfterId) throws IOException, GitAPIException {
        final Git git = openGit(group, repositoryName);

        final Repository repository = git.getRepository();
        final RevWalk revWalk = new RevWalk(repository);
        final RevCommit revCommit = revWalk.parseCommit(repository.resolve(commitBeforeId));
        final RevCommit nextCommit = revWalk.parseCommit(repository.resolve(commitAfterId));
        try {
            try (ObjectReader reader = repository.newObjectReader()) {
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, nextCommit.getTree().getId());
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, revCommit.getTree().getId());

                final List<DiffEntry> call = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();
                return call;
            }
        }finally {
            revWalk.dispose();
        }
    }

    Map<String,String> compilePath = new HashMap<>();
    {
        compilePath.put("src/main/java","classes");
        compilePath.put("src/main/resources","classes");
        compilePath.put("src/main/webapp","/");
    }

    /**
     * 创建补丁包
     * @param group
     * @param repositoryName
     * @param commitBeforeId
     * @param commitAfterId
     * @return
     */
    public File createPatch(String group, String repositoryName, String commitBeforeId, String commitAfterId) throws IOException, GitAPIException {
        final List<DiffEntry> diffEntries = loadChangeFiles(group, repositoryName, commitBeforeId, commitAfterId);
        final File repositoryDir = repositoryDir(group, repositoryName);

        List<FileInfo> modifyFileInfos = new ArrayList<>();
        List<FileInfo> deleteFileInfos = new ArrayList<>();
        for (DiffEntry diffEntry : diffEntries) {
            final DiffEntry.ChangeType changeType = diffEntry.getChangeType();
            if (diffEntry.getNewPath().contains("src/test")){
                log.info("测试文件夹跳过:{}",diffEntry);
                continue;
            }

            switch (changeType){
                case MODIFY:
                case ADD:
                case COPY:
                case RENAME:
                    File modifyFile = new File(repositoryDir, diffEntry.getNewPath());
                    if (diffEntry.getNewPath().contains("src/main/java")){
                        File compilePath = findCompilePath(modifyFile, repositoryDir);
                        final File modulePath = compilePath.getParentFile().getParentFile();
                        final Path relativePath = Paths.get("src/main/java").relativize(modulePath.toPath().relativize(modifyFile.toPath()));

                        final String extension = FilenameUtils.getExtension(modifyFile.getName());
                        if ("java".equals(extension)) {
                            // 在编译路径找到对应类, 需要包含内部类
                            final String baseName = FilenameUtils.getBaseName(modifyFile.getName());
                            final AndFileFilter andFileFilter = new AndFileFilter(new WildcardFileFilter(baseName + "*"), new SuffixFileFilter("class"));
                            final Collection<File> files = FileUtils.listFiles(compilePath, andFileFilter, TrueFileFilter.INSTANCE);
                            final Iterator<File> iterator = files.iterator();
                            while (iterator.hasNext()){
                                final File file = iterator.next();
                                if (!(baseName+".class").equals(file.getName()) && !file.getName().contains("$")){
                                    // 去掉找到的错误文件
                                    iterator.remove();
                                }
                            }
                            final FileInfo fileInfo = new FileInfo(diffEntry, relativePath, files);
                            fileInfo.setModulePath(modulePath);
                            modifyFileInfos.add(fileInfo);
                        }else{
                            modifyFileInfos.add(new FileInfo(diffEntry,relativePath,Arrays.asList(modifyFile)));
                        }
                    }else if (diffEntry.getNewPath().contains("src/main/resources")){
                        File compilePath = findCompilePath(modifyFile, repositoryDir);
                        final File modulePath = compilePath.getParentFile().getParentFile();
                        final Path relativePath = Paths.get("src/main/resources").relativize(modulePath.toPath().relativize(modifyFile.toPath()));
                        modifyFileInfos.add(new FileInfo(diffEntry,relativePath,Arrays.asList(modifyFile)));
                    }else if (diffEntry.getNewPath().contains("src/main/webapp")){
                        // 因为大多数项目前端端分离, webapp 不应该在用在分层的项目中
                        final Path relativePath = Paths.get("src/main/webapp").relativize(repositoryDir.toPath().relativize(modifyFile.toPath()));
                        modifyFileInfos.add(new FileInfo(diffEntry,relativePath,Arrays.asList(modifyFile)));
                    }else{
                        final Path relativePath = repositoryDir.toPath().relativize(modifyFile.toPath());
                        modifyFileInfos.add(new FileInfo(diffEntry,relativePath,Arrays.asList(modifyFile)));
                    }
                    break;
                case DELETE:
                    final String replace = diffEntry.getOldPath().replace("src/main/java", "target/classes").replace("src/main/resources", "").replace("src/main/webapp", "");
                    File deleteFile = new File(repositoryDir,replace);
                    final Path relativePath = repositoryDir.toPath().relativize(deleteFile.toPath());
                    deleteFileInfos.add(new FileInfo(diffEntry,relativePath));
                    break;

            }
        }

        // 创建压缩包
        final File patch = fileManager.mkTmpDir("gitpatch/" + System.currentTimeMillis());
        patch.mkdirs();

        // 写入总计信息
        final File allChange = new File(patch, "allchange.txt");
        List<String> allChangeText = new ArrayList<>();
        for (DiffEntry diffEntry : diffEntries) {
            allChangeText.add(diffEntry.toString());
        }
        FileUtils.writeLines(allChange,allChangeText);

        final File modifyFileDir = new File(patch, "modify");

        if (CollectionUtils.isNotEmpty(modifyFileInfos)){
            modifyFileDir.mkdirs();

            for (FileInfo fileInfo : modifyFileInfos) {
                final DiffEntry diffEntry = fileInfo.getDiffEntry();
                final Path relativePath = fileInfo.getRelativePath();
                final File modulePath = fileInfo.getModulePath();
                final Collection<File> compileFiles = fileInfo.getCompileFiles();
                for (File compileFile : compileFiles) {
                    if (!compileFile.exists()){
                        final File parentFile = compileFile.getParentFile();
                        final String baseName = FilenameUtils.getBaseName(compileFile.getName());
                        final String extension = FilenameUtils.getExtension(compileFile.getName());
                        final File target = new File(parentFile, baseName + "丢失" + extension);
                        target.createNewFile();

                        compileFile = target;
                    }

                    Path resolve = relativePath.getParent() == null ? Paths.get(compileFile.getName()) : relativePath.getParent().resolve(compileFile.getName());

                    if (modulePath != null){
                        resolve = modulePath.toPath().getFileName().resolve(resolve);
                    }
                    final File file = modifyFileDir.toPath().resolve(resolve).toFile();
                    FileUtils.copyFile(compileFile,file);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(deleteFileInfos)){
            final File file = new File(patch, "delete.txt");

            List<String> deleteText = new ArrayList<>();
            for (FileInfo fileInfo : deleteFileInfos) {
                deleteText.add(fileInfo.getRelativePath().toString());
            }
            FileUtils.writeLines(file,deleteText);
        }

        final File zip = ZipUtil.zip(patch);

        FileUtils.deleteDirectory(patch);

        return zip;
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

    private Git openGit(String group, String repositoryName) throws IOException {
        final File repositoryDir = repositoryDir(group, repositoryName);
        Git git = Git.open(repositoryDir);
        return git;
    }

    private File repositoryDir(String group, String repositoryName) {
        final File baseDir = fileManager.mkTmpDir(baseDirName);
        final File repositoryDir = new File(baseDir, group + "/" + repositoryName);
        return repositoryDir;
    }

    private void addAuth(String group, TransportCommand transportCommand) throws IOException, URISyntaxException {
        URIish urIish = null;
        if (transportCommand instanceof CloneCommand){
            CloneCommand cloneCommand = (CloneCommand) transportCommand;
            final Object uri = ReflectionUtils.getField(FieldUtils.getDeclaredField(CloneCommand.class, "uri",true), cloneCommand);
            urIish = new URIish(Objects.toString(uri));
        }else{
            final List<RemoteConfig> allRemoteConfigs = RemoteConfig.getAllRemoteConfigs(transportCommand.getRepository().getConfig());
            urIish = allRemoteConfigs.get(0).getURIs().get(0);
        }
        final GitParam gitParam = (GitParam) connectService.readConnParams(module, group);
        if ("git".equals(urIish.getScheme())){
            transportCommand.setTransportConfigCallback(new TransportConfigCallback() {
                @Override
                public void configure(Transport transport) {
                    SshTransport sshTransport = (SshTransport)transport;
                    sshTransport.setSshSessionFactory(sshSessionFactory);
                }
            });
        }else {
            final AuthParam authParam = gitParam.getAuthParam();
            if (authParam != null) {
                final UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(authParam.getUsername(), authParam.getPassword());
                transportCommand.setCredentialsProvider(usernamePasswordCredentialsProvider);
            }
        }
    }

    SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
		@Override
		protected void configure(OpenSshConfig.Host host, Session session) {
			/*
			 * 解除HostKey检查，也就意味着可以接受未知的远程主机的文件，这是不安全的，这种模式只是用于测试为目的的。
			 * 利用ssh-keyscan -t rsa hostname，收集主机数据。
			 */
			session.setConfig("StrictHostKeyChecking", "no");
		}
	};

    private boolean branchNameExist(Git git, String branchName) throws GitAPIException {
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            if (ref.getName().contains(branchName)) {
                return true;
            }
        }
        return false;
    }

    @PostConstruct
    public void register(){
        pluginManager.register(PluginDto.builder().module("docs").name("git").author("sanri").logo("git.jpg").desc("git代码管理").envs("default").build());
    }
}
