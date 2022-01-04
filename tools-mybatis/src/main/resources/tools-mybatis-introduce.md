## Mybatis模块插件介绍 
 
mybatis 模块提供可根据 xml 直接调用数据库数据的能力，主要是为了解决一些动态参数的手动替换麻烦的问题

* 上传 mapper 文件,配合类加载器,可以找到所有的 mapper 文件中的所有 statementId
* 根据 statementId 可以知道当前 statementId 所必传的参数
* 传入必传参数,和数据库连接,可以直接调用 statementId, 得到 sql 语句 ,这里的数据库连接引用数据库模块的连接名

