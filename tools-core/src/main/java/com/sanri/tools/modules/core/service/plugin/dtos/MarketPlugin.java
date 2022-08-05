package com.sanri.tools.modules.core.service.plugin.dtos;

import com.sanri.tools.modules.core.utils.Version;
import lombok.Data;

@Data
public class MarketPlugin {
    private String id;
    private String name;
    private String author;
    private String desc;
    private String downloadAddress;
    private Version version;

    public MarketPlugin() {
    }

    public MarketPlugin(String id, String name, String author, String desc, String downloadAddress, Version version) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.desc = desc;
        this.downloadAddress = downloadAddress;
        this.version = version;
    }
}
