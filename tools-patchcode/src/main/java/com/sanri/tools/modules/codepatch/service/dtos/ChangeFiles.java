package com.sanri.tools.modules.codepatch.service.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChangeFiles {
    private List<String> commitIds = new ArrayList<>();
    @JsonIgnore
    private List<FileInfo> modifyFileInfos = new ArrayList<>();
    @JsonIgnore
    private List<FileInfo> deleteFileInfos = new ArrayList<>();

    public ChangeFiles() {
    }

    public ChangeFiles(List<String> commitIds) {
    }

    public ChangeFiles(List<String> commitIds, List<FileInfo> modifyFileInfos, List<FileInfo> deleteFileInfos) {
        this.commitIds = commitIds;
        this.modifyFileInfos = modifyFileInfos;
        this.deleteFileInfos = deleteFileInfos;
    }

    public ChangeFiles(List<FileInfo> modifyFileInfos, List<FileInfo> deleteFileInfos) {
        this.modifyFileInfos = modifyFileInfos;
        this.deleteFileInfos = deleteFileInfos;
    }

    /**
     * 获取原始变更文件列表
     * @return
     */
    public List<ChangeFile> getChangeFiles(){
        final ArrayList<FileInfo> fileInfos = new ArrayList<>(modifyFileInfos);
        fileInfos.addAll(deleteFileInfos);

        return fileInfos.stream().map(modifyFileInfo ->
                new ChangeFile(modifyFileInfo.getDiffEntry().getChangeType(),
                        modifyFileInfo.getRelativePath().toString(),
                        modifyFileInfo.getCompileFiles().stream().map(File::toPath).map(Path::toString).collect(Collectors.toList())
                        )).collect(Collectors.toList());
    }

    public List<String> getCommitIds() {
        return commitIds;
    }

    @Data
    public static final class ChangeFile{
        private DiffEntry.ChangeType changeType;
        private String relativePath;
        private List<String> compilePaths = new ArrayList<>();

        public ChangeFile() {
        }

        public ChangeFile(DiffEntry.ChangeType changeType, String relativePath, List<String> compilePaths) {
            this.changeType = changeType;
            this.relativePath = relativePath;
            this.compilePaths = compilePaths;
        }
    }

    public List<FileInfo> getDeleteFileInfos() {
        return deleteFileInfos;
    }

    public List<FileInfo> getModifyFileInfos() {
        return modifyFileInfos;
    }

    public void setCommitIds(List<String> commitIds) {
        this.commitIds = commitIds;
    }
}
