# 数据存储

目前所有数据均使用文件存储，目的是为了更少的依赖，和更快的启动速度，一个工具类型的应用这两点其实是比较重要的。

## 存储的目录结构

系统主要存储 3 大类文件， 分别是 配置(configs)，数据(data)和临时数据(temp)，你可以在 `application-*.yml` 中看到我分别把他们存储到哪里了，配置大概像这样。

```yml
data:
  path:
    configs: /tmp/sanritools/configs
    tmp: /tmp/sanritools/temp
    data: /tmp/sanritools/data
```

其中配置目录有一个单独的文件夹用来存储连接信息，连接信息的存储结构详情看 [连接管理](/connect)