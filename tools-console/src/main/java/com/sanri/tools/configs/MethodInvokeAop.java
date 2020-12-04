package com.sanri.tools.configs;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Component
@Aspect
@Slf4j
public class MethodInvokeAop {
    @Pointcut("execution(public * com.sanri.tools.modules.*.controller.*.*(..))")
    public void pointcut(){}

    @Before("pointcut()")
    public void before(JoinPoint joinpoint){
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String requestMethod = request.getMethod();
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        String argsFormat = formatArgs(joinpoint.getArgs());
        log.info("{} {}?{}\n{}",requestMethod,requestURI,queryString,argsFormat);
    }

    private String formatArgs(Object[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof HttpServletRequest){

            }
        }
        return "";
    }
}
