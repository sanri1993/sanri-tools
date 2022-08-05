文档更新日期: {docsify-updated} 

## 启动项目3.2.0

1. 下载完整包 <https://cowtransfer.com/s/4fc38439977f40>

2. 解压 `unzip sanritools3.2.0-jdk8.zip`

3. 进入目录, 并执行启动脚本

```
cd sanritools3.2.0-jdk8
chmod +x server.sh 

./server.sh start 
```

4. 访问项目 http://localhost:8084/public/index.html , 默认管理员帐号为 admin / 0

## 创建一个连接

1. 进入菜单 【基础数据】->【安全连接管理】
2. 点击 database 那行的 【查看连接】
3. 点击【新建 database 连接】打开添加数据库连接界面，并添加一个数据连接

## 使用数据库元数据功能

1. 进入菜单 【数据库管理】->【数据库元数据】
2. 选择刚才建好的数据库连接
3. 在输入框进行一个空搜索，即可展示出所有的数据表
4. 点击任意一个表，便可以在右边展示表的列，索引，表标记，表关系等信息

## 其它强大能力

- [database](/modules/database)
- [Redis](/modules/redis)
- [kafka](/modules/kafka)
- [rocketmq](/modules/rocketmq)
- [gitpatch](/modules/gitpatch)
- [mybatis](/modules/mybatis)
- [quartz](/modules/quartz)
- [name](/modules/name)

