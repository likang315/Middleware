###  哨兵（Sentinel）

------

[TOC]

##### 01：概述

- Sentinel（哨岗、哨兵）是Redis的高可用性（high availability）解决方案：由一个或多个Sentinel实例（instance）组成的Sentinel系统（system）可以监视任意多个主服务器，以及这些主服务器属下的所有从服务器，并在被监视的主服务器进入下线状态时，自动将下线主服务器属下的某个从服务器升级为新的主服务器，然后由新的主服务器代替已下线的主服务器继续处理命令请求。

###### 故障转移操作

- 当主服务器的下线时长超过用户设定的下线时长上限时，Sentinel系统就会对主执行故障转移操作：
  1. Sentinel系统会**挑选主服务器属下的其中一个从服务器**，并将这个被选中的从服务器升级为新的主服务器
  2. Sentinel系统会向主服务器属下的**所有从服务器发送新的复制指令，**让它们成为新的主服务器的从服务器，当所有从服务器都开始复制新的主服务器时，故障转移操作执行完毕。
  3. Sentinel还会继续监视已下线的主服务器，并在它重新上线时，将它**设置为新的主服务器的从服务器**。

##### 02：启动并初始化Sentinel

- sentinel  启动命令：redis-sentinel /path/to/your/sentinel.conf
- Sentinel 启动步骤
  1. 初始化服务器。
  2. 将普通Redis服务器使用的代码替换成Sentinel专用代码。
  3. 初始化Sentinel状态。
  4. 根据给定的配置文件，初始化Sentinel的监视主服务器列表。
  5. 创建连向主服务器的网络连接。

###### 初始化服务器

- Sentinel本质上只是一个**运行在特殊模式下的Redis服务器**，所以启动Sentinel的第一步，就是初始化一个普通的Redis服务器;

###### 使用Sentinel 专用代码

- 普通Redis服务器使用 **redis.h/REDIS_SERVERPORT** 常量的值作为服务器端口， 而Sentinel则使用**sentinel.c/REDIS_SENTINEL_PORT** 常量的值作为服务器端口；

###### 初始化 Sentinel 状态

- 服务器会初始化一个 **sentinel.c/sentinelState 结构**，这个结构保存了服务器中所有和 Sentinel 功能有关的状态。

  - ```c
    struct sentinelState {
        // 当前纪元，用于实现故障转移
        unit64_t current_epoch;
        // 保存了所有被这个sentinel监视的主服务器信息
        dict *masters;
        // 是否进入了TILT模式
        int tilt;
        // 目前正在执行的脚本的数量
        int running_scripts;
        // 进入TILT模式的时间
        mstime_t tilt_start_time;
        // 最后一次执行时间处理器的时间
        mstime_t previous_time;
        // 一个FIFO队列，包含了所有需要执行的用户脚本
        list *script_queue;
    } sentinel;
    ```

###### 初始化 Sentinel 状态的 master 属性

- Sentinel状态中的masters字典记录了所有被Sentinel监视的主服务器的相关信息
  - 字典的键：是被监视主服务器的名字。
  - 字典的值：则是被监视主服务器对应的 sentinel.c/sentinelRedisInstance 结构，代表一个Sentinel 监视的redis 服务器实例；
- 对Sentinel状态的初始化将引发对masters字典的初始化，而masters字典的初始化是根据**被载入的Sentinel配置文件**来进行的。

###### 创建连向主服务器的网络连接

- Sentinel将成为主服务器的客户端，它可以向主服务器发送命令，并从命令回复中获取相关的信息。
- Sentinel会创建两个连向主服务器的**异步网络连接**：
  - **命令连接：**这个连接专门用于向主服务器发送命令，并接收命令回复。
  - **订阅连接：**这个连接专门用于订阅主服务器的`__sentinel__`:hello频道。

##### 03：获取主服务器信息

- Sentinel默认会以**每十秒一次**的频率，通过命令连接向被监视的主服务器发送INFO命令，并通过分析INFO命令的回复来获取主服务器的当前信息。
- Sentinel可以获取以下两方面的信息：
  - 关于**主服务器本身**的信息，包括run_id域记录的服务器运行ID，以及role域记录的服务器角色；
  - 关于**主服务器属下所有从服务器**的信息，根据从服务器的IP地址和端口号，Sentinel无须用户提供从服务器的地址信息，就可以自动发现从服务器，并进行监视；

##### 04：获取从服务器的信息

- 当Sentinel发现主服务器有新的从服务器出现时，Sentinel除了会为这个新的从服务器**创建相应的实例结构**之外，Sentinel还会**创建连接到从服务器的命令连接和订阅连接**。
- 在创建命令连接之后，Sentinel在默认情况下，会以每**十秒一次的频率**通过命令连接向从服务器发送INFO命令，并解析回复，更新从服务器的实例结构；
- <img src="/Users/likang/Code/Git/Middleware/04：Redis/photos/sentinel-master-slave.png" alt="sentinel-master-slave" style="zoom:75%;" />



##### 05：向主服务器和从服务器发送消息

- 在默认情况下，Sentinel会以每**两秒一次**的频率，通过命令连接向**所有被监视的主服务器和从服务器**发送以下格式的命令：

  - ```c
    PUBLISH __sentinel__:hello "<s_ip>, <s_port>, <s_runid>, <s_epoch>,<m_name>,<m_ip>,<m_port>,<m_epoch>" 
    ```

- 向服务器的`__sentinel__`:hello频道发送了一条信息，其中以`s_`开头的参数记录的是Sentinel本身的信息，而` m_`开头的参数记录的则是主或从服务器的信息。

##### 06：接收来自主服务器和从服务器的频道信息

- 当Sentinel与一个主服务器或者从服务器建立起订阅连接之后，Sentinel就会通过订阅连接，向服务器发送以下命令，Sentinel对`__sentinel__`:hello频道的订阅会一直持续到**Sentinel与服务器的连接断开为止**。

  - ```
    SUBSCRIBE __sentinel__:hello
    ```

- 对于**监视同一个服务器的多个Sentinel**来说，一个Sentinel发送的信息会被其他Sentinel接收到，这些信息会被用于**更新其他Sentinel对发送信息Sentinel的认知，也会被用于更新其他Sentinel对被监视服务器的认知**。

###### 更新sentinels 字典

- Sentinel**为主服务器创建的实例结构中**的sentinels字典保存了除Sentinel本身之外，所有同样监视这个主服务器的其他Sentinel的资料：
  - 键：其他sentinel 的 **ip + port** ；
  - 值：sentinel 的结构；
- 当一个Sentinel接收到其他Sentinel发来的信息时，目标Sentinel会从信息中分析并提取出以下两方面参数：
  - 与**Sentinel**有关的参数：源Sentinel的IP地址、端口号、运行ID和配置纪元。
  - 与**主服务器**有关的参数：源Sentinel正在监视的主服务器的名字、IP地址、端口号和配置纪元。
- 目标Sentinel会**在自己的Sentinel状态的masters字典中查找相应的主服务器实例结构**，然后根据提取出的Sentinel参数，检查主服务器实例结构的sentinels字典中，**源Sentinel的实例结构是否存在**：
  - 如果源Sentinel的实例结构**已经存在**，那么对源Sentinel的实例结构**进行更新**。
  - 如果源Sentinel的实例结构**不存在**，那么说明源Sentinel是刚刚开始监视主服务器的新Sentinel，目标Sentinel会为源Sentinel**创建一个新的实例结构，并将这个结构添加到sentinels字典里面**。
- 在使用Sentinel的时候并不需要提供各个Sentinel的地址信息，监视同一个主服务器的多个Sentinel可以自动发现对方。

###### 创建连向其他Sentinel 的命令连接

- 当Sentinel通过频道信息发现一个新的Sentinel时，**它不仅会为新Sentinel在sentinels字典中创建相应的实例结构，还会创建一个连向新Sentinel的命令连接，而新Sentinel也同样会创建连向这个Sentinel的命令连接**，最终监视同一主服务器的多个Sentinel将形成**相互连接的网络**。

###### Sentinel之间不会创建订阅连接

- Sentinel在连接主服务器或者从服务器时，会同时创建命令连接和订阅连接，但是在连接其他Sentinel时，却只会创建命令连接，而不创建订阅连接。
- 因为Sentinel需要通过接收主服务器或者从服务器**发来的频道信息来发现未知的新Sentinel**，所以才需要建立订阅连接，**而相互已知的Sentinel只要使用命令连接来进行通信**就足够了。

##### 07：检测主观下线状态

- 









