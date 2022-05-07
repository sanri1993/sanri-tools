# 说明
类似 jconsole 的一个工具, 用于监控 jmx 信息

需要目标机器开启了 jmx 监控, 那么就可以监控到其数据信息

```shell script
-Dcom.sun.management.jmxremote.port=1090 
-Dcom.sun.management.jmxremote.ssl=false 
-Dcom.sun.management.jmxremote.authenticate=false 
-Djava.rmi.server.hostname=***.***.***.*** 
```