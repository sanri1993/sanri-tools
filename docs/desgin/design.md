## 总体设计

大多数情况下，并不是所有的工具每个人都需要使用，多余的工具会占用包大小，使系统出错，传输慢，启动慢问题，所以系统在一开始就设计成多模块，可插拨设计，包括核心包在内的所有模块都是可以删除的，但需要注意[包依赖](/desgin/dependency)。

对于数据的存储，我并不想依赖任何的存储工具，因为这会带来别的依赖项，我觉得 Linux 的设计很酷，所有的东西都可以看做是文件，所以我参考了 Linux 的设计，系统内所有的配置，数据都是以文件来存储的，这使得系统下载下来只需要一个 jre 环境就可以启动，并且这还带来了另一个好处，就是比较快的启动速度。


## 多模块设计

每个模块可以单独为前端提供 web 接口，只需将这个模块使用 maven 引入 tools-console 中即可。

为了提供模块的详细信息和使用说明文档，需要模块开发者在 classpath 添加一个模块的说明文件 `tools-模块名.plugin.properties`，这将在首页展示这个插件的信息，说明文档，并统计访问次数和最近访问时间。


## 存储设计

系统主要存储 3 大类文件，分别是配置(configs)，数据(data)和临时文件(temp)，你可以在 `application-*.yml` 中看到我分别把他们存储到哪里了，配置大概像这样。

```yml
data:
  path:
    configs: /tmp/sanritools/configs
    tmp: /tmp/sanritools/temp
    data: /tmp/sanritools/data
```

### 修改存储位置 

默认所有数据是放在 /tmp 目录的，在 linux 系统中，/tmp 中的文件隔一段时间会清理一次，这有可能导致刚配置的一些信息被清空，你可以在启动的时候将数据放到另一个地方去，方法就是

在 jar 包同级目录创建 config 文件夹，里面写入 application.yml，配置 3 大路径，利用 springboot 的配置文件加载顺序覆盖默认的路径信息。

> 可能你会觉得配置 3 个路径太繁琐，所以我提供了一个简单的写法，你可以只配置 data.path.base，3 大文件将以此为基础路径，并按照默认命名，配置信息将存储在 $base/configs, 临时文件在 $base/tmp, 数据文件在 $base/data

## 工具内部原理及设计

* [数据可视化](/desgin/visualization)
* [数据调用](/desgin/datacall)
* [连接管理](/desgin/connect)

