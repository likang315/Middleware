### 数据库

------

[TOC]

##### 01：服务器中的数据库

- Redis 服务器将所有数据库都保存在服务器状态redis.h/redis Server 结构的db数组中， db 数组的每个项都是一个redis.h/redisDb 结构， 每个redisDb 结构代表一个数据库：

- dbnum 的值由服务器配置的database选项决定，默认：16；

  - ```c
    struct redsiServer {
        // 一个数组，保存着服务器中所有的数据
        redisDb *db;
        // 初始化服务器时，决定应该创建多少个数据库
        int dbnum;
    }
    ```

##### 02：切换数据库

- 每个Redis 客户端都有自己的目标数据库， 每当客户端执行数据库命令的时候， 目标数据库就会成为这些命令的操作对象。
- 默认情况下， Redis 客户端的目标数据库为 0 号数据库， 但客户端可以通过执行 SELECT 命令来切换目标数据库。
- 在服务器内部， 客户端状态 redisClient 结构的 db 属性记录了客户端当前的目标数据库， 这个属性是一个指向redis Db 结构的指针，db 指针指向redis Serve r.db 数组的其中一个元素， 而被指向的元素就是客户端的目标数据库。
- <img src="/Users/likang/Code/Git/Middleware/04：Redis/photos/redis-database.png" style="zoom:40%;" />

##### 03：数据库键空间

- redisDb 结构的 **dict 字典**保存了数据库中的所有键值对，将这个字典称为键空间( key space ) 。

- 

  ```c
  typedef struct redisDb {
  	// 数据库空间，保存着数据库中所有的键值对【键空间】
  	dict *dict;
  	diet *expires;
  } redisDb;
  ```

###### redis-db 空间分布

<img src="/Users/likang/Code/Git/Middleware/04：Redis/photos/redis-db-space.png" style="zoom:30%;" />

- 实际上就是一个字典，对redis 的操作，就是对字典的增删改查；

###### 读取键空间时的维护操作

1. 在读取一个键之后（ 读操作和写操作都要对键进行读取）， 服务器会根据键是否存在来更新服务器的键空间命中( hit) 次数或键空间不命中( miss ) 次数， 这两个值可以在INFO stats 命令的keyspace_hits 和keyspace_misses属性查看。
2. 在读取一个键之后， 服务器会更新键的LRU （ 最后一次使用） 时间， 这个值可以用于计算键的闲置时间， 使用OBJECT idletime <key> 命令可以查看键key 的闲置时间。
3. 如果服务器在读取一个键时发现该键已过期， 那么服务器会先删除这个过期键，然后才执行余下的其他操作。

##### 04：设置键的过期时间

- 通过EXPIRE命令，客户端可以以秒或者亳秒精度为数据库中的某个键设置**生存时间( Time To Live , TTL )** , 在经过指定的秒数或者毫秒数之后， 服务器就会自动删除生存时间为0 的键（永久key：-1）。
- 过期时间是一个UN IX 时间戳，TTL 命令接受一个带有生存时间或者过期时间的键， 返回这个键的剩余生存时间；

###### 设置过期时间

- PEXPIREAT key  timestamp：命令用于将键 key 的过期时间设置为timestap所指定的毫秒数时间戳；
- 不管是哪个设置过期时间命令，最终都转换成该命令执行；
  - 伪代码：判断设置过期时间的key 是否存在键空间中，若存在，则设置，返回设置成功或失败；

###### 保存过期时间

- redisDb 结构的expires 字典保存了数据库中所有键的过期时间， 我们称这个字典为**过期字典**：
- 过期字典的键是一个指针， 这个指针指向键空间中的**某个键对象**（也即是某个数据库键）。
- 过期字典的值是一个long long 类型的整数， 这个整数保存了键所指向的数据库键的过期时间(毫秒精度的UNIX时间戳)。















