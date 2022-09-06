package com.sanri.tools.modules.core.service.file.configfile;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.LinkedHashMap;

public class YamlConfigFile extends ConfigFile {

    public YamlConfigFile(String content) {
        this.content = content;
    }

    public YamlConfigFile() {
    }

    @Override
    public LinkedHashMap read() {
        Yaml yaml = new Yaml();
        LinkedHashMap<String, Object> m = yaml.load(content);
        return m;
    }

    @Override
    public void write(LinkedHashMap map) {
        Yaml yaml = new Yaml();
//        this.content = yaml.dump(map);
        this.content = yaml.dumpAs(map,null, DumperOptions.FlowStyle.BLOCK);
    }
}
