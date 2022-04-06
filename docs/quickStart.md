文档更新日期: {docsify-updated} 

## 启动项目

1. 下载最新版本 release [去下载](https://gitee.com/sanri/sanri-tools-maven/releases/)

2. 下载依赖的 jar 包， 并解压   sanritoolslib [下载地址](https://cowtransfer.com/s/0df01e65aaca4f)

3. 启动项目
   ```shell
   java -Dloader.path=sanritoolslib -Xms256m -Xmx256m -jar sanritools.jar
   ```
4. 访问地址 `http://localhost:8084/`，默认帐号密码为 admin/0, 当进入首页能看到插件列表时, 表示安装成功

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
- [gitpatch](/modules/gitpatch)

