# RocketMQ使用文档

rocketmq 主要用于监控集群, 主题, 消费组信息, rocketmq 比较像 kafka, 大部分信息和 kafka 类似, 
我主要参考了 rocketmq-console-ng 写了此工具, 但比 kafka 不同的时, rocketmq 的消息消费和启动好像都很慢, 超时时间增加了 4 ~ 5 秒都经常超时
有人知道原因的话, 可以帮忙提交 pr 帮修复

## 集群
这个 tab 页主要展示集群和主机信息, 每个 broker 可以看到具体的统计数据,版本, 主从信息

rocketmq 的 broker 是一个主从的关系, 他的结构大致可以看成是这样子

```
clusterA
  brokerA
    ip1(master, 编号为 0 的主机为 master)
    ip2(slave)
  brokerB
    ip1(master)
    ip2(slave)
clusterB
  brokerC
    ip1(master)
```

## 主题

* 可以分类型查询主题, 可通过搜索框进行过滤
* 主题信息查看和删除
  * 主题配置: 主题分别在 broker 上的配置, 权限等
  * 主题状态: 可以看到主题有多少分区, 同时可以查询当前分区上的消息
  * 主题路由: 查看消息的路由（现在你发这个主题的消息会发往哪些broker，对应broker的queue信息）
  * 消费组: 有哪些消费组在消费这个主题
  * 发送消息: 可以直接向这个主题发送消息, 可以发 json和序列化数据

## 消费组

* 查询所有的消费组, 可通过搜索框进行过滤
* 消费组信息查看和删除
  * 消费组配置: 消费组分别在 broker 上的配置, 重试配置等
  * 消费组状态: 消费的主题和 offset 信息
  * 连接信息: 可以查看消费组的消费模式, 消息模式和订阅的主题, tag 信息, 同时可以对主题的消息进行查询

## 消息

* 可以通过主题和 key 进行查询
* 根据时间进行查询
* 查看消息轨迹

![集群信息](http://pic.yupoo.com/sanri1993/138c8287/7d0ae70e.png)

![主题信息](http://pic.yupoo.com/sanri1993/2a354c70/c15598b3.png)

![主题消息查询](http://pic.yupoo.com/sanri1993/2d23ce20/3ed23855.png)

![消费组信息](http://pic.yupoo.com/sanri1993/8f5ca974/965181de.png)

![消费查询Json展示](http://pic.yupoo.com/sanri1993/4a950823/86aae6db.png)