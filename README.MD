# 9420 开发工具包
---
sanri-tools-maven 是一个开源的用于企业开发的工具包，重点想解决项目开发中一些数据可视化,监控的问题

目前已经提供一些常用的工具,读者也可以开发自己的工具注册进来 ,欢迎 pr 或者提 issue 

## Giteye chart
[![Giteye chart](https://chart.giteye.net/gitee/sanri/sanri-tools-maven/RMY45L94.png)](https://giteye.net/chart/RMY45L94)

---

## 工具理念

1. 轻量级,快速启动,只依赖于文件系统,可以自行决定要不要添加权限模块 
2. 多模块,可插拨设计,只加载想要的工具
3. 做数据监控和数据的可视化工具
4. 尽量做到开发中用到的中间件一体式监控和数据查看

## 适用人群
* 本工具适用群体是开发, 不是运维监控, 也不适用于监控生产数据

## 更新记录(代码库重要功能更新记录, 不是发行版)

* 2021.11.09 增加 Redis 树形 Key
* 2021.11.01 增加 git 增量工具
* 2022.01.01 增加权限管理

## 如何运行

### 方式一下载源码启动 
* issue 因为 tools-name 模块中的 ik 分词在中央仓库是没有的,所以你如果不需要这个模块,可以直接删除此模块, 或者你想要这个模块可以安装 ik 分词依赖,包和安装脚本都在 tools-name/src/main/resources 中 

解决 ik 分词问题后,就可以直接运行 tools-console 中的 com.sanri.tools.Application 启动 ,标准的 springboot 项目

### 方式二下载 release 启动

[https://gitee.com/sanri/sanri-tools-maven/releases](https://gitee.com/sanri/sanri-tools-maven/releases)

2.0.0 版本之前,下载的 release 为 tomcat ,双击 bin/start.bat 启动

@since 2.0.0  java -Xms256m -Xmx256m -Ddata.path.configs=配置文件存储路径 -Ddata.path.tmp=临时文件存储路径 -jar  sanritools.jar 

@since 2.2.0 java -Dloader.path=sanritoolslib --data.path.configs=配置文件存储路径 --data.path.tmp=临时文件存储路径 -Xms256m -Xmx256m -jar sanritools.jar

sanritoolslib [下载地址](https://gitee.com/sanri/sanri-tools-maven/attach_files/877457/download)

@since 2.4.0 要区分有权限和无权限版本, 需要将下载的 jar 包放到一个文件夹下进行合并

有权限版本: java -Dloader.path=(baselib + securitylib) --data.path.base=数据基础路径 -Xms256m -Xmx256m -jar sanritools-security.jar
无权限版本: java -Dloader.path=(baselib) --data.path.base=数据基础路径 -Xms256m -Xmx256m -jar sanritools.jar

### 方式三 , 前端嵌入后台中启动 (推荐)
这种方式适合于后端人员需要调试工具时使用

* 在 tools-console/src/main 文件夹中建一个目录 webapp ,并标识为 web 
* 解压 tools-console/webapp.zip 放到上一步建的 webapp 目录
* 运行 tools-console 中的 Application.main() , 就可以做后台的开发调试了
* 访问路径  http://localhost:8084

注: tools-console 的 webapp.zip 是一个比较旧的版本(20201211),后面的打包版本会放在[前端项目Release](https://gitee.com/sanri/sanritoolsvue/releases)

### 默认路径和端口

```shell
配置目录(data.path.configs):    /tmp/sanritools/configs
临时目录(data.path.tmp):        /tmp/sanritools/temp
数据目录(data.path.data):       /tmp/sanritools/temp
日志目录(logger.path):          /tmp/sanritools/logs
后端端口(server.port):          8084
```

## 版本和文档说明

当前工具的版本在 `tools-console/src/main/resources/version` 文件中, 使用 3 位版本号, 主版本.次版本.bug更新

各模块版本信息和描述信息在模块的 `resources/tool_plugin.properties` 中注册进来(名称,版本,作者,依赖项,帮助文档), 
帮助文档通过链接来指定, http 开头时为网络帮助文档, 否则在本地 resources 中查找 

* [核心模块](tools-core/src/main/resources/tools-core-introduce.md)
* [数据库模块](tools-database/src/main/resources/tools-database-introduce.md)
* [Kafka数据监控](tools-kafka/src/main/resources/tools-kafka-introduce.md)
* [代码增量工具](tools-patchcode/src/main/resources/tools-patchcode-introduce.md)
* [Redis监控工具](tools-redis/src/main/resources/tools-redis-introduce.md)
* [权限管理](tools-security/src/main/resources/tools-security-introduce.md)

## 关于单元测试
工具使用了更方便的单元测试,直接发送 http 请求来测试,所有的单元测试都在 requests/模块名.http 文件中

**如果添加了权限, 则需要这样发起请求**

1. 先调用登录请求, 它在 `tools-security.http` 请求里面的第一个请求
2. 然后在请求头中添加 token 信息: `Authorization: {{Authorization}}`

## 关于项目前端
官方前端 [https://gitee.com/sanri/sanritoolsvue](https://gitee.com/sanri/sanritoolsvue)

如果对这个前端不满意,可以使用接口进行自定义开发, 本项目只是提供数据服务, 可以通过运行 `smart-doc` 获取接口文档 

**官网前端只会支持最新版本, 不会对旧版本做兼容**

## 如何交流、反馈、参与贡献？

* Git仓库：https://gitee.com/sanri/sanri-tools-maven
* 技术讨论、二次开发等咨询、问题和建议，请移步到微信群，我会在第一时间进行解答和回复
* 如需关注项目最新动态，请Watch、Star项目，同时也是对项目最好的支持
* 微信扫码并关注我，获得项目最新动态及更新提醒,拉你进入微信技术群
* 如果有需要支持的中间件,可以提交 pr, 或者微信联系我

![我的微信](https://images.gitee.com/uploads/images/2020/0802/183913_c89fb735_409739.jpeg)

