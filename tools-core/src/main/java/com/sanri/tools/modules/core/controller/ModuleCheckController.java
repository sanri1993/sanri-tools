package com.sanri.tools.modules.core.controller;

import com.sanri.tools.modules.core.security.UserService;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.core.service.plugin.dtos.EnhancePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 检测是否有某些模块支持
 */
@RestController
public class ModuleCheckController {
    @Autowired(required = false)
    private UserService userService;

    @Autowired
    private PluginManager pluginManager;

    /**
     * 判断当前系统是否添加了权限
     * @return
     */
    @GetMapping("/needSecurity")
    public boolean hasSecurity(){
        return userService != null;
    }

    /**
     * 判断当前系统是否有某个模块
     * @param moduleId 模块标识
     * @return
     */
    @GetMapping("/hasModule")
    public boolean hasModule(String moduleId) throws IOException {
        final List<EnhancePlugin> list = pluginManager.localPlugins();
        for (EnhancePlugin enhancePlugin : list) {
            final String id = enhancePlugin.getPluginRegister().getId();
            if (id.equals(moduleId)){
                return true;
            }
        }
        return false;
    }
}
