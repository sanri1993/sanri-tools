## 背景

对于 webservice，dubbo 等中间件，我们缺少一个可以直接调试的工具，为了调一个接口，每个人再单独去写个测试代码简直是浪费时间，所以便有了可以直接调用数据的工具，目前已经提供的数据调用工具有 

* soap 工具
* Mybatis 工具 
* Dubbo 工具
* Quartz 工具

## 原理

需要了解中间件底层调用的原理，然后再组装对应数据，使用对应的协议发送请求得到响应数据，然后再展示给前端。

* webservice 是使用 soap 消息，其实还是发送的 http 请求，只过改送的内容比较特殊，我们只需要把这个内容进行组装，然后发送一个 http 请求便可； 并且 wsdl 是一种可自解析的语言，可以使用 wsdl4j 这个工具包对 wsdl 的 xml 进行解析，拿到方法和参数格式
* dubbo 是使用 netty 改送 tcp 数据包，我们可以直接使用它里面已经封装好的类，对数据进行组装，然后进行请求
* mybatis 是将 xml 中的 sql 进行解析，创建 executor 然后执行 MapperStatement ，根据绑定的参数，得到对应结果 

我只不过结合了上一章的类加载器，将调用过程可视化了，使用户可以轻易的对某一个接口进行测试而不用去编写大量的代码