package com.sanri.tools.controller;

import com.sanri.tools.modules.core.exception.BusinessException;
import com.sanri.tools.modules.core.utils.Version;
import com.sanri.tools.service.VersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/version")
@Slf4j
public class VersionController {
   @Autowired
   private VersionService versionService;

   /**
    * 当前版本
    * @return 项目当前版本信息
    */
   @GetMapping
   public String current(){
       return versionService.currentVersion().toString();
   }

   /**
    * 当前版本详细信息
    */
   @GetMapping("/detail")
   public Version detail(){
      return versionService.currentVersion();
   }
}
