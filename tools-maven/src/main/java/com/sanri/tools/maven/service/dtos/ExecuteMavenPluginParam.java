package com.sanri.tools.maven.service.dtos;

import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ExecuteMavenPluginParam {
    private File pomFile;
    private List<String> goals = new ArrayList<>();
    private boolean skipTest = true;

    public ExecuteMavenPluginParam(File pomFile,String...goals) {
        this.pomFile = pomFile;
        if (ArrayUtils.isNotEmpty(goals)){
            this.goals.addAll(Arrays.asList(goals));
        }
    }
}
