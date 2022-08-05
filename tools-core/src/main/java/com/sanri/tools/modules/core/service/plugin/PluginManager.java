package com.sanri.tools.modules.core.service.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import javax.annotation.PreDestroy;

import com.dtflys.forest.backend.okhttp3.OkHttp3Backend;
import com.sanri.tools.modules.core.service.plugin.dtos.EnhancePlugin;
import com.sanri.tools.modules.core.service.plugin.dtos.MarketPlugin;
import com.sanri.tools.modules.core.service.plugin.dtos.PluginCallInfo;
import com.sanri.tools.modules.core.service.plugin.dtos.PluginWithHelpContent;
import com.sanri.tools.modules.core.utils.Version;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sanri.tools.modules.core.dtos.PluginRegister;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.file.FileManager;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PluginManager implements InitializingBean {

    @Autowired
    private FileManager fileManager;

    private Map<String,EnhancePlugin> pluginRegisterMap = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 列出所有插件
     */
    public List<EnhancePlugin> localPlugins() throws IOException {
        final List<EnhancePlugin> values = new ArrayList<>(pluginRegisterMap.values());
        Comparator<EnhancePlugin> comparator = (a,b) -> {
            final PluginCallInfo self = a.pluginCallInfo;
            final PluginCallInfo other = b.pluginCallInfo;
            long currentTimeMillis = System.currentTimeMillis();
            boolean otherTimeout = currentTimeMillis - other.lastCallTime > FIVE_MINUTES;
            boolean thisTimeout = currentTimeMillis - self.lastCallTime > FIVE_MINUTES;

            if(otherTimeout && !thisTimeout){
                return -1;
            }
            if(!otherTimeout && thisTimeout){
                return 1;
            }
            if(!otherTimeout && !thisTimeout){
                // 5 分钟之内,调用时间会有一定占比, 5 分钟之后只根据调用次数
                long sub = other.lastCallTime - self.lastCallTime;
                return (int) (((double)sub) * 0.01 + ((double)( self.totalCalls - other.totalCalls )) * 0.5);
            }

            // 5 分钟之后 , 只根据调用次数排序
            return other.totalCalls - self.totalCalls;
        };
        Collections.sort(values,comparator);
        return values;
    }

    @Autowired
    private PluginRepoManager pluginRepoManager;

    /**
     * 可以安装的插件列表
     */
    public List<MarketPlugin> marketPlacePlugins() throws IOException {
        List<MarketPlugin> marketPlugins = new ArrayList<>();
        final String current = pluginRepoManager.current();
        if (StringUtils.isNotBlank(current)){
            try(final InputStream inputStream = new URL(current).openStream()){
                final String pluginString = IOUtils.toString(inputStream);
                final String[] pluginArray = StringUtils.split(pluginString, '\n');
                for (String pluginInfo : pluginArray) {
                    final String[] columns = StringUtils.split(pluginInfo, ":");
                    final MarketPlugin marketPlugin = new MarketPlugin(columns[0], columns[1], columns[2], columns[3], columns[4], new Version(columns[5]));
                    marketPlugins.add(marketPlugin);
                }
            }
        }
        return marketPlugins;
    }

    public void deletePlugin(String pluginId){
        pluginRegisterMap.remove(pluginId);
    }

    /**
     * 插件详情
     * @param pluginId
     * @return
     */
    public PluginWithHelpContent detailWithHelpContent(String pluginId) {
        final EnhancePlugin enhancePlugin = pluginRegisterMap.get(pluginId);
        if (enhancePlugin == null){
            throw new ToolException("找不到插件:"+pluginId);
        }
        final PluginWithHelpContent pluginWithHelpContent = new PluginWithHelpContent(enhancePlugin);
        final String help = enhancePlugin.getPluginRegister().getHelp();
        if (StringUtils.isNotBlank(help)){
            try {
                final Resource resource = applicationContext.getResource(help);
                if (!resource.exists()){
                    // 如果详细介绍不存在, 则从本地资源路径去找(找到最有可能是当前模块的介绍文档, 并取第一个)
                    final Resource[] resources = applicationContext.getResources("classpath*:" + help);
                    for (Resource res : resources) {
                        if (res.toString().contains(pluginId)){
                            log.info("找到插件[{}]的资源:{}",pluginId,res);
                            String content = IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8);
                            // 本地的图片路径需要替换相对路径为相对根路径的路径
                            content = content.replaceAll("../../../../images","/images");
                            pluginWithHelpContent.setHelpContent(content);
                            break;
                        }
                    }
                }else{
                    log.info("找到插件[{}]的资源:{}",pluginId,resource);
                    final String content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                    pluginWithHelpContent.setHelpContent(content);
                }

            }catch (Exception e){
                log.error("加载插件[{}]帮助文件[{}]失败:{}",pluginId,help,e.getMessage(),e);
            }
        }
        return pluginWithHelpContent;
    }

    /**
     * 访问一次插件
     * @param pluginId
     */
    public void visitedPlugin(String pluginId){
        final EnhancePlugin enhancePluginDto = pluginRegisterMap.get(pluginId);
        if (enhancePluginDto == null){
            throw new ToolException("没有找到插件:"+pluginId);
        }
        if (enhancePluginDto != null){
            final PluginCallInfo pluginCallInfo = enhancePluginDto.getPluginCallInfo();
            pluginCallInfo.setLastCallTime(System.currentTimeMillis());
            pluginCallInfo.setTotalCalls(pluginCallInfo.getTotalCalls() + 1);
        }
    }

    @PreDestroy
    public void destory(){
        // 在即将销毁时序列化插件配置
        serializer();
    }

    /**
     * 定时将访问次数和上次访问时间序列化
     * 30 分钟序列化一次,100 秒后第一次序列化,不能和初始化的读取冲突
     */
    @Scheduled(fixedRate = 1800000,initialDelay = 100000)
    public void serializer(){
        // 准备数据
        List<String> lines = new ArrayList<>();
        for (EnhancePlugin value : pluginRegisterMap.values()) {
            final PluginCallInfo pluginCallInfo = value.getPluginCallInfo();
            final List<? extends Serializable> serializables = Arrays.asList(pluginCallInfo.getPluginId(), pluginCallInfo.getTotalCalls(), pluginCallInfo.getLastCallTime());
            lines.add(StringUtils.join(serializables,','));
        }

        String join = StringUtils.join(lines, '\n');
        try {
            fileManager.writeConfig("plugin","visited",join);
        } catch (IOException e) {}
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 读取各个插件上次序列化的访问明细值
        final String readConfig = fileManager.readConfig("plugin", "visited");
        final String[] lines = StringUtils.split(readConfig, '\n');
        Map<String,PluginCallInfo> pluginCallInfoMap = new HashMap<>();
        if (ArrayUtils.isNotEmpty(lines)) {
            for (String line : lines) {
                final String[] split = StringUtils.split(line, ',');
                final PluginCallInfo pluginCallInfo = new PluginCallInfo(split[0], NumberUtils.toInt(split[1]), NumberUtils.toLong(split[2]));
                pluginCallInfoMap.put(pluginCallInfo.getPluginId(), pluginCallInfo);
            }
        }

        // 读取插件信息
//        final ClassLoader classLoader = PluginManager.class.getClassLoader();
//        final Enumeration<URL> resources = classLoader.getResources("tool_plugin.properties");
        final Resource[] resources = applicationContext.getResources("classpath*:*.plugin.properties");
        for (Resource urlResource : resources) {
            final String filename = urlResource.getFilename();
            final String[] filenameParts = StringUtils.split(filename, ".");
            final EncodedResource encodedResource = new EncodedResource(urlResource, StandardCharsets.UTF_8);

            final Properties properties = PropertiesLoaderUtils.loadProperties(encodedResource);
            final String pluginId = filenameParts[0];
            final PluginRegister pluginRegister = new PluginRegister(pluginId);
            pluginRegister.setAuthor(properties.getProperty("author"));
            pluginRegister.setName(properties.getProperty("name"));
            pluginRegister.setDesc(properties.getProperty("desc"));
            pluginRegister.setHelp(properties.getProperty("help"));
            final String version = properties.getProperty("version");
            if (StringUtils.isNotBlank(version)) {
                pluginRegister.setVersion(new Version(version));
            }
            final String dependencies = properties.getProperty("dependencies");
            if (StringUtils.isNotBlank(dependencies)){
                final String[] split = StringUtils.split(dependencies, ",");
                pluginRegister.setDependencies(Arrays.asList(split));
            }

            PluginCallInfo pluginCallInfo = pluginCallInfoMap.get(pluginId);
            if (pluginCallInfo == null){
                pluginCallInfo = new PluginCallInfo(pluginId,0,System.currentTimeMillis());
            }
            final EnhancePlugin enhancePlugin = new EnhancePlugin(pluginRegister, pluginCallInfo);
            pluginRegisterMap.put(pluginId,enhancePlugin);
        }
    }

    public static final long FIVE_MINUTES = 5 * 60 * 1000;

    public Map<String, EnhancePlugin> getPluginRegisterMap() {
        return pluginRegisterMap;
    }
}
