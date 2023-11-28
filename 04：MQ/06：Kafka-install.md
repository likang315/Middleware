### Kafka Install

------

[TOC]

##### 01：环境配置

- 安装 **Java**：Kafka 是用 Java 开发的应用程序。需要有一个 Java 运行时环境；

- 安装 **ZooKeeper**：Kafka 用 ZooKeeper 来**保存集群元数据和消费者信息**；

- 安装 **broker**：

  - ```shell
    mv kafka_2.13-2.7.0 /usr/local/kafka
    mkdir /tmp/kafka-logs
    export JAVA_HOME=/usr/java/jdk-11.0.10
    # 以配置文件 server.properties 启动 kafka
    /usr/local/kafka/bin/kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties
    
    # 创建 topic 
    /usr/local/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 1 --topic test_topic
    # 发送消息测试
    /usr/local/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test_topic
    # 消费消息
    /usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test_topic --from-beginning
    ```

##### 02：配置 Broker

###### 常规配置参数

- **broker.id**
  - 每个 broker 唯一整数标识符，使用 broker.id 来表示。它的默认值是0，也可以被设置成其他任意整数。这个值在整个 Kafka 集群里必须是唯一的；建议把 ID 设置成与主机名具有相关性的整数。
- **listeners**
  - 如果使用了 Kafka 自带的示例配置，那么Kafka 在启动时会在 **TCP 端口 9092** 上设置一个监听器；
  - 新的 listeners 配置参数是一个**用逗号分隔的 URI 列表**，也就是要监听的地址和端口和安全协议；
    - 监听器的格式为 `<protocol>://<hostname>:<port>`，例如：**PLAINTEXT://localhost:9092,SSL://:9091**；
  
- **zookeeper.connect**
  - 用于保存 broker 元数据的，Zookeeper 地址是通过 zookeeper.connect 来指定的。
  - 该配置参数是用冒号分隔的一组 hostname:port/path 列表，每一部分的含义如下：
    - hostname：是 Zookeeper 服务器的机器名或IP地址；
    - port：是 Zookeeper 的客户端连接端口；
    - /path 是可选的 Zookeeper 路径，作为 Kafka 集群的 chroot 环境。如果不指定，默认使用根路径。如果指定的chroot 路径不存在，broker 会在启动的时候创建它。
- **log.dirs**
  - Kafka 把所有消息都保存在磁盘上，**存放这些日志片段的目录是通过 log.dirs 指定的**。它是一组用逗号分隔的本地文件系统路径。如果指定了多个路径，那么 broker 会根据“**最少使用**”原则，把同一个分区的日志片段保存到同一个路径下;
- num.recovery.threads.per.data.dir
  - Kafka 会使用可配置的线程池来处理日志片段：
    - 服务器正常启动，用于打开每个分区的日志片段；
    - 服务器崩溃后重启，用于检查和截短每个分区的日志片段；
    - 服务器正常关闭，用于关闭日志片段。
  - 所配置的数字对应的是 log.dirs 指定的单个日志目录对应的并发度；
- **auto.create.topics.enable**
  - 默认情况下，Kafka 会在如下几种情形下自动创建主题，隐式创建topic；
    - 当一个生产者开始往主题写入消息时；
    - 当一个消费者开始从主题读取消息时；
    - 当任意一个客户端向主题发送元数据请求时。
- auto.leader.rebalance.enable
  - 为了确保主题的所有权不会集中在一台 broker 上，可以将这个参数设置为 true，让主题的所有权尽可能地在集群中保持均衡。
  - 如果启用了这个功能，那么就会有一个后台线程定期检查分区的分布情况（时间间隔： leader.imbalance.check.interval.seconds）。如果不均衡的所有权超出了 leader.imbalance.per.broker.percentage 指定的百分比，则会**启动一次分区首领再均衡**。

- delete.topic.enable
  - 以防主题被随意删除。把这个参数设置为 false 就可以**禁用主题删除功能**；


###### 主题的默认配置

- **num.partitions**
  - 指定了**新创建的主题将包含多少个分区**。如果启用了主题自动创建功能（该功能默认是启用的），主题分区的个数就是该参数指定的值。该参数的**默认值是1**。
  - 可以增加主题分区的个数，但不能减少分区的个数;
- default.replication.factor
  - 如果启用了自动创建主题功能，那么这个参数的值就是**新创建主题的复制系数**；
- log.retention.ms
  - Kafka 通常根据配置的时间长短来决定**数据可以被保留多久**，默认为 一周。作用于日志片段而不是单条消息上；
- log.retention.bytes
  - 计算已保留的消息的字节总数来判断旧消息是否过期。这个**字节总数阈值**通过参数 log.retention.bytes 来指定，对应的是每一个分区。
- **log.segment.bytes**
  - 当消息到达 broker 时，它们会被追加到分区的当前日志片段上。当日志片段大小达到 log.segment.bytes 指定的上限（默认是 1 GB）时，当前日志片段会被关闭，一个新的日志片段会被打开。
  - **一旦日志片段被关闭，就可以开始进入过期倒计时**。
-  **log.roll.ms**
  - 指定了多长时间之后日志片段可以被关闭。
  - 在默认情况下，log.roll.ms 没有设定值，所以**使用 log.roll.hours 设定的默认值——168 小时，也就是 7 天。**

-  min.insync.replicas
  - 为了提升集群的数据持久性，可以将 min.insync.replicas 设置为 2，确保至少有两个副本跟生产者保持同步。

- **message.max.bytes**
  - broker 通过设置 message.max.bytes 参数来限制单条消息的大小，**默认值是 1MB**。超过，拒收返回错误信息；
  - 需要与以下两个参数保持一致；
    - fetch.max.bytes：消费者拉取的最大字节
    - replica.fetch.max.bytes：broker 同步拉取的最大字节；


##### 03：配置 **Kafka** 集群【高可用】

###### 一个 Kafka 集群需要多少个 broker

- 磁盘容量
  - 整个集群需要保留多大的数据；
- 单个 broker 的复制容量
- CPU
  - 目前，建议每个 broker 的分区副本不超过 14 000 个，每个集群的分区副本不超过 100 万个。
- 网络

###### broker 的配置

- 要让一个 broker 加入集群，只需要修改两个配置参数。
  - **zookeeper.connect：**这个参数指定了用于保存元数据的 ZooKeeper 的群组和路径；
  - **broker.id：** 每个 broker 都必须为其指定唯一值；

##### 04：共享 Zookeeper

- Kafka 使用 ZooKeeper 保存 broker、主题和分区的元数据。
- **只有当消费者群组成员或 Kafka 集群本身发生变化时才会向 ZooKeeper 写入数据**。这些流量通常很小，所以没有必要为单个 Kafka 集群使用专门的 ZooKeeper 集群。（使用单个  ZooKeeper 来保存多个 Kafka 集群的元数据）。
- 随着时间的推移，Kafka 对 ZooKeeper 的依赖在减少，最后做到无 ZK 化；





