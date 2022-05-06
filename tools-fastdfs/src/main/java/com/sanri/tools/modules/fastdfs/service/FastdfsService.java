package com.sanri.tools.modules.fastdfs.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.csource.common.MyException;
import org.csource.fastdfs.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.service.connect.ConnectService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FastdfsService {

    @Autowired
    private ConnectService connectService;

    /**
     * fastdfs 客户端 connName => FastDfsClient
     */
    private Map<String,FastDfsClient> fastDfsClientMap = new ConcurrentHashMap<>();

    /**
     * 下载或者预览文件
     * @param connName
     * @param dfsId
     * @return
     * @throws IOException
     * @throws MyException
     */
    public byte [] downloadStream(String connName,String dfsId) throws IOException, MyException {
        final FastDfsClient fastDfsClient = fastDfsClient(connName);
        return fastDfsClient.downloadStream(dfsId);
    }

    /**
     * 获取文件信息
     * @param connName
     * @param dfsId
     * @return
     * @throws IOException
     * @throws MyException
     */
    public FileInfo fileInfo(String connName,String dfsId) throws IOException, MyException {
        final FastDfsClient fastDfsClient = fastDfsClient(connName);
        return fastDfsClient.fileInfo(dfsId);
    }

    FastDfsClient fastDfsClient(String connName) throws IOException, MyException {
        FastDfsClient fastDfsClient = fastDfsClientMap.get(connName);
        if (fastDfsClient != null){
            return fastDfsClient;
        }
        fastDfsClient = createFastdfsClient(connName);
        fastDfsClientMap.put(connName,fastDfsClient);
        return fastDfsClient;
    }

    private FastDfsClient createFastdfsClient(String connName) throws IOException, MyException {
        final String content = connectService.loadContent("fastdfs", connName);
        final FastDfsConfig fastDfsConfig = JSON.parseObject(content, FastDfsConfig.class);
        final FastDfsClient fastDfsClient = new FastDfsClient(fastDfsConfig);
        fastDfsClient.reConnect();
        return fastDfsClient;
    }
}
