package com.sanri.tools.modules.versioncontrol.dtos;

import lombok.Data;

@Data
public class ModuleLocation {
    private ProjectLocation projectLocation;
    private String relativePath;
}
