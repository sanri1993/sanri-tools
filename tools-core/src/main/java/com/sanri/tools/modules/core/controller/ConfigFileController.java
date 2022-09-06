package com.sanri.tools.modules.core.controller;

import com.sanri.tools.modules.core.service.file.configfile.*;
import com.sanri.tools.modules.core.validation.custom.EnumStringValue;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置文件功能
 * @author sanri
 */
@RestController
@RequestMapping("/configFile")
@Validated
public class ConfigFileController {

    /**
     * 配置文件互相转换
     * @param source
     * @param target
     * @param sourceContent
     * @return
     */
    @PostMapping("/convert/{source}/{target}")
    public String convertConfig(@PathVariable("source") @NotBlank @EnumStringValue({"xml","json","yaml","properties"}) String source,
                                @PathVariable("target") @NotBlank @EnumStringValue({"xml","json","yaml","properties"}) String target,
                                @RequestBody String sourceContent) throws Exception {
        ConfigFile sourceConfigFile = map.get(source).newInstance();
        sourceConfigFile.setContent(sourceContent);

        ConfigFile targetConfigFile = map.get(target).newInstance();

        targetConfigFile.write(sourceConfigFile.read());

        return targetConfigFile.getContent();
    }

    static final Map<String,Class<? extends ConfigFile>> map = new HashMap<>();
    static {
        map.put("json", JsonConfigFile.class);
        map.put("xml", XmlConfigFile.class);
        map.put("yaml", YamlConfigFile.class);
        map.put("properties", PropertiesConfigFile.class);
    }
}
