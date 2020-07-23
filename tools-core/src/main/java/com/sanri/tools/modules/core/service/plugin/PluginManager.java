package com.sanri.tools.modules.core.service.plugin;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PluginManager {
    private List<PluginDto> pluginDtos = new ArrayList<>();

    /**
     * 注册插件
     * @param pluginDto
     */
    public void register(PluginDto pluginDto){
        log.info("注册插件[{}]",pluginDto);
        pluginDtos.add(pluginDto);
    }
}
