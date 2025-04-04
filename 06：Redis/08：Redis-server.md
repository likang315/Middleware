### RedisServer

------

[TOC]

##### 01：Redis 服务器概述

- Redis 服务器负责与多个客户端建立网络连接，处理客户端发送的命令请求，在数据库中保存客户端执行命令所产生的数据，并通过资源管理来维持服务器自身的运转。

##### 02：命令请求的执行过程【重要】

###### 发送命令请求

- 当用户在客户端中键入一个命令请求时，**客户端会将这个命令请求转换成协议格式**，然后通过连接到服务器的套接字，将协议格式的命令请求发送给服务器。

###### 读取命令请求

- 当客户端与服务器之间的连接套接字**因为客户端的写入而变得可读时， 服务器将调用命令请求处理器**来执行以下操作：
  1. 读取套接字中协议格式的命令请求， 并**将其保存到客户端状态的输入缓冲区**里面。
  2. 对输入缓冲区中的命令请求进行分析， 提取出命令请求中包含的命令参数， 以及命令参数的个数， 然后分别将参数和参数个数保存到客户端状态的 `argv` 属性和 `argc` 属性里面。
  3. **调用命令执行器**， 执行客户端指定的命令。

###### 命令执行器

1. **查找命令的实现：**根据客户端状态的 `argv[0]` 参数， 在**命令表（command table）中查找参数所指定的命令**， 并将找到的命令保存到客户端状态的 `cmd` 属性里面。

   - **命令表是一个字典**， 字典的键是一个个命令名字，比如 `"set"` 等等； 而字典的值则是一个个 `redisCommand` 结构， 每个 `redisCommand` 结构记录了一个 Redis 命令的实现信息。
   
2. **执行预备操作：**在真正执行命令之前，程序还要做一些**预备检查操作**，从而确保命令可以正确、顺利地被执行。

   - **检查客户端状态的 `cmd` 指针是否指向 `NULL`**，如果是的话， 那么说明用户输入的命令名字找不到相应的命令实现， 服务器不再执行后续步骤， 并向客户端返回一个错误。
   - 根据客户端 `cmd` 属性指向的 `redisCommand` 结构的 `arity` 属性， **检查命令请求所给定的参数个数是否正确**， 当参数个数不正确时， 不再执行后续步骤， 直接向客户端返回一个错误。
   - **检查客户端是否已经通过了身份验证（首次）**， 未通过身份验证的客户端只能执行 AUTH 命令， 如果未通过身份验证的客户端试图执行除 AUTH 命令之外的其他命令， 那么服务器将向客户端返回一个错误。
   - 如果服务器**打开了 `maxmemory` 功能**， 那么在执行命令之前，**先检查服务器的内存占用情况**，并在有需要时进行内存回收， 从而使得接下来的命令可以顺利执行。 如果内存回收失败， 那么不再执行后续步骤， 向客户端返回一个错误。
   - 如果服务器上一次执行 BGSAVE 命令时出错， 并且服务器打开了 `stop-writes-on-bgsave-error` 功能， 而且服务器即将要执行的命令是一个写命令， 那么服务器将拒绝执行这个命令， 并向客户端返回一个错误。
   - 如果客户端当前正在用 SUBSCRIBE 命令订阅频道， 或者正在用 PSUBSCRIBE 命令订阅模式， 那么服务器只会执行客户端发来的 SUBSCRIBE 、 PSUBSCRIBE 、 UNSUBSCRIBE 、 PUNSUBSCRIBE 四个命令， 其他别的命令都会被服务器拒绝。
   - 如果服务器正在进行数据载入， 那么客户端发送的命令必须带有 `l` 标识（比如 INFO 、 SHUTDOWN 、 PUBLISH ，等等）才会被服务器执行， 其他别的命令都会被服务器拒绝。
   - 如果服务器因为**执行 Lua 脚本而超时并进入阻塞状态**， 那么服务器**只会执行客户端发来的 SHUTDOWN nosave 命令和 SCRIPT KILL 命令**， 其他别的命令都会被服务器拒绝。
   - 如果客户端正在执行事务， 那么服务器只会执行客户端发来的 EXEC 、 DISCARD 、 MULTI 、 WATCH 四个命令， 其他命令都会被放进事务队列中。
   - 如果服务器**打开了监视器功能， 那么服务器会将要执行的命令和参数等信息发送给监视器**。

3. **调用命令的实现函数**

   - 服务器已经将要执行命令的实现保存到了客户端状态的 `cmd` 属性里面， 并将命令的参数和参数个数分别保存到了客户端状态的 `argv` 属性和 `argc` 属性里面， 当服务器决定要执行命令时， 它只要执行以下语句就可以了。

   - ```c
     // client 是指向客户端状态的指针
     
     client->cmd->proc(client);
     ```

   - 被调用的命令实现函数会执行指定的操作， 并产生相应的命令回复， 这些**回复会被保存在客户端状态的输出缓冲区里面**（`buf` 属性和 `reply` 属性）， 之后实现函数还会**为客户端的套接字关联 命令回复处理器， 这个处理器负责将命令回复返回给客户端**。

4. **执行收尾操作：**在执行完实现函数之后， 服务器还需要执行一些收尾工作。

   - 如果服务器开启了**慢查询日志功能**， 那么慢查询日志模块会检查是否需要为刚刚执行完的命令请求添加一条新的慢查询日志。
   - 根据刚刚执行命令所耗费的时长， **更新被执行命令的 `redisCommand` 结构的 `milliseconds` 属性**， 并将命令的 `redisCommand` 结构的 `calls` 计数器的值增一。
   - 如果服务器开启了 AOF 持久化功能， 那么 AOF 持久化模块会**将刚刚执行的命令请求写入到 AOF 缓冲区**里面。
   - 如果**有其他从服务器正在复制当前这个服务器， 那么服务器会将刚刚执行的命令传播给所有从服务器**。

##### 03：将命令回复发送给客户端

- 命令实现函数会将命令回复保存到客户端的输出缓冲区里面， 并为客户端的套接字关联命令回复处理器， **当客户端套接字变为可写状态时**， 服务器就会执行命令回复处理器， **将保存在客户端输出缓冲区中的命令回复发送给客户端**。
- 当命令回复发送完毕之后， 回复处理器会清空客户端状态的输出缓冲区， 为处理下一个命令请求做好准备。

##### 04：客户端接收并打印命令回复

- 当客户端接收到协议格式的命令回复之后， 它会**将这些回复转换成可读的格式**， 并打印给用户观看。

##### 05：serverCron 函数【重要】

- Redis服务器中的 **serverCron 函数默认每隔 100 毫秒执行一次**，这个函数负责管理服务器的资源，并保持服务器的良好运转。
- 主要执行 12 个功能；

###### redisServer 结构

```c
struct redisServer {
    // 保存了秒级精度的系统当前 UNIX 时间戳
	time＿t_unixtime;
    // 保存了毫秒级精度的系统当前 UNIX 时间戳
	long long mstime;
    //每 10 秒更新一次的事件缓存 
    unsigned lruclock:22;
    
    // 上一次进行抽样的时间
    long long ops_sec_last_sarnple_time;
    // 上一次抽样时，服务器已执行命令的数量
    long long ops_sec_last_sample_ops;
    // REDIS_OPS_SEC_SAMPLES 大小(默认值为16) 的环形数组
    // 数组中的每个项纪录了一次抽样结果
	long long ops_sec_sample_es[REDIS_OPS_SEC_SAMPLES];
    // ops_sec_samples 数组的索引值 
    // 每次抽样后将自增为一，在值等于16时，设置为0
    // 让ops_ sec_sampl es 数组构成一个环形数组。
	int ops_sec_idx ;
    
    // 已使用的内存峰值
    size_t stat_peak_memory;
    // 关闭服务器的标识，值为1时，关闭服务器，值为0时，不做动作
	int shutdown_asap;
    // 如果值为1，那么表示有 BGREWRITEAOF 命令被延迟了
	int aof_rewrite_scheduled;
    // 记录执行BGSAVE命令的子进程ID，没有执行该命令则值为-1
	pid_t rdb_child_pid;
	// 记录执行BGREWRITEAOF命令的子进程ID，没有执行该命令则值为-1
	pid_t aof_child_pid;
    // erverCron函数执行计数器，每执行一次就+1
	int cronloops;
}
```

###### 更新服务器时间缓存

- Redis服务器中有不少功能需要获取系统的当前时间，而每次获取系统的当前时间都需要执行一次系统调用，为了减少系统调用的执行次数，服务器状态中的 **unixtime 属性和 mstime 属性被用作当前时间的缓存，但是精确度不高。**
- 服务器只会在打印日志、更新服务器的LRU时、决定是否执行持久化任务、计算服务器上线时 ( uptime)  类对时精确度要求不高的功能上。
- 对于为键设置过期时间、添加慢查查询日志这种需要高精确度时间的功能来说，服务器还是会再次执行系统调用从而获得最准确的系统当前时间。

###### 更新 LRU 时钟

- 服务器状态中的 **lrulock 属性保持了服务器的LRU时钟**，是服务器时间缓存的一种。
- 默认每10秒更新一次事件缓存，用于计算键的空转时间。

###### 更新服务器每秒执行命令次数

- serverCron 函数中的 **trackOperationsPerSecond 函数**会以每100毫秒一次的频率执行，这个函数的功能是以抽样计算的方式，估算并记录**服务器在最近一秒钟处理的命令请求数量**。
- trackOperationsPerSecond 函数和服务器状态中四个 ops_sec_ 开头的属性有关。

###### 更新服务器内存峰值记录

- 服务器状态中的 stat_peak_memory 属性记录了**服务器的内存峰值大小**。

- 每次 serverCron 函数执行时，程序都会**查看服务器当前使用的内存数量，并与stat_peak_memory保存的数值进行大小比较**，进行更新。

- INFO memory：查看当前内存使用情况
  

###### 处理 SIGTERM 信号

- 在启动服务器时，Redis 会为**服务器进程的 SIGTERM 信号关联处理器sigtermHandler函数**，这个信号处理器负责在服务器接到 SIGTERM 信号是，打开服务器状态的 shutdown_asap 标识。
- 每次 serverCron 函数运行时，程序都会**检查服务器的 shutdown_asap 属性**，并根据属性的值决定是否关闭服务器。

###### 管理客户端资源

- serverCron 函数每次执行都调用 **clientCron 函数**，clientCron 函数会对一定数量客户端进行两个检查
  - 检查是否超时（空转），释放客户端。
  - 检查输入缓冲区是否超过指定大小，若超过则释放，重新创建一个默认大小的缓冲区。

###### 管理数据库资源

- serverCron 函数每次执行都会调用 **databasesCron 函数**，这个函数会对服务器中的一部分**数据库进行检查，删除其中的过期键**，并在有需要时，对字典进行收缩操作。

###### 执行被延迟的BGREWRITEAOF

- 在服务器执行`BGSAVE`（RDB异步持久化）命令期间，如果客户端向服务器发送 `BGREWRITEAOF` 命令，那么服务器会将该命令延迟到 `BGSAVE` 命令执行完毕之后。
- 每次`serverCron`函数执行时，**函数都会检查`BGSAVE`命令或者`BGREWRITEAOF`是否正在执行，如果这两个命令都没在执行，并且 aof_rewrite_scheduled 属性值为1，那么执行之前被延迟的`BGREWRITEAOF`命令**。

###### 检查持久化操作的运行状态

- 服务器状态使用 **`rdb_child_pid` 属性和 `aof_child_pid` 属性记录执行 `BGSAVE` 命令和 `BGREWRITEAOF` 命令的子进程ID**，这两个属性也可以用于检查`BGSAVE` 命令和 `BGREWRITEAOF` 命令是否正在执行。
- 每次`serverCron`函数执行时，程序都会检查 rdb_child_pid 和 aof_child_pid 两个属性的值。**若有一个不为 -1，就会调用wait3 函数等待，直到持久化同步完成**。

###### 将AOF缓冲区内容写入AOF文件

- 如果服务器开启了AOF持久化功能，并且AOF缓冲区里还有待写入数据，那么`serverCron`函数会调用相应的程序，将**AOF缓冲区中的内容写入到AOF文件里**。

###### 关闭异步客户端

- 服务器会关闭输出缓冲区大小超出限制的客户端。

###### 增加 cronloops 计数器

- 服务器状态的 `cronloops` 属性**记录了 `serverCron` 函数执行的次数**；
- 在复制模块中使用该属性。

##### 06：初始化服务器（五步）【重要】

###### 初始化服务器状态

- 创建一个struct redisServer类型的实例变量 server 来作为**服务器的状态**，并且为结构中的各个属性去设置默认值。
- 初始化server变量的工作是由**redis.c/initServerConfig函数**去完成的，完成的主要工作如下所示：
  - 设置服务器的运行ID
  - 设置服务器的默认运行频率
  - 设置服务器的默认配置文件路径
  - 设置服务器的运行架构
  - 设置服务器的默认端口号
  - 设置服务器的默认RDB持久化条件和AOF持久化条件
  - 初始化服务器的LRU时钟
  - 创建命令表

- 设置的服务器状态属性基本都是一些整数、浮点数、或字符串属性，除了命令表之外，initServerConfig 函数没有创建服务器状态的其他数据结构，数据库、慢查 日志、Lua 环境、共享对象这些数据结构在之后的步骤才会被创建出来。

###### 载入配置选项

- 开始载入用户给定的**配置参数或者配置文件**，并根据用户设定的配置，对server变量（初始化的服务器状态）的相关属性的值进行更改；
  - 如果用户为这些属性的相应选项**设置了新值**，那么服务器就使用用户指定的值来**更新相应的属性**。

###### 初始化服务器数据结构

- 当初始化到这一步时，就会去执行 **initServer函数**，为下面的**数据结构分配内存**，并且在有需要的时候会**关联初始化值**。
  - **server.clients 链表：**每一个元素都是一个客户端，即 redisClient 结构；
  - **server.db 数组：**存储Redis服务器里面所有数据库的数组；
  - **server.pubsub_channels 字典与 server.pubsub_patterns 链表**，前者用于保存频道订阅信息，后者用于保存模式订阅信息；
  - **server.lua：**用于执行Lua脚本的lua环境；
  - **server.slowlog：**用于保存慢查询日志的属性；
- 为什么要放在这里去执行**初始化数据结构**呢？
  - 因为在配置文件中，是包含对这些**数据结构信息的**，必须要载入配置选项，才可以正确无误地去初始化数据结构，这其实也是为什么，初始化过程分成了两个函数去执行，**第一个是 initServerConfig 去主要负责初始化一般属性，而 initServer 函数主要初始化数据结构；**
- 当 initServer 函数执行完毕之后，服务器将用 ASCII 字符在日志中**打印出 Redis 的图标以及Redis的版本号信息；**

###### 还原数据库状态

- 服务器载入RDB 文件或AOF 文件，并根据文件记录的内容来还原服务器的数据库状态。
  - 优先使用AOF，RDB文件次之；
- 当服务器完成数据库状态还原工作之后，服务器将在日志中打印出**载入文件并还原数据库状态所花费的时长**；

###### 执行时间循环

- 在初始的最后一步，服务器将打印出 ready 日志，并**开始执行服务器的事件循环（IO 多路复用），处理客户端请求**。
