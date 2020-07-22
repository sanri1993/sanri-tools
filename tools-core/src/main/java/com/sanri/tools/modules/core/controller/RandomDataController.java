package com.sanri.tools.modules.core.controller;

import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.classloader.ExtendClassloader;
import com.sanri.tools.modules.core.service.data.RandomDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/data")
public class RandomDataController {
    @Autowired
    private RandomDataService randomDataService;
    @Autowired
    private ClassloaderService classloaderService;

    /**
     * 随机填充数据
     * @param className
     * @param classloaderName
     * @return
     * @throws ClassNotFoundException
     */
    @GetMapping("/random")
    public Object randomData(String className,String classloaderName) throws ClassNotFoundException {
        ExtendClassloader classloader = classloaderService.getClassloader(classloaderName);
        return randomDataService.randomData(className,classloader);
    }

    /**
     * 随机填充列表数据
     * @param className
     * @param classloaderName
     * @return
     * @throws ClassNotFoundException
     */
    @GetMapping("/random/list")
    public List<Object> randomListData(String className, String classloaderName) throws ClassNotFoundException {
        List<Object> list = new ArrayList<>();
        ExtendClassloader classloader = classloaderService.getClassloader(classloaderName);
        for (int i = 0; i < 10; i++) {
            Object randomData = randomDataService.randomData(className, classloader);
            list.add(randomData);
        }
        return list;
    }
}
