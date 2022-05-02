## 组件特性

* 数据库元数据 catalog,schema,table,column,index,primary,clientinfo 查询
* 扩展元数据 表类型(字典表,配置表,业务表),表关联关系设置,展示和搜索
* 数据库文档生成和下载
* 使用模板生成代码,组合模板成方案生成代码
* 使用 sql 预览数据库数据和 excel 导出,导入 excel 数据 ,表随机数据生成 
* nacos,diamond 等依赖于数据库的配置数据可以直接展示 
* 强大的搜索功能,可以使用表名,列名,注释,表标记,schema 来搜索表
* 随机数据插入,在压力测试时可以用 , 可以用 spring 表达式,正则,关联另一张表来随机生成表数据
* 元数据比对, 在发版本时, 数据库变更是个薄弱环节, 需要开发人员写变更脚本, 然后由 flyway 或者 Liquibase 执行脚本执行, 这里我可以帮做两库变更, 然后生成可重复执行的 sql 脚本 
* 脏数据清除, 表关系配置后, 可以根据关系查询库中的脏数据, 由用户决定是否需要清除
* sql 生成代码, 对于复杂的查询 sql 时, 我工具一样有能力生成相应代码 

## 组件能力

### 元数据

通过 jdbc 的 MetaData 功能，可以获取到数据库的元数据信息，包括表，列，索引，主键，函数，存储过程

获取到元数据后，可以做数据库文档的生成和为后面扩展元数据做准备

可以试试搜索表中注释，或者表名，列名，都是可以找到你需要的表的

![数据表元数据](http://pic.yupoo.com/sanri1993/9617ca49/37792bec.png)

### 扩展元数据

通常数据库的元数据是不够业务来使用的，数据库只是一个存储数据的工具而已，并且现在一般不会在数据库来建立外键，所以我们需要在外面来扩充数据库的元数据，我主要扩充了两种类型的元数据 

* 表标记: 用来标记某一张表具体是用来做什么的，你可以将表标记为业务表，字典表，系统表，已弃用，这将在搜索表时可以使用
* 表关系: 这个主要是用来扩充表的外键的，做了一个简单的表关系设置，能支持 80% 的表关系情况，A.a = B.b 一对一，一对多，多对多关系 

获取到表关系后，可以用表关系来清除系统中的脏数据，并且可以生成有关联关系的代码，会稍微比单表的代码生成少写一些模板代码。

### 元数据比对

在版本上线时，一般会有数据库变更，这时开发去手动整理脚本时容易漏并且容易出错，这时便可以使用我的元数据

### 代码生成

一种是生成代码片段，这应该来说是比较常用的功能，可以把数据库的元数据当成数据源，然后使用模板来生成相应的功能代码，组合多种模板成一个方案，可以根据方案来生成代码。

一种是使用现成的代码生成工具，主要是生成 Mapper，适用于使用 mybatis 的项目

最后还可以直接生成项目，需要依赖其它组件，生成便可以直接运行，对于一些简单项目，由于表关系也直接生成了，相应的存储，定时任务，web 交互数据协议，都已经直接通过组件进行连接，可以少写业务或者不写业务便可以直接使用。 

![代码生成-1](http://pic.yupoo.com/sanri1993/22bd8df6/20435249.png)

![代码生成-2](http://pic.yupoo.com/sanri1993/79c32fa4/31420bc6.png)

#### 关于模板

模板使用 freemarker, 可用变量列表为

**通用变量**

```
date: String yyyy-MM-dd
time: String HH:mm:ss
author: String 服务器用户名
connectProperties: Map
  url: String 连接数据库 url
  username: String 连接数据库用户名
  driverClassName: String 驱动类名
```

**每张数据表生成一个文件**

```
table: TableMetaData
mapping: JavaBeanInfo
  className: String
  lowerClassName: String
  imports: Set<String>;
  fields: BeanField
    typeName: String
    fieldName: String
    comment: String
    key: boolean
    column: Column
    capitalName: String
package: PackageConfig
  parent: String
  mapper: String
  service: String
  controller: String
  entity: String
  vo: String
  dto: String
  param: String
```

**所有数据表生成一个文件**

```
tables: List<TableMetaData>  数据表列表
  actualTableName: ActualTableName 表名信息
    namespace: Namespace
      catalog: String
      schema: String
    tableName: String
  table: Table
    actualTableName: ActualTableName
    remark: String
  columns: List<Column>
    actualTableName: ActualTableName
    columnName: String
    dataType: int 字段类型 javax.sql.Types
    typeName: String
    columnSize: int 列长度
    decimalDigits: int 列精度
    nullable: boolean 是否可以空, 真表示可以空
    remark: String
    autoIncrement: boolean 自增
    defaultValue: String 默认值
  indices: List<Index> 索引信息
    actualTableName: ActualTableName
    unique: boolean
    indexName: String
    indexType: short
    ordinalPosition: short
    columnName: String
  primaryKeys: List<PrimaryKey>
    actualTableName: ActualTableName
    columnName: String
    keySeq: int
    pkName: String
```

### 数据操作

![数据导出](http://pic.yupoo.com/sanri1993/39fdf282/86304951.png)

对于运维中的一些数据导出，一般不会做在 web 服务中，用其它数据库工具在导出百万数据时也比较捉急，便可以使用工具中的数据导出，有实际测试过100万数据导出，大概导出 4 分钟左右。

对于开发中测试时，需要创建一些数据来测试，这可不是单表随便插入几条数据就完事，有时候需要关联的插入数据，我这个工具在这个时候就有用了，关联插入数据是我这插入数据中的一个亮点。

如果你设置了表关系，那么我可以帮你检测出数据表中的脏数据，原理是根据表关系， 然后由你来决定是不是要删除这些脏数据

在一个需求结束时，有可能会有一些如菜单，业务配置之类的数据需要形成脚本插入到生产库，我这里可以帮助你生成可重复执行的脚本来插入这些数据，支持 mysql 和 oracle 



## FAQ
