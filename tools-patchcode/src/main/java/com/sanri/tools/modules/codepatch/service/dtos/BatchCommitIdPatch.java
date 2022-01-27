package com.sanri.tools.modules.codepatch.service.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatchCommitIdPatch {
    /**
     * 分组名
     */
    private String group;
    /**
     * 仓库名
     */
    private String repository;
    /**
     * 上一个提交, 放弃, {@link commitIds}
     */
    @Deprecated
    private String commitBeforeId;
    /**
     * 下一个提交 , 放弃, {@link commitIds}
     */
    @Deprecated
    private String commitAfterId;
    /**
     * 提交记录列表
     */
    private List<String> commitIds = new ArrayList<>();
    /**
     * 补丁名称
     */
    private String title;
}
