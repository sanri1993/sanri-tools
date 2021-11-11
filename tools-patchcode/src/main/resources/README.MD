## 算法 : 生成 maven 项目模块树

难点: 项目模块的目录深度和目录层级是不可预知的 

### 实现方法

主要实现逻辑在 `com.sanri.tools.modules.codepatch.service.GitService.modules`

### 算法详细说明

1. 加载项目目录下所有 pom.xml 文件
2. 根据路径由短到长进行排序, 并只取路径段 
3. 映射成 path => Module 结构 
4. 使用**快速树生成**,标志为子模块依据为: 当前模块路径起始于查找子模块的模块路径,并且路径间距数小于 2
5. 然后把顶部元素拿出来, 实现方式为**同一方向路径最短**
   1. 定义一个顶部路径列表
   2. 遍历路径,找到当前路径有在顶部路径列表中有存在同方向的
   3. 如果当前路径比顶部路径更短, 则替换这个顶部路径
   4. 否则找一个顶部路径
6. 查找顶部元素的模块 

### 快速树生成说明 

快速生成树, 不使用递归, 只需要一个循环, 时间复杂度能达到 O(n)

假设有这样一个平铺树结构 
```java
class TreeDto{
    private String id;
    private Stirng parentId;
    private String name;
    private List<TreeDto> treeDto  = new ArrayList();
}

List<TreeDto> list = {.....}
```

实现步骤如下 

1. 映射成 map 结构 id => TreeDto
2. 遍历 TreeDto , 对于每一个  TreeDto , 根据 parentId 在第一步的 map 中找到 TreeDto 添加当前节点为 child
   
 
