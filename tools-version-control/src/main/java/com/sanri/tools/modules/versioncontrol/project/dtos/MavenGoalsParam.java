package com.sanri.tools.modules.versioncontrol.project.dtos;

import com.sanri.tools.modules.versioncontrol.dtos.ProjectLocation;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class MavenGoalsParam {
    @NotBlank
    private String settingsName;
    @Valid
    private ProjectLocation projectLocation;
    @NotBlank
    private String relativePomFile;
    @NotEmpty
    private List<String> goals = new ArrayList<>();
}
