# 9420 开发工具包 

<img src="http://pic.yupoo.com/sanri1993/95547376/55af459b.png" width="80px" alt="logo">

---

[![star](https://gitee.com/sanri/sanri-tools-maven/badge/star.svg?theme=dark)](https://gitee.com/sanri/sanri-tools-maven/stargazers) [![fork](https://gitee.com/sanri/sanri-tools-maven/badge/fork.svg?theme=dark)](https://gitee.com/sanri/sanri-tools-maven/members)

sanri-tools-maven 是一个开源的用于企业开发的工具包，重点想解决项目开发中一些数据可视化,数据监控,数据调用的问题

目前已经提供一些常用的工具,读者也可以开发自己的工具 ,欢迎 pr 或者提 issue 

---

## 文档

* 文档地址: https://sanri.gitee.io/sanri-tools-maven

## 工具理念和定位

1. 轻量级,快速启动,只依赖于文件系统,可以自行决定要不要添加权限模块 
2. 多模块,可插拨设计,只加载想要的工具
3. 工具定位
   * 数据可视化, Redis, Kafka, Rocketmq 之类的数据明文展示
   * 数据监控, Fastdfs 的图片查看和文件下载
   * 数据调用, 可以进行无环境的数据调用, 如 Dubbo , Soap, Mybatis 
   * 数据库元数据管理, 代码生成, 模板代码, 通用项目的代码生成
   * 任意无界面中间件的监控, 数据管理
4. 本工具主要适用于开发, 不能用于监控生产数据

## 更新记录(代码库重要功能更新记录, 不是发行版)

[更新记录](tools-console/src/main/resources/update.md)

## 快速开始
**说明:** 这里只会说当前发布最新版本的运行说明, 旧版本请咨询作者

从 3.2.0 版本开始, 前端运行包(4M), 依赖包(90M), 和后端jar包(4M) 将分离, 因为之前的版本将前端包合并在后端包里无法快速修复前端 bug 

大致的目录结构可以这样放置, 然后运行后端, 前端会加载 public 目录下的前端文件, 下载完整包: <https://cowtransfer.com/s/54549a5602394d>

```
sanritoolslib(依赖包)
  fastjson.jar
  connect-mysql-java.jar 
  ....
public(前端包)
  index.html
  static
sanritools.jar(后端运行包)

启动命令, 极简形式
java -Dloader.path=sanritoolslib -jar sanritools.jar

指定内存, 守护进程方式启动
nohup java -Dloader.path=sanritoolslib -Xms256m -Xmx256m -jar sanritools.jar >/dev/null 2>&1 & 
```


## 版本和文档说明
当前工具的版本在 `tools-console/src/main/resources/version` 文件中, 使用 3 位版本号, 主版本.次版本.bug更新

各模块版本信息和描述信息在模块的 `resources/tools-[模块名].plugin.properties` 中注册进来(名称,版本,作者,依赖项,帮助文档), 
> 帮助文档通过链接来指定, http:// 开头时为网络帮助文档, 否则在本地 resources 中查找 

* [核心模块](tools-core/src/main/resources/tools-core-introduce.md)
* [数据库模块](tools-database/src/main/resources/tools-database-introduce.md)
* [Kafka数据监控](tools-kafka/src/main/resources/tools-kafka-introduce.md)
* [rocketmq数据监控](tools-rocketmq/src/main/resources/tools-rocketmq-introduce.md)
* [Redis监控工具](tools-redis/src/main/resources/tools-redis-introduce.md)
* [fastdfs](tools-fastdfs/src/main/resources/tools-fastdfs-introduce.md)
* [代码增量工具](tools-version-control/src/main/resources/tools-version-control-introduce.md)
* [权限管理](tools-security/src/main/resources/tools-security-introduce.md)
* [Mybatis工具](tools-mybatis/src/main/resources/tools-mybatis-introduce.md)
* [jvm工具](tools-jvm/src/main/resources/tools-jvm-introduce.md)
* [soapui](tools-soap/src/main/resources/tools-soap-introduce.md)

## 关于单元测试
工具使用了更方便的单元测试,直接发送 http 请求来测试,所有的单元测试都在 requests/模块名.http 文件中

**如果添加了权限, 则需要这样发起请求**

1. 先调用登录请求, 它在 `tools-security.http` 请求里面的第一个请求
2. 然后在请求头中添加 token 信息: `Authorization: {{Authorization}}`

## 关于项目前端
官方前端 [https://gitee.com/sanri/sanritoolsvue](https://gitee.com/sanri/sanritoolsvue)

如果对这个前端不满意,可以使用接口进行自定义开发, 本项目只是提供数据服务, 可以通过运行 `smart-doc` 获取接口文档 

**官网前端只会支持最新版本, 不会对旧版本做兼容**

![首页](http://pic.yupoo.com/sanri1993/3ae171e2/25570811.png)

## 如何二开
详情见[二开文档](docs/custom.md)

## 如何交流、反馈、参与贡献？

* Git仓库：https://gitee.com/sanri/sanri-tools-maven
* 技术讨论、二次开发等咨询、问题和建议，请移步到微信群，我会在第一时间进行解答和回复
* 如需关注项目最新动态，请Watch、Star项目，同时也是对项目最好的支持
* 微信扫码并关注我，获得项目最新动态及更新提醒,拉你进入微信技术群
* 如果有需要支持的中间件,可以提交 pr, 或者微信联系我

![我的微信](http://pic.yupoo.com/sanri1993/136eb7a3/c31cdd37.jpg)

## 捐赠
创作不易, 觉得工具好用的话, 可以帮打赏一下啊

![支付宝](http://pic.yupoo.com/sanri1993/0ee103ad/medium.jpg)![微信](http://pic.yupoo.com/sanri1993/b1c913bf/medium.jpg)