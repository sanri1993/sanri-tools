package com.sanri.tools.modules.versioncontrol.git;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sanri.tools.modules.core.utils.OnlyPaths;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.versioncontrol.dtos.ProjectLocation;
import com.sanri.tools.modules.versioncontrol.git.dtos.DiffChanges;
import com.sanri.tools.modules.versioncontrol.git.dtos.DiffChangesTree;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GitDiffService2 {

    @Autowired
    private GitDiffService gitDiffService;
    @Autowired
    private GitRepositoryService gitRepositoryService;

    /**
     * 提交记录转树结构
     * @param projectLocation
     * @param commitIds
     * @return
     * @throws IOException
     */
    public DiffChangesTree.TreeFile parseDiffChangesTree(ProjectLocation projectLocation, List<String> commitIds) throws IOException {
        final DiffChanges diffChanges = gitDiffService.parseDiffChanges(projectLocation.getGroup(), projectLocation.getProjectName(), commitIds);

        final File repositoryDir = gitRepositoryService.loadRepositoryDir(projectLocation.getGroup(), projectLocation.getRepository());
        final OnlyPath repositoryPath = new OnlyPath(repositoryDir);

        // 映射成 TreeFile
        List<DiffChangesTree.TreeFile> treeFiles = new ArrayList<>();
        for (DiffChanges.DiffFile changeFile : diffChanges.getChangeFiles()) {
            final DiffChangesTree.TreeFile treeFile = new DiffChangesTree.TreeFile(changeFile.getRelativeFile().path());
            treeFiles.add(treeFile);
        }

        // 先按模块将变更文件分类 modulePath(相对于仓库路径) => 变更文件列表
        MultiValueMap<OnlyPath, DiffChangesTree.TreeFile> treeFileMultiValueMap = new LinkedMultiValueMap<>();
        List<DiffChangesTree.TreeFile> otherTreeFiles = new ArrayList<>();
        A:for (DiffChangesTree.TreeFile treeFile : treeFiles) {
            final OnlyPath relativePath = treeFile.getRelativePath();
            if ("pom.xml".equals(relativePath.getFileName())){
                // 如果本身修改的就是 pom.xml 文件, 则当前就是一个模块
                final OnlyPath parent = relativePath.getParent();
                if (parent == null){
                    // 如果修改的是父级 pom.xml
                    treeFileMultiValueMap.add(repositoryPath,treeFile);
                }else {
                    treeFileMultiValueMap.add(parent,treeFile);
                }
                continue;
            }

            // 其它情况, 每一个文件都必须一层一层往上找到模块
            final File wholeFilePath = relativePath.resolveFile(repositoryDir);
            File parent = wholeFilePath;
            while (!parent.equals(repositoryDir)){
                if (ArrayUtils.contains(parent.list(),"pom.xml")){
                    // 如果找到了 pom 文件, 当前可标识为一个模块
                    treeFileMultiValueMap.add(repositoryPath.relativize(new OnlyPath(parent)),treeFile);
                    continue A;
                }
                parent = parent.getParentFile();
            }

            // 如果找遍仓库都没找到一个模块, 则归为杂类
            log.warn("未找到文件[{}]的模块归属, 归类为其它",treeFile.getRelativePath());
            otherTreeFiles.add(treeFile);
        }

        // 映射Map, 同时把所有父级模块都查找出来
        Map<OnlyPath, DiffChangesTree.TreeFile> moduleTreeFileMap = new HashMap<>();
        for (OnlyPath onlyPath : treeFileMultiValueMap.keySet()) {
            OnlyPath parent = onlyPath;
            do {
                final DiffChangesTree.TreeFile treeFile = new DiffChangesTree.TreeFile(parent);
                moduleTreeFileMap.put(parent,treeFile);

                parent = parent.getParent();
            }while (parent != null);
        }

        // 生成树状结构, 同时挂载变更文件树, 变更文件树同时生成树状结构
        // 模块排序
        final List<DiffChangesTree.TreeFile> changeModules = new ArrayList<>(moduleTreeFileMap.values());
        Comparator<DiffChangesTree.TreeFile> comparator = (a,b) -> a.getRelativePath().getNameCount() - b.getRelativePath().getNameCount();
        Collections.sort(changeModules,comparator);

        // 转树
        for (DiffChangesTree.TreeFile moduleTreeFile : changeModules) {
            final DiffChangesTree.TreeFile parentModuleTreeFile = moduleTreeFileMap.get(moduleTreeFile.getRelativePath().getParent());
            if (parentModuleTreeFile != null){
                parentModuleTreeFile.getChildren().add(moduleTreeFile);
            }else {
                log.warn("找不到父级模块: {}",moduleTreeFile.getRelativePath());
            }
        }

        // 挂载变更树
        for (DiffChangesTree.TreeFile moduleTreeFile : changeModules) {
            final List<DiffChangesTree.TreeFile> changeFiles = treeFileMultiValueMap.get(moduleTreeFile.getRelativePath());
            if (CollectionUtils.isEmpty(changeFiles)){
                continue;
            }

            List<DiffChangesTree.TreeFile> changeFilesTree = changeFilesToTree(changeFiles,moduleTreeFile);
            moduleTreeFile.getChildren().addAll(changeFilesTree);
        }

        // 如果模块列表中有仓库根路径, 则仓库根路径做为根节点响应
        if (moduleTreeFileMap.containsKey(repositoryPath)){
            return moduleTreeFileMap.get(repositoryPath);
        }

        // 否则, 新建仓库做为根节点, 添加路径最短节点做为二级节点响应
        final DiffChangesTree.TreeFile repositoryTreeFile = new DiffChangesTree.TreeFile(repositoryPath);
        final Set<OnlyPath> onlyPaths = moduleTreeFileMap.keySet();
        final List<OnlyPath> filterShorterPaths = OnlyPaths.filterShorterPaths(onlyPaths);
        for (OnlyPath filterShorterPath : filterShorterPaths) {
            repositoryTreeFile.getChildren().add(moduleTreeFileMap.get(filterShorterPath));
        }
        return repositoryTreeFile;
    }

    /**
     * 变更文件转树结构
     * @param changeFiles
     * @param moduleTreeFile
     * @return
     */
    private List<DiffChangesTree.TreeFile> changeFilesToTree(List<DiffChangesTree.TreeFile> changeFiles, DiffChangesTree.TreeFile moduleTreeFile) {
        final Map<OnlyPath, DiffChangesTree.TreeFile> treeFileMap = changeFiles.stream().collect(Collectors.toMap(DiffChangesTree.TreeFile::getRelativePath, Function.identity()));

        // 新生成的上级数据
        Map<OnlyPath, DiffChangesTree.TreeFile> parentTreeFileMap = new HashMap<>();

        // 变更文件生成树
        for (DiffChangesTree.TreeFile changeTreeFile : changeFiles) {
            final OnlyPath relativePath = changeTreeFile.getRelativePath();
            final OnlyPath parent = relativePath.getParent();
            if (treeFileMap.containsKey(parent)){
                // 如果有父级,则挂载到父级
                treeFileMap.get(parent).getChildren().add(changeTreeFile);
                continue;
            }

            // 否则新建上级, 并挂载到上级
            final DiffChangesTree.TreeFile parentTreeFile = new DiffChangesTree.TreeFile(parent);
            parentTreeFile.getChildren().add(changeTreeFile);

            // 上级的挂载, 在本次处理完成后, 再进行挂载
            parentTreeFileMap.put(parent,parentTreeFile);
        }

        // 新生成的上级挂载, 一直找到模块路径都没有可挂载处, 则直接挂载模块路径
        final ArrayList<DiffChangesTree.TreeFile> parentTreeFiles = new ArrayList<>(parentTreeFileMap.values());
        B: for (DiffChangesTree.TreeFile parentTreeFile : parentTreeFiles) {
            OnlyPath parentPath = parentTreeFile.getRelativePath().getParent();
            do {
                if (parentTreeFileMap.containsKey(parentPath)){
                    parentTreeFileMap.get(parentPath).getChildren().add(parentTreeFile);
                    continue B;
                }
                parentPath = parentPath.getParent();
            }while (!parentPath.equals(moduleTreeFile.getRelativePath()));

            // 未找到挂载处时, 挂载到模块路径
            moduleTreeFile.getChildren().add(parentTreeFile);
        }

        // 过滤出路径最短节点进行响应
        final Set<OnlyPath> parentPaths = parentTreeFileMap.values().stream().map(DiffChangesTree.TreeFile::getRelativePath).collect(Collectors.toSet());
        final List<OnlyPath> filterShorterPaths = OnlyPaths.filterShorterPaths(parentPaths);
        final List<DiffChangesTree.TreeFile> parentTopTreeFiles = filterShorterPaths.stream().map(parentTreeFileMap::get).collect(Collectors.toList());
        return parentTopTreeFiles;
    }
}
