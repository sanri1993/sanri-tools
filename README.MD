# 9420 开发工具包
---
sanri-tools-maven 是一个开源的用于企业开发的工具包，重点想解决项目开发中一些比较麻烦的问题

目前已经提供一些常用的工具,读者也可以开发自己的工具注册进来 ,欢迎 pr 或者提 issue 

## Giteye chart
[![Giteye chart](https://chart.giteye.net/gitee/sanri/sanri-tools-maven/RMY45L94.png)](https://giteye.net/chart/RMY45L94)

---

## 工具理念

1. 轻量级,只依赖于文件系统,可以自行决定要不要添加权限模块 
2. 多模块,可插拨设计,只加载想要的工具
3. 做数据监控和数据的可视化工具
4. 尽量做到开发中用到的中间件一体式监控和数据查看

### 想对读者说的话
* 欢迎来提交 issue 或者 pr , 不要在评论中说还有一堆问题之类的, 因为你这样说我也不知道哪里有问题, 评论中也没办法截图, 加入官方群来说也行
* 本工具适用群体是开发, 不是运维监控, 也不适用于监控生产数据

### 更新记录(代码库重要功能更新记录, 不是发行版)

* 2021.11.09 增加 Redis 树形 Key
* 2021.11.01 增加 git 增量工具

### 计划中的模块 
**目前已经经过公司实践并且功能强大的模块有**

* tools-redis
* tools-kafka
* tools-database
* tools-patchcode

**计划加强的模块有**
* tools-tcp
* tools-mybatis

**其它计划模块**

欢迎网友提供点子 
    
### 每一个模块的使用文档
对于一个工具来说,使用文档是比较重要的,使用文档放到每个模块的 resources 中, 然后通过 PluginManager 注册进来

在首页会读取使用文档,并展示给用户,你可以在 md 文件中做外链,以便链接外部说明文档

## 如何运行

### 方式一下载源码启动 
* issue 因为 tools-name 模块中的 ik 分词在中央仓库是没有的,所以你如果不需要这个模块,可以直接删除此模块, 或者你想要这个模块可以安装 ik 分词依赖,包和安装脚本都在 tools-name/src/main/resources 中 

解决 ik 分词问题后,就可以直接运行 tools-console 中的 com.sanri.tools.Application 启动 ,标准的 springboot 项目

### 方式二下载 release 启动

[https://gitee.com/sanri/sanri-tools-maven/releases](https://gitee.com/sanri/sanri-tools-maven/releases)

2.0.0 版本之前,下载的 release 为 tomcat ,双击 bin/start.bat 启动

@since 2.0.0  java -Xms256m -Xmx256m -Ddata.path.config=配置文件存储路径 -Ddata.path.tmp=临时文件存储路径 -jar  sanritools.jar 

@since 2.2.0 java -Dloader.path=sanritoolslib --data.path.config=配置文件存储路径 --data.path.tmp=临时文件存储路径 -Xms256m -Xmx256m -jar sanritools.jar

sanritoolslib [下载地址](https://gitee.com/sanri/sanri-tools-maven/attach_files/877457/download)

**如何查看当前正在开发的工具版本**

当前工具的版本在 tools-console/src/main/resources/version 文件中, 各模块的版本后面会写在 pluginDto 中注册进来

### 方式三 , 前端嵌入后台中启动 (推荐)
这种方式适合于后端人员需要调试工具时使用; 

* 在 tools-console/src/main 文件夹中建一个目录 webapp ,并标识为 web 
* 解压 tools-console/webapp.zip 放到上一步建的 webapp 目录
* 运行 tools-console 中的 Application.main() , 就可以做后台的开发调试了
* 访问路径  http://localhost:8084

注: tools-console 的 webapp.zip 是一个比较旧的版本(20201211),后面的打包版本会放在[前端项目Release](https://gitee.com/sanri/sanritoolsvue/releases)

### 使用工具(此处只是简单描述,更加详细的说明在 WIKI 或者 issue 中查找)
* 创建连接

  1. 基础数据->连接管理->新模块( 可以创建 database, redis, kafka, mongo,zookeeper, git) 等模块 
  2. 在模块里新建连接, 这个连接是有模板的, 其中, 数据库连接中的 dbType 可以是 (mysql, postgresql, oracle ) 之中的一种 
  
* 重要可变更参数说明(默认情况, 可以自行修改; **使用工具前一定要先行创建这些目录, 否则无法使用**)

```shell
配置目录: /tmp/sanritools/configs
临时目录: /tmp/sanritools/temp
外部lib: /tmp/sanritools/libs
日志目录: /tmp/sanritools/logs
后端端口: 8084
```

### 关于单元测试
工具使用了更方便的单元测试,直接发送 http 请求来测试,所有的单元测试都在 requests/模块名.http 文件中

**如果添加了权限, 则需要这样发起请求**

1. 先调用登录请求, 它在 `tools-security.http` 请求里面的第一个请求
2. 然后在请求头中添加 token 信息: `Authorization: {{Authorization}}`

### 关于项目前端
官方前端 [https://gitee.com/sanri/sanritoolsvue](https://gitee.com/sanri/sanritoolsvue)

前端美图 [https://gitee.com/sanri/sanri-tools-maven/issues/I1UEJR](https://gitee.com/sanri/sanri-tools-maven/issues/I1UEJR)

如果对这个前端不满意,可以使用接口进行自定义开发, 本项目只是提供数据服务

## 如何交流、反馈、参与贡献？

* Git仓库：https://gitee.com/sanri/sanri-tools-maven
* 官方前端：[https://gitee.com/sanri/sanritoolsvue](https://gitee.com/sanri/sanritoolsvue)
* 技术讨论、二次开发等咨询、问题和建议，请移步到微信群，我会在第一时间进行解答和回复
* 如需关注项目最新动态，请Watch、Star项目，同时也是对项目最好的支持
* 微信扫码并关注我，获得项目最新动态及更新提醒,拉你进入微信技术群
* 如果有需要支持的中间件,可以提交 pr, 或者微信联系我

![我的微信](https://images.gitee.com/uploads/images/2020/0802/183913_c89fb735_409739.jpeg)

