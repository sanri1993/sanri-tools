package com.sanri.tools.modules.classloader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanri.tools.modules.classloader.dtos.*;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.data.RandomDataService;
import com.sanri.tools.modules.core.service.data.jmock.JMockData;
import com.sanri.tools.modules.core.service.data.jmock.MockConfig;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MethodService {

    @Autowired
    private ClassloaderService classloaderService;
    @Autowired
    private RandomDataService randomDataService;

    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    /**
     * 列出所有的方法名
     * @param classloaderName
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public Set<String> listClassMethodNames(String classloaderName, String className) throws ClassNotFoundException {
        final ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        final Class<?> clazz = classloader.loadClass(className);

        final Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(clazz);
        Set<String> classMethodNames = new HashSet<>();
        for (Method declaredMethod : allDeclaredMethods) {
            if (ReflectionUtils.isObjectMethod(declaredMethod)){
                continue;
            }
            classMethodNames.add(declaredMethod.getName());
        }
        return classMethodNames;
    }

    /**
     * 列出所有方法列表
     * @param classloaderName 类加载器名称
     * @param className 类全名称
     */
    public List<ClassMethodSignature> listClassMethods(String classloaderName, String className) throws ClassNotFoundException {
        final ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        final Class<?> clazz = classloader.loadClass(className);

        List<ClassMethodSignature> classMethodSignatures = new ArrayList<>();
        final Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(clazz);
        for (Method declaredMethod : allDeclaredMethods) {
            if (ReflectionUtils.isObjectMethod(declaredMethod)){
                continue;
            }

            final Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            final ClassMethodSignature classMethodSignature = new ClassMethodSignature(declaredMethod.getName());
            classMethodSignatures.add(classMethodSignature);
            final List<String> argTypes = Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.toList());
            classMethodSignature.setArgTypes(argTypes);
        }
        return classMethodSignatures;
    }

    /**
     * 获取方法信息, 输入参数和返回值
     * @param classloaderName 类加载器名称
     * @param className 类全名称
     * @param classMethodSignature 方法标识
     * @return
     */
    public ClassMethodInfo methodInfo(String classloaderName, String className, ClassMethodSignature classMethodSignature) throws ClassNotFoundException, NoSuchMethodException {
        final Method method = toMethod(classloaderName, className, classMethodSignature);
        final ClassMethodInfo classMethodInfo = toMethodInfo(method);
        return classMethodInfo;
    }

    /**
     * 方法参数模拟
     * @param classloaderName
     * @param className
     * @param methodSignature
     * @return
     */
    public List<String> mockMethodParams(String classloaderName,String className,ClassMethodSignature methodSignature) throws ClassNotFoundException, NoSuchMethodException {
        if (CollectionUtils.isEmpty(methodSignature.getArgTypes())){
            return new ArrayList<>();
        }

        final Method method = toMethod(classloaderName, className, methodSignature);
        final Type[] genericParameterTypes = method.getGenericParameterTypes();

        List<String> values = new ArrayList<>();
        for (Type genericParameterType : genericParameterTypes) {
            final Object mock = JMockData.mock(genericParameterType, new MockConfig());

            if (genericParameterType instanceof Class){
                Class<?> classType = (Class<?>) genericParameterType;
                if (ClassUtils.isPrimitiveOrWrapper(classType)){
                    values.add(mock.toString());
                }else if (classType == String.class){
                    values.add((String) mock);
                }else if (classType == Date.class){
                    Date value = (Date) mock;
                    values.add(String.valueOf(value.getTime()));
                }else if (classType == BigDecimal.class){
                    values.add(((BigDecimal) mock).toString());
                }else {
                    values.add(JSON.toJSONString(mock));
                }
                continue;
            }

            values.add(JSON.toJSONString(mock));
        }

        return values;
    }

    /**
     * 方法信息转 Method
     * @param classloaderName
     * @param className
     * @param methodSignature
     * @return
     * @throws ClassNotFoundException
     */
    public Method toMethod(String classloaderName,String className,ClassMethodSignature methodSignature) throws ClassNotFoundException, NoSuchMethodException {
        final ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        final Class<?> clazz = classloader.loadClass(className);

        final List<String> argTypes = methodSignature.getArgTypes();
        Class<?> [] parameterTypes = new Class[argTypes.size()];
        if (CollectionUtils.isNotEmpty(argTypes)){
            for (int i = 0; i < argTypes.size(); i++) {
                parameterTypes[i] = TypeSupport.getType(argTypes.get(i),classloader);
            }
        }

        final Method method = clazz.getMethod(methodSignature.getMethodName(), parameterTypes);
        return method;
    }

    /**
     * Method 转 ClassMethodInfo
     * @param declaredMethod
     * @return
     */
    public ClassMethodInfo toMethodInfo(Method declaredMethod){
        final String name = declaredMethod.getName();
        // 获取方法参数
        final Type[] parameterTypes = declaredMethod.getGenericParameterTypes();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(declaredMethod);

        // 解析获取不到参数名的情况
        if (parameterNames == null){
            parameterNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterNames.length; i++) {
                parameterNames[i] = "arg" + i;
            }
        }

        List<ClassMethodInfo.Arg> args = new ArrayList<>();
        for (int i = 0; i < parameterNames.length; i++) {
            final Type parameterType = parameterTypes[i];
            final ClassMethodInfo.Arg arg = new ClassMethodInfo.Arg(parameterNames[i]);
            args.add(arg);

            ClassMethodInfo.NestClass argType = ClassMethodInfo.NestClass.convertTypeToNestClass(parameterType);
            arg.setType(argType);
        }
        final Type returnType = declaredMethod.getGenericReturnType();

        final ClassMethodInfo.NestClass nestClass = ClassMethodInfo.NestClass.convertTypeToNestClass(returnType);
        ClassMethodInfo classMethodInfo = new ClassMethodInfo(name,args,nestClass);
        return classMethodInfo;
    }

    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用上传类的方法
     * @param invokeMethodRequest
     * @return
     * @throws ClassNotFoundException
     */
    public InvokeMethodResponse invokeMethod(InvokeMethodRequest invokeMethodRequest) throws Throwable {
        final MethodReq methodReq = invokeMethodRequest.getMethodReq();


        final Method method = toMethod(methodReq.getClassloaderName(), methodReq.getClassName(), methodReq.getMethodSignature());
        final Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.isEnum()){
            throw new ToolException("枚举类型不允许调用方法");
        }

        Object[] values = convertToMethodParams(methodReq,invokeMethodRequest.getParams());

        // 判断是否是静态方法
        final ClassMethodInfo classMethodInfo = toMethodInfo(method);
        try {
            if (Modifier.isStatic(method.getModifiers())){
                long startTime = System.currentTimeMillis();
                final Object invoke = method.invoke(null,values);
                long spendTime = System.currentTimeMillis() - startTime;
                return new InvokeMethodResponse(spendTime,classMethodInfo.getReturnType(),invoke);
            }
            final Object newInstance = declaringClass.newInstance();

            long startTime = System.currentTimeMillis();
            final Object invoke = method.invoke(newInstance, values);
            long spendTime = System.currentTimeMillis() - startTime;
            return new InvokeMethodResponse(spendTime,classMethodInfo.getReturnType(),invoke);
        }catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

    }

    public Object[] convertToMethodParams(MethodReq methodReq, List params) throws ClassNotFoundException {
        final ClassLoader classloader = classloaderService.getClassloader(methodReq.getClassloaderName());
        final ClassMethodSignature methodSignature = methodReq.getMethodSignature();
        final List<String> argTypes = methodSignature.getArgTypes();
        Class<?> [] parameterTypes = new Class[argTypes.size()];
        Object[] values = new Object[argTypes.size()];
        if (CollectionUtils.isNotEmpty(argTypes)){
            for (int i = 0; i < argTypes.size(); i++) {
                parameterTypes[i] = TypeSupport.getType(argTypes.get(i), classloader);

                if (ClassUtils.isPrimitiveOrWrapper(parameterTypes[i]) || parameterTypes[i] == String.class){
//                    values[i] = parameterTypes[i].cast();
                    final Object o = params.get(i);
                    if (o instanceof Number && (parameterTypes[i] == Byte.class || parameterTypes[i] == Byte.TYPE)){
                        values[i] = ((Number)o).byteValue();
                    }else if (o instanceof Number && (parameterTypes[i] == Short.class || parameterTypes[i] == Short.TYPE) ){
                        values[i] = ((Number)o).shortValue();
                    }else if (o instanceof Number && (parameterTypes[i] == Integer.class || parameterTypes[i] == Integer.TYPE)){
                        values[i] = ((Number)o).intValue();
                    }else if (o instanceof Number && (parameterTypes[i] == Float.class || parameterTypes[i] == Float.TYPE) ){
                        values[i] = ((Number)o).floatValue();
                    }else if (o instanceof Number && (parameterTypes[i] == Double.class || parameterTypes[i] == Double.TYPE) ){
                        values[i] = ((Number)o).doubleValue();
                    }else if (o instanceof Number && (parameterTypes[i] == Long.class || parameterTypes[i] == Long.TYPE)){
                        values[i] = ((Number)o).longValue();
                    }

                    if (ClassUtils.isPrimitiveOrWrapper(parameterTypes[i]) && o instanceof String){
                        BigDecimal bigDecimal = new BigDecimal((String)o);

                        if ((parameterTypes[i] == Byte.class || parameterTypes[i] == Byte.TYPE)){
                            values[i] = (bigDecimal).byteValue();
                        }else if ((parameterTypes[i] == Short.class || parameterTypes[i] == Short.TYPE) ){
                            values[i] = (bigDecimal).shortValue();
                        }else if ((parameterTypes[i] == Integer.class || parameterTypes[i] == Integer.TYPE)){
                            values[i] = (bigDecimal).intValue();
                        }else if ((parameterTypes[i] == Float.class || parameterTypes[i] == Float.TYPE) ){
                            values[i] = (bigDecimal).floatValue();
                        }else if ((parameterTypes[i] == Double.class || parameterTypes[i] == Double.TYPE) ){
                            values[i] = (bigDecimal).doubleValue();
                        }else if ( (parameterTypes[i] == Long.class || parameterTypes[i] == Long.TYPE)){
                            values[i] = (bigDecimal).longValue();
                        }
                    }

                    if (parameterTypes[i] == Character.class || parameterTypes[i] == Character.TYPE){
                        final String value = String.valueOf(o);
                        if (StringUtils.isNotBlank(value)){
                            values[i] = value.charAt(0);
                        }
                    }

                    if (parameterTypes[i] == Boolean.class || parameterTypes[i] == Boolean.TYPE){
                        final String value = String.valueOf(o);
                        values[i] = Boolean.parseBoolean(value);
                    }

                    if (parameterTypes[i] == String.class){
                        values[i] = String.valueOf(o);
                    }
                }else if (parameterTypes[i] == Date.class){
                    final Object o = params.get(i);
                    log.info("date: {}",o);
                }else if (parameterTypes[i] == BigDecimal.class){
                    final String value = (String) params.get(i);
                    values[i] = new BigDecimal(value);
                }else{
                    final String value = (String) params.get(i);
                    values[i] = JSON.parseObject(value,parameterTypes[i]);
                }
            }
        }
        return values;
    }


}
