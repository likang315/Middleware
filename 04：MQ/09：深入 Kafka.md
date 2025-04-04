### 深入 Kafka

------

[TOC]

##### 01：集群成员关系（Broker 注册）

- Kafka 使用 Zookeeper 来维护集群成员的信息。每个 broker 都有一个**唯一标识符(brokerID)**，这个标识符可以在配置文件里指定，也可以自动生成。在 broker 启动的时候，它通过**创建临时节点把自己的 ID 注册到 Zookeeper**。Kafka 组件订阅 **Zookeeper 的/brokers/ids 路径**（broker 在 Zookeeper 上的注册路径），当有 broker 加入集群或退出集群时，这些组件就可以获得通知。
- 在 **broker 宕机、出现网络分区或长时间垃圾回收停顿**时，broker 会从 Zookeeper 上断开连接，此时**broker 在启动时创建的临时节点会自动从 Zookeeper 上移除**。监听 broker 列表的 Kafka 组件会被告知该 broker 已移除。
- 在关闭 broker 时，它对应的节点也会消失，不过**它的 ID 会继续存在于其他数据结构中**。例如，主题的副本列表里就可能包含这些ID。在完全关闭一个 broker 之后，如果**使用相同的ID 启动另一个全新的broker**，它会立即加入集群，并**拥有与旧broker相同的分区和主题**。

##### 02：集群控制器（Controller）

<img src="https://github.com/likang315/Middleware/blob/master/04：MQ/photos/kafka-zk.png?raw=true" alt="kafka-zk" style="zoom:30%;" />

- 集群控制器其实就是一个broker，只不过它**除了具有一般  broker 的功能之外，还负责分区首领的选举**。集群里第一个启动的 broker 通过在 Zookeeper 里**创建一个临时节点/controller 让自己成为控制器**。其他 broker 在启动时也会尝试创建这个节点，不过它们会收到一个**“节点已存在”的异常**，然后“意识”到控制器节点已存在。**其他 broker 在控制器节点上创建Zookeeper watch 对象（观察者模式）**，这样它们就可以**收到这个节点的变更通知**。

###### 集群控制器选举（Controller 选举）

- Kafka 通过 Zookeeper 的临时节点来选举控制器，并在节点加入集群或退出集群时通知控制器。
- 如果**控制器被关闭或者与Zookeeper 断开连接**，Zookeeper 上的临时节点就会消失。集群里的**其他broker 通过 watch 对象得到控制器节点消失的通知**，它们会尝试让自己成为新的控制器。**第一个在Zookeeper 里成功创建控制器节点的broker 就会成为新的控制器，其他节点会收到“节点已存在”的异常，然后在新的控制器节点上再次创建watch 对象。**每个新选出的控制器**通过 Zookeeper 的条件递增操作获得一个全新的、数值更大的 controller epoch**。其他 broker 在收到当前 controller epoch 后，如果收到由控制器发出的包含较旧 epoch 的消息，就会**忽略旧的**；
- 控制器**使用 epoch 来避免“脑裂”**。“脑裂”是指两个节点同时认为自己是当前的控制器。

###### 分区首领选举（Leader 选举）

- 当控制器发现一个broker 已经离开集群（通过观察相关的Zookeeper 路径），它就知道，那些**失去首领的分区需要一个新首领**（这些分区的首领刚好是在这个 broker 上）。控制器遍历失去首领的分区，并确定谁应该成为新首领（简单来说就是**分区副本列表里的下一个 同步的副本**），然后向所有包含新首领或现有跟随者的 broker 发送请求。该请求消息包含了**谁是新首领以及谁是分区跟随者的信息**。随后，新**首领开始处理来自生产者和消费者的请求，而跟随者开始从新首领那里复制消息**。

###### 群主选举

- 第一个加入群组的消费者将成为”群主”，选举时选出第一个 HashMap 对应的成员，基本是随机选取的；

###### 新控制器KRaft

- 2021 年 9 月发布的 **Kafka 3.0 包含了它的第一个生产版本**，Kafka 集群既可以使用基于 ZooKeeper 的传统控制器，也可以使用 KRaft。
- 在新架构中，控制器节点形成了**一个 Raft 仲裁，管理着元数据事件日志**。这个日志中包含了集群元数据的每一个变更。原先保存在 ZooKeeper 中的所有东西（比如主题、分区、ISR、配置等）都将被保存在这个日志中。
- 因为使用了 Raft 算法，所以**控制器节点可以在不依赖外部系统的情况下选举首领**。首领节点被称为主控制器，负责处理所有来自 broker 的 RPC 调用。跟随者控制器会从主控制器那里复制数据，并会作为主控制器的热备。因为控制器会跟踪最新的状态，所以当发生控制器故障转移时（在此期间，所有的状态都将被转移给新控制器），很快就可以完成状态的重新加载。

##### 03：同步（复制）机制

- Kafka 高可用：它可以在个别节点故障时，保证 Kafka 的可用性和持久性。
- Kafka 使用主题（Topic）来组织数据，**每个主题被分为若干个分区，每个分区有多个副本**。那些副本被保存在broker 上，**每个 broker 可以保存成百上千个属于不同主题和分区副本**；

###### AR | LSR | OSR | HW | LEO

- **AR（Assigned Replicas）**：分区中的分配的副本；
- **ISR（In-Sync Replica）**：所有同步的副本；
- **OSR（Out-of-Sync Replica）**：所有不同步的副本；
- **HW（High Watermark）**：高水位，当前**已经被提交且被同步到副本的最高偏移量**（offset），消费者只能拉取到这个 offset之前的消息。
- **LEO（Log End Offset）**：Kafka 每个分区中**最新消息的偏移量**，即当前已经被写入的最高偏移量；

###### 首领副本（Leader）

- 每个分区都有一个首领副本。为了保证一致性，**所有生产者请求和消费者请求都会经过这个副本**。

###### 跟随者副本（Follower）

- 首领以外的副本都是跟随者副本。**跟随者副本不处理来自客户端的请求，它们唯一的任务就是从首领那里同步消息，保持与首领一致的状态**。如果首领发生崩溃，其中的一个跟随者会被升级为新首领。
  - 可以**从跟随者副本读取数据**。这个特性的主要目的是允许客户端从最近的同步副本而不是首领副本读取数据，以此来降低网络流量成本，需要配置；

- 首领的另一个任务是弄清楚**哪个跟随者的状态与自己是一致的**。跟随者为了保持与首领的状态一致，在有新消息到达时尝试从首领那里复制（同步）消息，不过有各种原因会导致同步失败，但是最终会保持一致；首领通过查看每个跟随者请求的最新偏移量，首领就会知道每个跟随者复制的进度。如果跟随者在10s 内没有请求任何消息，或者虽然在请求消息，但在10s 内**没有请求最新的数据，那么它被认为是不同步的**。如果一个副本无法与首领保持一致，在首领发生失效时，它就**不可能成为新首领**，毕竟它没有包含全部的消息。相反，**持续请求得到的最新消息副本被称为同步的副本**（ISR)。在首领发生失效时，只有同步副本才有可能被选为新首领。

###### 首选首领（Preferred Leader）

- 除了首领之外，每个分区都有一个首选首领，在分区的领导者选举中优先考虑的副本。**在创建分区时人为分配的首领就是分区的首选首领**。通常是副本清单里的第一个副本。
- 默认情况下，Kafka 的 auto.leader.rebalance.enable 被设为true，它会检**查首选首领是不是当前首领**，如果不是，并且该副本是同步的，那么就会触发首领选举，让首选首领成为当前首领。

##### 04：处理请求

- broker 的大部分工作是**处理客户端、分区副本和控制器发送给分区首领的请求**，按照请求到达的顺序来处理它们，这种顺序既能保证让 Kafka 具备消息队列的特性，又能保证保存的消息是有序的。

###### Kafka 处理请求的流程

1. broker 会在它所监听的每一个端口上运行一个 **Acceptor 线程**，这个线程会创建一个连接，并把它交给 **Processor 线程**去处理；

2. Processor 线程（“网络线程”）的数量是可配置的。网络线程负责从客户端获取请求消息，把它们放进请求队列；

3. **IO 线程**负责处理他们，然后从响应队列获取响应消息，把它们发送给客户端；

   <img src="https://github.com/likang315/Middleware/blob/master/04：MQ/photos/kafka-process-request.png?raw=true" style="zoom:50%;" />

###### 元数据请求【重要】

- 客户端如何知道把请求发送给分区的首领副本？
  - 客户端使用**元数据请求类型**。这种请求包含了**客户端订阅的主题列表**。服务器端的响应消息里指明了这些**主题所包含的分区、每个分区都有哪些副本，以及哪个副本是首领**。元数据请求可以发送给任意一个broker，因为**所有 broker 都缓存了这些信息**。
  - 一般情况下，**客户端会缓存这些信息**，并直接往目标 broker 上发送生产请求和获取请求。它们需要时不时地通过**发送元数据请求来刷新这些信息**（刷新的时间间隔通过 metadata.max.age.ms 参数来配置），从而知道元数据是否发生了变更；
  - 还会告知那个 broker 是集群控制器；

###### 生产请求

- **生产者发送的请求**，它包含客户端要写入 broker 的消息。
- 参数 acks 用于指定需要多少个 broker 确认才可以认为一个消息写入是成功的，如果 acks 配置的值是 `all`，那么这些请求会被保存在炼狱（Purgatory）的缓冲区中，直到首领副本发现跟随者副本都复制了消息，响应才会发送给客户端；

###### 读取请求

- 在消费者和跟随者副本需要**向 broker 发送读取消息的请求**。
- 客户端发送请求，向 broker 请求**主题分区里具有特定偏移量的消息**，Kafka 使用**零复制（拷贝）技术**向客户端发送消息，Kafka 直接把消息从文件（或者更确切地说是Linux 文件系统缓存）里发送到网络通道，而不需要经过任何中间缓冲区（MySQL 会经过缓冲池）。其他数据库在将数据发送给客户端之前会先把它们保存在本地缓存中。**这项技术避免了字节复制，也不需要管理内存缓冲区**，从而能够获得更好的性能。
- 客户端除了可以设置 broker 返回数据的上限，也可以设置下限，**上限用于控制消息数量，防止撑爆内存，下限用于减少网络开销**。当然消费者也不会一直等待，有超时时间限制；
- <img src="https://github.com/likang315/Middleware/blob/master/04：MQ/photos/kafka-reqest.png?raw=true" style="zoom:70%;" />

###### 注意

- **并不是所有保存在分区首领上的数据都可以被客户端读取**，因为还没有被足够多副本复制的消息被认为是“不安全”的，**如果首领发生崩溃**，另一个副本成为新首领，那么这些消息就丢失了。如果我们允许消费者读取这些消息，可能就会破坏一致性；

###### 其他请求

- **LeaderAndIsr 请求**【重要】
  - 当一个新首领被选举出来，控制器会发送 **LeaderAndIsr 请求给新首领**（这样它就可以开始接收来自客户端的请求）**和跟随者**（这样它们就知道要开始跟随新首领同步消息）。

##### 05：物理存储（持久化机制）

- **Kafka 的基本存储单元是分区**。分区无法在多个 broker 间进行再细分，也无法**在同一个  broker 的多个磁盘上进行再细分**。所以，分区的大小受到**单个挂载点可用空间的限制**；
- 在配置 Kafka 的时候，指定了一个用于存储分区的目录清单，**log.dirs 参数**，该参数一般会包含每个挂载点的目录。
- **日志追加**的方式，避免了随机写。不是直接写磁盘，**写入到缓冲中**，定时刷新到磁盘上；

###### 分层存储【不建议使用】

- 在分层存储架构中，Kafka 集群配置了两个存储层：**本地存储层和远程存储层**。本地存储层和当前的Kafka 存储层一样，使用 broker 的本地磁盘存储日志片段，远程存储层则使用 HDFS、S3 等专用存储系统存储日志片段。用户可以**单独为每一层配置保留策略**。

###### 分区的分配

- 在创建主题时，Kafka 首先要决定如何在 broker 间分配分区，以保证高可用；**自动进行的；**
  - 在broker 间平均地分布分区副本，**确保每个分区的每个副本分布在不同的broker 上。**
  - 如果为 broker 指定了机架信息（机房），那么尽可能把**每个分区的副本分配到不同机架的broker上**。
- 决定新分区应该使用哪个目录
  - 计算每个目录里的分区数量，**新的分区总是被添加到数量最小的那个目录**，以达到均衡分配的状态。

###### 文件管理

- 数据保留：Kafka 不会一直保留数据，也不会一直等到消息被所有消费者读取了之后才将其删除。相反，**Kafka 管理员会为每个主题配置数据保留期限**，主题的数据要么在达到指定的时间之后被清除，要么在达到指定的数量之后被清除。
- **把分区分成若干个片段**。在默认情况下，每个片段包含 1 GB 或一周的数据，以较小的那个为准。在 broker 向分区写入数据时，如果触及任意一个上限，就关闭当前文件，并打开一个新文件，**顺序读写**。

###### 文件格式 & 索引【重要】

- 每个分区对应一个文件目录，用于保存该分区的数据。每个文件目录下，有多个日志片段（Segment）。**每个 Segment 有index、 log 和 timeindex 三个文件。**
  - **index 文件：**偏移量索引文件，**将偏移量与片段文件以及偏移量在文件中的位置做了映射**，让消费者从任意可用的偏移量处开始消费；
    - offset：为每一个批次最后的offset；
    - position：相对于文件起始的地址；在磁盘中的物理地址为这个文件的起始物理地址 + position。

  - **timeindex：**时间戳索引文件，基于时间的索引文件；
  - **log 文件：**记录了一批的消息，一批消息公用一批元数据；
    - offset
    - CreateTime
    - keysize
    - valuesize
    - headerKeys
    - key
    - payload

- 保存在磁盘上的数据格式与生产者发送给服务器的消息格式以及服务器发送给消费者的消息格式是一样的。因为**磁盘存储和网络传输采用了相同的格式，所以 Kafka 可以使用零复制技术向消费者发送消息**，并避免对生产者压缩过的消息进行解压和再压缩。

###### 压实策略（Compact）

- 保留数据的三种策略
  - **delete：默认的**，根据设置的时间来保留数据，把超过时效的旧数据删除；
  - **compact：只为每个键（key，相当于分区）保留最新的值**，节省空间，如果topic 中包含了 null 键，那么就会失效；
  - **delete,compact（组合策略）**：既能节省空间，又能满足需要在一段时间后删除数据的业务；

###### 压实的工作原理

- 每个日志片段可以分为以下两个部分
  - 干净的部分：这些消息之前被压实过，每个键只有一个对应的值；
  - 浑浊的部分：这些消息是在上一次压实之后写入的；

- 压实原理
  - 通过配置 **log.cleaner.enabled** 参数来开启， broker 在启动时会**创建一个压实管理器线程和一些压实工作线程**来执行压实任务。这些线程会选择浑浊率（浑浊的消息占分区总体消息的比例）最高的分区来压实。
  - 压实线程会**读取分区的浑浊部分，并在内存中创建一个 map。**map 的每个元素都包含消息键的哈希值（16 字节）和上一条具有相同键的消息的偏移量（8 字节）；在创建好 map 后，**压实线程会开始从干净的片段读取消息，它会先读取最旧的消息，把它们的内容与 map 中的内容进行比对，检查消息的键是否存在于 map 中，如果不存在，则说明这条消息的值是最新的，就把它复制到替换片段上。如果键已存在，就忽略这条消息，因为后面会有一条更新的包含相同键的消息。**在复制完所有消息后，将替换片段与原始片段进行交换，然后开始压实下一个片段。**完成整个压实过程后，每一个键对应一条消息，这些消息的值都是最新的。**
  - <img src="https://github.com/likang315/Middleware/blob/master/04：MQ/photos/compact.png?raw=true" style="zoom:70%;" />


###### 何时会压实主题

- 在默认情况下，**Kafka 会在 topic 中有 50% 的数据包含脏记录的情况下进行压实**。这样做的目的是避免压实太过频繁（因为压实会影响主题的读写性能），同时也能避免存在太多脏记录（因为它们会占用磁盘空间）。
- min.compaction.lag.ms：用于确保消息被写入之后最短需要经过多长时间才可以被压实。

##### 06：零拷贝（Zero Copy)【重要】

- 使用场景：消费已经持久化的消息时，**必须需要从磁盘中将数据读取到内存中，并通过网卡发送给消费者**；
- 指将数据在**内核空间直接从磁盘文件复制到网卡中**，而不需要经由用户态的应用程序，**减少两次不必要的拷贝和上下文的切换**，提高数据传输效率。
- DMA（Direct Memory Access）：直接内存访问技术，绕过 CPU 直接在内存和外设之间进行数据传输。

###### 传统的文件拷贝流程传统数据文件拷贝过程

1. 操作系统将数据从磁盘中加载到内核空间的Read Buffer（页缓存区）中。
2. 应用程序将**Read Buffer中的数据拷贝到应用空间的应用缓冲区**中。
3. 应用程序将**应用缓冲区的数据拷贝到内核的Socket Buffer**中。
4. 操作系统将数据从Socket Buffer中发送到网卡，通过网卡发送给数据接收方。
   - <img src="https://github.com/likang315/Middleware/blob/master/04：MQ/photos/zero-copy.png?raw=true" style="zoom:70%;" />

###### 零拷贝过程

- 操作系统将数据从磁盘中加载到内核空间的Read Buffer（页缓存区）中。
- 操作系统之间**将数据从内核空间的Read Buffer（页缓存区）直接传输到网卡中，并通过网卡将数据发送给接收方**。
- 操作系统**将文件的描述符拷贝到Socket Buffer中，不会拷贝数据**。

###### 零拷贝实现机制

- MMap（Memory Map）：Linux 操作系统中提供的一种**将文件映射到进程地址空间的一种机制**，通过 MMap 进程可以像访问内存一样访问文件，而无需显式的复制操作。
- `sendFile()`： 是一个系统调用函数，用于高效地**将文件数据从内核空间直接传输到网络套接字（Socket）上**，从而实现零拷贝技术。
