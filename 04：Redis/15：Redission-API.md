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

##### 06：





#### 