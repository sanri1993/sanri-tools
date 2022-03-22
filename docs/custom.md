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
```
3. 尝试能不能运行成功, 并运行版本号接口 tools-console.http 中的第一个接口
4. 下载最新版本前端 [去下载](https://gitee.com/sanri/sanritoolsvue/releases)
5. 替换 webapp 里面的前端，并标识 webapp 为 web 项目
6. 重新启动项目，尝试访问前端 `http://localhost:8084`

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

**注册插件**

如果需要把你的插件展示到首页上来，你需要提供 `tools-[模块名]_plugin.properties` 文件到 classpath 