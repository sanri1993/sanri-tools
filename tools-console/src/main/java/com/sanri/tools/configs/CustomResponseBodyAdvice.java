package com.sanri.tools.configs;

import com.sanri.tools.modules.core.dtos.ResponseDto;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * 可以定义空返回的时候返回正确的信息，如成功信息
 */
@RestControllerAdvice
public class CustomResponseBodyAdvice implements ResponseBodyAdvice {

    public boolean supports(MethodParameter returnType, Class converterType) {
        Executable executable = returnType.getExecutable();
        AnnotatedType annotatedReturnType = executable.getAnnotatedReturnType();
        Type type = annotatedReturnType.getType();
        return type != ResponseEntity.class;
    }

    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Executable executable = returnType.getExecutable();
        AnnotatedType annotatedReturnType = executable.getAnnotatedReturnType();
        Type type = annotatedReturnType.getType();

        // 对于异常的处理
        ResponseDto errorResponse = null;
        if(executable.getDeclaringClass() == BasicErrorController.class){
            // 如果是方法参数错误 ，被 BasicErrorController 拦截，则只需要取其中的 message 信息做返回
            ResponseEntity responseEntity = (ResponseEntity) body;
            HttpStatus statusCode = responseEntity.getStatusCode();
            Map<String,Object> map = (Map<String, Object>) responseEntity.getBody();
            Object message = map.get("message");
            errorResponse  = ResponseDto.err(statusCode.value() + "").message(Objects.toString(message));
        }
        if(executable.getDeclaringClass() == GlobalExceptionHandler.class){
            //如果是出异常了，直接返回错误处理，如果在异常处理中没有包裹消息，则包裹
            if(type != ResponseDto.class){  //暂时只支持 String 类型，后面可以扩展成层级对象
                String errorInfo = Objects.toString(body);
                errorResponse =  ResponseDto.err().message(errorInfo);
            }
            errorResponse = (ResponseDto) body;
        }
        if(errorResponse != null){
            return errorResponse;
        }

        // 对于正常输出的处理
        ResponseDto outResponse = null;
        if(type == ResponseDto.class){
            // 如果本身就返回的是结果类型，则不用转换
//            outResponse = (ResponseDto) body;
            return body;
        }
        if(type == Void.TYPE){
            // 空返回直接返回成功
            outResponse =  ResponseDto.ok();
        }

        // 否则使用成功消息包裹数据
        return ResponseDto.ok().data(body);
    }
}
