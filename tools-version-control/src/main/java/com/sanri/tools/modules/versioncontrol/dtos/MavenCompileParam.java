package com.sanri.tools.modules.versioncontrol.dtos;

import lombok.Data;

@Data
public class MavenCompileParam {
    private String settings;
    private ChoseCommits choseCommits;
}
