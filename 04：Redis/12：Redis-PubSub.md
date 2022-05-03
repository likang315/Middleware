### 发布订阅模式

------

[TOC]

##### 01：概述

- Redis的发布与订阅功能由PUBLISH、SUBSCRIBE、PSUBSCRIBE等命令组成。
- 客户端通过执行**SUBSCRIBE 和 PSUBSCRIBE命令订阅一个或多个频道、模式**，从而成为这些频道或模式的订阅者：每当有其他客户端向某个频道发送消息时，消息不仅会被发送给这个频道的所有订阅者，它还会被发送给所有与这个频道相匹配的模式的订阅者。
  - 模式：模糊匹配频道名；
- ![](/Users/likang/Code/Git/Middleware/04：Redis/photos/publish_subscribe.png)

##### 02：频道的订阅和退订

- **频道订阅关系**：当一个客户端执行SUBSCRIBE命令订阅某个或某些**频道**的时候，这个客户端与被订阅频道之间就建立订阅关系。

  - Redis将**所有频道的订阅关系都保存在服务器状态的 pubsub_channels 字典里面**，这个字典的**键是某个被订阅的频道，而键的值则是一个链表**，链表里面记录了所有订阅这个频道的客户端：

    ```c
    struct redisServer {
    	// 保存所有频道订阅关系
    	dict *pubsub_channels;
        // 保存所有模式的订阅关系
        list *pubsub_patterns;
    };
    ```

###### 订阅频道

- 服务器都会将**客户端与被订阅的频道在pubsub_channels字典中进行关联**，根据频道是否已经有其他订阅者，关联操作分为两种情况执行（SUBSCRIBE 伪代码）
  - 如果频道已经有其他订阅者，那么它在pubsub_channels字典中必然有相应的订阅者链表，程序唯一要做的就是将客户端添加到订阅者链表的末尾。
  - 如果频道还未有任何订阅者，那么它必然不存在于pubsub_channels字典，程序首先要在pubsub_channels字典中为频道创建一个键，并将这个键的值设置为空链表，然后再将客户端添加到链表，成为链表的第一个元素。

###### 退订频道

- **UNSUBSCRIBE** 命令的行为和SUBSCRIBE命令的行为正好相反，当一个客户端退订某个或某些频道的时候，服务器将从pubsub_channels中解除客户端与被退订频道之间的关联
  - 程序会根据被退订频道的名字，在pubsub_channels字典中找到频道对应的订阅者链表，然后从**订阅者链表中删除退订客户端的信息**。
  - 如果删除退订客户端之后，频道的订阅者链表变成了空链表，那么说明**这个频道已经没有任何订阅者了，程序将从pubsub_channels字典中删除频道对应的键**。

##### 03：模式的订阅和退订

- 模式的订阅关系：服务器将所有模式的订阅关系都保存在服务器状态的 **pubsub_patterns** 属性里面

  - pubsub_patterns属性是一个链表，链表中的每个节点都包含着一个**pubsubPattern结构**，这个结构的pattern属性记录了被订阅的模式，而client属性则记录了订阅模式的客户端：

  - ```c
    typedef struct pubsubPattern {
    	// 订阅模式的客户端
    	redisClient *client;
    	// 被订阅的模式
    	robi *pattern;
    }
    ```

###### 订阅模式

- 每当客户端执行PSUBSCRIBE命令订阅某个或某些模式的时候，服务器会对每个被订阅的模式执行以下两个操作：

  - 新建一个pubsubPattern结构，将结构的pattern属性设置为被订阅的模式，client属性设置为订阅模式的客户端。

  - 将pubsubPattern结构添加到pubsub_patterns链表的表尾。

  - ```c
    PSUBSCRIBE 'new.*'
    ```

###### 退订模式

- 模式的退订命令 **PUNSUBSCRIBE** 是PSUBSCRIBE命令的反操作：当一个客户端退订某个或某些模式的时候，服务器将在pubsub_patterns 链表中**查找并删除**那些 pattern 属性为被退订模式，并且client属性为执行退订命令的客户端的pubsubPattern结构。 

##### 04：发送消息

- 当一个Redis客户端执行PUBLISH ＜channel＞＜message＞命令将消息message发送给频道channel的时候，服务器需要执行以下两个动作（先后顺序）：
  - 将消息message发送给channel频道的所有订阅者。
  - 如果有一个或多个模式pattern与频道channel相匹配，那么将消息message发送给pattern模式的订阅者。

######  将消息发送给频道订阅者

- PUBLISH命令要做的就是在 pubsub_channels 字典里找到频道channel的订阅者名单（一个链表），然后将消息发送给名单上的所有客户端。

###### 将消息发送给模式订阅者

- PUBLISH命令要做的就是**遍历整个pubsub_patterns链表**，查找那些**与channel频道相匹配的模式**，并将消息发送给订阅了这些模式的客户端。

##### 05：查看订阅消息

######  PUBSUB CHANNELS

- PUBSUB CHANNELS [pattern] 子命令用于返回服务器当前被订阅的频道，其中pattern参数是可选的：
  - 如果不给定pattern参数，那么命令返回**服务器当前被订阅的所有频道**。
  - 如果给定pattern参数，那么命令返回**服务器当前被订阅的频道中那些与pattern模式相匹配的频道**。
- 这个子命令是通过**遍历服务器pubsub_channels字典的所有键**（每个键都是一个被订阅的频道），然后记录并返回所有符合条件的频道来实现的；

###### PUBSUB NUMSUB

- PUBSUB NUMSUB [channel-1 channel-2...channel-n] 子命令接受**任意多个频道作为输入参数**，并返回这些**频道的订阅者数量**。
- 这个子命令是通过在 pubsub_channels 字典中找到频道对应的订阅者链表，然后**返回订阅者链表的长度来实现的**（订阅者链表的长度就是频道订阅者的数量）

###### PUBSUB NUMPAT

- PUBSUB NUMPAT子命令用于返回服务器当前**被订阅模式的数量**。
- 这个子命令是通过返回 pubsub_patterns 链表的长度来实现的，因为这个**链表的长度就是服务器被订阅模式的数量**。

##### 06：疑难杂症

```c
SUBSCRIBE 'a.b'
PSUBSCIRBE 'a.*'

PUBLISH 'a.b'
// 是否会重复收到两条消息？ yes?
```

