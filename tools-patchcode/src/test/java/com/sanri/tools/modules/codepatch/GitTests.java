package com.sanri.tools.modules.codepatch;

import com.sanri.tools.modules.codepatch.service.GitService;
import com.sanri.tools.modules.codepatch.service.dtos.Branchs;
import com.sanri.tools.modules.codepatch.service.dtos.Commit;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class GitTests {
    @Autowired
    private GitService gitService;

    private static final String group = "sanri";
    private static final String repository = "sanri-tools-maven";

    @Test
    public void testClone() throws GitAPIException, URISyntaxException, IOException {
//        gitService.cloneRepository(group,"http://47.115.15.222:8088/hzr/iot-device-control-server.git");
        gitService.cloneRepository(group,"https://gitee.com/sanri/sanri-tools-maven.git");
    }

    @Test
    public void testGroups() throws GitAPIException, URISyntaxException, IOException {
//        final String[] groups = gitService.groups();
//        System.out.println(StringUtils.join(groups));
    }

    @Test
    public void testrepositorys(){
        System.out.println(StringUtils.join(gitService.repositorys(group)));
    }

    @Test
    public void testBranchs() throws IOException, GitAPIException {
        final List<Branchs.Branch> mycy = gitService.branchs(group, repository);
        for (Branchs.Branch branch : mycy) {
            System.out.println(branch);
        }
    }

    @Test
    public void testCurrentBranch()throws IOException, GitAPIException{
        final String mycy = gitService.currentBranch(group, repository);
        System.out.println(mycy);
    }

    @Test
    public void testListCommits() throws IOException, GitAPIException {
        final List<Commit> commits = gitService.listCommits(group, repository,-1);
        for (Commit commit : commits) {
            System.out.println(commit);
        }
    }

    @Test
    public void testPull() throws IOException, GitAPIException, URISyntaxException {
        gitService.pull(group,repository);
    }

    @Test
    public void testChangeFiles() throws IOException, GitAPIException {
        final List<DiffEntry> diffEntries = gitService.loadChangeFiles(group, repository, "25cfde622100fce5fdd58ed51a084b6a00370db6", "c20a02d06ded8c36c3d23d130f154ddca63ae8f1");
        for (DiffEntry diffEntry : diffEntries) {
            System.out.println(diffEntry);
        }
    }

    @Test
    public void testCreatePatch() throws IOException, GitAPIException {
        final File file = gitService.createPatch(group, repository, "25cfde622100fce5fdd58ed51a084b6a00370db6", "c20a02d06ded8c36c3d23d130f154ddca63ae8f1");
        System.out.println(file);
    }

    @Test
    public void test1(){
        final Path path = Paths.get("a/bc/d/e/pom.xml");
        final Path path1 = Paths.get("a/bc/d/pom.xml");

        System.out.println(path.getParent());
        System.out.println(path.startsWith(path1.getParent()));

        System.out.println(Paths.get("pom.xml").getParent());
        System.out.println(path.getNameCount());
    }
}
