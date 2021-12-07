package com.sanri.tools.modules.security.service;

import com.sanri.tools.modules.core.security.entitys.ToolRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitAdmin implements InitializingBean {
    @Autowired
    private UserManagerService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ResourcePermLoad resourcePermLoad;

    @Override
    public void afterPropertiesSet() throws Exception {
        resourcePermLoad.initAdmin();

        if (!userService.existUser("admin")){
            groupService.initAdmin();
            roleService.initAdmin();
            userService.initAdmin();
        }
    }
}
