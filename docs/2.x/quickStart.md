文档更新日期: {docsify-updated} 

< 2.1.0 此时工具包还不是特别大, 可以直接下载整包, 这个整包是包含前后端的, 直接 java -jar 启动即可
```shell script
java -jar sanritools.jar
```

2.2.0 因为包大小超过了 gitee 的附件限制, 所以我把依赖包和代码做了分离, 你可以去附件中下载依赖包, 然后加载依赖包来启动项目

下载依赖包 <https://gitee.com/sanri/sanri-tools-maven/attach_files>

```shell script
java -Dloader.path=sanritoolslib -jar sanritools.jar
```

访问 http://localhost:8084

![](http://pic.yupoo.com/sanri1993/3ae171e2/25570811.png)