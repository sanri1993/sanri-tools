package com.sanri.tools.maven.service.dtos;

import lombok.Data;
import org.eclipse.aether.artifact.Artifact;

import java.util.ArrayList;
import java.util.List;

@Data
public class DependencyTree {
    private Artifact artifact;
    private List<DependencyTree> children = new ArrayList<>();

    public DependencyTree(Artifact artifact) {
        this.artifact = artifact;
    }

    /**
     * 坐标信息
     * @return
     */
    public String getCoords(){
        if (artifact == null){
            return null;
        }
        return artifact.getGroupId() + ":"+artifact.getArtifactId() + ":"+artifact.getVersion();
    }

    public String getCoordsKey(){
        if (getCoords() == null){
            return getCoords();
        }
        return getCoords() + Math.random();
    }
}
