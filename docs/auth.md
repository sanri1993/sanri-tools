## 权限设计

因为系统涉及到大量的连接管理，但是在公司对于一个团队来说，连接相关的帐号密码是需要保密的，所以这个系统必须需要权限，否则就变成个人使用的工具了。

和大多数后台管理系统一样，我也是设计了用户管理，角色管理，资源管理，菜单信息，组织管理

我把组织做为了一颗总的树，用户，角色，资源，菜单都是挂载到这颗树上的，在树上级的节点会拥有更大的权限，可能我这样描述并不是很清晰，下面我放上一张图加深大伙对我这个权限设计的理解。

```
root          --admin(User) 
  group1
    group11       --user1(User) --role1(Role) --resource1(Resource)
      group111
        group111  --resource3(Resource)
    group12
  group2          --user1(User) --resource1(Resource)
    group21
 
role1 --resource1(Resource),resource2(Resource)

user2 --role1,role2 group111,group112

Group(name,path)
Role(name,$Groups,$Resource)
Resource(name,$Groups)
User(name,$Roles,$Groups)
```

* 上图表示 admin 是一个用户，挂载到 root 节点，拥有最高权限
* user1 是一个用户，挂载到了 group11 和 group2 节点，拥有这两个节点及之下的节点的所有用户，角色和资源的访问权限
* role1 是一个角色，挂载到了 groupo11 节点，拥有这个角色的用户能访问这个节点及之下所有角色和资源的访问权限
* resource1 是一个资源，挂载到了 group11 和 group2 节点，拥有这个资源的角色的用户能访问这两个节点及之下节点的所有资源
* role1 拥有资源  resource1 和 resource2 
* user2 拥有角色 role1和 role2 , 挂载到了 group111 和 group112 

> 资源，角色，组织 我在后面统称之为能力

## 能力的拥有与分配属性

用户只有拥有了某个能力，才能把这个能力分配出去，像公司的组织架构一样，每个用户的能力是逐级递减的

* 权限访问属性: 用户可以访问哪些权限内容
   * 组织属性: 用户可以访问自己所在组织和所有子组织
   * 用户属性: 用户可以访问自己所在组织和子组织内的所有用户
   * 角色属性: 用户可以访问的角色所在组织和子组织内的所有角色
   * 资源属性: 用户可以访问的角色-> 找到角色所有资源 -> 找到资源的组织 -> 组织的顶层列表组织下的所有资源都可以访问
* 权限分配属性: 用户拥有的权限可以分配给用户可访问的用户

## 配置与使用

每个模块需要提供 3 个文件来确定资源(tools-模块名.resources.conf)，访问权限(authority.conf)和菜单信息(tools-模块名.menus.conf)，格式如下 

**resources.conf**

```
资源id:资源名称:访问路径:类型(Resource,SubResource):父级资源名称:组织
connectlist:连接列表:connect/all:Resource:connect:/core
```

**menus.conf**

这个和资源信息类似，主要用于前端展示的菜单名，资源信息会挂载到菜单下面

```
菜单id:菜单名称:访问路径:类型(Menu,SubMenu):父级菜单名称:组织:路由路径
menu_resource:资源管理:/security/resource:Menu:menu_level_1_security:/security/resource:/security/resource
```

**authority.conf**

这个配置主要用来强制限制某些资源的访问权限，格式: antMatch=角色配置表达式(JS)

```
/security/users/**=admin && data 表示需要 admin 和 data 角色同时存在时才能访问 /security/users 里面的接口
```

## 数据权限说明

上面只是控制了下功能权限，在需要更细粒度的控制中，需要控制数据权限，像连接信息，这个我无法在 tools-security 中总控，所以提供了一个接口，供用户自己去实现数据权限功能

* tools-core 中添加 UserService 接口,由 tools-security 实现
* 每个模块如需要控制数据权限需要自行注入 UserService , 非强依赖方式 `@Autowired(required = false)`
* 当 UserService 不空时, 可以调用里面的方式获取用户属性来控制数据

使用示例: 安全连接管理类 `com.sanri.tools.modules.core.service.connect.FileBaseConnectService` 使用用户的组织来判断用户的访问数据的权限 

## 权限的存储结构

权限相关的信息都是存储在 $configDir/security 目录下的

### 用户信息

```
$configDir[Root]
  security[Dir]
   users[Dir]        
    user1[Dir]       
      base[File]     
      profile[File]  
      ...
```

base 文件的存储结构如下

```
用户名:密码(明文):组织列表:角色列表
sanri:0:/core:operator,op
```

### 角色信息

角色信息存储在文件 `$configDir/security/roles` 中,存储的结构如下,一行一个角色信息

```
角色名称:组织列表:可访问资源列表
op:/core:menu_git,menu_level_1_basedata
```

### 组织信息

组织信息存储在文件 `$configDir/security/groups` 中,存储的结构如下,一行一个组织

```
组织路径 
/root/a/b/
```

## FAQ 

对于这个权限系统，当时我自己写的时候都有点搞懵逼，估计大部分用户也一样，所以我添加了一个权限视图的菜单，可以更方便的看到当前的权限挂载情况，菜单路径为 【安全管理】->【权限视图】

如果还有其它不理解的问题，我后面会把提问和解答都放到本页面来，供大家参考