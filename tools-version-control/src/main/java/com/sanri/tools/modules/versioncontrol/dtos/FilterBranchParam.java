package com.sanri.tools.modules.versioncontrol.dtos;

import lombok.Data;

@Data
public class FilterBranchParam {
    private String group;
    private String repository;
    private String filterValue = "";
}
