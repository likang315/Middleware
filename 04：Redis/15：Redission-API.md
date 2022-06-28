### Redission API

------

[TOC]

##### 01：RKeys 接口

- 每个Redisson对象实例都会有一个与之对应的Redis数据实例，可以通过调用`getName`方法来取得Redis数据实例的名称（key）。

- 所有与Redis key相关的操作都归纳在[`RKeys`](http://static.javadoc.io/org.redisson/redisson/3.10.0/org/redisson/api/RKeys.html)这个接口里：

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

##### 0：布隆过滤器

- BitSet 的扩展，使用了 m 个哈希函数，每个字符串跟 m 个bit对应，从而降低了冲突的概率。
- 一般拥有判断某个数据，是否在大量数据集合里，容许一定的错误率；
  - 时间复杂度：O(m)，速度快；

```java
RClusteredBloomFilter<SomeObject> bloomFilter = redisson.getClusteredBloomFilter("sample");
// 初始化布隆过滤器，预计统计元素数量为 255000000，期望误差率为0.03
bloomFilter.tryInit(255000000L, 0.03);
bloomFilter.add(new SomeObject("field1Value", "field2Value"));
bloomFilter.add(new SomeObject("field5Value", "field8Value"));
bloomFilter.contains(new SomeObject("field1Value", "field8Value"));
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

#### 