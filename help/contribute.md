## 工具设计说明

每一个模块都是单独提供数据服务的, 然后依赖于 `tools-core` 的一些基础功能, 提供的基础功能包含 

- 连接管理
- 文件管理
- 类加载器管理

### 工具模块间依赖关系

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

## 开发自己的工具
```
1. clone 项目
git clone https://gitee.com/sanri/sanri-tools-maven.git
2. 安装外部 jar 包到本地仓库
tools-name/src/main/resources 执行 install.txt 
3. 尝试能不能运行成功, 并运行版本号接口 tools-console.http 中的第一个接口
4. 新建自己模块并开发
5. 将自己模块加到 tools-console 中的 pom.xml  就可以调试了
6. 将插件注册上来, 添加文件 tool_plugin.properties , 内容参考任何一个模块即可
```

### 接口调试
工具使用了更方便的单元测试,直接发送 http 请求来测试,所有的单元测试都在 requests/模块名.http 文件中

**如果添加了权限, 则需要这样发起请求**

1. 先调用登录请求, 它在 `tools-security.http` 请求里面的第一个请求
2. 然后在请求头中添加 token 信息: `Authorization: {{Authorization}}`

### 如何接入前端
下载前端的发布包 [去下载](https://gitee.com/sanri/sanritoolsvue/releases)

- 在 tools-console/src/main 文件夹中建一个目录 webapp ,并标识为 web
- 将前端包 dist 中的内容放到 webapp 目录 
- 启动项目, 访问 http://localhost:8084

注意: 前端只会支持最新版本的后端, 并且这个前端现在全是后端开发出来的, 比较丑, 有能力者可以自研前端

### 如何让自己的模块可以新建连接
在 resources 目录中添加一个连接配置模板文件 `connect.模块名.template.配置格式`

### 如何把自己的模块在前端菜单中展示
在 resources 目录中添加一个菜单配置文件 `menus.conf` 和一个资源文件 `resources.conf`

配置格式参考 [tools-security](../tools-security/src/main/resources/tools-security-introduce.md) 中介绍的配置说明 

### 如何控制权限
在 resources 目录中添加一个权限配置文件 `authority.conf`

配置格式参考 [tools-security](../tools-security/src/main/resources/tools-security-introduce.md) 中介绍的配置说明 

## 技术指导 
联系 sanri 

![我的微信](https://images.gitee.com/uploads/images/2020/0802/183913_c89fb735_409739.jpeg)

## 提交贡献
如果开发的功能具有通用性时, 欢迎将开发的模块提交上来, 一起完善 