### Redission API

------

[TOC]

##### 01：RKeys 接口

- 每个Redisson对象实例都会有一个与之对应的Redis数据实例，可以通过调用`getName`方法来取得Redis数据实例的名称（key）。

- 所有与Redis key相关的操作都归纳在 `RKeys` 这个接口里：

  - ```java
    RMap map = redisson.getMap("mymap");
    map.getName(); // = mymap
    RKeys keys = redisson.getKeys();
    
    Iterable<String> allKeys = keys.getKeys();
    Iterable<String> foundedKeys = keys.getKeysByPattern('key*');
    long numOfDeletedKeys = keys.delete("obj1", "obj2", "obj3");
    long deletedKeysAmount = keys.deleteByPattern("test?");
    String randomKey = keys.randomKey();
    long keysAmount = keys.count();
    ```

##### 02：通用对象桶（Object Bucket）

- Redisson的分布式[`RBucket`](http://static.javadoc.io/org.redisson/redisson/3.10.0/org/redisson/api/RBucket.html)Java对象是一种通用对象桶可以用来存放任类型的对象。

  - 存储对象时，需要序列化其接口；

- ```java
  RBucket<AnyObject> bucket = redisson.getBucket("anyObject");
  bucket.set(new AnyObject(1));
  AnyObject obj = bucket.get();
  
  bucket.trySet(new AnyObject(3));
  bucket.compareAndSet(new AnyObject(4), new AnyObject(5));
  bucket.getAndSet(new AnyObject(6));
  ```

##### 03： BitSet

- Redisson的分布式[`RBitSet`](http://static.javadoc.io/org.redisson/redisson/3.10.0/org/redisson/api/RBitSet.html)Java对象采用了与`java.util.BiteSet`类似结构的设计风格。可以理解为它是一个分布式的可伸缩式位向量。需要注意的是`RBitSet`的大小受Redis限制，最大长度为`4 294 967 295`。

  ```java
  RBitSet set = redisson.getBitSet("simpleBitset");
  set.set(0, true);
  set.set(1812, false);
  set.clear(0);
  set.addAsync("e");
  set.xor("anotherBitset");
  ```

##### 04：原子整长形（AtomicLong）

- Redisson的分布式整长形[`RAtomicLong`](http://static.javadoc.io/org.redisson/redisson/3.10.0/org/redisson/api/RAtomicLong.html)对象和Java中的`java.util.concurrent.atomic.AtomicLong`对象类似。

- ```java
  RAtomicLong atomicLong = redisson.getAtomicLong("myAtomicLong");
  atomicLong.set(3);
  atomicLong.incrementAndGet();
  atomicLong.get();
  ```

##### 05：原子双精度浮点（AtomicDouble）

- Redisson还提供了分布式原子双精度浮点RAtomicDouble，弥补了Java自身的不足。

- ```java
  RAtomicDouble atomicDouble = redisson.getAtomicDouble("myAtomicDouble");
  atomicDouble.set(2.81);
  atomicDouble.addAndGet(4.11);
  atomicDouble.get();
  ```

##### 06：整长型累加器（LongAdder）

- 用客户端内置的LongAdder对象，为分布式环境下递增和递减操作提供了很高得性能，适用于分布式统计场景；

- 当不再使用整长型累加器对象的时候应该自行手动销毁，如果Redisson对象被关闭（shutdown）了，则不用手动销毁。

- ```java
  RLongAdder atomicLong = redisson.getLongAdder("myLongAdder");
  atomicLong.add(12);
  atomicLong.increment();
  atomicLong.decrement();
  atomicLong.sum();
  
  RLongAdder atomicLong = ...
  atomicLong.destroy();
  ```

##### 07：双精度浮点累加器（DoubleAdder）

- ```java
  RLongDouble atomicDouble = redisson.getLongDouble("myLongDouble");
  atomicDouble.add(12);
  atomicDouble.increment();
  atomicDouble.decrement();
  atomicDouble.sum();
  
  RLongDouble atomicDouble = ...
  atomicDouble.destroy();
  ```

##### 08：Redission 实现MQ

- ```java
  RTopic topic = redisson.getTopic("anyTopic");
  topic.addListener(SomeObject.class, new MessageListener<SomeObject>() {
      @Override
      public void onMessage(String channel, SomeObject message) {
          //...
      }
  });
  
  // 在其他线程或JVM节点
  RTopic topic = redisson.getTopic("anyTopic");
  long clientsReceivedMessage = topic.publish(new SomeObject());
  ```

##### 09：布隆过滤器

- BitSet 的扩展，使用了 m 个哈希函数，每个字符串跟 m 个bit对应，从而降低了冲突的概率。
- 一般拥有判断某个数据，是否在大量数据集合里，容许一定的错误率；
  - 时间复杂度：O(m)，速度快；
- 底层实际上是两个key，一个存放0,1值，一个是布隆配置；占用内存大小在初始化时就指定，不会更改；
  - `size`: 布隆过滤器的大小，即使用多少个 bit 来存储数据。这个参数越大，则布隆过滤器的容量越大，但是也会占用更多的内存。
  - `hashIterations`: 哈希函数的迭代次数。每一次迭代都会产生一个不同的哈希值，用于将元素映射到不同的 bit 上。迭代次数越多，则布隆过滤器的准确率越高，但是也会增加计算量。
  - `expectedInsertions`: 预计要插入的元素数量。用来估算布隆过滤器的容量，以便更好地选择 `size` 参数的值。
  - `falseProbability`: 期望的误判率，即布隆过滤器判断某个元素是否存在时，允许的最大误判率。


```java
RClusteredBloomFilter<String> bloomFilter = redisson.getClusteredBloomFilter("sample");
// 初始化布隆过滤器，大小固定，一旦创建就不能更改，预计统计元素数量为 2000000，期望误差率为0.01
bloomFilter.tryInit(2_000_000L, 0.01);
bloomFilter.add("12312312");
bloomFilter.contains("1");
```

##### 10：限流器（RateLimiter）

- 用于在分布式环境下现在请求方的调用频率。既适用于不同Redisson实例下的多线程限流，也适用于相同Redisson实例下的多线程限流。该算法不保证公平性。

- ```java
  // 限流器名称
  RRateLimiter rateLimiter = redisson.getRateLimiter("global_reteLimiter_name");
  // 最大流速 = 每1秒钟产生10个令牌， RateType 用于控制是否不同的Redission实例是否限流
  rateLimiter.trySetRate(RateType.OVERALL, 10, 1, RateIntervalUnit.SECONDS);
  
  // 获取令牌成功则执行
  limiter.tryAcquire(3, TimeUnit.SECONDS);
  ```

##### 11：Map

- 分布式映射结构的 RMap Java对象实现了`java.util.concurrent.ConcurrentMap`接口和`java.util.Map`接口。与HashMap不同的是，RMap保持了**元素的插入顺序**。该对象的最大容量受Redis限制，最大元素数量是`4 294 967 295`个。

- RMapCache： 带有元素淘汰（Eviction）机制的Map

  - 基于Redis的以**LRU为驱逐策略**的分布式LRU有界映射对象；

  - ```java
    RMap<String, SomeObject> map = redisson.getMap("anyMap");
    SomeObject prevObject = map.put("123", new SomeObject());
    SomeObject currentObject = map.putIfAbsent("323", new SomeObject());
    SomeObject obj = map.remove("123");
    
    map.fastPut("321", new SomeObject());
    map.fastRemove("321");
    
    RMapCache<String, SomeObject> map = redisson.getMapCache("anyMap");
    // 尝试将该映射的最大容量限制设定为10
    map.trySetMaxSize(10);
    
    // 有效时间 ttl = 10分钟
    map.put("key1", new SomeObject(), 10, TimeUnit.MINUTES);
    // 有效时间 ttl = 10分钟, 最长闲置时间 maxIdleTime = 10秒钟
    map.put("key1", new SomeObject(), 10, TimeUnit.MINUTES, 10, TimeUnit.SECONDS);
    ```

- 集合数据自动分片

  - RClusteredMap

##### 12：RList

- 基于Redis的Redisson分布式列表（List）结构的`RList` Java对象在实现了`java.util.List`接口的同时，确保了元素插入时的顺序。该对象的最大容量受Redis限制，最大元素数量是`4 294 967 295`个。

- ```java
  RList<SomeObject> list = redisson.getList("anyList");
  list.add(new SomeObject());
  list.get(0);
  list.remove(new SomeObject());
  ```

##### 13：RBolckingQueue

- 基于Redis的Redisson分布式无界阻塞队列（Blocking Queue）结构的`RBlockingQueue` Java对象实现了`java.util.concurrent.BlockingQueue`接口。尽管`RBlockingQueue`对象无初始大小（边界）限制，但对象的最大容量受Redis限制，最大元素数量是`4 294 967 295`个。

- ```java
  RBlockingQueue<SomeObject> queue = redisson.getBlockingQueue("anyQueue");
  queue.offer(new SomeObject());
  
  // 查看收个元素，但不会移除
  SomeObject obj = queue.peek();
  // 移除且查看收个元素
  SomeObject someObj = queue.poll();
  SomeObject ob = queue.poll(10, TimeUnit.MINUTES);
  ```

##### 14：RDelayedQueue

- 基于Redis的Redisson分布式延迟队列（Delayed Queue）结构的`RDelayedQueue` Java对象在实现了`RQueue`接口的基础上提供了向队列按要求延迟添加项目的功能。该功能可以用来实现消息传送延迟按几何增长或几何衰减的发送策略。

- ```java
  RQueue<String> distinationQueue = redisson.getBlockingFairDeque("myDeque");
  RDelayedQueue<String> delayedQueue = getDelayedQueue(distinationQueue);
  // 10秒钟以后将消息发送到指定队列(distinationQueue)
  delayedQueue.offer("msg1", 10, TimeUnit.SECONDS);
  // 一分钟以后将消息发送到指定队列
  delayedQueue.offer("msg2", 1, TimeUnit.MINUTES);
  
  // 在该对象不再需要的情况下，应该主动销毁
  RDelayedQueue<String> delayedQueue = ...
  delayedQueue.destroy();
  ```

##### 15：RSet

- 基于Redis的Redisson的分布式Set结构的`RSet` Java对象实现了`java.util.Set`接口。通过元素的相互状态比较保证了每个元素的唯一性。该对象的最大容量受Redis限制，最大元素数量是`4 294 967 295`个。

- RSetCache：针对单个元素的淘汰机制；

- ```java
  RSet<SomeObject> set = redisson.getSet("anySet");
  set.add(new SomeObject());
  set.remove(new SomeObject());
  
  RSetCache<SomeObject> set = redisson.getSetCache("anySet");
  // ttl = 10 seconds
  set.add(new SomeObject(), 10, TimeUnit.SECONDS);
  ```

##### 16：SortedSet

- 基于Redis的Redisson的分布式`RSortedSet` Java对象实现了`java.util.SortedSet`接口。在保证元素唯一性的前提下，通过比较器（Comparator）接口实现了对元素的排序。

- ```java
  RSortedSet<Integer> set = redisson.getSortedSet("anySet");
  set.trySetComparator(new MyComparator()); // 配置元素比较器
  set.add(3);
  set.add(1);
  set.add(2);
  
  set.removeAsync(0);
  set.addAsync(5);
  ```

##### 17：ScoredSortedSet【计分排序集 ZSet】

- 基于Redis的Redisson的分布式`RScoredSortedSet` Java对象是一个可以按插入时指定的元素评分排序的集合。它同时还保证了元素的唯一性。

- ```java
  RScoredSortedSet<SomeObject> set = redisson.getScoredSortedSet("simple");
  set.add(0.13, new SomeObject(a, b));
  set.addAsync(0.251, new SomeObject(c, d));
  set.add(0.302, new SomeObject(g, d));
  
  set.pollFirst();
  set.pollLast();
  
  // 获取元素在集合中的位置
  int index = set.rank(new SomeObject(g, d)); 
  // 获取元素的评分
  Double score = set.getScore(new SomeObject(g, d)); 
  ```

 
