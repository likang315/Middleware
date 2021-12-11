### Redis 运维

------

[TOC]

##### 01：安装 Reids

1. 下载：https://github.com/MSOpenTech/redis/releases，下载Redis压缩包

2. 用 cd 命令切换目录到 C:\redis  运行，启动服务端： 
  
   - redis-server  ：执行此命令,运行服务，加载默认的配置文件，redis.conf 是一个默认的配置文件
   - redis-server  [配置文件路径]  ：启动指定配置文件的redis服务；
   - redis 服务默认的端口是6379 ，可以通过检测端口是否是redis进程占用
   
3. **检测redis服务是否启动**；
- redis-cli ping ：检测redis服务是否启动，若返回PONG，则运行中；
4.  **连接redis数据库**；
- 本地需要另启一个 cmd 窗口，原来的不要关闭，不然就无法访问服务端了，切换到 redis 目录下运行: 
  
- redis-cli -h 127.0.0.1 -p 6379   【本地】
   - **redis-cli -h {ip} -p {port}**
5. 设置键值对：set myKey abc 

6. 取出键值对：get myKey

##### 02：Redis 配置信息

Redis 的配置文件CINFIU位于 Redis 安装目录下，**文件名为 redis.conf**

- 通过 CONFIG 命令查看或设置配置项
  - CONFIG GET dir：获取 redis 安装目录可以使用 
  - CONFIG  GET  CONFIG_SETTING_NAME  ：获取指定 CONFIG_SETTING_NAME的值，配置信息
    - config get port ：得到redis服务端口号；
  - CONFIG  GET  *     ：* 号获取所有配置项
  - CONFIG  SET   CONFIG_SETTING_NAME    NEW_CONFIG_VALUE  ：使用 CONFIG set 修改配置信息

##### 03：连接Redis服务

- redis-cli：**命令行客户端程序**，可以将命令直接发送到Redis，并直接从终端读取服务器返回的应答；
- ping：用于检测Redis服务是否启动
- AUTH password：验证密码是否正确
- QUIT：关闭当前连接
- SELECT index：切换到指定的数据库
- **redis-cli -h host -p port -a password ：远程连接Redis 服务**

##### 04：Redis 键：用于管理 redis 的键

- **DEL key**
  - key 存在时**删除  key**;
  - 如果键被删除成功，命令执行后输出 (integer) 1，否则将输出 (integer) 0
- **FLUSHALL**：
  - 删除所有的的所有key；
- DUMP key：
  - 用于序列化给定 key ，并返回被序列化的值，如果 key 不存在，那么返回 nil 
  - 便于传输；
- **EXISTS key**
  - 用于检查给定 key 是否存在，若 key 存在返回 1 ，否则返回 `0`;
- **Expire key second**
  - 命令用于设置 key 的过期时间，key 过期后将不再可用，单位以秒计;
- Expire key timestamp ：命令用于以 UNIX 时间戳(unix timestamp)格式设置 key 的过期时间
- **KEYS pattern**
  - 查找所有符合给定模式 pattern 的 key，正则；
- MOVE key db
  - 命令用于将当前数据库的 key 移动到给定的数据库 db 当中， redis默认使用数据库 0
- PERSIST key
  - 用于移除给定 key 的过期时间，使得 key 永不过期
- **TTL key**
  - 以**秒为单位**返回 key 的剩余过期时间；
- PTTL key
  - 以毫秒为单位返回 key 的剩余过期时间；
- **RANDOMKEY**
  - 从当前数据库中随机返回一个 key ，用于测试数据库为不为空，为空时，返回 nil；
- **RENAME key newkey**
  - 修改key的值
- **Type key**
  - 返回存储值的类型；
  - none：不存在；

##### 05：Redis 服务器

- **INFO ：获取 Redis 服务器的各种信息和统计数值**
  - redis_version : Redis 服务器版本；
  - os : Redis 服务器的宿主操作系统；
  - **clients : 已连接客户端信息**，包含以下域：
    - **connected_clients : 已连接客户端的数量**（不包括通过从属服务器连接的客户端）
    - client_longest_output_list : 当前连接的客户端当中，最长的输出列表
    - client_longest_input_buf : 当前连接的客户端当中，最大输入缓存
    - blocked_clients : 正在等待阻塞命令（BLPOP、BRPOP、BRPOPLPUSH）的客户端的数量
  - **memory : 内存信息**
    - used_memory_human : 以人类可读的格式返回 Redis 分配的内存总量；
    - used_memory_rss：向操作系统申请的内存大小；
    - used_memory_peak：redis的内存消耗峰值(以字节为单位)；
  - Keyspace：数据库相关的统计信息
- CLIENT LIST：返回所有连接到服务器的客户端信息和统计数据
  - addr ： 客户端的地址和端口
  - fd ： 套接字所使用的文件描述符
  - age ： 以秒计算的已连接时长
  - idle ： 以秒计算的空闲时长
  - flags ： 客户端 flag
  - db ： 该客户端正在使用的数据库 ID
  - sub ： 已订阅频道的数量
  - psub ： 已订阅模式的数量
  - multi ： 在事务中被执行的命令数量
  - qbuf ： 查询缓冲区的长度（字节为单位， 0 表示没有分配查询缓冲区）
  - qbuf-free ： 查询缓冲区剩余空间的长度（字节为单位， 0 表示没有剩余空间）
  - obl ： 输出缓冲区的长度（字节为单位， 0 表示没有分配输出缓冲区）
  - oll ： 输出列表包含的对象数量（当输出缓冲区没有剩余空间时，命令回复会以字符串对象的形式被入队到这个队列里）
  - omem ： 输出缓冲区和输出列表占用的内存总量
  - events ： 文件描述符事件
  - cmd ： 最近一次执行的命令
- **DBSIZE：返回当前数据库的 key 的数量**
- **FLUSHDB：删除当前数据库的所有key**
- FLUSHALL ：删除整个redis中(所有数据库)所有的key

##### 06：Redis 数据备份于恢复

- SAVE ：执行一个**同步保存**操作，将当前 Redis 实例的所有数据**快照(snapshot)以 RDB 文件的形式保存到硬盘**；
- BGSAVE：用于在后台**异步保存**当前数据库的数据到磁盘；
  - BGSAVE 命令执行之后立即返回 OK ，然后 Redis **fork 出一个新子进程**，原来的 Redis 进程(父进程)继续处理客户端请求，而**子进程则负责将数据保存到磁盘**，然后退出；
- 数据恢复只需将备份文件 (dump.rdb) 移动到 redis 安装目录并启动服务即可；

##### 07：字符串键

| 命令        | `int` 编码的实现方法                                         | `embstr` 编码的实现方法                                      | `raw` 编码的实现方法                                         |
| :---------- | :----------------------------------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| SET         | 使用 `int` 编码保存值。                                      | 使用 `embstr` 编码保存值。                                   | 使用 `raw` 编码保存值。                                      |
| GET         | 拷贝对象所保存的整数值， 将这个拷贝转换成字符串值， 然后向客户端返回这个字符串值。 | 直接向客户端返回字符串值。                                   | 直接向客户端返回字符串值。                                   |
| APPEND      | 将对象转换成 `raw` 编码， 然后按 `raw` 编码的方式执行此操作。 | 将对象转换成 `raw` 编码， 然后按 `raw` 编码的方式执行此操作。 | 调用 `sdscatlen` 函数， 将给定字符串追加到现有字符串的末尾。 |
| INCRBYFLOAT | 取出整数值并将其转换成 `long double` 类型的浮点数， 对这个浮点数进行加法计算， 然后将得出的浮点数结果保存起来。 | 取出字符串值并尝试将其转换成 `long double` 类型的浮点数， 对这个浮点数进行加法计算， 然后将得出的浮点数结果保存起来。 如果字符串值不能被转换成浮点数， 那么向客户端返回一个错误。 | 取出字符串值并尝试将其转换成 `long double` 类型的浮点数， 对这个浮点数进行加法计算， 然后将得出的浮点数结果保存起来。 如果字符串值不能被转换成浮点数， 那么向客户端返回一个错误。 |
| INCRBY      | 对整数值进行加法计算， 得出的计算结果会作为整数被保存起来。  | `embstr` 编码不能执行此命令， 向客户端返回一个错误。         | `raw` 编码不能执行此命令， 向客户端返回一个错误。            |
| DECRBY      | 对整数值进行减法计算， 得出的计算结果会作为整数被保存起来。  | `embstr` 编码不能执行此命令， 向客户端返回一个错误。         | `raw` 编码不能执行此命令， 向客户端返回一个错误。            |
| STRLEN      | 拷贝对象所保存的整数值， 将这个拷贝转换成字符串值， 计算并返回这个字符串值的长度。 | 调用 `sdslen` 函数， 返回字符串的长度。                      | 调用 `sdslen` 函数， 返回字符串的长度。                      |
| SETRANGE    | 将对象转换成 `raw` 编码， 然后按 `raw` 编码的方式执行此命令。 | 将对象转换成 `raw` 编码， 然后按 `raw` 编码的方式执行此命令。 | 将字符串特定索引上的值设置为给定的字符。                     |
| GETRANGE    | 拷贝对象所保存的整数值， 将这个拷贝转换成字符串值， 然后取出并返回字符串指定索引上的字符。 | 直接取出并返回字符串指定索引上的字符。                       | 直接取出并返回字符串指定索引上的字符。                       |

##### 08：Redis 哈希(Hash)

- Hmset key filed value  filed1 value1
  - 将哈希表 key 中的字段 field 的值设为 value，可以存储多个；
- Hmget key field1 [field2] 
  - 获取哈希表中所有给定字段的值；
- Hset key filed value
  - 用于为哈希表中的字段赋值，如果哈希表不存在，一个新的哈希表被创建并进行 HSET 操作；
- Hget key filed ：获取存储hash表中指定的字段值
- Hkeys key ：获取所有哈希表中的字段名
- Hlen key ：获取哈希表中字段的数量

```shell
redis> HMSET myhash field1 "Hello" field2 "World"
"OK"
redis> HGET myhash field1
"Hello"
redis> HGET myhash field2
"World"
```

##### 09：列表键

| 命令    | `ziplist` 编码的实现方法                                     | `linkedlist` 编码的实现方法                                  |
| :------ | :----------------------------------------------------------- | :----------------------------------------------------------- |
| LPUSH   | 调用 `ziplistPush` 函数， 将新元素推入到压缩列表的表头。     | 调用 `listAddNodeHead` 函数， 将新元素推入到双端链表的表头。 |
| RPUSH   | 调用 `ziplistPush` 函数， 将新元素推入到压缩列表的表尾。     | 调用 `listAddNodeTail` 函数， 将新元素推入到双端链表的表尾。 |
| LPOP    | 调用 `ziplistIndex` 函数定位压缩列表的表头节点， 在向用户返回节点所保存的元素之后， 调用 `ziplistDelete` 函数删除表头节点。 | 调用 `listFirst` 函数定位双端链表的表头节点， 在向用户返回节点所保存的元素之后， 调用 `listDelNode` 函数删除表头节点。 |
| RPOP    | 调用 `ziplistIndex` 函数定位压缩列表的表尾节点， 在向用户返回节点所保存的元素之后， 调用 `ziplistDelete` 函数删除表尾节点。 | 调用 `listLast` 函数定位双端链表的表尾节点， 在向用户返回节点所保存的元素之后， 调用 `listDelNode` 函数删除表尾节点。 |
| LINDEX  | 调用 `ziplistIndex` 函数定位压缩列表中的指定节点， 然后返回节点所保存的元素。 | 调用 `listIndex` 函数定位双端链表中的指定节点， 然后返回节点所保存的元素。 |
| LLEN    | 调用 `ziplistLen` 函数返回压缩列表的长度。                   | 调用 `listLength` 函数返回双端链表的长度。                   |
| LINSERT | 插入新节点到压缩列表的表头或者表尾时， 使用 `ziplistPush` 函数； 插入新节点到压缩列表的其他位置时， 使用 `ziplistInsert` 函数。 | 调用 `listInsertNode` 函数， 将新节点插入到双端链表的指定位置。 |
| LREM    | 遍历压缩列表节点， 并调用 `ziplistDelete` 函数删除包含了给定元素的节点。 | 遍历双端链表节点， 并调用 `listDelNode` 函数删除包含了给定元素的节点。 |
| LTRIM   | 调用 `ziplistDeleteRange` 函数， 删除压缩列表中所有不在指定索引范围内的节点。 | 遍历双端链表节点， 并调用 `listDelNode` 函数删除链表中所有不在指定索引范围内的节点。 |
| LSET    | 调用 `ziplistDelete` 函数， 先删除压缩列表指定索引上的现有节点， 然后调用 `ziplistInsert` 函数， 将一个包含给定元素的新节点插入到相同索引上面。 | 调用 `listIndex` 函数， 定位到双端链表指定索引上的节点， 然后通过赋值操作更新节点的值。 |

##### 10：Redis 集合(Set)

- Sadd key member1 member2
  - 向集合添加一个或多个成员，若集合 key 不存在，则创建一个只包含添加的元素作成员的集合；
- Smembers key
  -  返回集合中的所有的成员，不存在的集合 key 被视为空集合；
- Srem key member1 member2
  - 移除集合中一个或多个成员
- Scard key
  - 返回集合中元素的数量

```shell
redis 127.0.0.1:6379> sadd key redis
(integer) 1
redis 127.0.0.1:6379> sadd key mongodb
(integer) 1
redis 127.0.0.1:6379> sadd key rabitmq
(integer) 1
redis 127.0.0.1:6379> sadd key rabitmq
(integer) 0
redis 127.0.0.1:6379> smembers key
1) "redis"
2) "rabitmq"
3) "mongodb"
# 以上实例中 rabitmq 添加了两次，但根据集合内元素的唯一性，第二次插入的元素将被忽略
```

##### 11：Redis 有序集合 (zset)

- Zadd key score1 member1 [score2 member2]
  - 用于**将一个或多个成员元素及其分数值加入到有序集当中**，如果某个成员已经是有序集的成员，那么更新这个成员的分数值，并通过重新插入这个成员元素，来保证该成员在正确的位置上；
  - 分数值可以是整数值或双精度浮点数；
-  Zrange key start stop
  - 返回有序集中，指定区间内的成员，成员的位置是**按分数值递增排序**，具有相同分数值的成员按字典序来排列；
- Zcard key
  - 获取有序集合的成员数
- Zrem key member
  - 用于移除有序集中的一个或多个成员，不存在的成员将被忽略;
- Zrank key member
  - 返回有序集中指定成员的排名。其中有序集成员按分数值递增(从小到大)顺序排列;

```shell
redis 127.0.0.1:6379> ZADD myset 1 "hello"
(integer) 1
redis 127.0.0.1:6379> ZADD myset 1 "foo"
(integer) 1
redis 127.0.0.1:6379> ZADD myset 2 "world" 3 "bar"
(integer) 2
redis 127.0.0.1:6379> ZRANGE myzset 0 -1 WITHSCORES
1) "hello"
2) "1"
3) "foo"
4) "1"
5) "world"
6) "2"
7) "bar"
8) "3"
```