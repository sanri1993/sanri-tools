# 自定义插件开发

虽然我已经提供了常用的中间件，但不一定能满足所有用户的开发情况，大多数情况下，用户需要开发自己的插件。


## 环境搭建

1. clone 仓库
```shell 
git clone https://gitee.com/sanri/sanri-tools-maven.git
```
2. 安装外部 jar 包 
```
复制 tools-name/src/main/resources/install.txt 中的内容在控制台执行
复制 tools-fastdfs/src/main/resources/install.txt 中的内容在控制台执行
```
3. 尝试能不能运行成功, 并运行版本号接口 tools-console.http 中的第一个接口
4. 下载最新版本前端 [去下载](https://gitee.com/sanri/sanritoolsvue/releases)
5. 将前端解压并放在用户目录 public 下面
6. 启动后端, 访问 http://localhost:8084/public/index.html

## 前端环境搭建(前后台分离)

1. clone 仓库
```shell
git clone https://gitee.com/sanri/sanritoolsvue.git
```
2. 安装 
```shell
npm install 
```
3. 启动
```shell
npm run dev 
```
4. 访问地址 `http://localhost:9527`


## 模块开发

新建一个模块，命名为 `tools-*`， 包路径 `com.sanri.tools.modules.模块名`， 然后就可以开始你的开发，将服务提供出来。

最后将模块添加到 tools-console/pom.xml 中就开发了一个新的模块

### 使用平台提供的功能

**新建连接**

如果你的模块需要新建连接，则你需要创建一个文件在 classpath 目录下，文件名为 `connect.模块名.template.配置格式`

**使用 tools-core 中的能力**

如果需要使用到 `tools-core` 中的能力，则需要引入 tools-core 模块，它提供了随机数，文件存储服务，类加载器等功能

**根据名称获取一个类加载器**
```java
class Test{
    @Autowired
    private ClassloaderService classloaderService;

    ClassLoader classLoader = classloaderService.getClassloader(classloaderName);
}
```

**根据名称获取一个序列化工具**
```java
import com.sanri.tools.modules.serializer.service.Serializer;

class Test{
    @Autowired
    private SerializerChoseService serializerChoseService;

    Serializer serializer = serializerChoseService.choseSerializer(serializerParam.getHashKey());
}
    
```

**根据名称获取用户创建的连接信息**

这里获取的数据都是字符串信息, 需要用户自己将数据转化成存储的数据结构 

```java
class Test{
    @Autowired
    private ConnectService connectService;

    String database = connectService.loadContent("database", connName);
}
```

**注册插件**

如果需要把你的插件展示到首页上来，你需要提供 `tools-[模块名]_plugin.properties` 文件到 classpath 

**提供菜单信息**

如果需要向前端提供菜单, 需要提供配置文件 `tools-[模块名].menus.conf` 到 classpath 

### 接口调试
工具使用了更方便的单元测试,直接发送 http 请求来测试,所有的单元测试都在 requests/模块名.http 文件中

**如果添加了权限, 则需要这样发起请求**

1. 先调用登录请求, 它在 `tools-security.http` 请求里面的第一个请求
2. 然后在请求头中添加 token 信息: `Authorization: {{Authorization}}`

### 如何控制权限
在 resources 目录中添加一个权限配置文件 `authority.conf`

配置格式参考 [tools-security](../tools-security/src/main/resources/tools-security-introduce.md) 中介绍的配置说明 

### 打包部署时注意事项 

在 build 中的 spring-boot-maven-plugin 插件中, 需要将自己的模块包含到打包目录, 并且 lib 包中也要更新新引入的 jar 包

## 技术指导 
联系 sanri 

![我的微信](https://images.gitee.com/uploads/images/2020/0802/183913_c89fb735_409739.jpeg)

## 提交贡献
如果开发的功能具有通用性时, 欢迎将开发的模块提交上来, 一起完善