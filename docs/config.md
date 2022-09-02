## 系统级配置

如果需要修改系统的一些固定参数，你可以在 jar 包的启动目录新建一个 `config` 目录，并添加 `application-deploy.yml` 文件来覆盖默认配置

### 修改启动端口号

在 `application-deploy.yml` 中添加配置 

```yml
server
  port: 8084
```



### 修改系统的数据存储位置

系统产生的数据主要分为三种，配置数据，临时数据，数据，日志信息，你可以在 `application-deploy.yml` 中覆盖默认的路径配置

```yaml
data:
  path:
    configs: /tmp/sanritools/configs
    data: /tmp/sanritools/data
    tmp: /tmp/sanritools/tmp
logging:
  path: /tmp/sanritools/logs
```

### 不加载全量工具

在下载的 jar 包中删除不需要的工具即可，但需要注意依赖关系，详情可咨询作者。

## 用户级配置

### 新建连接

【基础数据】- 【安全连接管理】-  【(对应模块) 查看连接】- 【新建(模块)连接】