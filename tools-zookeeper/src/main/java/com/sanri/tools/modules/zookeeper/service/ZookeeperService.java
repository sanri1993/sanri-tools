package com.sanri.tools.modules.zookeeper.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectInput;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;
import com.sanri.tools.modules.core.service.connect.events.DeleteSecurityConnectEvent;
import com.sanri.tools.modules.core.service.connect.events.SecurityConnectEvent;
import com.sanri.tools.modules.core.service.connect.events.UpdateSecurityConnectEvent;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.core.dtos.UpdateConnectEvent;
import com.sanri.tools.modules.core.dtos.param.ConnectParam;
import com.sanri.tools.modules.core.dtos.param.SimpleConnectParam;
import com.sanri.tools.modules.core.service.file.ConnectServiceOldFileBase;

import com.sanri.tools.modules.serializer.SerializerConstants;
import com.sanri.tools.modules.serializer.service.Serializer;
import com.sanri.tools.modules.serializer.service.SerializerChoseService;
import com.sanri.tools.modules.zookeeper.dtos.ZooNodeACL;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ZookeeperService implements ApplicationListener<SecurityConnectEvent> {
    /**
     * zookeeper 客户端列表
     * connName ==> ZkClient
     */
    private Map<String, ZkClient> zkClientMap = new ConcurrentHashMap<String, ZkClient>();

    @Autowired
    private ConnectService connectService;
    @Autowired
    private SerializerChoseService serializerChoseService;


    public static final String module = "zookeeper";

    /**
     * 检查路径是否存在
     * @param connName
     * @param path
     * @return
     */
    public boolean exists(String connName,String path) throws IOException {
        ZkClient zkClient = zkClient(connName);
        path = resolvePath(path);
        boolean exists = zkClient.exists(path);
        return exists;
    }

    /**
     *  列出直接子节点
     * @return
     */
    public List<String> childrens(String connName, String path) throws IOException {
        ZkClient zkClient = zkClient(connName);
        path = resolvePath(path);
        List<String> children = zkClient.getChildren(path);
        return children;
    }

    public int countChildren(String connName, String path) throws IOException {
        ZkClient zkClient = zkClient(connName);
        return zkClient.countChildren(path);
    }

    /**
     * 获取节点元数据
     * @param connName
     * @param path
     * @return
     */
    public Stat meta(String connName, String path) throws IOException {
        path = resolvePath(path);
        ZkClient zkClient = zkClient(connName);
        Map.Entry<List<ACL>, Stat> acl = zkClient.getAcl(path);
        Stat value = acl.getValue();
        return value;
    }

    /**
     * 获取 acl 权限列表
     * @param connName
     * @param path
     * @return
     */
    public List<ZooNodeACL> acls(String connName, String path) throws IOException{
        path = resolvePath(path);

        ZkClient zkClient = zkClient(connName);
        Map.Entry<List<ACL>, Stat> entry = zkClient.getAcl(path);
        List<ACL> acls = entry.getKey();

        List<ZooNodeACL> zooNodeACLS = new ArrayList<ZooNodeACL>();
        if(CollectionUtils.isNotEmpty(acls)){
            for (ACL acl : acls) {
                Id id = acl.getId();
                ZooNodeACL zooNodeACL = new ZooNodeACL(id.getScheme(), id.getId(), acl.getPerms());
                zooNodeACLS.add(zooNodeACL);
            }
        }
        return zooNodeACLS;
    }

    /**
     * 默认序列化工具
     */
    private static final ZkSerializer DEFAULT_ZKSERIALIZER = new BytesPushThroughSerializer();

    /**
     * 获取一个客户端
     * @return
     */
    ZkClient zkClient(String connName) throws IOException {
        ZkClient zkClient = zkClientMap.get(connName);
        if(zkClient == null){
            SimpleConnectParam zookeeperConnectParam = (SimpleConnectParam) connectService.readConnParams(module, connName);
            ConnectParam connectParam = zookeeperConnectParam.getConnectParam();
            int connectionTimeout = connectParam.getConnectionTimeout();
            int sessionTimeout = connectParam.getSessionTimeout();
            zkClient = new ZkClient(connectParam.getConnectString(),sessionTimeout, connectionTimeout,DEFAULT_ZKSERIALIZER);
            zkClientMap.put(connName,zkClient);
        }
        return zkClient;
    }

    /**
     * 删除节点
     * @param connName
     * @param path
     * @return
     * @throws IOException
     */
    public void deleteNode(String connName,String path) throws IOException{
        path = resolvePath(path);

        ZkClient zkClient = zkClient(connName);
        zkClient.deleteRecursive(path);
    }

    /**
     * 读取数据
     * @param connName
     * @param path
     * @param serializer 序列化工具
     * @return
     */
    public Object readData(String connName,String path,String serializer) throws IOException {
        path = resolvePath(path);

        ZkClient zkClient = zkClient(connName);
        Object data = zkClient.readData(path, true);
        if(data == null){
            return "";
        }
        byte [] dataBytes = (byte[]) data;
        Serializer serializerChose = serializerChoseService.choseSerializer(serializer);
        if(serializerChose == null){
            // 如果找不到序列化工具,选择 string 序列化
            serializerChose = serializerChoseService.choseSerializer(SerializerConstants.STRING);
        }
        ZkSerializerAdapter zkSerializerAdapter = new ZkSerializerAdapter(serializerChose);
        Object object = zkSerializerAdapter.deserialize(dataBytes);
        return object;
    }

    /**
     * 写入字符串格式数据
     * @param connName
     * @param path
     * @param data
     * @throws IOException
     */
    public void writeData(String connName, String path, String data) throws IOException {
        path = resolvePath(path);

        ZkClient zkClient = zkClient(connName);
        zkClient.writeData(path,data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 重置一些可能路径不规范的 path
     * @param path
     * @return
     */
    private String resolvePath(String path) {
        if(StringUtils.isBlank(path)){
            path = "/";
        }else if(!path.startsWith("/")){
            path = "/"+path;
        }
        if(path.startsWith("//")){
            path = path.substring(1);
        }
        String cleanPath = org.springframework.util.StringUtils.cleanPath(path);
        return cleanPath;
    }

//    @PostConstruct
//    public void register(){
//        pluginManager.register(PluginDto.builder().module("monitor").name(module).author("sanri").logo("zookeeper.jpg").desc("做为 kafka dubbo 的支撑模块").build());
//    }

    @PreDestroy
    public void destory(){
        log.info("清除 {} 客户端列表:{}",module,zkClientMap.keySet());
        Iterator<ZkClient> iterator = zkClientMap.values().iterator();
        while (iterator.hasNext()){
            ZkClient next = iterator.next();
            try {
                next.close();
            }catch (Exception e){}
        }
    }

    @Override
    public void onApplicationEvent(SecurityConnectEvent event) {
        ConnectOutput connectOutput = (ConnectOutput) event.getSource();
        final ConnectInput connectInput = connectOutput.getConnectInput();

        if (event instanceof UpdateSecurityConnectEvent){

            if (module.equals(connectInput.getModule())) {
                String connName = connectInput.getBaseName();
                zkClientMap.remove(connName);
                log.info("[{}]模块[{}]配置变更,将移除存储的元数据信息",module,connName);
            }
        }else if (event instanceof DeleteSecurityConnectEvent){
            if (ZookeeperService.module.equals(connectInput.getModule())){
                final String baseName = connectOutput.getConnectInput().getBaseName();
                log.info("zookeeper 删除连接[{}]",baseName);
                zkClientMap.remove(baseName);
            }
        }

    }
}
