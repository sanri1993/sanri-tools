# 9420 开发工具包
---
sanri-tools-maven 是一个开源的用于企业开发的工具包，重点想解决项目开发中一些数据可视化,数据监控,数据调用的问题

目前已经提供一些常用的工具,读者也可以开发自己的工具注册进来 ,欢迎 pr 或者提 issue 

---

## 文档

* 文档地址: https://sanri.gitee.io/sanri-tools-maven
* javaDoc: 稍后生成 

## 工具理念和定位

1. 轻量级,快速启动,只依赖于文件系统,可以自行决定要不要添加权限模块 
2. 多模块,可插拨设计,只加载想要的工具
3. 工具定位
   * 数据可视化, Redis, Kafka 之类的数据明文展示
   * 数据监控, 一些服务器信息, jvm 信息的监控
   * 数据调用, 可以进行无环境的数据调用, 如 Dubbo , Soap, Mybatis 
   * 代码生成, 模板代码, 通用项目的代码生成
4. 本工具主要适用于开发, 不能用于监控生产数据

## 更新记录(代码库重要功能更新记录, 不是发行版)

* 2021.11.09 增加 Redis 树形 Key
* 2021.11.01 增加 git 增量工具
* 2022.01.01 增加权限管理
* 2022.03.01 数据库模块重构

## 快速开始
**说明:** 这里只会说当前发布最新版本的运行说明, 旧版本请参考对应的文档, 文档命名为(help/QuickStart-版本号.MD)

1. 下载主运行包和依赖 jar 包
  主运行包下载 [去下载](https://gitee.com/sanri/sanri-tools-maven/releases)
  
  sanritoolslib [下载地址](https://cowtransfer.com/s/0df01e65aaca4f)

2. 启动 jar  
```
java -Dloader.path=sanritoolslib -Xms256m -Xmx256m -jar sanritools.jar
```

## 版本和文档说明
当前工具的版本在 `tools-console/src/main/resources/version` 文件中, 使用 3 位版本号, 主版本.次版本.bug更新

各模块版本信息和描述信息在模块的 `resources/tools-[模块名].plugin.properties` 中注册进来(名称,版本,作者,依赖项,帮助文档), 
帮助文档通过链接来指定, http 开头时为网络帮助文档, 否则在本地 resources 中查找 

* [核心模块](tools-core/src/main/resources/tools-core-introduce.md)
* [数据库模块](tools-database/src/main/resources/tools-database-introduce.md)
* [Kafka数据监控](tools-kafka/src/main/resources/tools-kafka-introduce.md)
* [代码增量工具](tools-patchcode/src/main/resources/tools-patchcode-introduce.md)
* [Redis监控工具](tools-redis/src/main/resources/tools-redis-introduce.md)
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

## 如何二开
详情见[二开文档](help/contribute.md)

## 如何交流、反馈、参与贡献？

* Git仓库：https://gitee.com/sanri/sanri-tools-maven
* 技术讨论、二次开发等咨询、问题和建议，请移步到微信群，我会在第一时间进行解答和回复
* 如需关注项目最新动态，请Watch、Star项目，同时也是对项目最好的支持
* 微信扫码并关注我，获得项目最新动态及更新提醒,拉你进入微信技术群
* 如果有需要支持的中间件,可以提交 pr, 或者微信联系我

![我的微信](https://images.gitee.com/uploads/images/2020/0802/183913_c89fb735_409739.jpeg)

