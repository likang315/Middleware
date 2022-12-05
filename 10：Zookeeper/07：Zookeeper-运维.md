### Zookeeper 运维

------

[TOC]

##### 01：基本配置参数

| 参数名                        | 说明                                                         |
| ----------------------------- | ------------------------------------------------------------ |
| clientPort                    | 用于配置当前服务器对外的服务端口，客户端通过该端口与Zk服务器创建连接，一般设置为：2181，无默认值，必须配置，不支持系统属性方式配置。 |
| dataDir                       | 用于配置ZK服务器存储快照文件的目录。默认情况下，如果没有配置dataLogDir 参数，那么事务日志也会存储在该目录下。由于事务日志量大，导致ZK 整体服务能力降低，因此建议单独配置。 |
| tickTime                      | 用于配置ZK 中最小时间单元的长度。默认值：3000，单位：毫秒（ms） |
| **高级配置**                  |                                                              |
| dataLogDir                    | 用于配置 ZK 服务器存储事务日志文件的目录，建议单独配置一个磁盘。 |
| initLimit                     | 用于配置Leader服务器等待Follower启动，并完成数据同步的时间。默认值：10，表示：tickTime 的10倍。 |
| syncLimit                     | 用于配置Leader服务器和Follower之间进行心跳检测的最大延时时间。默认值：5，表示：tickTime 的 10 倍。 |
| snapCount                     | 用于配置相邻两次快照之间的事务操作次数，默认值：100000。     |
| minSessionTimeout             | 用于服务对端客户端会话的超时时间进行限制。默认是tickTIme 的2倍。 |
| maxSessionTimeout             | 默认是tickTime 的20倍。                                      |
| maxClientCnxns                | 用户限制单台客户端与单台服务器之间的并发连接数。默认值：60。too many connections |
| **server.id=host:port:port**  | 该参数用于配置组成ZooKeeper集群的机器列表，其中id即为ServerID，与每台服务器myid文件中的数字相对应。<br/>第一个端口：用于指定Follower服务器与Leader进行 运行时通信和数据同步时所使用的端口<br />第二个端口：用于进行Leader选举过程中的投票通信。 |
| **autopurge.snapRetainCount** | 配置在自动清理文件时，需要**保留的快照数据文件数量和对应的事务日志文件**。默认值：3 |
| **autopurge.purgeInterval**   | 用于配置ZK对历史文件自动清理的频率。默认是：0（表示不开启），单位是小时。 |

##### 02：常用命令

- conf
  - 用于输出 ZooKeeper 服务器运行时使用的基本配置信息。
- cons
  - 用于输出与当前这台服务器上所有连接客户端的详细信息。
- dump
  - 用于输出当前集群的所有会话信息，包括这些会话的会话ID，以及每个会话创建的临时节点信息。
- envi
  - 用于输出 ZooKeeper 所在服务器运行时的环境信息，包括os.version、java.version和user.home等。
- ruok
  - 用于输出当前ZooKeeper服务是否正在运行。
- stat
  - 用于获取ZooKeeper服务器的运行时状态信息，包括基本的ZooKeeper版本、打包信息、运行时角色、集群数据节点个数等信息，另外还会将当前服务器的客户端连接信息打印出来。
- wchs
  - 用于输出当前服务器上管理的Watcher的概要信息。
- wchc
  - 用于输出当前服务器上管理的 Watcher 的详细信息，以会话为单位进行归组，同时列出被该会话注册了Watcher的节点路。
- wchp
  - 和wchc命令非常类似，也是用于输出当前服务器上管理的Watcher的详细信息，不同点在于该命令输出的信息以节点路径为单位进行归组。
- mntr
  - 用于输出比stat命令更为详尽的服务器统计信息，包括请求处理的延迟情况、服务器内存数据库大小和集群数据的同步情况。

##### 03：JMX（Java Management Extensions）

- 对运行时 Java 系统的管控。
- JConsole：(Java监视和管理控制台) 是一个Java内置的基于JMX的图形管理化工具。是最常用的JMX连接器。
- 可以使用 JConsole 来进行ZooKeeper的 JMX 管理。

##### 04：如何构键一个高可用 ZK 集群

- ZooKeeper 集群通常设计部署成奇数台服务器。
  - **过半存活即可用**：一个 ZooKeeper 集群如果要对外提供可用的服务，那么集群中必须要有过半的机器正常工作并且彼此之间能够正常通信。
- 容灾
  - 三机房部署。
- 扩容和缩容
  - ZooKeeper 在水平扩容方面做得并不十分完美，**需要进行整个集群的重启**。
  - 通常有两种重启方式：一种是集群整体重启，另一种是**逐台进行服务器的重启**。
    - 逐台进行重启，时刻保证服务的可用性。

##### 05：日常运维

###### 纯Shell 脚本进行清理

- 将清除脚本配置到 crontab 中，设置执行时间；

###### 使用清理脚本：zkCleanup.sh

###### 自动清理机制

- 通过配置：autopurge.snapRetainCount和autopurge.purgeInterval这两个参数来实现定时清理。



