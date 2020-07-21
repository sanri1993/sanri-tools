```
tool-console 用于聚合其它服务，并对前端提供统一的输出格式，异常处理

tools-core 主要提供配置文件服务，临时文件服务，类加载器，随机数据生成 

tools-database 提供关系型数据库服务，支持 mysql,oracle,postgresql

tools-protocol  用于组件间调用时，引用统一输入输出协议 

tools-serializable  用于需要序列化的组件，如 redis kafka 工具

tools-kafka kafka 工具
 |- tools-zookeeper
   |- tools-core
     |- tools-protocol 
     |- tools-serializable
 |- tools-websocket 用于实时监听主题消费
   |- tools-core
     |- tools-protocol 
     |- tools-serializable
 
tools-redis redis 工具
 |- tools-core
   |- tools-serializable
   
tools-websocket 如果需要实时向前端发送数据，引入这个包

```
每一个模块自己提供数据访问控制器，如被 console 聚合，则能提供相应功能 