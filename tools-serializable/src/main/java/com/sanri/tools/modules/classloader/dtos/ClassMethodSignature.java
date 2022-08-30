package com.sanri.tools.modules.classloader.dtos;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法标识, 用于获取方法列表
 */
@Getter
@Setter
public class ClassMethodSignature {
    private String methodName;
    private List<String> argTypes = new ArrayList<>();

    public ClassMethodSignature() {
    }

    public ClassMethodSignature(String name) {
        this.methodName = name;
    }

    /**
     * 获取方法标识
     * @return
     */
    public String getSignature(){
        if (CollectionUtils.isEmpty(argTypes)){
            return methodName+"()";
        }
        try{
            List<String> simpleNameArgTypes = new ArrayList<>();
            for (String argType : argTypes) {
                if (argType.startsWith("java.")){
                    simpleNameArgTypes.add(ClassLoader.getSystemClassLoader().loadClass(argType).getSimpleName());
                }else {
                    simpleNameArgTypes.add(argType);
                }
            }
            return methodName+"("+ StringUtils.join(simpleNameArgTypes,",")+")";
        }catch (Exception e){
            // ignore
        }
        return methodName+"("+ StringUtils.join(argTypes,",")+")";
    }
}
