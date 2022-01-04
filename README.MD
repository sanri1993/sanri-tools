# 9420 开发工具包
---
sanri-tools-maven 是一个开源的用于企业开发的工具包，重点想解决项目开发中一些比较麻烦的问题

目前已经提供一些常用的工具,读者也可以开发自己的工具注册进来 ,欢迎 pr 或者提 issue 

## Giteye chart
[![Giteye chart](https://chart.giteye.net/gitee/sanri/sanri-tools-maven/RMY45L94.png)](https://giteye.net/chart/RMY45L94)

---

## 工具理念

1. 轻量级,只依赖于文件系统,无权限管理, 读者可以自行开发权限模块
2. 多模块,可插拨设计,只加载想要的工具
3. 做数据监控和数据的可视化工具,这个可以直接看到数据明文,而不是乱码
4. 尽量做到开发中用到的中间件一体式监控和数据查看

### 想对读者说的话
* 欢迎来提交 issue 或者 pr , 不要在评论中说还有一堆问题之类的, 因为你这样说我也不知道哪里有问题, 评论中也没办法截图, 加入官方群来说也行
* 本工具适用群体是开发, 不是运维监控, 也不适用于监控生产数据
* 对于学习者的意义
   * 项目中有一些不错的算法, 后面我会将有算法的写在对应模块的 resources/README.MD 中, 大家有兴趣可以查看,并且可以提供更优的算法给我(提交 pr)
   * 一些插件的 api 的使用
   * 中间件的一些大致部署结构和细节 

### 更新记录(代码库重要功能更新记录, 不是发行版)

* 2021.11.09 增加 Redis 树形 Key
* 2021.11.01 增加 git 增量工具

## 已经有的工具,分模块展示
2. tools-serializable
   * 提供序列化,反序列化等基础功能 
8. tools-swagger-doc
   * 将 swagger 的文档生成更美观的文档并提供下载文档
   * 注意: 这个工具尚不完善,输入框填入网络请求中 json 数据那个链接
9. tools-dubbo
   * 依赖于 zookeeper 模块,得到 dubbo 的提供者,获取提供的方法,并可以执行调用
10. tools-name
    * 想要给某个方法取名,但一下子想不到合适的名字,可以用这个工具帮你取名
    * 支持设置业务词,ik 分词,英语翻译
11. tools-soap
    * 支持 soap 调用
12. tools-websocket
    * 主要是给其它模块提供前端推送数据功能 
13. tools-quartz
    * 触发器和任务列表,cron 表达式以及后面 10 次的任务执行时间
    * 触发,停止,恢复,需要配合类加载器
    * 新建,移除任务,需要配合类加载器
14. tools-mongodb
    * 数据库列表,集合列表
    * 数据查询
15. tools-elasticsearch
    * 数据监控
    * 数据查询
16. tools-tcp
    * tcp-client 用于设备调试时,给服务端发送数据
    * tcp-server 用于设备调试时,模拟服务端
17. tools-patchcode


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

#### 说明下注意事项,对于刚开始使用这个工具的人来说
1. 连接管理中的模块名必须是固定的,只能取 database,kafka,redis,zookeeper,mongo,git 具体可以在 ConnectService.moduleParamFactory() 方法中查看
2. 数据库类型只支持 mysql,postgresql,oracle 具体可以在 DatabaseConnectParam 中查看
3. 类加载器 "上传 dto" 可以是单类文件,无目录结构,java 实体类,但不能是标准结构的; "上传标准 dto" 只能是标准结构类文件
4. 新建连接是有模板的,如果模板没有加载出来,检查模块名是否正确,可以重新打开来加载配置模板

### 工具模块间依赖关系,扩展自己的工具时可能需要用到

```
tools-core
 |- tools-serializable
 
tools-zookeeper
 |- tools-core

tools-kafka 
 |- tools-zookeeper
 |- tools-websocket
  
tools-dubbo
 |- tools-zookeeper
 
tools-redis
 |- tools-core
   
tools-database
 |- tools-core

tools-swagger-doc
 |- tools-core

tools-mybatis 
 |- tools-database

tools-name
 |- tools-core
 
tools-soap
 |- tools-core
```

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

## 如何扩展自己的工具
1. 新增一个模块,如 tools-xxx ,然后依赖于 tools-core 
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>tools-core</artifactId>
    <version>${project.version}</version>
</dependency>
```
2. 然后将模块使用插件管理注册进来, (在 2.4.0 之后废弃这种注册方式, 模块可以不依赖 tools-core)
```java
@Autowired
private PluginManager pluginManager;

@PostConstruct
public void register(){
    pluginManager.register(PluginDto.builder().module("tools-xxx").name("main").build());
}
```
module 在前端是做为一级菜单,然后 name 是二级菜单 , 同一个模块下可以注册多个功能项, 插件的开发和工作中开发 controller 没有区别 

**重要**
在 2.4.0 之后, 注册方式改为在 resources 目录下添加 `plugin.properties` 文件, 里面描述这个插件的详细信息;
一个模块只能注册一个插件, 因为不再用插件信息来做菜单, 菜单是菜单, 插件是插件, 这两者不能混淆
```
name=插件标题
author=sanri
dependencies=
desc=提供插件管理, 连接管理, 类加载器管理等功能
help=README.MD
```
其中, 依赖项列表使用逗号分隔每个依赖模块; 帮助文档可以是一个url, 也可以是本地 md 文件, 你需要把它描述成资源格式

3. 将开发的模块集成进工具
在 tools-console/pom.xml 中添加 
```java
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>tools-xxx</artifactId>
    <version>${project.version}< /version>
</dependency>
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

### 数据库的文档生成

数据库文档生成，每个公司要求的模板都不一样，可以自己扩展，使用 jdbcService 可以拿到你想要的元数据，然后想怎么生成，全靠你自己， 我这里提供了三个示例
* 生成 markdown 的 ，使用 database-doc.md.ftl 模板
* 生成 html 的，使用 database-doc.html.ftl 模板
* 生成 excel 的，实现类在 MetadataControllerShow.generate() 方法中

## 如何交流、反馈、参与贡献？

* Git仓库：https://gitee.com/sanri/sanri-tools-maven
* 官方前端：[https://gitee.com/sanri/sanritoolsvue](https://gitee.com/sanri/sanritoolsvue)
* 技术讨论、二次开发等咨询、问题和建议，请移步到微信群，我会在第一时间进行解答和回复
* 如需关注项目最新动态，请Watch、Star项目，同时也是对项目最好的支持
* 微信扫码并关注我，获得项目最新动态及更新提醒,拉你进入微信技术群
* 如果有需要支持的中间件,可以提交 pr, 或者微信联系我

![我的微信](https://images.gitee.com/uploads/images/2020/0802/183913_c89fb735_409739.jpeg)

