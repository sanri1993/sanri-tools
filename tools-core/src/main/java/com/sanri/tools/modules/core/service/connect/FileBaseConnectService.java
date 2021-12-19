package com.sanri.tools.modules.core.service.connect;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.sanri.tools.modules.core.aspect.SerializerToFile;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.security.UserService;
import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectInput;
import com.sanri.tools.modules.core.service.connect.dtos.ConnectOutput;
import com.sanri.tools.modules.core.service.file.FileManager;

import lombok.extern.slf4j.Slf4j;

/**
 * @author sanri
 */
@Component
@Slf4j
public class FileBaseConnectService extends ConnectService implements InitializingBean {

    private final FileManager fileManager;
    private final ApplicationContext applicationContext;

    /**
     * 保存的连接信息 module => baseName => ConnectOutput
     */
    private static final Map<String, Map<String,ConnectOutput>> connectInfoMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private UserService userService;

    public FileBaseConnectService(FileManager fileManager, ApplicationContext applicationContext) {
        this.fileManager = fileManager;
        this.applicationContext = applicationContext;
    }

    @Override
    @SerializerToFile
    public void updateConnect(ConnectInput connectInput) throws IOException {
        // 填充元数据
        final String module = connectInput.getModule();
        final String baseName = connectInput.getBaseName();
        final Map<String, ConnectOutput> connectOutputMap = connectInfoMap.computeIfAbsent(module, value -> new ConcurrentHashMap<>(16));
        ConnectOutput connectOutput = connectOutputMap.get(baseName);
        String username = userService != null ? userService.username() : null;
        if (connectOutput != null){
            connectOutput.setConnectInput(connectInput);
            connectOutput.setLastUpdateTime(new Date());
            connectOutput.setLastUpdateUser(username);
        }else{
            connectOutput = new ConnectOutput(Math.round(Math.random() * 1000), connectInput, username, new Date(), new Date(), 0, module + "/" + baseName);
            connectOutputMap.put(baseName,connectOutput);
        }

        // 填充内容
        final File connectBase = fileManager.mkConfigDir("connectBase");
        final File file = new File(connectBase, connectOutput.getPath());
        file.getParentFile().mkdirs();
        FileUtils.writeStringToFile(file,connectInput.getContent(),StandardCharsets.UTF_8);

        // 不放太多的字符串在内存
        connectInput.setContent(null);

        applicationContext.publishEvent(new UpdateSecurityConnectEvent(connectInput));
    }

    @Override
    @SerializerToFile
    public void deleteConnect(String module, String baseName) {
        final ConnectOutput connect = findConnect(module, baseName);
        connectInfoMap.get(module).remove(baseName);
        final String path = connect.getPath();
        final File connectBase = fileManager.mkConfigDir("connectBase");
        final File file = new File(connectBase, path);
        if (file.exists()){
            final boolean delete = file.delete();
            if (!delete){
                log.warn("文件删除失败:{}",file.toPath());
            }
        }
    }

    @Override
    public ConnectOutput findConnect(String module, String baseName) {
        final Map<String, ConnectOutput> connectOutputMap = connectInfoMap.get(module);
        if (connectOutputMap != null){
            return connectOutputMap.get(baseName);
        }
        throw new ToolException("不存在的连接配置:"+module+"/"+baseName);
    }

    /**
     * 使用连接中的组织信息进行权限过滤
     * @return 所有可用的连接列表
     */
    @Override
    public List<ConnectOutput> connectsInternal() {
        List<ConnectOutput> outputs = new ArrayList<>();

        final List<String> groups = userService != null ? userService.user().getGroups() : new ArrayList<>();

        for (Map<String, ConnectOutput> next : connectInfoMap.values()) {
            final Collection<ConnectOutput> values = next.values();
            A:
            for (ConnectOutput value : values) {
                final ConnectInput connectInput = value.getConnectInput();
                final String group = connectInput.getGroup();
                if (userService != null) {
                    for (String userGroup : groups) {
                        if (group.startsWith(userGroup)) {
                            outputs.add(value);
                            continue A;
                        }
                    }
                } else {
                    outputs.add(value);
                }
            }
        }
        return outputs;
    }



    @Override
    public List<String> modules() {
        return new ArrayList<>(connectInfoMap.keySet());
    }

    /**
     * 创建一个模块(内存中) , 如果模块内部无数据的话, 下次重启就会被删除
     * @param name 模块名
     */
    @Override
    public void createModule(String name) {
        connectInfoMap.computeIfAbsent(name,key -> new ConcurrentHashMap<>());
    }

    @Override
    public String loadContent(String module, String baseName) throws IOException {
        final Map<String, ConnectOutput> connectOutputMap = connectInfoMap.get(module);
        if (connectOutputMap != null){
            final ConnectOutput connectOutput = connectOutputMap.get(baseName);
            if (connectOutput != null){
                if (userService != null) {
                    // 检查数据权限
                    final ConnectInput connectInput = connectOutput.getConnectInput();
                    final String group = connectInput.getGroup();
                    final List<String> groups = userService.user().getGroups();
                    boolean hasSecurity = false;
                    for (String userGroup : groups) {
                        if (group.startsWith(userGroup)){
                            hasSecurity = true;
                            break;
                        }
                    }

                    if (!hasSecurity){
                        throw new ToolException("您没有权限访问这个连接数据");
                    }
                }

                final String path = connectOutput.getPath();
                final File connectBase = fileManager.mkConfigDir("connectBase");
                final File file = new File(connectBase, path);
                return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            }
        }
        throw new ToolException("连接信息不存在或已被删除:"+module+"/"+baseName);
    }

    /**
     * 序列化连接元数据到文件
     */
    public void serializer() throws IOException {
        final File connectBase = fileManager.mkConfigDir("connectBase");
        final Iterator<Map.Entry<String, Map<String, ConnectOutput>>> moduleEntryIterator = connectInfoMap.entrySet().iterator();
        List<String> lines = new ArrayList<>();
        while (moduleEntryIterator.hasNext()){
            final Map.Entry<String, Map<String, ConnectOutput>> topEntry = moduleEntryIterator.next();
            final String module = topEntry.getKey();
            for (Map.Entry<String, ConnectOutput> entry : topEntry.getValue().entrySet()) {
                final String baseName = entry.getKey();
                final ConnectOutput connectOutput = entry.getValue();
                final ConnectInput connectInput = connectOutput.getConnectInput();
                final String[] fields = {connectOutput.getId() + "", module, baseName, connectInput.getConfigTypeName(), connectInput.getGroup(),
                        connectOutput.getLastUpdateUser(), connectOutput.getLastUpdateTime().getTime() + "", connectOutput.getLastAccessTime().getTime() + "",
                        connectOutput.getLinkErrorCount() + "", connectOutput.getPath()};
                String line = StringUtils.join(fields, ":");

                lines.add(line);
            }
        }
        final File info = new File(connectBase, "info");
        FileUtils.writeLines(info,lines,false);
    }


    /**
     * $configDir[DIR]
     *   connectBase[Dir]
     *     info[File] 存储连接元数据
     *     $module1[Dir]
     *       $baseName => content
     *     $module2[Dir]
     *
     *  info 格式
     *  id:模块:基础名:类加载器名称:类名:配置类型:分组:上次更新人:上次访问时间:连接失败次数:相对路径
     *  id:module:baseName:configTypeName:group:lastUpdateUser:lastUpdateTime:lastAccessTime:linkErrorCount:path
     *  会根据上次访问时间进行连接顺序排序
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        final File connectBase = fileManager.mkConfigDir("connectBase");
        final File info = new File(connectBase, "info");
        if (!info.exists()) {
            info.createNewFile();
            return ;
        }
        final List<String> connectLines = FileUtils.readLines(info, StandardCharsets.UTF_8);
        for (String connectLine : connectLines) {
            final String[] split = StringUtils.splitPreserveAllTokens(connectLine, ":", 11);
            final ConnectInput connectInput = new ConnectInput(split[1], split[2], split[3],split[4]);
            final ConnectOutput connectOutput = new ConnectOutput(NumberUtils.toLong(split[0]), connectInput, split[5], new Date(NumberUtils.toLong(split[6])), new Date(NumberUtils.toLong(split[7])), NumberUtils.toInt(split[8]), split[9]);
            final Map<String, ConnectOutput> connectOutputMap = connectInfoMap.computeIfAbsent(connectInput.getModule(),value -> new ConcurrentHashMap<>(16));
            connectOutputMap.put(connectInput.getBaseName(), connectOutput);
        }
    }
}
