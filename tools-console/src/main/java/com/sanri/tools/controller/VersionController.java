package com.sanri.tools.controller;

import com.sanri.tools.service.VersionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;

@RestController
@RequestMapping("/version")
@Slf4j
public class VersionController {
   @Autowired
   VersionService versionService;

   @GetMapping("/current")
   public String current(){
       return versionService.currentVersion();
   }
}
