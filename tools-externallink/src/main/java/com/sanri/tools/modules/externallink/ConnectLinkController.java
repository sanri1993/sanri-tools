package com.sanri.tools.modules.externallink;

import com.sanri.tools.modules.externallink.service.ExternalLinkService;
import com.sanri.tools.modules.externallink.service.dtos.ExternalLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/security/connect")
@Slf4j
public class ConnectLinkController {
    @Autowired
    private ExternalLinkService externalLinkService;

    /**
     * 外链列表
     */
    @GetMapping("/externalLinkRoutes")
    public List<ExternalLink> externalLinkRoutes() throws IOException {
        return externalLinkService.externalLinkRoutes();
    }

    /**
     * 外链登录方法
     * @param request request
     * @param response response
     * @param connName 连接名
     */
    @PostMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response,String connName) throws IOException, ClassNotFoundException {
        externalLinkService.login(request, response, connName);
    }
}
