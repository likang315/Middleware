### Redis 应用

------

##### 1：缓存

相比基于内存的 Redis，基于磁盘存储的关系型数据库读取⽤时会⾮常⻓, 主要有⼀下⼏个原因:

- Redis 的数据存储在内存中，RDS 的数据主要存储于磁盘
- Redis 的查询⽅式是 Hash, 效率接近于 O(1), ⽽ RDS 的查询⽅式是 B+Tree
- Redis 缓存空间大，不像InnoDB只有 256KB 缓存空间
- Redis 只缓存热点数据，并且存在过期时间和内存淘汰，注定数据量⼩, ⽽ RDS 需要存储全量数据

##### 2：缓存穿透、缓存雪崩

- 缓存穿透：多次请求缓存中不存在的数据
- 缓存雪崩：redis 缓存大量失效

###### 解决穿透：

1. 布隆过滤或压缩filter提前拦截
2. 数据库找不到也将空对象进行缓存

###### 解决雪崩：

- 惰性删除【贪心策略】、定期删除

##### 3：缓存不一致

![](https://github.com/likang315/Java-and-Middleware/blob/master/Redis/Redis/%E7%BC%93%E5%AD%98%E4%B8%8D%E4%B8%80%E8%87%B4.png?raw=true)

### 2：分布式锁

分布式锁的本质是在 Redis 中**设置⼀个标识, 当其他进程试图来访问的时候, 发现已经有标识, 代表该资源已被锁住**, 就需要放弃或者稍后重试.
⼀般使⽤ **setnx(set if not exists) 指令,** 只允许被⼀个客户端占领, 当使⽤完毕后, 再调⽤ del 指令释放.但以上的⽅式存在⼀个问题, 如果逻辑执⾏中出现了异常, 可能会导致 del 指令没有调⽤, 锁永远就没有机会得到释放.因此在拿到锁之后, 再给锁加上⼀个过期时间, 即使出现异常也可以保证 5 秒之后锁⾃动释放

但是如果在加锁和释放锁之间的逻辑执⾏太⻓, 以⾄于超出了锁的超时限制, 就会导致多个线程提前重新只有这把锁, 导致临界区代码不能得到严格的串⾏执⾏.为了避免这个问题, Redis 分布式锁不要⽤于较⻓时间的任务



## Java使用Redis

1:下载驱动包 **下载 jedis.jar**,确保下载最新驱动包

2：在你的 classpath 中包含该驱动

```java
import redis.clients.jedis.Jedis;
public class RedisStringJava {
    public static void main(String[] args) {
        //连接本地的 Redis 服务
        Jedis jedis = new Jedis("localhost");
        System.out.println("连接成功");
        //设置 redis 字符串数据
        jedis.set("runoobkey", "www.runoob.com");
        // 获取存储的数据并输出
        System.out.println("redis 存储的字符串为: "+ jedis.get("runoobkey"));
    }
}
 // 获取数据并输出
 Set<String> keys = jedis.keys("*"); 
 //列表设置key-value 
 jedis.lpush("site-list", "Runoob");
```









