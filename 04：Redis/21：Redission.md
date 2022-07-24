### Redission

------

[TOC]

##### 01：概述

- Redisson是一个在Redis的基础上实现的**Java驻内存数据网格**（In-Memory Data Grid）。它不仅提供了一系列的**分布式的Java常用对象，还提供了许多分布式服务**。
- Redisson 作为独立节点可以用于独立执行其他节点发布到分布式执行服务和分布式调度任务服务里的远程任务。
- Redisson底层采用的是Netty框架。支持Redis 2.8以上版本，支持 Java1.6+以上版本。
- Redis 命令与Redission 对象匹配列表
  - https://github.com/redisson/redisson/wiki/11.-redis命令和redisson对象匹配列表

##### 02：配置方法

###### 程序化配置方法

- Redisson程序化的配置方法是通过构建`Config`对象实例来实现的

- ```java
  Config config = new Config();
  config.setTransportMode(TransportMode.EPOLL);
  config.useClusterServers()
        //可以用"rediss://"来启用SSL连接
        .addNodeAddress("redis://127.0.0.1:7181");
  ```

###### 文件方式配置（yaml 或者properties 文件）

- ```java
  Config config = Config.fromYAML(new File("config-file.yaml"));
  RedissonClient redisson = Redisson.create(config);
  ```

##### 03：常用设置

- `org.redisson.Config`类的配置参数，它适用于所有Redis组态模式（单机，集群和哨兵）

###### codec（编码）

- 默认值: `org.redisson.codec.JsonJacksonCodec`

- Redisson的对象编码类是用于将对象进行序列化和反序列化，以实现对该对象在Redis里的读取和存储.

- | 编码类名称                                      | 说明                                                         |
  | ----------------------------------------------- | ------------------------------------------------------------ |
  | `org.redisson.codec.JsonJacksonCodec`           | Jackson JSON 编码 **默认编码**                               |
  | `org.redisson.codec.AvroJacksonCodec`           | [Avro](http://avro.apache.org/) 一个二进制的JSON编码         |
  | `org.redisson.codec.SmileJacksonCodec`          | [Smile](http://wiki.fasterxml.com/SmileFormatSpec) 另一个二进制的JSON编码 |
  | `org.redisson.codec.CborJacksonCodec`           | [CBOR](http://cbor.io/) 又一个二进制的JSON编码               |
  | `org.redisson.codec.MsgPackJacksonCodec`        | [MsgPack](http://msgpack.org/) 再来一个二进制的JSON编码      |
  | `org.redisson.codec.IonJacksonCodec`            | [Amazon Ion](https://amzn.github.io/ion-docs/) 亚马逊的Ion编码，格式与JSON类似 |
  | `org.redisson.codec.KryoCodec`                  | [Kryo](https://github.com/EsotericSoftware/kryo) 二进制对象序列化编码 |
  | `org.redisson.codec.SerializationCodec`         | JDK序列化编码                                                |
  | `org.redisson.codec.FstCodec`                   | [FST](https://github.com/RuedigerMoeller/fast-serialization) 10倍于JDK序列化性能而且100%兼容的编码 |
  | `org.redisson.codec.LZ4Codec`                   | [LZ4](https://github.com/jpountz/lz4-java) 压缩型序列化对象编码 |
  | `org.redisson.codec.SnappyCodec`                | [Snappy](https://github.com/xerial/snappy-java) 另一个压缩型序列化对象编码 |
  | `org.redisson.client.codec.JsonJacksonMapCodec` | 基于Jackson的映射类使用的编码。可用于避免序列化类的信息，以及用于解决使用`byte[]`遇到的问题。 |
  | `org.redisson.client.codec.StringCodec`         | 纯字符串编码（无转换）                                       |
  | `org.redisson.client.codec.LongCodec`           | 纯整长型数字编码（无转换）                                   |
  | `org.redisson.client.codec.ByteArrayCodec`      | 字节数组编码                                                 |
  | `org.redisson.codec.CompositeCodec`             | 用来组合多种不同编码在一起                                   |

###### threads（线程池数量）

- 默认值: `当前处理核数量 * 2`
- 这个线程池数量被所有`RTopic`对象监听器，`RRemoteService`调用者和`RExecutorService`任务共同共享。

###### nettyThreads （Netty线程池数量）

- 这个线程池数量是在一个Redisson实例内，被其创建的所有分布式数据类型和服务，以及底层客户端所一同共享的线程池里保存的线程数量。

###### executor（线程池）

- 单独提供一个用来**执行所有`RTopic`对象监听器，`RRemoteService`调用者和`RExecutorService`任务的线程池（ExecutorService）实例**。

###### eventLoopGroup

- 用于特别指定一个EventLoopGroup。
- EventLoopGroup是用来处理所有通过Netty与Redis服务之间的连接发送和接受的消息。每一个Redisson都会在默认情况下自己创建管理一个EventLoopGroup实例。因此，如果在同一个JVM里面可能存在多个Redisson实例的情况下，采取这个配置实现多个Redisson实例共享一个EventLoopGroup的目的。
- 只有`io.netty.channel.epoll.EpollEventLoopGroup`或`io.netty.channel.nio.NioEventLoopGroup`才是允许的类。

###### transportMode（传输模式）

- 默认值：`TransportMode.NIO`
- 可选参数：
  - `TransportMode.NIO`,
  - TransportMode.EPOLL`:需要依赖里有`netty-transport-native-epoll`包（Linux）
  -  `TransportMode.KQUEUE` :需要依赖里有 `netty-transport-native-kqueue`包（macOS）

###### lockWatchdogTimeout（监控锁的看门狗超时，单位：毫秒）

- 默认值：`30000`
- 监控锁的看门狗超时时间单位为毫秒。该参数只适用于分布式锁的加锁请求中未明确使用`leaseTimeout`参数的情况。如果该看门狗未使用`lockWatchdogTimeout`去重新调整一个分布式锁的`lockWatchdogTimeout`超时，那么这个锁将变为失效状态。这个参数可以用来避免由Redisson客户端节点宕机或其他原因造成死锁的情况。

###### keepPubSubOrder（保持订阅发布顺序）

- 默认值：`true`
- 通过该参数来修改是否按**订阅发布消息的接收顺序出来消息**，如果选否将对消息实行并行处理，该参数只适用于订阅发布消息的情况。

##### 04：集群模式（ClusterServersConfig）

###### 示例

```java
Config config = new Config();
config.useClusterServers()
    .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
    //可以用"rediss://"来启用SSL连接
    .addNodeAddress("redis://127.0.0.1:7000", "redis://127.0.0.1:7001")
    .addNodeAddress("redis://127.0.0.1:7002");

RedissonClient redisson = Redisson.create(config);
ClusterServersConfig clusterConfig = config.useClusterServers();
```

###### nodeAddresses（添加节点地址）

- 可以通过`host:port`的格式来添加Redis集群节点的地址。多个节点可以一次性批量添加。

###### scanInterval（集群扫描间隔时间）

- 默认值： `1000`
- 对Redis集群节点状态扫描的时间间隔。单位是毫秒。

###### slots（分片数量）

- 默认值： `231` 用于指定数据分片过程中的分片数量。支持数据分片/框架结构有：SET、MAP、BITSET、Bloom filter等；

###### readMode（读取操作的负载均衡模式）

- 默认值： `SLAVE`（只在从服务节点里读取）
- 注：在从服务节点里读取的数据说明已经至少有两个节点保存了该数据，确保了数据的高可用性。
- 设置读取操作选择节点的模式。 可用值为：
  -  **`SLAVE` - 只在从服务节点里读取**；
  - `MASTER` - 只在主服务节点里读取；
  -  **`MASTER_SLAVE` - 在主从服务节点里都可以读取；**

###### subscriptionMode（订阅操作的负载均衡模式）

- 默认值：`SLAVE`（只在从服务节点里订阅）
- 设置订阅操作选择节点的模式。 可用值为： `SLAVE` - 只在从服务节点里订阅。 `MASTER` - 只在主服务节点里订阅。

###### loadBalancer（负载均衡算法类的选择）

- 默认值： `org.redisson.connection.balancer.RoundRobinLoadBalancer`
- 在多Redis服务节点的环境里，可以选用以下几种负载均衡方式选择一个节点：
  - `org.redisson.connection.balancer.WeightedRoundRobinBalancer` - **权重轮询调度算法**
  - `org.redisson.connection.balancer.RoundRobinLoadBalancer` - 轮询调度算法
  - `org.redisson.connection.balancer.RandomLoadBalancer` - 随机调度算法法

###### subscriptionConnectionMinimumIdleSize（从节点发布和订阅连接的最小空闲连接数）

- 默认值：`1`
- 多从节点的环境里，每个从服务节点里用于发布和订阅连接的最小保持连接数（长连接）。Redisson内部经常通过发布和订阅来实现许多功能。长期保持一定数量的发布订阅连接是必须的。

###### subscriptionConnectionPoolSize（从节点发布和订阅连接池大小）

- 默认值：`50`
- 多从节点的环境里，每个从服务节点里用于发布和订阅连接的连接池最大容量。连接池的连接数量自动弹性伸缩。

###### slaveConnectionMinimumIdleSize（从节点最小空闲连接数）

- 默认值：`32`
- 多从节点的环境里，每个从服务节点里用于普通操作（非发布和订阅）的最小保持连接数（长连接）。长期保持一定数量的连接有利于提高瞬时读取反映速度。

###### slaveConnectionPoolSize（从节点连接池大小）

- 默认值：`64`
- 多从节点的环境里，每个从服务节点里用于普通操作（非发布和订阅）连接的连接池最大容量。连接池的连接数量自动弹性伸缩。

##### masterConnectionMinimumIdleSize（主节点最小空闲连接数）

- 默认值：`32`
- 多节点的环境里，每个主节点的最小保持连接数（长连接）。长期保持一定数量的连接有利于提高瞬时写入反应速度。

###### masterConnectionPoolSize（主节点连接池大小）

- 默认值：`64`
- 多主节点的环境里，每个主节点的连接池最大容量。连接池的连接数量自动弹性伸缩。

###### idleConnectionTimeout（连接空闲超时，单位：毫秒）

- 默认值：`10000`
- **如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉**。时间单位是毫秒。

###### connectTimeout（连接超时，单位：毫秒）

- 默认值：`10000`
- 同任何节点建立连接时的等待超时。时间单位是毫秒。

###### timeout（命令等待超时，单位：毫秒）

- 默认值：`3000`
- 等待节点回复命令的时间。该时间从命令发送成功时开始计时。

###### retryAttempts（命令失败重试次数）

- 默认值：`3`
- 如果尝试达到 **retryAttempts（命令失败重试次数）** 仍然不能将命令发送至某个指定的节点时，将抛出错误。如果尝试在此限制之内发送成功，则开始启用 **timeout（命令等待超时）** 计时。

###### retryInterval（命令重试发送时间间隔，单位：毫秒）

- 默认值：`1500`
- 在某个节点执行相同或不同命令时，**连续** 失败 **failedAttempts（执行失败最大次数）** 时，该节点将被从可用节点列表里清除，直到 **reconnectionTimeout（重新连接时间间隔）** 超时以后再次尝试。

###### password（密码）

- 默认值：`null`
- 用于节点身份验证的密码。

###### subscriptionsPerConnection（单个连接最大订阅数量）

- 默认值：`5`
- 每个连接的最大订阅数量。

###### clientName（客户端名称）

- 默认值：`null`
- 在Redis节点里显示的客户端名称。

###### sslEnableEndpointIdentification（启用SSL终端识别）

- 默认值：`true`
- 开启SSL终端识别能力。

###### sslProvider（SSL实现方式）

- 默认值：`JDK`，确定采用哪种方式（JDK或OPENSSL）来实现SSL连接。

###### sslTruststore（SSL信任证书库路径）

- 默认值：`null`，指定SSL信任证书库的路径。

###### sslTruststorePassword（SSL信任证书库密码）

- 默认值：`null`，指定SSL信任证书库的密码。

###### sslKeystore（SSL钥匙库路径）

- 默认值：`null`，指定SSL钥匙库的路径。

##### sslKeystorePassword（SSL钥匙库密码）

- 默认值：`null`，指定SSL钥匙库的密码。

##### 05：单节点模式（SingleServerConfig）

###### 示例

```java
// 默认连接地址 127.0.0.1:6379
RedissonClient redisson = Redisson.create();

Config config = new Config();
config.useSingleServer().setAddress("redis://127.0.0.1:6379");
RedissonClient redisson = Redisson.create(config);
SingleServerConfig singleConfig = config.useSingleServer();
```

###### address

- 可以通过`host:port`的格式来指定节点地址。

###### subscriptionConnectionMinimumIdleSize（发布和订阅连接的最小空闲连接数）

- 默认值：`1`，用于发布和订阅连接的最小保持连接数（长连接）。Redisson内部经常通过发布和订阅来实现许多功能。长期保持一定数量的发布订阅连接是必须的。

###### subscriptionConnectionPoolSize（发布和订阅连接池大小）

- 默认值：`50`，用于发布和订阅连接的连接池最大容量。连接池的连接数量自动弹性伸缩。

###### connectionMinimumIdleSize（最小空闲连接数）

- 默认值：`32`，最小保持连接数（长连接），也是初始化初始连接数。长期保持一定数量的连接有利于提高瞬时写入反应速度。

###### connectionPoolSize（连接池大小）

- 默认值：`64`，redis 连接池最大数量。

###### dnsMonitoringInterval（DNS监测时间间隔，单位：毫秒）

默认值：`5000`，监测DNS的变化情况的时间间隔，设置 -1不启动

###### idleConnectionTimeout（连接空闲超时，单位：毫秒）

- 默认值：`10000`，如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。时间单位是毫秒。

###### connectTimeout（连接超时，单位：毫秒）

- 默认值：`10000`，同节点建立连接时的等待超时。时间单位是毫秒。

###### timeout（命令等待超时，单位：毫秒）

- 默认值：`3000`，等待节点回复命令的时间。该时间从命令发送成功时开始计时。

###### retryAttempts（命令失败重试次数）

- 默认值：`3`，如果尝试达到 **retryAttempts（命令失败重试次数）** 仍然不能将命令发送至某个指定的节点时，将抛出错误。如果尝试在此限制之内发送成功，则开始启用 **timeout（命令等待超时）** 计时。

###### retryInterval（命令重试发送时间间隔，单位：毫秒）

- 默认值：`1500`，在某个节点执行相同或不同命令时，**连续** 失败 **failedAttempts（执行失败最大次数）** 时，该节点将被从可用节点列表里清除，直到 **reconnectionTimeout（重新连接时间间隔）** 超时以后再次尝试。

###### database（数据库编号）

- 默认值：`0`，尝试连接的数据库编号。

###### password（密码）

- 默认值：`null`，用于节点身份验证的密码。

###### subscriptionsPerConnection（单个连接最大订阅数量）

- 默认值：`5`，每个连接的最大订阅数量。

###### clientName（客户端名称）

- 默认值：`null`，在Redis节点里显示的客户端名称。

###### sslEnableEndpointIdentification（启用SSL终端识别）

- 默认值：`true`，开启SSL终端识别能力。
- SSL 协议设置通集群模式；

##### 06：主从模式（MasterSlaveServersConfig）

###### 示例

```java
Config config = new Config();
config.useMasterSlaveServers()
    //可以用"rediss://"来启用SSL连接
    .setMasterAddress("redis://127.0.0.1:6379")
    .addSlaveAddress("redis://127.0.0.1:6389", "redis://127.0.0.1:6332", "redis://127.0.0.1:6419")
    .addSlaveAddress("redis://127.0.0.1:6399");

RedissonClient redisson = Redisson.create(config);
MasterSlaveServersConfig masterSlaveConfig = config.useMasterSlaveServers();
```

###### dnsMonitoringInterval（DNS监控间隔，单位：毫秒）

- 默认值：`5000`，用来指定检查节点DNS变化的时间间隔。使用的时候应该确保JVM里的DNS数据的缓存时间保持在足够低的范围才有意义。用`-1`来禁用该功能。

###### masterAddress（主节点地址）

- 可以通过`host:port`的格式来指定主节点地址。

###### addSlaveAddress（添加从主节点地址）

- 可以通过`host:port`的格式来指定从节点的地址。多个节点可以一次性批量添加。

###### readMode（读取操作的负载均衡模式）

- 默认值： `SLAVE`（只在从服务节点里读取）
- 设置读取操作选择节点的模式。 
  -  `SLAVE`：只在从服务节点里读取。
  -  `MASTER：`只在主服务节点里读取。
  -  `MASTER_SLAVE：`在主从服务节点里都可以读取。

