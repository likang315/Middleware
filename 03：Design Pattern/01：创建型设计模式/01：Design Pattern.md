### 设计模式： 一般认为有 23 种，其他类型都是衍生的设计模式

------

[TOC]

##### 01：设计原则：

- 开闭原则：对扩展开放，对修改关闭；
- 高内聚，低耦合：尽量提高模块的聚合度，尽量降低模块之间的联系（尽量减少类之间的依赖性）；
- 里氏代换原则：任何基类可以出现的地方，子类一定可以出现；

![设计模式](/Users/likang/Code/Git/Middleware/03：Design Pattern/01：创建型设计模式/photo/设计模式.png)

##### 02：分为三类

###### 创建型模式（4种）

​	提供了一种在**创建对象的同时隐藏创建逻辑的方式**，而不是使用 new 运算符直接实例化对象；

###### 结构性模式（8种）

​	关注类和对象的组合（**类间关系**），被用来组合接口和定义组合对象获得新功能的方式；

###### 行为型模式（12种）

​	关注对象之间的通信（**如何调用**）；
