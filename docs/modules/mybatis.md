做这个工具主要是因为每次从 mybatis 的 xml 中复制sql 时， 需要替换那些标签和变量， 工作量比较大。

既然 mybatis 可以拿这些 sql 和变量来执行，那么我可不可以复制他的过程呢，把 sql 提取出来，输入参数传可以在目标环境执行。

## 原理

1. 利用 Mybatis 已经解析好的 Configuration , 拿到所有的 MappedStatement 
2. 然后绑定参数,得到 BoundSql 对象
3. 最后指定一个数据库连接,便可以执行对应 sql  
