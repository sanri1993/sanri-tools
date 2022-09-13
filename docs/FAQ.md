## 问答

当哪个模块有遇到问题时，可以在评论区进行评论，消息将同步到我的 github issue 中。

如果评论加载不出来时，请直接去我的 gitee 项目中提 issue 

### 关于新建连接后,连接列表未更新问题

这是由于初期对前端不熟悉,未使用 vuex 同步数据,可以在标签页右键刷新当前页就可以拿到最新建立的连接

### kafka 连接不上问题

**node -1 类似的错误**

kafka 的 broker 获取原理: 读取同名连接 zookeeper 上的 /brokers/ids/0 上的数据, 所以需要配置同名的 zookeeper 连接 

**权限配置错误**

因为给的模板中默认是配置了 kafka 的权限的, 使用的是 jaas 的权限配置, 即这几行配置, 还有安全的通信协议, 大部分应该都是明文(PLAINTEXT), 
如果 kafka 本身是没有配置权限的, 可以把这个属性配置去掉, 应该就可以连接上了

```
properties:
    sasl:
      jaas:
        config: org.apache.kafka.common.security.plain.PlainLoginModule required username=hd password=hd-kafka;
      mechanism: PLAIN
    security:
      protocol: SASL_PLAINTEXT
```

### 关于 elasticsearch 配置连接无效, 还是连接到了 localhost

因为 es 是使用 http 协议的, 然后我使用了 forest 框架

所以在配置 es 连接时, 需要添加前缀  http:// 不然会连接到 localhost 

### 菜单问题
因为在 3.2.0 后的版本菜单变动, 有可能导致管理员不能看到全部菜单, 这个数据没有办法去修复, 如果已经在前面版本添加了人员和权限信息的话, 请联系作者

最简单的做法就是删除 $configDir/security 目录所有数据, 然后重新启动项目, 即可恢复管理员的菜单数据