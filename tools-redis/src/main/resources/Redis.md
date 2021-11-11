## Redis

### 界面美图 

![Redis](../../../../images/Redis.png)

![Redis-2](../../../../images/Redis-2.png)

![Redis-3](../../../../images/Redis-3.png)

### 功能支持

* 集群，主从，单机数据查询和管理
* 客户端连接管理，每个客户端占用的连接数查询
* 每台主机占用内存情况,慢查询监控
* 节点拓扑结构查询
* 数据查询，支持 string，list，hash，set，zset ；序列化方式可选 json，jdk ，kryo，hex，hessian
* set 数据类型支持集合的交，并，差操作
* 批量删除 key 
* 支持树状结构的 key 展示, 以 key 模式删除 key 

### 主要实现逻辑
1. RedisNode 类做为主要功能类, 表示一个 Redis 节点, 在刷新他的连接数据的时候, 会一直往上找到父节点, 一直往下找到所有的子节点
2. RedisConnection 类; 表示一个连接, 这个连接有可能是集群模式, 有可能是单机或主从模式
3. ClusterNode 类; 表示集群节点,把集群当一个整体来看待
4. RedisConnection 聚合 ClusterNode 和 RedisNode 分支表示集群和非集群,提供 redis 的相关操作
5. 集群 key 扫描时,需要把所有 master 节点扫描一遍,所以下次扫描时会要带上上次未扫描完的节点Id 

