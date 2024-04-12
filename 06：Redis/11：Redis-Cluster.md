### 集群（Cluster）

------

[TOC]

##### 01：概述

- Redis 集群是 Redis 提供的**分布式数据库方案**，集群通过分片（sharding）来进行数据共享，并提供**复制和故障转移功能**。

##### 02：节点

- 一个 Redis 集群通常由多个节点（node）组成，在刚开始的时候，每个节点都是相互独立的，它们都处于一个只包含自己的集群当中，要**组建一个真正可工作的集群，我们必须将各个独立的节点连接起来**，构成一个包含多个节点的集群;

- 组建集群的命令

  - ```shell
    CLUSTER MEET <ip> <port>
    ```

  - 向一个节点 node 发送该命令时行，可以**让 node 节点与指定的 ip 和 port 对应的结点握手（handshake**），当握手成功时，node 节点就会将 ip 和 port 所指定的节点添加到 node 节点所在的集群中；

- 查看集群中有那些节点及节点的被指派了那些槽：CLUSTER NODE

##### 03：启动节点

- 一个节点就是一个运行在集群模式下的 Redis 服务器，Redis 服务器在启动时会根据 **cluster-enabled 配置选项是否为 yes **来决定是否开启服务器的集群模式；

  - ```
    cluster_enabled:1
    ```

  - 1：开启服务器的集群模式成为一个节点；
  - 0：开启服务器的单机模式，成为一个普通的 redis 服务器；

- 只有在集群模式下才会用到的数据，节点将它们保存到了**cluster.h/clusterNode结构、cluster.h/clusterLink结构，以及cluster.h/clusterState结构**里面。

##### 04：集群数据结构

###### clusterNode 结构

- clusterNode 结构**保存了一个节点的当前状态**，比如节点的创建时间、节点的名字、节点当前的配置纪元、节点的IP地址和端口号等等。

- 每个节点都会使用一个 clusterNode 结构来记录自己的状态，并**为集群中的所有其他节点（包括主节点和从节点）都创建一个相应的 clusterNode 结构**，以此来记录其他节点的状态；

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
        // 保存连接节点所需的有关信息
        clusterLink *link;
        
        // 记录该节点负责的槽位
        unsigned char slots[16384/8];
        // 该节点负责槽的数量
        int numslots;
        // 如果这是一个从节点，那么指向复制的主节点
        struct clusterNode *slaveof;
        // 从节点的数量
        int numslvaes;
        // 一个数组，记录没一个从节点的信息
        struct clusterNode **slaves;
        
        // 一个链表，记录了所有其他节点对该节点的下线报告
        list *fail_reports;
    };
    ```

###### clusterLink结构

- link属性：是一个 clusterLink 结构，该结构**保存了连接该节点所需的有关信息**，比如套接字描述符，输入缓冲区和输出缓冲区：

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

- redisClient 结构和 clusterLink 结构的相同和不同之处

  - redisClient结构和clusterLink结构都有自己的套接字描述符和输入、输出缓冲区，这两个结构的区别在于，redisClient结构中的套接字和缓冲区是**用于连接客户端的**，而clusterLink结构中的套接字和缓冲区则是**用于连接节点的**。

###### clusterState 结构

- 每个节点都保存着一个 clusterState 结构，这个结构记录了**在当前节点的视角下，集群目前所处的状态**，例如集群是在线还是下线，集群包含多少个节点，集群当前的配置纪元

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
      // 集群节点名单（包括 myself 节点）
      // 字典的键为节点的名字，字典的值为节点对应的clusterNode 结构
      dict *nodes;
      // ...
      // 记录槽指派信息，通过 o(1) 的复杂度查询槽指派信息
      clusterNode *slots[16384];
      // 用跳跃表保存槽和键之间的关系
      zskipList *slot_to_keys;
      // 记录当前节点正在从其他节点导入的槽
      clusterNode *importing_slots_from[16384];
  } clusterState;
  ```

##### 05：CLUSTER MEET 命令的实现

- 通过**向节点A发送 CLUSTER MEET 命令**，客户端可以让接收命令的节点A将另一个节点B添加到节点A当前所在的集群里面； 

###### 握手流程（三次握手）

1. 节点A会为节点B创建一个clusterNode结构，并将该结构添加到自己的clusterState.nodes字典里面。
2. 节点A将根据 CLUSTER MEET 命令给定的IP地址和端口号，**向节点 B 发送一条 MEET 消息（message）**。
3. 如果一切顺利，节点B将接收到节点A发送的MEET消息，节点B会为节点A创建一个clusterNode结构，并将该结构添加到自己的 clusterState.nodes 字典里面。
4. **节点B将向节点A返回一条PONG消息**。
5. 如果一切顺利，节点A将接收到节点B返回的PONG消息，通过这条PONG消息节点A可以知道节点B已经成功地接收到了自己发送的MEET消息。
6. **节点A将向节点B返回一条PING消息**。
7. 如果一切顺利，节点B将接收到节点A返回的PING消息，通过这条PING消息节点B可以知道节点A已经成功地接收到了自己返回的PONG消息，握手完成。
8. 节点A会将节点B的信息**通过 Gossip 协议传播给集群中的其他节点**，让其他节点也与节点B进行握手，最终，经过一段时间之后，节点B会被集群中的所有节点认识。

![redis_cluster_meet_shakehand](https://github.com/likang315/Middleware/blob/master/06：Redis/photos/redis_cluster_meet_shakehand.png?raw=true)

##### 06：槽（slot） 指派【重要】

- Redis 集群**通过分片的方式来保存数据库中的键值对，集群的整个数据库被分为16384个槽（slot）**，数据库中的每个键都属于这 16384 个槽的其中一个，集群中的每个节点可以处理0个或最多16384个槽。
- 当数据库中的16384个槽都有节点在处理时，集群处于上线状态（ok）；相反地，如果数据库中有任何一个槽没有得到处理，那么集群处于下线状态（fail）。
- CUSTER INFO
  - 查看槽指派信息
- CLUSTER ADDSLOTS  `<slot>  [slot ...]`
  - 通过向节点发送该命令，可以将一个或多个未分配的槽指派（assign）给节点负责；

###### 记录节点的槽指派信息

- clusterNode 中 slots 和 numslots 属性记录了该节点槽信息；
- **slots 属性：**是一个二进制位数组（bit array），这个数组的长度为16384/8=2048个字节，共包含16384个二进制位。
  - Redis以 **0为起始索引，16383为终止索引**，对slots数组中的16384个二进制位进行编号，并根据索引 i 上的二进制位的值来判断节点是否负责处理槽i，**槽i 值为 1 时，负责处理**，相反，则不处理。（BitMap）
- **numslots：**记录节点负责处理的槽的数量，也即是slots数组中值为1的二进制位的数量。

###### 传播节点的槽指派信息

- 一个节点除了会将自己负责处理的槽记录在 clusterNode 结构的 slots 属性和 numslots 属性之外，它还会**将自己的slots数组通过消息发送给集群中的其他节点，以此来告知其他节点自己目前负责处理哪些槽**。
- 收到消息的集群中其他节点，则会在自己的**clusterState.nodes字典中查找发送源节点对应的clusterNode结构**，并对结构中的**slots数组进行保存或者更新**。

###### 记录集群所有槽的指派信息

- clusterState.slots[16384] ：记录了集群中所有16384个槽的指派信息；
- slots 数组包含 16384 个项，每个数组项都是一个指向clusterNode结构的指针：
  - 如果slots[i]指针指向NULL，那么表示槽i尚未指派给任何节点。
  - 如果slots[i]指针指向一个clusterNode结构，那么表示槽i已经指派给了clusterNode结构所代表的节点。

##### 07：在集群中执行命令【重要】

- 当客户端向节点发送与数据库键有关的命令时，接收命令的节点会**计算出命令要处理的数据库键属于哪个槽**，并检查这个槽是否指派给了自己：
  - 如果键所在的槽正好就**指派给了当前节点**，那么节点**直接执行**这个命令。
  - 如果键所在的槽并**没有指派给当前节点**，那么节点会**向客户端返回一个 MOVED 错误，指引客户端转向（redirect）至正确的节点，并再次发送之前想要执行的命令。**

###### 计算键属于哪个槽

- 算法

  - ```python
    def slot_number(key):
    	return CRC16(key) & 16383
    ```

  - CRC16（key）语句用于计算键key的CRC-16校验和，而 &16383语句则用于计算出**一个介于0至16383之间的整数作为键key的槽号**。

- CLUSTER KEYSLOT＜key＞

  - 查看 key 属于哪个槽；

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

  - slot 为键所在的槽，而ip和port则是**槽 slot 所分配的节点的IP地址和端口号**。

- **集群模式的 redis-cli 客户端**在接收到MOVED错误时，**并不会打印出MOVED错误**，而是**根据MOVED错误自动进行节点重定向，并打印出转向信息**，所以我们是看不见节点返回的MOVED错误的。

- **单机（stand alone）模式的redis-cli客户端**，向节点发送相同的命令，那么 MOVED 错误就会被客户端打印出来，因为**单机模式的 redis-cli 客户端不清楚 MOVED 错误的作用**，所以它只会直接将MOVED错误直接打印出来，而不会进行自动重定向；

##### 08：节点数据库存储的实现

- 集群节点和单机服务器在方面的唯一一个区别是，**节点只能使用0号数据库，而单机Redis服务器则没有这一限制**。
- clusterState.slots_to_keys：保存槽和键之间的关系；
  - slots_to_keys：**每个节点的分值（score）都是一个槽号，而每个节点的成员（member）都是一个数据库键**。
    - 每当节点往数据库中添加一个新的键值对时，节点就会将这个键以及键的槽号关联到slots_to_keys跳跃表。
    - 当节点删除数据库中的某个键值对时，节点就会在slots_to_keys跳跃表解除被删除键与槽号的关联。

##### 09：重新分片（slot 迁移）【重要】

- Redis 集群的重新分片操作可以**将任意数量已经指派给某个节点（源节点）的槽改为指派给另一个节点（目标节点），并且相关槽所属的键值对也会从源节点被移动到目标节点**。

###### 实现原理

- Redis 集群的重新分片操作是由**Redis的集群管理软件redis-trib负责执行的**，Redis提供了进行重新分片所需的所有命令，而redis-trib则通过**向源节点和目标节点发送命令**来进行重新分片操作。
- 分别通知源，和目标节点告知迁移 slot 数据，然后统计源节点在 slot 的 key 数量，开始一个一个迁移；
  1. redis-trib对目标节点发送CLUSTER SETSLOT＜slot＞IMPORTING＜source_id＞命令，**让目标节点准备好从源节点导入（import）属于槽slot的键值对**。
  2. redis-trib对源节点发送CLUSTER SETSLOT＜slot＞MIGRATING＜target_id＞命令，**让源节点准备好将属于槽slot的键值对迁移（migrate）至目标节点**。
  3. redis-trib向源节点发送 CLUSTER GETKEYSINSLOT＜slot＞＜count＞命令，**获得最多count个属于槽slot的键值对的键名（key name）**。
  4. 对于获得的每个键名，redis-trib都向源节点发送一个 **MIGRATE ＜target_ip＞＜target_port＞＜key_name＞0＜timeout＞**命令，将被选中的键原子地从源节点迁移至目标节点。
  5. **重复执行步骤3和步骤4**，直到源节点保存的所有属于槽slot的键值对都被迁移至目标节点为止。

- redis-trib**向集群中的任意一个节点发送CLUSTER SETSLOT＜slot＞NODE＜target_id＞命令**，将槽slot指派给目标节点，这一**指派信息会通过消息发送至整个集群**，最终集群中的所有节点都会知道槽slot已经指派给了目标节点。

###### CLUSTER SETSLOT IMPORTING 命令的实现

- clusterState 结构的 **importing_slots_from 数组**记录了当前节点正在从其他节点导入的槽；
- 如果importing_slots_from[i]的值不为NULL，而是**指向一个clusterNode结构**，那么表示**当前节点正在从clusterNode所代表的节点导入槽i**。

###### CLUSTER SETSLOT MIGRATING 命令的实现

- clusterState结构的 **migrating_slots_to 数组**记录了当前节点正在迁移至其他节点的槽；
- 如果migrating_slots_to[i]的值不为NULL，而是指向一个clusterNode结构，那么表示**当前节点正在将槽i迁移至clusterNode所代表的节点**。

###### ASK 错误

- 当客户端向源节点发送一个与数据库键有关的命令，并且命令要处理的数据库键恰好就属于正在被迁移的槽时（重新分片）
  - 源节点会先在自己的数据库里面查找指定的键，如果找到的话，就直接执行客户端发送的命令；
  - 如果节点没有在自己的数据库里找到键key，那么**节点会检查自己的 clusterState.migrating_slots_to[i]，看键key所属的槽i是否正在进行迁移**，如果槽i的确在进行迁移的话，那么节点会**向客户端发送一个ASK错误，重定向客户端到正在导入槽i的节点去查找键key**。
  - ```java
    MOVED <slot> <ip>:<port>
    ```

###### ASKING 命令

- 唯一要做的就是服务器打开发送该命令的客户端的 REDIS_ASKING 标识；
- 在一般情况下，如果客户端向节点发送一个关于槽i的命令，而**槽i又没有指派给这个节点的话**，那么节点将向客户端返回一个MOVED错误；但是，如果**节点的clusterState.importing_slots_from[i]显示节点正在导入槽i，并且发送命令的客户端带有REDIS_ASKING标识**，那么节点将破例执行这个关于槽i的命令一次；
- 当客户端接收到ASK错误并转向至正在导入槽的节点时，**客户端会先向节点发送一个ASKING命令，然后才重新发送想要执行的命令**，这是因为如果客户端不发送ASKING命令，而直接发送想要执行的命令的话，那么客户端发送的命令将被节点拒绝执行，并返回MOVED错误。

##### 10：复制故障和转移【重要】

- Redis 集群中的节点**分为主节点（master）和从节点（slave）**，其中主节点用于处理槽，而从节点则用于复制某个主节点，并在被复制的主节点下线时，代替下线主节点继续处理命令请求。

###### 设置从节点

- 向一个节点发送命令，可以让接收命令的节点成为node_id所指定节点的从节点，并开始对主节点进行复制；

  - ```
    CLUSTER REPLICATE <node_id>
    ```

  - 接收到该命令的节点首先会在自己的 **clusterState.nodes 字典**中找到node_id所对应节点的clusterNode结构，并**将自己的 clusterState.myself.slaveof 指针指向这个结构**，以此来记录这个节点正在复制的主节点；

  - 然后节点会修改自己在**clusterState.myself.flags中的属性，关闭原本的REDIS_NODE_MASTER标识，打开REDIS_NODE_SLAVE标识**，表示这个节点已经由原来的主节点变成了从节点。

  - 集群中的所有节点都会在代表主节点的 clusterNode 结构的slaves属性和numslaves属性中记录正在复制这个主节点的从节点名单；

###### 故障检测

1. **主观下线状态**：集群中的**每个节点 1一次/s 都会向集群中的其他节点发送PING消息**，以此来检测对方是否在线，如果接收PING消息的节点**没有在规定的时间内，向发送PING消息的节点返回PONG消息**，那么发送PING消息的节点就会将接收PING消息的节点标记为**疑似下线（probable fail，PFAIL）**。
2. 当一个**主节点A通过消息得知主节点B认为主节点C进入了疑似下线状态时**，主节点A 会在自己的 clusterState.nodes 字典中找到主节点C所对应的clusterNode结构，并**将主节点B的下线报告（failure report）添加到 C  的 clusterNode结构的fail_reports 链表里面**；
3. 当收到 B 节点，认为 C 节点下线后，节点 A 也会向节点 C 发送一条 ping，检测是否真的下线；
4. **客观下线状态**：如果在一个集群里面，**半数以上负责处理槽的主节点都将某个主节点x报告为疑似下线**，那么这个主节点x将被标记为已下线（FAIL），将主节点x标记为已下线的节点会**向集群广播一条关于主节点x的FAIL消息，所有收到这条FAIL消息的节点都会立即将主节点x标记为已下线**。

###### 故障转移

- 与 Sentinel 一致；

###### 选举新的主节点

- 与 Sentinel 首领选举一致；

##### 11：消息

- 集群中的各个节点通过发送和接收消息（message）来进行通信；【五种消息格式】
  - **MEET消息**：请求加入集群；
  - **PING消息**：
    - 集群里的每个节点默认**每隔一秒钟就会从已知节点列表中随机选出五个节点**，然后对这五个节点中最长时间没有发送过PING消息的节点发送PING消息（贪婪），以此来检测被选中的节点是否在线；	
    - 如果节点A最后一次收到节点B发送的PONG消息的时间，距离当前时间已经超过了节点A的**cluster-node-timeout**选项设置时长的一半，则节点A会将节点B标记为下线，并开始尝试将其从集群中移除。
  - **PONG 消息**：
    - 为了向发送者确认这条MEET消息或者PING消息已到达，接收者会向发送者返回一条PONG消息；
    - 故障转移时：一个节点也可以通过向集群广播自己的PONG消息来让集群中的其他节点立即刷新关于这个节点的认识；
  - **FAIL消息**：当一个主节点A判断另一个主节点B已经进入FAIL状态时，节点A会向集群广播一条关于节点B的FAIL消息，所有收到这条消息的节点都会立即将节点B标记为**已下线**。
  - **PUBLISH消息**：当节点接收到一个PUBLISH命令时，节点会执行这个命令，并向集群广播一条PUBLISH消息，所有接收到这条PUBLISH消息的节点都会执行相同的PUBLISH命令。

###### 消息头

- 每个消息头都由一个cluster.h/clusterMsg结构表示：

- clusterMsg.data 属性指向联合cluster.h/clusterMsgData，这个联合就是消息的正文：

- ```c
  typedef struct {
      char sig[4];        /* Siganture "RCmb" (Redis Cluster message bus). */
      uint32_t totlen;    /* 消息总长度 */
      uint16_t ver;       /* 协议版本，当前设置为1。 */
      uint16_t port;      /* TCP端口号. */
      uint16_t type;      /* 消息类型 */
      uint16_t count;     /* data中的gossip session个数。（只在发送MEET、PING和PONG这三种消息时使用） */
      uint64_t currentEpoch;  /* 发送者所处的配置纪元 */
      uint64_t configEpoch;   /* 如果消息发送者是一个主节点，那么该项为消息发送者配置纪元。
                                如果消息发送者是一个从节点，那么该项为发送者正在复制的主节点纪元。 */
      uint64_t offset;                        /* 复制偏移量. */
      char sender[CLUSTER_NAMELEN];           /* 发送节点名称 */
      unsigned char myslots[CLUSTER_SLOTS/8]; /*消息发送者目前的槽指派信息*/
      char slaveof[CLUSTER_NAMELEN];          /*发送方如果是从，对应主的名称。*/
      char myip[NET_IP_STR_LEN];              /* 发送人IP地址，如果不是全部为0. */
      char notused1[34];                      /* 34 字节保留字节 */
      uint16_t cport;                         /* 发送方集群TCP 端口 */
      uint16_t flags;                         /* 发送人节点标志 */
      unsigned char state;                    /* 消息发送者所在集群的状态 */
      unsigned char mflags[3];                /* 消息标志: CLUSTERMSG_FLAG[012]_... */
      union clusterMsgData data;              /* 消息包内容 */
  } clusterMsg;
  ```

###### MEET、PING、PONG消息的实现

- Redis集群中的各个节点通过Gossip协议来交换各自关于不同节点的状态信息，其中Gossip协议由MEET、PING、PONG三种消息实现，这**三种消息的正文都由两个cluster.h/clusterMsgDataGossip结构组成**；

- ```c
  union clusterMsgData {
  	struct {
  		// 每条meet ping pong 消息都包含两个该结构
  		clusterMsgDataGossip gossip[1];
  	} ping;
  }
  ```

- 每次发送MEET、PING、PONG消息时，发送者都**从自己的已知节点列表中随机选出两个节点**（可以是主节点或者从节点），并将这两个被选中节点的信息**分别保存到两个clusterMsgDataGossip结构里面**。clusterMsgDataGossip结构记录了被选中节点的名字，发送者与被选中节点最后一次发送和接收PING消息和PONG消息的时间戳，被选中节点的IP地址和端口号，以及被选中节点的标识值：

- 当接收者收到MEET、PING、PONG消息时，接收者会访问消息正文中的两个clusterMsgDataGossip结构，并根据自己是否认识clusterMsgDataGossip结构中记录的被选中节点来选择握手（不存在）还是更新操作（存在）；

- 同样返回PONG消息，也会写到任意两个已知节点；

###### FAIL 消息的实现

- Gossip 协议也可以实现，只是传播较慢，下线需要立即让集群其他节点知道；

- FAIL消息的正文由cluster.h/clusterMsgDataFail结构表示，这个结构只包含一个nodename属性，该属性记录了**已下线节点的名字**：

  - ```c
    typedef struct {
    	char nodename[REDIS_CLUSTE_NAMELEN];
    } clusterMsgaDataFail;
    ```

###### PUBLISH 消息的实现

- 当客户端向集群中的某个节点发送命令，接收到PUBLISH命令的节点**不仅会向channel频道发送消息message，它还会向集群广播一条PUBLISH消息，所有接收到这条PUBLISH消息的节点都会向channel频道发送message消息**。

  - ```
    PUBLISH <channle> <message>
    ```

- PUBLISH消息的正文由cluster.h/clusterMsgDataPublish结构表示：

  - ```c
    typedef struct {
        uint32_t channel_len;        // 渠道名称长度
        uint32_t message_len;        // 消息长度
        unsigned char bulk_data[8];  // 渠道和消息内容
    } clusterMsgDataPublish;
    ```





