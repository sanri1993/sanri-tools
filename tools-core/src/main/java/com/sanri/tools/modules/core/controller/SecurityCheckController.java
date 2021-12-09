package com.sanri.tools.modules.core.controller;

import com.sanri.tools.modules.core.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security")
public class SecurityCheckController {
    @Autowired(required = false)
    private UserService userService;

    /**
     * 判断当前系统是否添加了权限
     * @return
     */
    @GetMapping("/hasSecurity")
    public boolean hasSecurity(){
        return userService != null;
    }
}
