###  哨兵（Sentinel）

------

[TOC]

##### 01：概述

- Sentinel（哨岗、哨兵）是**Redis的高可用性（high availability）解决方案**：由一个或多个Sentinel实例（instance）组成的Sentinel系统（system）可以监视任意多个主服务器，以及这些主服务器属下的所有从服务器，并在被监视的主服务器进入下线状态时，自动将下线主服务器属下的某个从服务器升级为新的主服务器，然后由新的主服务器代替已下线的主服务器继续处理命令请求。

###### 故障转移操作【主从复制、主从切换】

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

- 在默认情况下，Sentinel会以**每秒一次的频率**向所有与它创建了命令连接的实例（包括主服务器、从服务器、其他Sentinel在内）**发送PING命令**，并通过实例返回的PING命令回复来判断实例是否在线。

###### 实例对PING命令的回复情况

- 有效回复：实例返回+PONG、-LOADING、-MASTERDOWN三种回复的其中一种。
- 无效回复：实例返回除以上三种回复之外的其他回复，或者在指定时限内没有返回任何回复。

###### down-after-milliseconds 属性

- Sentinel配置文件中的down-after-milliseconds选项指定了**Sentinel判断实例进入主观下线所需的时间长度**：如果一个实例在down-after-milliseconds毫秒内，连续向Sentinel返回无效回复，那么**Sentinel会修改这个实例所对应的实例结构，在结构的flags属性中打开SRI_S_DOWN标识**，以此来表示这个实例**已经进入主观下线状态**。

###### 多个Sentinel设置的主观下线时长可能不同

- 对于**监视同一个主服务器的多个Sentinel**来说，这些Sentinel所设置的down-after-milliseconds选项的值也可能不同，因此，当一个Sentinel将主服务器判断为主观下线时，其他Sentinel可能仍然会认为主服务器处于在线状态。

##### 08：检查客观下线状态

- 当Sentinel将一个主服务器判断为主观下线之后，为了确认这个主服务器是否真的下线了，它会**向同样监视这一主服务器的其他Sentinel进行询问（发送命令请求）**，看它们是否也认为主服务器已经进入了下线状态（可以是主观下线或者客观下线）。当Sentinel从其他Sentinel那里**接收到足够数量的已下线判断之后，Sentinel就会将从服务器判定为客观下线**，并对主服务器**执行故障转移**操作。

###### SENTINEL is-master-down-by-addr 命令通信过程

- ```shell
  # curent_epoch:当前纪元
  SENTINEL is-master-down-by-adr <ip> <port> <current_epoch> <runid>
  ```

- 用于询问其他Sentinel 是否统一主服务器已下线；

- 当收到命令请求时，目标Sentinel会**分析并取出命令请求中包含的各个参数**，并根据其中的主服务器IP和端口号，检查主服务器是否已下线，然后向源Sentinel返回一条**包含三个参数的Multi Bulk回复**；

  - **down_state**：返回目标Sentinel对Master的检查结果，**1代表主服务器已下线，0代表主服务器未下线；**
  - leader_runid：Sentinel运行ID，用于选举首领Sentinel，“.”用于检测主服务器的下线状态；
  - leader_epoch：目标Sentinel的局部首领Sentinel的配置纪元，用于选举首领Sentinel

- Sentinel将**统计其他Sentinel同意主服务器已下线的数量，当这一数量达到配置指定的判断客观下线所需的数量时**，Sentinel会将**主服务器实例结构flags属性的 SRI_O_DOWN 标识打开，**表示主服务器已经进入客观下线状态。

##### 09：选举首领Sentinel

- 当一个主服务器被判断为客观下线时，监视这个下线主服务器的**各个Sentinel会进行协商，选举出一个首领Sentinel**，并由**首领Sentinel对下线主服务器执行故障转移**操作。

###### 首领选举流程（Raft算法）

- 监视同一个主服务器的**多个在线Sentinel中**的任意一个都有可能成为领头Sentinel，**每次进行领头Sentinel选举之后，不论选举是否成功，所有Sentinel的配置纪元（configuration epoch）的值都会自增一次**。配置纪元实际上就是一个计数器，并没有什么特别的。
- 在一个配置纪元里面，**所有Sentinel都有 一次 将某个Sentinel设置为局部领头Sentinel的机会**，并且局部领头一旦设置，在这个配置纪元里面就不能再更改。
- 每个**发现主服务器进入客观下线的Sentinel都会要求其他Sentinel将自己设置为局部领头Sentinel**。
- 当一个Sentinel（源Sentinel）向另一个Sentinel（目标Sentinel）**发送SENTINEL is-master-down-by-addr命令，并且命令中的runid参数不是*符号而是源Sentinel的运行ID时**，这表示源Sentinel要求目标Sentinel将前者设置为后者的局部领头Sentinel。
  - Sentinel设置局部领头Sentinel的规则是先到先得：最先向目标Sentinel发送设置要求的源Sentinel将成为目标Sentinel的局部领头Sentinel，而**之后接收到的所有设置要求都会被目标Sentinel拒绝**。
  - 目标Sentinel在接收到SENTINEL is-master-down-by-addr命令之后，将向源Sentinel返回一条命令回复，**回复中的leader_runid参数和leader_epoch参数分别记录了目标Sentinel的局部领头Sentinel的运行ID和配置纪元**。
- 源Sentinel在接收到目标Sentinel返回的命令回复之后，会**检查回复中leader_epoch参数的值和自己的配置纪元是否相同**，如果相同的话，那么源Sentinel继续取出回复中的leader_runid参数，**如果leader_runid参数的值和源Sentinel的运行ID一致**，那么表示目标Sentinel将源Sentinel设置成了局部领头Sentinel。
- 如果有**某个Sentinel被半数以上的Sentinel设置成了局部领头Sentinel，那么这个Sentinel成为领头Sentinel**。
  - 因为领头Sentinel的产生需要半数以上Sentinel的支持，并且**每个Sentinel在每个配置纪元里面只能设置一次局部领头Sentinel**，所以在一个配置纪元里面，只会出现一个领头Sentinel。
- 如果在给定时限内，没有一个Sentinel被选举为领头Sentinel，那么各个Sentinel将在一段时间之后再次进行选举，直到选出领头Sentinel为止。

##### 10：故障转移

###### 选出新的主服务器

- 在已下线主服务器属下的所有从服务器中，挑选出一个状态良好、数据完整的从服务器，然后向这个从服务器发送SLAVEOF no one命令，将这个从服务器转换为主服务器。

###### 挑选流程（候选人列表

- 领头Sentinel会将已下线主服务器的所有从服务器保存到一个列表里面，然后按照以下规则，一项一项地对列表进行过滤；
  1. 删除列表中所有处于下线或者断线状态的从服务器，这可以保证列表中剩余的从服务器都是**正常在线**的。
  2. 删除列表中所有最近五秒内没有回复过领头Sentinel的INFO命令的从服务器，这可以保证列表中剩余的从服务器都是**最近成功进行过通信**的。
  3. 删除所有与已下线主服务器连接断开超过down-after-milliseconds*10毫秒的从服务器，**最新的数据**；
  4. 领头Sentinel将**根据从服务器的优先级**，对列表中剩余的从服务器进行排序，并选出其中**优先级最高的从服务器**，若优先级相同，挑选复制偏移量最大的，若以上两个都相同，那挑选run-id最小的；
- 在发送SLAVEOF no one命令之后，领头Sentinel会以每**秒一次的频率（平时是每十秒一次），向被升级的从服务器发送INFO命令**，并观察命令回复中的角色（role）信息，当被升级服务器的role从原来的slave变为master时，领头Sentinel就知道被选中的从服务器已经顺利升级为主服务器；

###### 修改从服务器的复制目标

- 当新的主服务器出现之后，领头Sentinel下一步要做的就是，让已下线主服务器属下的所有从服务器去复制新的主服务器，这一动作可以通过向**从服务器发送SLAVEOF命令**来实现。

- ```shell
  SLAVEOF <master_ip> <master_port>
  ```

###### 将旧的主服务器变为从服务器

- 将已下线的主服务器设置为新的主服务器的从服务器，因为旧的主服务器已经下线，所以这种设置**是保存在server1对应的实例结构里面的**，当server1重新上线时，**Sentinel就会向它发送SLAVEOF命令，让它成为server2的从服务器**。

###### 广播让跟随者 Sentinel 知道master 

- 通过向 topic: `__sentienl__:hello` 发送消息，告知跟随者当前主服务器配置信息；






