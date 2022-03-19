# 安全连接管理 

这里管理着所有模块的连接信息，如果启用了权限，这里每一个人将会看到不同的连接信息，毕竟一个公司里面，帐号信息可是个敏感信息。 如何管理权限，参考[权限管理](/tools/auth) 

## 操作界面

左侧展示的是模板列表，点击【查看连接】按扭，即可以在右边连接列表建立新的连接；个人感觉这个 UI 界面已经足够清晰，就没有去详细的讲解如何操作了，下面主要是讲连接管理的内部实现。

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


## 新建连接模板

每个模块建立连接的模板信息由各个模块来维护，命名为 connect.模块名.template.文件格式。例如：数据库的新建连接模板文件为 connect.database.template.json 模板数据如下 

```json

{
  "url": "jdbc:mysql://localhost:3306/test",
  "username": "root",
  "password": "root",
  "driverClassName": "com.mysql.cj.jdbc.Driver"
}

```