package com.sanri.tools.modules.core.controller;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.service.ConnectService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.ReachableUtil;
import com.sanri.tools.modules.protocol.param.*;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@RestController
@RequestMapping("/connect")
public class ConnectManager {
    @Autowired
    private ConnectService connectService;
    @Autowired
    private FileManager fileManager;

    // 连接都保存在这个目录
    public static final String CONNECT_NAME = "connect";

    /**
     * 模块列表
     * @return
     */
    @GetMapping("/modules")
    public List<String> modules(){
        return fileManager.modules();
    }

    /**
     * 指定模块下的连接列表
     * @param module
     * @return
     */
    @GetMapping("/{module}/names")
    public List<String> names(@PathVariable("module") String module){
        return fileManager.simpleConfigNames(module);
    }

    /**
     * 获取配置详情
     * @param module
     * @param connName
     * @return
     */
    @GetMapping("/{module}/{connName}")
    public String content(@PathVariable("module") String module,@PathVariable("connName")String connName) throws IOException {
        return fileManager.readConfig(module,connName);
    }

    /**
     * 创建连接
     * @param databaseConnectParam
     * @throws IOException
     */
    @PostMapping("/create/database")
    public void createDatabase(@RequestBody DatabaseConnectParam databaseConnectParam) throws IOException {
        checkHostportReachable(databaseConnectParam);
        ConnectIdParam connectIdParam = databaseConnectParam.getConnectIdParam();
        String module = connectIdParam.getModule();
        String connName = connectIdParam.getConnName();
        fileManager.writeConfig(module,CONNECT_NAME+"/"+connName, JSON.toJSONString(databaseConnectParam));
    }

    @PostMapping("/create/redis")
    public void createRedis(@RequestBody RedisConnectParam redisConnectParam) throws IOException {
        checkHostportReachable(redisConnectParam);
        ConnectIdParam connectIdParam = redisConnectParam.getConnectIdParam();
        String module = connectIdParam.getModule();
        String connName = connectIdParam.getConnName();
        fileManager.writeConfig(module,CONNECT_NAME+"/"+connName, JSON.toJSONString(redisConnectParam));
    }

    @PostMapping("/create/kafka")
    public void createKafka(@RequestBody KafkaConnectParam kafkaConnectParam) throws IOException {
        checkHostportReachable(kafkaConnectParam);
        ConnectIdParam connectIdParam = kafkaConnectParam.getConnectIdParam();
        String module = connectIdParam.getModule();
        String connName = connectIdParam.getConnName();
        fileManager.writeConfig(module,CONNECT_NAME+"/"+connName, JSON.toJSONString(kafkaConnectParam));
    }

    private void checkHostportReachable(Object param){
        try {
            ConnectParam connectParam = (ConnectParam) FieldUtils.readDeclaredField(param, "connectParam", true);
            connectService.testConnectReachable(connectParam);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
