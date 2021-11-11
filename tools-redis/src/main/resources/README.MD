## 算法 : Redis 的树状 key

本来以为以冒号做分隔, 然后组装一下就行, 后面发现, 这棵树的枝干也可以做为 key 存储数据, 并且每次都要从顶级开始找

### 实现方法

主要实现逻辑在 `com.sanri.tools.modules.redis.service.RedisTreeKeyService.appendTree`

### 算法详细说明

引用 : StringUtils.split() 方法操作出的每一个子串, 我称呼为 part 

1. 首先我定义了一个虚拟结点 top 
2. 每次分割一个 key 之后, 从第 0 个深度开始, 查找父元素的子节点是否包含当前这个深度的 part
3. 如果找不到这个 part,则其为新的顶节点,将它的其它 part 拼接到它后面即可
4. 如果找到了这个 part,则深度+1, 继续第 2 个步骤
5. 如果这个 part 后面拼接了其它 part,则它为目录
6. 对于找到的所有 key , 如果某个 key 他是目录,则添加一个同名值节点