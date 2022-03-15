## 快速开始

下载的 jar 包是一个完整的可运行的 jar 包, 可以直接使用 springboot 的方式启动

```
java -Xms256m -Xmx256m -Ddata.path.configs=配置文件存储路径 -Ddata.path.tmp=临时文件存储路径 -jar  sanritools.jar 
```

### 有另一种覆盖配置的方式

在 jar 包同级目录建一个配置文件 config/application-deploy.yml , 然后指定激活配置 --spring.profiles.active=deploy 