package com.sanri.tools.modules.externallink;

import com.sanri.tools.modules.externallink.service.ExternalLinkService;
import com.sanri.tools.modules.externallink.service.dtos.ExternalLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @return
     */
    @GetMapping("/externalLinkRoutes")
    public List<ExternalLink> externalLinkRoutes() throws IOException {
        return externalLinkService.externalLinkRoutes();
    }
}
