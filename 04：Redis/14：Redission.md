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

- 