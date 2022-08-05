# Kafka使用文档

Kafka 工具主要提供了 kafka 集群中中的主题管理，消费组管理，JMX 数据监控

## 主题管理

* 查看主题列表，主题分区
* 查看主题分区数据
* 主题尾部数据
* 创建主题
* 模拟数据发送

## 消费组管理

* 消费组列表
* 消费组消费进度
* 当前消费卡住的数据信息
* 消费组消费的主题列表

## 创建一个 kafka 连接

我使用了 spring 对于 kafka 的配置，只需要把 spring: 前缀去掉并复制到创建连接框中就可以创建一个 kafka 连接

因为 kafka 需要依赖于 zookeeper, 这里 kafka 连接名需要和 zookeeper 连接同名 


![kafka消费组](http://pic.yupoo.com/sanri1993/12de473e/e6616cfd.png)

![kafka数据查询](http://pic.yupoo.com/sanri1993/6c895960/532fb639.png)