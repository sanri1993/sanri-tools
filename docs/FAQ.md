## 问答

当哪个模块有遇到问题时，可以在评论区进行评论，消息将同步到我的 github issue 中。

如果评论加载不出来时，请直接去我的 gitee 项目中提 issue 

### 关于新建连接后,连接列表未更新问题

这是由于初期对前端不熟悉,未使用 vuex 同步数据,可以在标签页右键刷新当前页就可以拿到最新建立的连接

### 关于代码增量工具

代码增量工具需要依赖于 maven , 所有部署的时候, 需要配置一个 maven 环境, 并告诉每个项目成员, maven 是在哪个路径
这样每个成员就能知道 `mavenConfigFilePath` 怎么配置 

```json
{
  "connectIdParam": {
    "module": "git",
    "connName": "sanri"
  },
  "authParam": {
    "username": "root",
    "password": "h123"
  },
  "sshKey":null,
  "mavenHome": "d:/test",
  "mavenConfigFilePath":"d:/test/conf/settings.xml"
}
```