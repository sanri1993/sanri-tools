package com.sanri.tools.modules.database.service.meta.aspect;

import com.alibaba.druid.pool.DruidDataSource;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.database.service.connect.ConnDatasourceAdapter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Joinpoint;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Aspect
@Component
@Slf4j
public class JdbcConnectionManagerAspect {

//    @Pointcut("execution(public * com.sanri.tools.modules.database.service.meta.*DatabaseMetaDataLoad.*(..))")
//    public void pointcut(){}

    @Autowired
    private ConnDatasourceAdapter connDatasourceAdapter;

    @Pointcut("within(com.sanri.tools.modules.database..*) && @annotation(com.sanri.tools.modules.database.service.meta.aspect.JdbcConnection)")
    public void pointcut(){}

    public static final ThreadLocal<Map<DruidDataSource,ConnectionHolder>> connectionThreadLocal = new NamedThreadLocal<>("Connection Holder");

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        final MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        final Method method = signature.getMethod();
        final Class<?> targetClass = proceedingJoinPoint.getTarget().getClass();
        final String methodIdentification = methodIdentification(method, targetClass);

        if (proceedingJoinPoint.getArgs()[0] == null){
            throw new ToolException("??????????????????????????????, ?????????????????????????????????connName???????????????DruidDatasource???????????????????????? connName ??????");
        }

        DruidDataSource druidDataSource = getDruidDataSource(proceedingJoinPoint.getArgs()[0]);

        try {
            createJdbcConnectionIfNecessary(methodIdentification,druidDataSource);

            // ??????????????????
            return proceedingJoinPoint.proceed();

        } catch (Throwable throwable) {
            // ????????????, ??????????????????
            final Map<DruidDataSource, ConnectionHolder> druidDataSourceConnectionHolderMap = connectionThreadLocal.get();
            for (ConnectionHolder connectionHolder : druidDataSourceConnectionHolderMap.values()) {
                if (connectionHolder != null) {
                    final Connection connection = connectionHolder.getConnection();
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                    }
                }else{
                    log.error("connectionHolder ??????");
                }
            }
            throw throwable;
        }finally {
            Map<DruidDataSource, ConnectionHolder> druidDataSourceConnectionHolderMap = connectionThreadLocal.get();
            if (druidDataSourceConnectionHolderMap == null){
                druidDataSourceConnectionHolderMap = new HashMap<>();
                connectionThreadLocal.set(druidDataSourceConnectionHolderMap);
            }
            ConnectionHolder connectionHolder = druidDataSourceConnectionHolderMap.get(druidDataSource);

            // ?????????????????????
            if (connectionHolder != null && connectionHolder.getConnectionStatus().isNewConnection()){
                log.info("????????????: {}",methodIdentification);
                try{
                    if (connectionHolder.getConnection() != null) {
                        connectionHolder.getConnection().close();
                    }
                    druidDataSourceConnectionHolderMap.put(druidDataSource,null);
                } catch (SQLException throwables) {
                    log.error("?????????????????????:{}, {}",throwables.getErrorCode(),throwables.getMessage(),throwables);
                }
            }
            // ?????????????????????????????????
            connectionHolder.setConnectionStatus(connectionHolder.getConnectionStatus().getOldConnectionStatus());
        }
    }

    /**
     * ?????? jdbc ??????
     * @param methodIdentification
     * @throws SQLException
     */
    @Value("${sanri.webui.package.prefix:com.sanri.tools}")
    protected String packagePrefix;

    /**
     * ??????????????????????????????
     * @param methodIdentification
     * @param pointArg
     * @throws SQLException
     * @throws IOException
     */
    private void createJdbcConnectionIfNecessary(String methodIdentification,DruidDataSource druidDataSource) throws SQLException, IOException {
        // ?????????????????????????????????, ?????????????????????
        Map<DruidDataSource, ConnectionHolder> druidDataSourceConnectionHolderMap = connectionThreadLocal.get();
        if (druidDataSourceConnectionHolderMap == null){
            druidDataSourceConnectionHolderMap = new HashMap<>();
            connectionThreadLocal.set(druidDataSourceConnectionHolderMap);
        }

        ConnectionHolder connectionHolder = druidDataSourceConnectionHolderMap.get(druidDataSource);
        if (connectionHolder != null && !connectionHolder.getConnection().isClosed()){
            final ConnectionHolder.ConnectionStatus connectionStatus = new ConnectionHolder.ConnectionStatus(methodIdentification, false);
            connectionStatus.setOldConnectionStatus(connectionHolder.getConnectionStatus());
            connectionHolder.setConnectionStatus(connectionStatus);
            return ;
        }

        // ??????????????????
        log.info("????????????:{}", methodIdentification);
        connectionHolder = new ConnectionHolder();
        connectionHolder.setConnection(druidDataSource.getConnection());
        connectionHolder.setConnectionStatus(new ConnectionHolder.ConnectionStatus(methodIdentification,true));
        druidDataSourceConnectionHolderMap.put(druidDataSource,connectionHolder);
    }

    private DruidDataSource getDruidDataSource(Object pointArg) throws IOException, SQLException {
        DruidDataSource druidDataSource = null;
        // ???????????????????????????????????????
        if (pointArg instanceof String){
            String connName = (String) pointArg;
            druidDataSource = connDatasourceAdapter.poolDataSource(connName);
        }

        // ???????????????????????? DruidDatasource
        if (pointArg instanceof DruidDataSource) {
            druidDataSource = (DruidDataSource) pointArg;
        }

        // ?????????????????????????????? connName
        final Field connNameField = FieldUtils.getDeclaredField(pointArg.getClass(), "connName", true);
        if (connNameField != null){
            String connName = (String) ReflectionUtils.getField(connNameField, pointArg);
            druidDataSource = connDatasourceAdapter.poolDataSource(connName);
        }else {
            // ???????????????????????? getConnName
            final Method getConnName = MethodUtils.getAccessibleMethod(pointArg.getClass(), "getConnName");
            if (getConnName != null){
                final String connName = (String) ReflectionUtils.invokeMethod(getConnName, pointArg);
                druidDataSource = connDatasourceAdapter.poolDataSource(connName);
            }
        }

        if (druidDataSource == null){
            final StackTraceElement[] stackTraceElements = Thread.getAllStackTraces().get(Thread.currentThread());
            StringBuffer showMessage = new StringBuffer();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                String className = stackTraceElement.getClassName();
                int lineNumber = stackTraceElement.getLineNumber();
                if (className.startsWith(packagePrefix)) {
                    showMessage.append(className + "(" + lineNumber + ")\n");
                }
            }

            throw new ToolException("????????????????????????, ????????????????????????????????????????????????:"+ pointArg + "\n ??????????????? : "+showMessage );
        }
        return druidDataSource;
    }

    private String methodIdentification(Method method, @Nullable Class<?> targetClass) {
        return ClassUtils.getQualifiedMethodName(method,  targetClass);
    }

    /**
     * ???????????????????????????
     * @return
     */
    public static Connection threadBoundConnection(DruidDataSource druidDataSource) throws IOException, SQLException {
        Map<DruidDataSource, ConnectionHolder> druidDataSourceConnectionHolderMap = connectionThreadLocal.get();
        if (druidDataSourceConnectionHolderMap == null){
            druidDataSourceConnectionHolderMap = new HashMap<>();
            connectionThreadLocal.set(druidDataSourceConnectionHolderMap);
        }
        final ConnectionHolder connectionHolder = druidDataSourceConnectionHolderMap.get(druidDataSource);
        if (connectionHolder != null){
            return connectionHolder.getConnection();
        }
        throw new ToolException("?????????????????????????????????");
    }
}
