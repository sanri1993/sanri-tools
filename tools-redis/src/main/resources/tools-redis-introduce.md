## Redis模块插件介绍 

Redis 模块提供 redis 的 key列表，key 数据查询，慢查询监控

### 功能支持

* 支持集群,主从和单机,展示节点主机,端口,角色,父节点; 集群可以展示槽位分配 
* key 列表查询,可以查询集群所有节点,hashKey 列表查询,key 的长度,ttl , type 展示
* 批量删除 key , 根据前缀删除 key(这个在树状 KEY 标签页提供支持)
* 查询某个 key 的数据,可以根据序列化和类加载器展示成友好的方式,支持(string,hash,list,set,zset)
* 在线求 set 类型的数据交集,并集,差集
* 监控每一个节点使用的内存信息
* 监控每一个节点的连接数,主机占用连接数信息,杀掉某个连接
* 监控每一个节点的慢查询
* 树状结构展示 Key 列表, 这个对于用习惯了 RDM 的人可能是个必须项,目前我的工具也提供了支持; 但其实这种展示结构只能处理当 key 数量比较少的时候, RDM 也只是 scan 了前 10000 条数据来展示树,对于生产环境还会卡死
* 注: 快速搜索(现改名单KEY)功能不一定快速, 做这个功能的目的是和精确数量来比较的,如果要精确数量,
那我每次 scan 的 limit 参数必须较正, 一次搜索的数量就少了, 但如果不需要精确数量的话 , 我可以一次 limit 10000 条,可以加快搜索速度 , 
这个适用于用户知道了很精确的 key ,对于一个有 100 万 key 的集群来说搜索会快很多

### 主要实现逻辑
1. RedisNode 类做为主要功能类, 表示一个 Redis 节点, 在刷新他的连接数据的时候, 会一直往上找到父节点, 一直往下找到所有的子节点
2. RedisConnection 类; 表示一个连接, 这个连接有可能是集群模式, 有可能是单机或主从模式
3. ClusterNode 类; 表示集群节点,把集群当一个整体来看待
4. RedisConnection 聚合 ClusterNode 和 RedisNode 分支表示集群和非集群,提供 redis 的相关操作
5. 集群 key 扫描时,需要把所有 master 节点扫描一遍,所以下次扫描时会要带上上次未扫描完的节点Id 

### 界面

![redis分模块](http://pic.yupoo.com/sanri1993/a79dec14/f353f7d9.png)

![Redis](http://pic.yupoo.com/sanri1993/12057b1b/87719dca.png)

![Redis-2](http://pic.yupoo.com/sanri1993/c8184c61/e5b8a764.png)

![Redis-3](http://pic.yupoo.com/sanri1993/e4214600/55f375bc.png)