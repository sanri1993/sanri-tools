package com.sanri.tools.modules.versioncontrol.dtos;

import lombok.Data;

@Data
public class ModuleResolveDepParam {
    private ProjectLocation projectLocation;
    private String relativePomFile;
    private String settings;
}
