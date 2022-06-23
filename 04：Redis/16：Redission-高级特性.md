### Redission 高级特性

------

[TOC]

##### 01：接口调用方式

- `**RedissonClient**`、`RedissonReactiveClient`和`RedissonRxClient`实例本身和Redisson提供的所有分布式对象都是线程安全的。
- Redisson框架提供的几乎所有对象都包含了同步和异步相互匹配的方法。这些对象都可以通过`RedissonClient`接口获取。同时还为大部分Redisson对象提供了满足异步流处理标准的程序接口`RedissonReactiveClient`。除此外还提供了`RxJava2`规范的`RedissonRxClient`程序接口。

###### 示例

```java
RedissonClient client = Redisson.create(config);
RAtomicLong longObject = client.getAtomicLong('myLong');
// 同步执行方式
longObject.compareAndSet(3, 401);
// 异步执行方式
RFuture<Boolean> result = longObject.compareAndSetAsync(3, 401);

RedissonReactiveClient client = Redisson.createReactive(config);
RAtomicLongReactive longObject = client.getAtomicLong('myLong');
// 异步流执行方式
Mono<Boolean> result = longObject.compareAndSet(3, 401);

RedissonRxClient client = Redisson.createRx(config);
RAtomicLongRx longObject= client.getAtomicLong("myLong");
// RxJava2方式
Flowable<Boolean result = longObject.compareAndSet(3, 401);
```

##### 02：单个集合的分片

- 在集群模式下，Redisson为**单个Redis集合类型提供了自动分片**的功能。
- Redisson 提供的所有数据结构都支持在集群环境下使用，但每个数据结构只被保存在一个固定的槽内。Redisson 提供的自动分片功能能够将单个数据结构拆分，然后均匀的分布在整个集群里，而不是被挤在单一一个槽里。自动分片功能的优势主要有以下几点：
  - 单个数据结构可以充分利用整个集群内存资源，而不是**被某一个节点的内存限制**。
  - 将单个数据结构分片以后分布在集群中不同的节点里，不仅可以大幅提高读写性能，还能够**保证读写性能随着集群的扩张而自动提升**。















