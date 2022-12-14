## 核心模块插件介绍 

核心模块用于提供系统最基础的功能，包括安全连接管理，类加载器管理，插件管理，临时文件管理，随机生成数据

### 功能支持

* 类加载管理 上传类 zip , class, java 文件到类加载器
* 连接管理 创建 zookeeper, redis, database, kafka 等连接
* 文件管理 系统的临时文件和配置文件的管理,提供临时文件下载功能 
* 插件管理 插件注册,提供给前端可访问的插件,做了一个监控指标,根据插件点击次数和最后访问时间来排序插件
* 数据管理 可以给 class 随机生成数据,使用正则表达式生成数据,和爬网站数据功能

### 安全连接管理

用于管理系统中的所有连接，每个模块注册上来后， 都来这里创建连接，可支持任意配置格式，但是解析需要子模块自己支持

#### 数据操作

如果添加了权限模块， 这个连接将会是安全的， 使用组织来区分每个人可见的模块和连接

![安全连接管理](http://pic.yupoo.com/sanri1993/ff0b5e54/94225e3c.png)

左边列表表示的是模块列表，每当有一个模块注册上来时，就会在左边显示， 然后可以在右边新建连接； **模块注册上来时就已经说明了该模块的配置模板，模块，格式信息**

在右侧进行添加连接，添加连接界面如图，以添加 Redis 连接为例

![新建Redis连接](http://pic.yupoo.com/sanri1993/380ee23d/12704c6f.png)

Redis 模块有一个 json 的配置模板， 所以这里展示出来是 Redis 的 json 配置，需要填写的信息为基础名，组织，和配置信息； 组织是为了权限区分时用的（必填）， 当没有权限时， 组织默认为 `/`

这里的模板文件在 [tools-redis/src/main/resources/connect.redis.template.json](tools-redis/src/main/resources/connect.redis.template.json), 取名规则 为 `connect.模块名.template.配置格式`

目前支持的模块有 `database`,`zookeeper`,`redis`,`kafka`,`git`,`fastdfs`,`maven`,`jvm`,`rocketmq`

* redis 不需要使用者区分是集群环境还是主从还是单点 ,只需要填写一个节点的信息即可
* zookeeper 也不需要区分是集群环境还是单点, 只需要输入一个节点信息
* kafka 是以 zookeeper 为基础的 ,当建立 kafka 连接时,需要保持连接名和 zookeeper 的连接名一致；kafka 的连接是 yaml 格式,和 spring 配置文件保持一致即可
* database 完整支持 mysql,oracle,postgresql, 并部分支持所有能支持 JDBC 的数据库
* git 用于 git 仓库代码打增量 class 包使用 

### 类加载器管理

类加载器用于加载系统外的类， 用于将获取到的数据进行序列化和反序列化操作，这也是本程序主要卖点之一，将数据明文友好的展示

#### 技术点说明

* 扩展了 `URLClassLoader` 可以自己上传带名称的类加载器，通过反射取出加载的类信息，动态添加 url 类加载路径
* 使用了 cfr 工件， 实现了像 jad 那样的反编译工具
* 可以分析 pom 文件结构 ， 并下载所有依赖包来加载需要的 jar 
* 可以分析 java 文件结构， 手动编译 java 文件来加载 class 

#### 数据操作

可以在类加载器中上传 `java`，`class`，`zip`，`jar`，`pom.xml`

![类加载器管理新](http://pic.yupoo.com/sanri1993/6b3ed757/0eeba0b4.png)

下拉选择可以选择是哪一个类加载器，然后下面展示的是当前加载加载器加载的类， 和右边当前第一个类的反编译结果

![类加载器-单文件上传](http://pic.yupoo.com/sanri1993/85bf0189/0d469ff9.png)

上传单个文件，将 class 加载到对应类加载器；类加载器填写不存在的类加载器时将新建一个类加载器



![类加载器-多文件上传](http://pic.yupoo.com/sanri1993/66f916ed/c92f1b13.png)

更多的情况是， 有多个 class 需要一起上传， 这些 class 可能还有一些依赖的 jar 包， 有时这些 jar 包不方便拿， 可以直接从 maven 下载； 当然， 这需要你提前配置好 maven 仓库 `application-repository.yml` 文件中的 `maven.config.repositories` ， 目前默认提供了华为的镜像



![类加载器-pom添加工件](http://pic.yupoo.com/sanri1993/e58c6a28/d9532efb.png)

如果项目管理得当的话， 可以直接引用一个 jar 包， 就能获取到所有的实体类， 这个maven 仓库配置到公司仓库时， 可以直接拉取公司的某个实体 jar 包

### 插件管理

其实这个不应该叫管理， 这个只能查看现在注册了哪些插件，然后可以查看这个插件的说明文档， 现在你看到的就是 `tools-core` 这个模块的说明文档， 由 `tools-core` 注册进去的

![插件列表](http://pic.yupoo.com/sanri1993/7eb14e23/3256f348.png)

每访问一次插件， 会记录最新的访问时间和累加访问次数， 然后这个列表的排序首先是按照前 5 分钟的时间排序， 然后按照访问次数排序

### 临时文件管理

系统临时目录中的文件，可以将下载目录中的文件或者删除目录

![临时文件管理](http://pic.yupoo.com/sanri1993/290128f9/0b71e01b.png)

### 随机数据生成

可根据需要， 使用类生成， 正则表达式生成， 或者爬网页数据 

![随机数据生成](http://pic.yupoo.com/sanri1993/e8765ea8/239397d0.png)
