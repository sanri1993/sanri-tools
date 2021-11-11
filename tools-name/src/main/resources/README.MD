## 算法 : 所有可能的句子组合

一句话经过分词后可能有多个词, 每个词又会对应不同的翻译结果,如何把这些词的翻译组成所有可能的句子, 例: 

我是中国人 经过拆词后变成了 我 是 中国人

我可以翻译为 I my me 

是可以翻译为 is am 

中国人翻译为 china

那么所有的可能变有
 
* I is china 
* I am china 
* my is china 
* my am china 
* me is china 
* me am china 

### 实现方法

主要实现逻辑在 `com.sanri.tools.modules.name.service.TranslateCharSequence.results`

### 算法详细说明

1. 先生成一颗这样的树, 创建一个顶部虚拟节点
```shell script
virtual
  I
    is am 
      china
  my 
    is am 
      china
  me
    is am 
      china 
```
2. 遍历所有可能的路径(使用指针回溯)