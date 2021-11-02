package com.sanri.tools.modules.codepatch.controller;

import com.sanri.tools.modules.codepatch.controller.dtos.GroupRepository;
import com.sanri.tools.modules.codepatch.service.GitService;
import com.sanri.tools.modules.codepatch.service.dtos.*;
import com.sanri.tools.modules.core.service.file.FileManager;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/git")
public class GitController {
    @Autowired
    private GitService gitService;
    @Autowired
    private FileManager fileManager;

    @GetMapping("/cloneRepository")
    public void cloneRepository(String group,String url) throws IOException, GitAPIException, URISyntaxException {
        gitService.cloneRepository(group,url);
    }

    @GetMapping("/groups")
    public List<String> groups(){
        return gitService.groups();
    }

    @GetMapping("/poms")
    public List<PomFile> poms(String group, String repository){
        return gitService.loadAllPomFile(group,repository);
    }

    @GetMapping("/modules")
    public List<Module> modules(String group, String repository) throws IOException {
        return gitService.modules(group, repository);
    }

//    @GetMapping("/compile")
//    public void compile(HttpServletRequest request,String websocketId,String group, String repository, String pomRelativePath) throws IOException, InterruptedException {
//        gitService.compile(request.getRemoteAddr(),websocketId,group,repository,pomRelativePath);
//    }

    @GetMapping("/repositorys")
    public String [] repositorys(String group){
        return gitService.repositorys(group);
    }

    /**
     * 分组和仓库列表, 这个是 groups 和 repositorys 接口的合并
     */
    @GetMapping("/compose/groupAndRepo")
    public List<GroupRepository> groupRepositorys(){
        List<GroupRepository> groupRepositories = new ArrayList<>();

        final List<String> groups = gitService.groups();
        for (String group : groups) {
            final String[] repositorys = gitService.repositorys(group);
            if (repositorys != null) {
                groupRepositories.add(new GroupRepository(group, Arrays.asList(repositorys)));
            }
        }
        return groupRepositories;
    }

    @GetMapping("/branchs")
    public Branchs branchs(String group,String repository) throws IOException, GitAPIException {
        final List<Branchs.Branch> branchs = gitService.branchs(group, repository);
        final String currentBranch = gitService.currentBranch(group, repository);
        return new Branchs(branchs, currentBranch);
    }

    @GetMapping("/switchBranch")
    public String switchBranch(String group, String repository, String branchName) throws IOException, GitAPIException, URISyntaxException {
        return gitService.switchBranch(group, repository, branchName);
    }

    @GetMapping("/pull")
    public void pull(String group, String repository) throws GitAPIException, IOException, URISyntaxException {
        gitService.pull(group,repository);
    }

    @GetMapping("/commits")
    public List<Commit> commits(String group, String repository) throws IOException, GitAPIException {
        return gitService.listCommits(group,repository,1000);
    }

    @PostMapping("/v2/changeFiles")
    public ChangeFiles changeFilesV2(@RequestBody BatchCommitIdPatch batchCommitIdPatch) throws IOException, GitAPIException {
        final String group = batchCommitIdPatch.getGroup();
        final String repository = batchCommitIdPatch.getRepository();
        return gitService.createPatch(group,repository,batchCommitIdPatch.getCommitIds());
    }

    @PostMapping("/changeFiles")
    public ChangeFiles changeFiles(@RequestBody BatchCommitIdPatch commitIdPatch) throws IOException, GitAPIException {
        final String group = commitIdPatch.getGroup();
        final String repository = commitIdPatch.getRepository();
        final ChangeFiles changeFiles = gitService.createPatch(group, repository, commitIdPatch.getCommitBeforeId(), commitIdPatch.getCommitAfterId());
        return changeFiles;
    }

    @PostMapping("/createPatch")
    public String createPatch(@RequestBody BatchCommitIdPatch commitIdPatch) throws IOException, GitAPIException {
        final String group = commitIdPatch.getGroup();
        final String repository = commitIdPatch.getRepository();
        final ChangeFiles changeFiles = gitService.createPatch(group, repository, commitIdPatch.getCommitBeforeId(), commitIdPatch.getCommitAfterId());
        final File compileFile = gitService.findCompileFiles(group, repository, changeFiles);
        final Path path = fileManager.relativePath(compileFile.toPath());

        return path.toString();
    }

    @PostMapping("/v2/createPatch")
    public String createPatchV2(@RequestBody BatchCommitIdPatch batchCommitIdPatch) throws IOException, GitAPIException {
        final String group = batchCommitIdPatch.getGroup();
        final String repository = batchCommitIdPatch.getRepository();
        final ChangeFiles changeFiles = gitService.createPatch(group, repository, batchCommitIdPatch.getCommitIds());
        final File compileFile = gitService.findCompileFiles(group, repository, changeFiles);
        final Path path = fileManager.relativePath(compileFile.toPath());

        return path.toString();
    }

    @GetMapping("/lock")
    public void lock(HttpServletRequest request,String group, String repository) throws IOException {
        gitService.lock(request.getRemoteAddr(),group,repository);
    }

    @GetMapping("/unLock")
    public void unLock(String group, String repository,String force) throws IOException {
        gitService.unLock(group,repository,Boolean.parseBoolean(force));
    }

    @GetMapping("/newCompileTime")
    public long newCompileTime(String group, String repository) throws IOException {
        return gitService.newCompileTime(group, repository);
    }
}
