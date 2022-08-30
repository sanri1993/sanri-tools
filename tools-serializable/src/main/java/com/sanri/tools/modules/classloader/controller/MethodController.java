package com.sanri.tools.modules.classloader.controller;

import com.sanri.tools.modules.classloader.MethodService;
import com.sanri.tools.modules.classloader.dtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/method")
public class MethodController {

    @Autowired
    private MethodService methodService;

    /**
     * 获取某个类的所有方法名
     * @param classloaderName 类加载器名称
     * @param className 类名称
     * @return
     */
    @GetMapping("/{classloaderName}/{className}/methodNames")
    public Set<String> methodNames(@PathVariable("classloaderName") String classloaderName, @PathVariable("className") String className) throws ClassNotFoundException {
        return methodService.listClassMethodNames(classloaderName, className);
    }

    /**
     * 获取方法列表
     * @param classloaderName
     * @param className
     * @return
     */
    @GetMapping("/{classloaderName}/{className}/methods")
    public List<ClassMethodSignature> methods(@PathVariable("classloaderName") String classloaderName, @PathVariable("className") String className) throws ClassNotFoundException {
        return methodService.listClassMethods(classloaderName, className);
    }

    /**
     * 获取方法信息
     * @param methodReq
     * @return
     */
    @PostMapping("/methodInfo")
    public ClassMethodInfo methodInfo(@RequestBody MethodReq methodReq) throws NoSuchMethodException, ClassNotFoundException {
        return methodService.methodInfo(methodReq.getClassloaderName(),methodReq.getClassName(),methodReq.getMethodSignature());
    }

    /**
     * 模拟方法参数
     * @param methodReq
     * @return
     */
    @PostMapping("/mockParams")
    public List<String> mockParams(@RequestBody MethodReq methodReq) throws NoSuchMethodException, ClassNotFoundException {
        return methodService.mockMethodParams(methodReq.getClassloaderName(),methodReq.getClassName(),methodReq.getMethodSignature());
    }

    /**
     * 调用上传的类中的方法
     * @param invokeMethodRequest
     */
    @PostMapping("/invoke")
    public InvokeMethodResponse invoke(@RequestBody @Valid InvokeMethodRequest invokeMethodRequest) throws Throwable {
        return methodService.invokeMethod(invokeMethodRequest);
    }
}
