package com.sanri.tools.modules.serializer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/serializer")
public class SerializerController {

    /**
     * 序列化工具列表
     * @return
     */
    @GetMapping("/serializers")
    public List<String> serializers(){

        return null;
    }
}
