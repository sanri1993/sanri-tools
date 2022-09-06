package com.sanri.tools.modules.core.service.file.configfile;

import java.io.IOException;
import java.util.LinkedHashMap;

public abstract class ConfigFile {
    protected String content;

    public abstract LinkedHashMap read() throws Exception;

    public abstract void write(LinkedHashMap map) throws Exception;

    public void setContent(String content){
        this.content = content;
    }

    public String getContent(){
        return content;
    }
}
