package com.sanri.tools.modules.versioncontrol.git;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.versioncontrol.dtos.CommitFilterParam;
import com.sanri.tools.modules.versioncontrol.git.dtos.Commit;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.*;
import org.eclipse.jgit.transport.RefFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.versioncontrol.git.dtos.Branches;

import lombok.extern.slf4j.Slf4j;

/**
 * GIT 分支服务,这个类都是在本地仓库工作 <br/>
 * <ul>
 *     <li>检查分支是否存在</li>
 *     <li>切换分支</li>
 *     <li>当前分支</li>
 *     <li>获取提交记录列表</li>
 * </ul>
 *
 */
@Service
@Slf4j
public class GitBranchService {
    @Autowired
    private GitRepositoryService gitRepositoryService;
    @Autowired
    private GitSecurityService gitSecurityService;
    @Autowired
    private RepositoryMetaService projectMetaService;

    /**
     * 获取分支列表
     * @param group
     * @param repository
     * @return
     */
    public List<Branches.Branch> branches(String group, String repository) throws IOException, GitAPIException {
        final Git git = gitRepositoryService.openGit(group, repository);
        final List<Ref> call = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

        List<Branches.Branch> branches = new ArrayList<>();
        for (Ref ref : call) {
            final Branches.Branch branch = new Branches.Branch(ref.getName(), ref.getObjectId().name());
            branches.add(branch);
        }
        return branches;
    }

    /**
     * 当前分支
     * @param group
     * @param repositoryName
     * @return
     * @throws IOException
     */
    public String currentBranch(String group,String repositoryName) throws IOException {
        Git git = gitRepositoryService.openGit(group, repositoryName);

        final Repository repository = git.getRepository();
        return repository.getBranch();
    }

    /**
     * 切换分支
     * @param group
     * @param repositoryName
     * @param branchName
     * @return
     * @throws IOException
     * @throws GitAPIException
     * @throws URISyntaxException
     */
    public String switchBranch(String group, String repositoryName, String branchName) throws IOException, GitAPIException, URISyntaxException {
        // 多人操作时, 切换分支需要加锁
        projectMetaService.lock(group,repositoryName);

        final Git git = gitRepositoryService.openGit(group, repositoryName);
        //如果分支在本地已存在，直接checkout即可。
        if (branchNameExist(git, branchName)) {
            git.checkout().setCreateBranch(false).setName(branchName).call();
            final PullCommand pull = git.pull();
            gitSecurityService.addAuth(new GitSecurityService.AuthDto(pull,group));
            pull.call();
        } else {
            //如果分支在本地不存在，需要创建这个分支，并追踪到远程分支上面。
            final String fileName = new OnlyPath(branchName).getFileName();
            git.checkout().setCreateBranch(true).setName(fileName).setStartPoint(branchName).call();
        }

        return branchName;
    }

    /**
     * 判断分支名是否存在
     * @param git
     * @param branchName
     * @return
     * @throws GitAPIException
     */
    public boolean branchNameExist(Git git, String branchName) throws GitAPIException {
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
            if (ref.getName().contains(branchName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤查询出提交记录列表
     * @param group
     * @param repository
     * @param commitFilterParam
     * @return
     */
    public List<Commit> filterCommits(String group, String repository, CommitFilterParam commitFilterParam) throws IOException, GitAPIException {
        // 仓库访问, 确保下次直接访问这个仓库
        projectMetaService.visitRepository(group,repository);

        List<RevFilter> filters = new ArrayList<>();

        final Date begin = commitFilterParam.getBegin();
        if (begin != null) {
            final RevFilter after = CommitTimeRevFilter.after(begin);
            filters.add(after);
        }
        final Date end = commitFilterParam.getEnd();
        if (end != null){
            final RevFilter before = CommitTimeRevFilter.before(end);
            filters.add(before);
        }
        if (ArrayUtils.isNotEmpty(commitFilterParam.getAuthors())){
            List<RevFilter> authorFilters = new ArrayList<>();
            for (String author : commitFilterParam.getAuthors()) {
                RevFilter revFilter = AuthorRevFilter.create(author);
                authorFilters.add(revFilter);
            }
            final RevFilter authorFilter =authorFilters.size() == 1 ? authorFilters.get(0) :  OrRevFilter.create(authorFilters);
            filters.add(authorFilter);
        }
        filters.add(MaxCountRevFilter.create(commitFilterParam.getMaxCount()));
        if (!commitFilterParam.getMerge()){
            // 如果不需要合并, 去掉合并项
            filters.add(RevFilter.NO_MERGES);
        }
        final RevFilter allFilter = filters.size() == 1 ? filters.get(0) : AndRevFilter.create(filters);

        final Git git = gitRepositoryService.openGit(group, repository);
        List<Commit> commits = new ArrayList<>();

        final Iterable<RevCommit> revCommits = git.log().setRevFilter(allFilter).call();
        return mapCommits(revCommits);
    }

    /**
     * 提交记录列表
     * @param group
     * @param repository
     * @param maxCount
     * @return
     * @throws IOException
     */
    public List<Commit> commits(String group, String repository, int maxCount) throws IOException, GitAPIException {
        // 仓库访问, 确保下次直接访问这个仓库
        projectMetaService.visitRepository(group,repository);

        final Git git = gitRepositoryService.openGit(group, repository);

        final Iterable<RevCommit> revCommits = git.log().setMaxCount(maxCount).call();
        return mapCommits(revCommits);
    }

    /**
     * 所有作者查询
     * @return
     */
    public Set<String> authors(String group, String repository) throws IOException, GitAPIException {
        final Git git = gitRepositoryService.openGit(group, repository);

        Set<String> authors = new HashSet<>();
        final Iterable<RevCommit> call = git.log().all().call();
        final Iterator<RevCommit> iterator = call.iterator();
        while (iterator.hasNext()){
            authors.add(iterator.next().getAuthorIdent().getName());
        }
        return authors;
    }

    public List<Commit> mapCommits(Iterable<RevCommit> revCommits) {
        List<Commit> commits = new ArrayList<>();
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
}
