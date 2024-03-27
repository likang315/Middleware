### Redission 高级特性

------

[TOC]

##### 01：接口调用方式

- `RedissonClient`、`RedissonReactiveClient`和`RedissonRxClient`实例本身和Redisson提供的所有分布式对象都是线程安全的。
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

##### 03：分布式锁

- Rlock 分布式锁接口；

###### 示例

- ```java
  RLock lock = redisson.getLock("anyLock");
  // 尝试加锁，最多等待10秒，上锁以后10秒自动解锁
  boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
  if (res) {
     try {
       ...
     } finally {
         lock.unlock();
     }
  }
  ```

##### 04：信号量

- RSemaphore：分布式信号量，全局的；
- 用于多个机器执行任务；

###### 示例

- ```java
  RSemaphore semaphore = redisson.getSemaphore("semaphore");
  
  // 阻塞式的
  semaphore.acquire();
  // 非阻塞式的，可以设置超时时间
  semaphore.tryAcquire();
  
  // 释放许可证
  semaphore.release();
  ```

##### 05：对Redis 结点的操作

- RedisNodes：提供了对Redis节点的操作；

  - 获取什么类型的redis结点，单点、主从、集群、哨兵；
  - `ping`：结点是否连接；

  ```java
  <T extends BaseRedisNodes> T getRedisNodes(RedisNodes<T> nodes);
  ```

##### 06：管道

- 多个连续命令可以通过`RBatch`对象**在一次网络会话请求里合并发送**，这样省去了产生多个请求消耗的时间和资源。这在Redis中叫做管道；

- ```java
  BatchOptions options = BatchOptions.defaults()
  // 指定执行模式
  // ExecutionMode.REDIS_READ_ATOMIC - 所有命令缓存在Redis节点中，以原子性事务的方式执行。
  // ExecutionMode.REDIS_WRITE_ATOMIC - 所有命令缓存在Redis节点中，以原子性事务的方式执行。
  // ExecutionMode.IN_MEMORY - 所有命令缓存在Redisson本机内存中统一发送，但逐一执行（非事务）。默认模式。
  // ExecutionMode.IN_MEMORY_ATOMIC - 所有命令缓存在Redisson本机内存中统一发送，并以原子性事务的方式执行。
  .executionMode(ExecutionMode.IN_MEMORY)
  
  // 告知Redis不用返回结果（可以减少网络用量）
  .skipResult()
  // 将写入操作同步到从节点，同步到2个从节点，等待时间为1秒钟
  .syncSlaves(2, 1, TimeUnit.SECONDS)
  // 处理结果超时为2秒钟
  .responseTimeout(2, TimeUnit.SECONDS)
  // 命令重试等待间隔时间为2秒钟
  .retryInterval(2, TimeUnit.SECONDS);
  // 命令重试次数。仅适用于未发送成功的命令
  .retryAttempts(4);
  ```

###### 示例

```java
RBatch batch = redisson.createBatch();
batch.getMap("test").fastPutAsync("1", "2");
batch.getMap("test").fastPutAsync("2", "3");
batch.getMap("test").putAsync("2", "5");
batch.getAtomicLongAsync("counter").incrementAndGetAsync();
batch.getAtomicLongAsync("counter").incrementAndGetAsync();

BatchResult res = batch.execute();
```

