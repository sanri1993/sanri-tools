## Kafka模块插件介绍 

Kafka 模块提供 kafka 集群的主题，消费组的数据监控以及主题数据模拟，依赖于 `tools-zookeeper`,`tools-serializer` 模块

### 主题管理

### 消费组管理

* 消费组列表,删除消费组,消费组消费的主题列表,消费组消费主题某分区消费数据情况,消费组消费主题某分区附近的数据
* 主题列表,主题尾部数据,创建主题,删除主题,模拟数据发送
* 主题数据实时监控,brokers 列表
* 所有的数据消费或监控都可以使用序列化配合类加载器来展示数据明文
* JMX 监控,详情见类 BrokerTopicMetrics 

![kafka消费组](http://pic.yupoo.com/sanri1993/12de473e/e6616cfd.png)

![kafka数据查询](http://pic.yupoo.com/sanri1993/6c895960/532fb639.png)