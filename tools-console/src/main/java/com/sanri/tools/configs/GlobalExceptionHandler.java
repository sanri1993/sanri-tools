package com.sanri.tools.configs;

import com.sanri.tools.modules.core.exception.BusinessException;
import com.sanri.tools.modules.core.exception.RemoteException;
import com.sanri.tools.modules.core.exception.SystemMessage;
import com.sanri.tools.modules.core.dtos.ResponseDto;
import com.sanri.tools.modules.core.exception.ToolException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.naming.ServiceUnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.ConnectException;

/**
 * @author sanri
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${sanri.webui.package.prefix:com.sanri.tools}")
    protected String packagePrefix;

    /**
     * 处理业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseDto businessException(BusinessException e){
        printLocalStackTrack(e);
        return e.getResponseDto();
    }

    @ExceptionHandler(RemoteException.class)
    public ResponseDto remoteException(RemoteException e){
        ResponseDto parentResult = e.getParent().getResponseDto();
        ResponseDto resultEntity = e.getResponseDto();
        //返回给前端的是业务错误，但是需要在控制台把远程调用异常给打印出来
        log.error(parentResult.getCode()+":"+parentResult.getMessage()
                +" \n -| "+resultEntity.getCode()+":"+resultEntity.getMessage());

        printLocalStackTrack(e);

        //合并两个结果集返回
        ResponseDto merge = ResponseDto.err(parentResult.getCode())
                .message(parentResult.getMessage()+" \n  |- "+resultEntity.getCode()+":"+resultEntity.getMessage());
        return merge;
    }

    /**
     * 打印只涉及到项目类调用的异常堆栈
     * @param e
     */
    private void printLocalStackTrack(BusinessException e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        StringBuffer showMessage = new StringBuffer();
        if (ArrayUtils.isNotEmpty(stackTrace)) {
            for (StackTraceElement stackTraceElement : stackTrace) {
                String className = stackTraceElement.getClassName();
                int lineNumber = stackTraceElement.getLineNumber();
                if (className.startsWith(packagePrefix)) {
                    showMessage.append(className + "(" + lineNumber + ")\n");
                }
            }
            log.error("业务异常:{}\n{}" , e.getMessage() ,showMessage);
        } else {
            log.error("业务异常,没有调用栈:{}" , e.getMessage());
        }
    }

    /**
     * get 请求绑定实体
     * @param ex
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    public ResponseDto bindException(BindException ex) {
        // ex.getFieldError():随机返回一个对象属性的异常信息。如果要一次性返回所有对象属性异常信息，则调用ex.getAllErrors()
        FieldError fieldError = ex.getFieldError();
        assert fieldError != null;
        String message = fieldError.getField() + " = " + fieldError.getRejectedValue() + "; cause "+ fieldError.getDefaultMessage();
        log.error(message);
        return SystemMessage.ARGS_ERROR.exception(fieldError.getField(),fieldError.getRejectedValue()).getResponseDto();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDto methodArgumentNotValidException(MethodArgumentNotValidException e){
        FieldError fieldError = e.getBindingResult().getFieldError();
        assert fieldError != null;
        String message = fieldError.getField() + " = " + fieldError.getRejectedValue() + "; cause "+ fieldError.getDefaultMessage();
        log.error(message);
        return SystemMessage.ARGS_ERROR.exception(fieldError.getField(),fieldError.getRejectedValue(),fieldError.getDefaultMessage()).getResponseDto();
    }

    /**
     * 方法普通参数验证
     * @param ex
     * @return
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseDto constraintViolationException(ConstraintViolationException ex){
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        PathImpl propertyPath = (PathImpl) constraintViolation.getPropertyPath();
        String name = propertyPath.getLeafNode().getName();
        String message = constraintViolation.getMessage();
        String logMessage = name + " " + message;
        log.error(logMessage);
        return SystemMessage.ARGS_ERROR2.exception(logMessage).getResponseDto();
    }

    /**
     * 异常处理，可以绑定多个
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseDto otherException(Exception e){
        if (e instanceof HttpMediaTypeNotAcceptableException){
            HttpMediaTypeNotAcceptableException exception = (HttpMediaTypeNotAcceptableException) e;
            if (RequestContextHolder.currentRequestAttributes() != null) {
                HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
                log.error("HttpMediaTypeNotAcceptableException 当前请求 url : {} \n supports: {}",request.getRequestURI(),exception.getSupportedMediaTypes());
            }else {
                log.info("HttpMediaTypeNotAcceptableException 不是 http 请求: {}",exception.getSupportedMediaTypes());
            }

        }
        log.error(e.getMessage(),e);
        return ResponseDto.err(e.getClass().getSimpleName()).message(e.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseDto fileNotFound(FileNotFoundException e){
        log.error(e.getMessage(),e);
        return SystemMessage.FILE_NOT_FOUND.result();
    }

    @ExceptionHandler(StreamCorruptedException.class)
    public ResponseDto StreamCorruptedException(StreamCorruptedException e){
        log.error(e.getMessage(),e);
        return BusinessException.create(e.getMessage()).getResponseDto();
    }

    @ExceptionHandler(IOException.class)
    public ResponseDto ioException(IOException e, HttpServletResponse response){
        if (e.getCause() != null && e.getCause() instanceof ServiceUnavailableException){
            return serviceUnavailableException((ServiceUnavailableException) e.getCause());
        }
        log.error(e.getMessage(),e);
        return SystemMessage.NETWORK_ERROR.result();
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseDto serviceUnavailableException(ServiceUnavailableException e){
        log.error(e.getMessage(),e);
        return SystemMessage.SERVICE_ERROR.result();
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseDto connectException(ConnectException e){
        log.error(e.getMessage(),e);
        return SystemMessage.CONNECT_ERROR.result();
    }

    @ExceptionHandler(ToolException.class)
    public ResponseDto toolException(ToolException e){
        final StackTraceElement[] stackTrace = e.getStackTrace();

        String className = stackTrace[0].getClassName();
        int lineNumber = stackTrace[0].getLineNumber();
        String methodName = stackTrace[0].getMethodName();
        String rootMark = StringUtils.join(new String[]{className,".", methodName, "(", lineNumber + "", ") ",}, "");
        BusinessException businessException = BusinessException.create(e.getMessage());

        StringBuffer stringBufferr = new StringBuffer(rootMark + " "+businessException.getMessage());

        for (StackTraceElement stackTraceElement : stackTrace) {
            className = stackTraceElement.getClassName();
            lineNumber = stackTraceElement.getLineNumber();
            methodName = stackTraceElement.getMethodName();
            String mark = StringUtils.join(new String[]{className,".", methodName, "(", lineNumber + "", ") ",}, "");

            if (className.startsWith(packagePrefix) && !className.contains("$")) {
                stringBufferr.append("\n").append("  at " + mark);
            }
        }
        log.error(stringBufferr.toString());
        return businessException.getResponseDto();
    }
}
