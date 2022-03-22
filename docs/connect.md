# 安全连接管理 

这里管理着所有模块的连接信息，如果启用了权限，这里每一个人将会看到不同的连接信息，毕竟一个公司里面，帐号信息可是个敏感信息。 如何管理权限，参考[权限管理](/auth) 

## 连接信息的存储结构

```
$configDir[Dir]
  connectBase[Dir]
    info[File] 存储连接元数据
    $module1[Dir]
      $baseName[File] => content
    $module2[Dir]
      $baseName[File] => content
```

### 连接元数据保存的内容 

```
id:模块名:连接名:配置格式:组织名:上次更新人:上次更新时间:上次访问时间:连接出错次数:相对路径
446:database:localhost:json:/:admin:1646269332273:1646269332273:0:database/localhost
```

使用这种格式的灵感来自 Linux 的用户信息的保存，这种每条数据都会有这些字段，一般不会有空的，使用 json 占用空间太大，这种数据刚刚好。

将连接的这些信息保存是为了更方便的做权限管理和数据排序

## 连接模板

每个模块建立连接的模板信息由各个模块来维护，命名为 connect.模块名.template.文件格式。例如：数据库的新建连接模板文件为 connect.database.template.json 模板数据如下 

```json

{
  "url": "jdbc:mysql://localhost:3306/test",
  "username": "root",
  "password": "root",
  "driverClassName": "com.mysql.cj.jdbc.Driver"
}

```