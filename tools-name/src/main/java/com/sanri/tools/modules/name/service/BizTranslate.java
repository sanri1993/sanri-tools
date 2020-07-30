package com.sanri.tools.modules.name.service;

import com.sanri.tools.modules.core.service.file.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class BizTranslate implements Translate {
    @Autowired
    private FileManager fileManager;

    public static final String module = "translate";

    @Override
    public void doTranslate(TranslateCharSequence translateCharSequence) {

    }

    @Override
    public String getName() {
        return null;
    }

    public List<String> bizs() {
        return fileManager.simpleConfigNames(module);
    }

    public List<String> mirrors(String biz) throws IOException {
        String content = fileManager.readConfig(module, biz);
        return Arrays.asList(StringUtils.split(content,'\n'));
    }

    public void writeMirror(String biz, String content) throws IOException {
        fileManager.writeConfig(module,biz,content);
    }
}
