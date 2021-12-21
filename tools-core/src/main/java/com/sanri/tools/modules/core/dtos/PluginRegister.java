package com.sanri.tools.modules.core.dtos;

import com.sanri.tools.modules.core.utils.Version;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PluginRegister {
    private String id;
    private String name;
    private String author;
    private String desc;
    private List<String> dependencies = new ArrayList<>();
    private String help;
    private Version version;

    public PluginRegister() {
    }

    public PluginRegister(String id) {
        this.id = id;
    }

    public String getVersionString(){
        return version.toString();
    }
}
