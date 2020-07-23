package com.sanri.tools.modules.zookeeper.service;

import com.sanri.tools.modules.core.service.file.ConnectService;
import com.sanri.tools.modules.protocol.param.AbstractConnectParam;
import com.sanri.tools.modules.protocol.param.ConnectParam;
import com.sanri.tools.modules.protocol.param.ZookeeperConnectParam;
import com.sanri.tools.modules.protocol.zk.ZooNodeACL;
import com.sanri.tools.modules.serializer.Serializer;
import com.sanri.tools.modules.serializer.service.SerializerChoseService;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ZookeeperService {
    // connName ==> ZkClient
    Map<String, ZkClient> zkClientMap = new ConcurrentHashMap<String, ZkClient>();

    @Autowired
    private ConnectService connectService;
    @Autowired
    private SerializerChoseService serializerChoseService;

    public static final String module = "zookeeper";

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

    // 默认序列化工具
    private static final ZkSerializer DEFAULT_ZKSERIALIZER = new BytesPushThroughSerializer();

    /**
     * 获取一个客户端
     * @return
     */
    ZkClient zkClient(String connName) throws IOException {
        ZkClient zkClient = zkClientMap.get(connName);
        if(zkClient == null){
            ZookeeperConnectParam zookeeperConnectParam = (ZookeeperConnectParam) connectService.readConnParams(module, connName);
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
     * @param deserialize 反序列化工具
     * @return
     */
    public Object readData(String connName,String path,String deserialize) throws IOException {
        path = resolvePath(path);

        ZkClient zkClient = zkClient(connName);
        Object data = zkClient.readData(path, true);
        if(data == null){
            return "";
        }
        byte [] dataBytes = (byte[]) data;
        Serializer serializer = serializerChoseService.choseSerializer(deserialize);
        ZkSerializerAdapter zkSerializerAdapter = new ZkSerializerAdapter(serializer);
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
        zkClient.writeData(path,data);
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
        String cleanPath = org.springframework.util.StringUtils.cleanPath(path);
        return cleanPath;
    }

    @PreDestroy
    public void destory(){
        log.info("清除 zookeeper 客户端列表");
        Iterator<ZkClient> iterator = zkClientMap.values().iterator();
        while (iterator.hasNext()){
            ZkClient next = iterator.next();
            try {
                next.close();
            }catch (Exception e){}
        }
    }
}
