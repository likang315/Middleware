### 集群（Cluster）

------

[TOC]

##### 01：概述

- Redis集群是Redis提供的**分布式数据库方案**，集群通过分片（sharding）来进行数据共享，并提供复制和故障转移功能。

##### 02：节点

- 一个Redis集群通常由多个节点（node）组成，在刚开始的时候，每个节点都是相互独立的，它们都处于一个只包含自己的集群当中，要**组建一个真正可工作的集群，我们必须将各个独立的节点连接起来**，构成一个包含多个节点的集群;

- 组建集群的命令

  - ```shell
    CLUSTER MEET <ip> <port>
    ```

  - 向一个节点node发送该命令时行，可以**让node节点与指定的ip和port对应的结点握手（handshake**），当握手成功时，node节点就会将ip和port所指定的节点添加到node节点当前所在的集群中；

- 查看集群中有那些节点及节点的被指派了那些槽：CLUSTER NODE

##### 03：启动节点

- 一个节点就是一个运行在集群模式下的Redis服务器，Redis服务器在启动时会根据**cluster-enabled**配置选项是否为yes来决定是否开启服务器的集群模式

  - ```
    cluster_enabled:1
    ```

  - 1：开启服务器的集群模式成为一个节点；
  - 0：开启服务器的单机模式，成为一个普通的redis 服务器；

- 只有在集群模式下才会用到的数据，节点将它们保存到了**cluster.h/clusterNode结构、cluster.h/clusterLink结构，以及cluster.h/clusterState结构**里面。

##### 04：集群数据结构

###### clusterNode结构

- clusterNode 结构**保存了一个节点的当前状态**，比如节点的创建时间、节点的名字、节点当前的配置纪元、节点的IP地址和端口号等等。

- 每个节点都会使用一个clusterNode结构来记录自己的状态，并**为集群中的所有其他节点（包括主节点和从节点）都创建一个相应的clusterNode结构**，以此来记录其他节点的状态；

  - ```c
    struct clusterNode {
        //创建节点的时间
        mstime_t ctime;
        //节点的名字，由40 个十六进制字符组成，68eef66df23420a5862208ef5b1a7005b806f2ff
        char name[REDIS_CLUSTER_NAMELEN];
        //节点标识
        //使用各种不同的标识值记录节点的角色（比如主节点或者从节点），
        //以及节点目前所处的状态（比如在线或者下线）。
        int flags;
        //节点当前的配置纪元，用于实现故障转移
        uint64_t configEpoch;
        //节点的IP 地址
        char ip[REDIS_IP_STR_LEN];
        //节点的端口号
        int port;
        //保存连接节点所需的有关信息
        clusterLink *link;
        
        // 记录该节点负责的槽位
        unsigned char slots[16384/8];
        // 该节点负责槽的数量
        int numslots;
    };
    ```

###### clusterLink结构

- link属性：是一个clusterLink结构，该结构**保存了连接该节点所需的有关信息**，比如套接字描述符，输入缓冲区和输出缓冲区：

  - ```c
    typedef struct clusterLink {
        // 连接的创建时间
        mstime_t ctime;
        // TCP 套接字描述符
        int fd;
        // 输出缓冲区，保存着等待发送给其他节点的消息（message ）。
        sds sndbuf;
        // 输入缓冲区，保存着从其他节点接收到的消息。
        sds rcvbuf;
        // 与这个连接相关联的节点，如果没有的话就为NULL
        struct clusterNode *node;
    } clusterLink;
    ```

- redisClient结构和clusterLink结构的相同和不同之处

  - redisClient结构和clusterLink结构都有自己的套接字描述符和输入、输出缓冲区，这两个结构的区别在于，redisClient结构中的套接字和缓冲区是**用于连接客户端的**，而clusterLink结构中的套接字和缓冲区则是**用于连接节点的**。

###### clusterState 结构

- 每个节点都保存着一个clusterState结构，这个结构记录了**在当前节点的视角下，集群目前所处的状态**，例如集群是在线还是下线，集群包含多少个节点，集群当前的配置纪元

  ```c
  typedef struct clusterState {
      // 指向当前节点的指针
      clusterNode *myself;
      // 集群当前的配置纪元，用于实现故障转移
      uint64_t currentEpoch;
      // 集群当前的状态：是在线还是下线
      int state;
      // 集群中至少处理着一个槽的节点的数量
      int size;
      // 集群节点名单（包括myself 节点）
      // 字典的键为节点的名字，字典的值为节点对应的clusterNode 结构
      dict *nodes;
      // ...
      // 记录槽指派信息，通过o(1) 的复杂度查询槽指派信息
      clusterNode *slots[16384];
      // 用跳跃表保存槽和键之间的关系
      zskipList *slot_to_keys;
  } clusterState;
  ```

##### 05：CLUSTER MEET命令的实现

- 通过**向节点A发送CLUSTER MEET命令**，客户端可以让接收命令的节点A将另一个节点B添加到节点A当前所在的集群里面；

###### 握手流程（三次握手）

1. 节点A会为节点B创建一个clusterNode结构，并将该结构添加到自己的clusterState.nodes字典里面。
2. 节点A将根据CLUSTER MEET命令给定的IP地址和端口号，**向节点B发送一条MEET消息（message）**。
3. 如果一切顺利，节点B将接收到节点A发送的MEET消息，节点B会为节点A创建一个clusterNode结构，并将该结构添加到自己的clusterState.nodes字典里面。
4. **节点B将向节点A返回一条PONG消息**。
5. 如果一切顺利，节点A将接收到节点B返回的PONG消息，通过这条PONG消息节点A可以知道节点B已经成功地接收到了自己发送的MEET消息。
6. **节点A将向节点B返回一条PING消息**。
7. 如果一切顺利，节点B将接收到节点A返回的PING消息，通过这条PING消息节点B可以知道节点A已经成功地接收到了自己返回的PONG消息，握手完成。
8. 节点A会将节点B的信息**通过Gossip协议传播给集群中的其他节点**，让其他节点也与节点B进行握手，最终，经过一段时间之后，节点B会被集群中的所有节点认识。

![redis_cluster_meet_shakehand](/Users/likang/Code/Git/Middleware/04：Redis/photos/redis_cluster_meet_shakehand.png)

##### 06：槽（slot） 指派

- Redis集群通过**分片的方式来保存数据库中的键值对**：**集群的整个数据库被分为16384个槽（slot），数据库中的每个键都属于这16384个槽的其中一个，集群中的每个节点可以处理0个或最多16384个槽**。
- 当数据库中的16384个槽都有节点在处理时，集群处于上线状态（ok）；相反地，如果数据库中有任何一个槽没有得到处理，那么集群处于下线状态（fail）。
- CUSTER INFO
  - 查看槽指派信息
- CLUSTER ADDSLOTS  `<slot>  [slot ...]`
  - 通过向节点发送该命令，可以将一个或多个槽指派（assign）给节点负责；

###### 记录节点的槽 指派信息

- clusterNode 中 slots 和numslots 属性记录了该节点槽信息；
- **slots属性：**是一个二进制位数组（bit array），这个数组的长度为16384/8=2048个字节，共包含16384个二进制位。
  - Redis以 **0为起始索引，16383为终止索引**，对slots数组中的16384个二进制位进行编号，并根据索引i上的二进制位的值来判断节点是否负责处理槽i，**槽i 值为 1 时，负责处理**，相反，则不处理。
- **numslots：**记录节点负责处理的槽的数量，也即是slots数组中值为1的二进制位的数量。

###### 传播节点的槽指派信息

- 一个节点除了会将自己负责处理的槽记录在clusterNode结构的slots属性和numslots属性之外，它还会**将自己的slots数组通过消息发送给集群中的其他节点，以此来告知其他节点自己目前负责处理哪些槽**。
- 收到消息的集群中其他节点，则会在自己的**clusterState.nodes字典中查找发送源节点对应的clusterNode结构**，并对结构中的**slots数组进行保存或者更新**。

###### 记录集群所有槽的指派信息

- clusterStat.slots[16384] ：记录了集群中所有16384个槽的指派信息；
- slots数组包含16384个项，每个数组项都是一个指向clusterNode结构的指针：
  - 如果slots[i]指针指向NULL，那么表示槽i尚未指派给任何节点。
  - 如果slots[i]指针指向一个clusterNode结构，那么表示槽i已经指派给了clusterNode结构所代表的节点。
- clusterState.slots：数组记录了集群中所有槽的指派信息，而clusterNode.slots数组只记录了clusterNode结构所代表的节点的槽指派信息，这是两个slots数组的关键区别所在。

##### 07：CLUSTER ADDSLOTS 命令实现

- 最后，在CLUSTER ADDSLOTS命令执行完毕之后，节点会**通过发送消息告知集群中的其他节点，自己目前正在负责处理哪些槽**。

  ```python
  def CLUSTER_ADDSLOTS(*all_input_slots):
      # 遍历所有输入槽，检查它们是否都是未指派槽
      for i in all_input_slots:
          # 如果有哪怕一个槽已经被指派给了某个节点
          # 那么向客户端返回错误，并终止命令执行
          if clusterState.slots[i] != NULL:
              reply_error()
              return
      # 如果所有输入槽都是未指派槽
      # 那么再次遍历所有输入槽，将这些槽指派给当前节点
      for i in all_input_slots:
          # 设置clusterState结构的slots数组
          # 将slots[i]的指针指向代表当前节点的clusterNode结构
          clusterState.slots[i] = clusterState.myself
          # 访问代表当前节点的clusterNode结构的slots数组
          # 将数组在索引i上的二进制位设置为1
          setSlotBit(clusterState.myself.slots, i)
  ```

##### 08：在集群中执行命令

- 当客户端向节点发送与数据库键有关的命令时，接收命令的节点会计算出命令要处理的数据库键属于哪个槽，并检查这个槽是否指派给了自己：
  - 如果键所在的槽正好就**指派给了当前节点**，那么节点**直接执行**这个命令。
  - 如果键所在的槽并**没有指派给当前节点**，那么节点会**向客户端返回一个MOVED错误，指引客户端转向（redirect）至正确的节点，并再次发送之前想要执行的命令。**

###### 计算键属于哪个槽

- 算法

  - ```python
    def slot_number(key):
    	return CRC16(key) & 16383
    ```

  - CRC16（key）语句用于计算键key的CRC-16校验和，而 &16383语句则用于 计算出**一个介于0至16383之间的整数作为键key的槽号**。

- CLUSTER KEYSLOT＜key＞

  - 查看key 属于哪个槽；

###### 判断槽是否由当前节点负责处理

- 当节点计算出键所属的槽i之后，节点就会**检查自己在clusterState.slots数组中的项i**，判断键所在的槽是否由自己负责：
  - 如果**clusterState.slots[i]等于clusterState.myself**，那么说明槽i由当前节点负责，节点可以执行客户端发送的命令。
  - 如果clusterState.slots[i]不等于clusterState.myself，那么说明槽i并非由当前节点负责，节点会**根据clusterState.slots[i]指向的clusterNode结构所记录的节点IP和端口号，向客户端返回MOVED错误，指引客户端转向至正在处理槽i的节点。**
    - MOVED 6257 127.0.0.1:7001

###### MOVED 错误

- 格式

  - ```shell
    MOVED <slot> <ip>:<port>
    ```

  - slot 为键所在的槽，而ip和port则是**负责处理槽slot的节点的IP地址和端口号**。

- 当客户端接收到节点返回的MOVED错误时，客户端会根据MOVED错误中提供的IP地址和端口号，**重定向至负责处理槽slot的节点，并向该节点重新发送之前想要执行的命令**。

- **集群模式的redis-cli客户端**在接收到MOVED错误时，**并不会打印出MOVED错误**，而是**根据MOVED错误自动进行节点重定向，并打印出转向信息**，所以我们是看不见节点返回的MOVED错误的。

- **单机（stand alone）模式的redis-cli客户端**，向节点发送相同的命令，那么MOVED错误就会被客户端打印出来，因为**单机模式的redis-cli客户端不清楚MOVED错误的作用**，所以它只会直接将MOVED错误直接打印出来，而不会进行自动重定向；

##### 09：节点数据库存储的实现

- 集群节点保存键值对以及键值对过期时间的方式，与单机Redis服务器保存键值对以及键值对过期时间的方式完全相同。节点和单机服务器在数据库方面的一个区别是，**节点只能使用0号数据库，而单机Redis服务器则没有这一限制**。
- clusterState.slots_to_keys：保存槽和键之间的关系；
  - slots_to_keys：**每个节点的分值（score）都是一个槽号，而每个节点的成员（member）都是一个数据库键**。
    - 每当节点往数据库中添加一个新的键值对时，节点就会将这个键以及键的槽号关联到slots_to_keys跳跃表。❑
    - 当节点删除数据库中的某个键值对时，节点就会在slots_to_keys跳跃表解除被删除键与槽号的关联。

##### 10：重新分片

- Redis集群的重新分片操作可以**将任意数量已经指派给某个节点（源节点）的槽改为指派给另一个节点（目标节点），并且相关槽所属的键值对也会从源节点被移动到目标节点**。

###### 实现原理

- Redis集群的重新分片操作是由**Redis的集群管理软件redis-trib负责执行的**，Redis提供了进行重新分片所需的所有命令，而redis-trib则通过**向源节点和目标节点发送命令**来进行重新分片操作。
- redis-trib对目标节点发送CLUSTER SETSLOT＜slot＞IMPORTING＜source_id＞命令，**让目标节点准备好从源节点导入（import）属于槽slot的键值对**。
- redis-trib对源节点发送CLUSTER SETSLOT＜slot＞MIGRATING＜target_id＞命令，**让源节点准备好将属于槽slot的键值对迁移（migrate）至目标节点**。
- redis-trib向源节点发送 CLUSTER GETKEYSINSLOT＜slot＞＜count＞命令，**获得最多count个属于槽slot的键值对的键名（key name）**。
- 对于获得的每个键名，redis-trib都向源节点发送一个 **MIGRATE ＜target_ip＞＜target_port＞＜key_name＞0＜timeout＞**命令，将被选中的键原子地从源节点迁移至目标节点。
- **重复执行步骤3和步骤4**，直到源节点保存的所有属于槽slot的键值对都被迁移至目标节点为止。
- redis-trib**向集群中的任意一个节点发送CLUSTER SETSLOT＜slot＞NODE＜target_id＞命令**，将槽slot指派给目标节点，这一**指派信息会通过消息发送至整个集群**，最终集群中的所有节点都会知道槽slot已经指派给了目标节点。

##### 11：ASK 错误

- 













