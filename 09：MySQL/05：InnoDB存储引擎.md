### InnoDB 存储引擎

------

[TOC]

##### 01：InnoDB 架构

- MySQL 从 5.5 版本之后默认的存储引擎，是第一个支持完整 ACID 特性的事务型存储引擎；

<img src="https://github.com/likang315/Middleware/blob/master/09：MySQL/photos/innoDB.png?raw=true" style="zoom:67%;" />

##### 02：后台线程

- 负责刷新缓存池中的数据，保证缓存池中的缓存的是最新的数据；
- 将已经修改的数据文件刷新到磁盘文件（刷盘），同时保证数据库在发生异常的情况下InnoDB能恢复到正常的运行状态；

1. **Master Thread：**负责将数据**异步刷新到磁盘，保证数据得一致性**，InnoDB 的主要工作都是该线程完成，该线程具有最高的优先级；
2. **IO Thread：**使用 AIO，处理 IO 请求的回调；
3. **Purge Thread：**在事务提交的时候**回收 Undo Log**；
   - 具体的规则是将删除版本号小于当前系统版本的行删除；
4. **Page Clearner Thread：**将脏页刷新操作都放入到单独的线程中来完成，减轻 Master Thread 的压力；
   - 脏页：读到的数据（缓冲池中），还没有刷新到磁盘上。

##### 03：内存

<img src="/Users/likang/work/kungFu/Middleware/09：MySQL/photos/InnoDB-dataObj.png" alt="InnoDB-dataObj" style="zoom:35%;" />

###### 缓冲池（innodb_buffer_pool）

1. InnoDB 存储引擎是基于磁盘存储的，并将其中所有的记录**按照页**的方式进行管理，由于 CPU 和磁盘速度的鸿沟，采用**缓存池技术**来提高数据库的性能；
2. 缓冲池是一块内存区域，被还划分为多个固定大小的缓冲块（buffer block）。每个缓冲块可存储一个数据页或索引页。
   - 对于读操作，首先将从磁盘读到的页存放到缓存池中, 该过程称为**将页"FIX"在缓冲池中**。下次再读相同页的时候先判断是否在缓冲池中，若是命中，直接读取该页。否则，读取磁盘上的页；
   - 对于写操作，**首先修改缓冲池中的页, 再以一定的频率刷新到磁盘上**，而不是每次发生页修改时触发，**通过"CheckPoint 机制"，刷新磁盘**；
   - 查看缓存池大小：show variables LIKE 'innodb_buffer_pool_size';
   - 查看缓存池实例：show variables LIKE 'innodb_buffer_pool_instances';

3. 重做日志缓冲：InnoDB 首先将 Redo Log 日志放入这个缓冲区，然后以一定的频率刷新到磁盘的 Redo Log 文件中。

###### LRU List、Free List 和 Flush List

- 缓冲池的大小默认为 **256KB**，使用LRU（最近最少使用）算法进行管理。
- midpoint insertion strategy：InnoDB 对LRU 算法进行优化，加入了 midpoint 位置。新读取的页，并不是直接放入到 LRU 列表的首部，而是放入到 midpoint  (默认5/8长度处)；
  - 原因：批量查询时，可能导致热页置换；
- Free List：空闲页列表，需要写入缓存数据时，从该列表中，获取空闲页，放入到 LRU List 中。
- **Flush List**：脏页列表，用于管理将页刷新会磁盘。
  - 查看 Flush 列表大小：show engine innodb status 结果中 modified db pages 就是脏页的数量；

###### 重做日志缓冲（redo log buffer）

- InnoDB 存储引擎首先将重做日志信息先放人到这个缓冲区，然后按一定频率将其刷新到重做日志文件。
- innodb_log_buffer_size 控制大小，默认8MB；
- 以下三种情况也会进行刷盘
  1. Master Thread 每**一秒**将重做日志缓冲刷新到重做日志文件；
  2. 每个**事务提交时**会将重做日志缓冲刷新到重做日志文件；
  3. 当**重做日志缓冲池剩余空间小于 1/2 时**，重做日志缓冲刷新到重做日志文件。

###### 额外的内存池

- 数据结构本身内存进行分配时，需要从额外的内存池中申请，若该区域不足时，则从缓冲池中申请。

##### 04：Checkpoint 技术

- 定义：触发 Checkpoint 时，将缓冲池中的脏页刷新到磁盘的操作；
- 场景
  1. Master Thread Checkpoint：以每秒或每十秒的速度从缓冲池的脏页列表中刷新一定比例的页回磁盘。这个过程是异步的，即此时 InnoDB 存储引擎可以进行其他的操作，用户查询线程不会阻塞。
  2. FLUSH_LRU_LIST Checkpoint：LRU 列表空闲页不足时，移除最尾端的脏页之前，进行刷盘；
     - Page Clearner Thread负责执行的。
  3. Async/Sync Flush Checkpoint：redo log buffer是循环使用的，当日志将要覆盖时，刷盘，此时的脏页是从脏页列表中选取的。
  4. Dirty Page too much Checkpoint：脏页数量太多（某个阈值），导致强制进行刷盘。保证缓冲池中有足够可用的页。

##### 05：Master Thread 的工作方式

- 每 1s 和每 10s，可能进行以下操作；
  1. 日志缓冲（redo log buffer）刷新到磁盘，即使这个事务还没有提交（总是）；
  2. 合并插入缓冲；
  3. 辅助删除无用的 Undo 页；
  4. 部分脏页的刷新操作，主要交给Page Clearner Thread线程负责；

##### 06：Insert Buffer

###### Insert Buffer：提高插入性能

- 在 InnoDB 中，主键是唯一的标识，在**插入记录的顺序是按照主键递增的顺序进行**，因此不需要磁盘的随机读取。但更多情况下表中**除了主键索引以外还存在其他辅助索引**。在进行插人操作时，**数据页的存放还是按主键进行顺序存放的，但是对于非聚集索引叶子节点的插人不再是顺序的**，这时就需要离散地访问非聚集索引页，由于**随机读取的存在而导致了插人操作性能下降**。
- 因此对于非聚簇索引的插入或者更新，不是一次直接插入到索引页中，而是先判断插入的非聚簇索引页是否在缓冲池中，如果在，直接插入，否则先放入⼀个 **Insert Buffer** 对象，作为占位符，然后**再以⼀定的频率进行 Insert Buffer 和 索引叶子节点的 merge 操作**，这时通常能将多个插入合并到一个操作中，大大提高了非聚集索引的插入性能。
- 内存中的 Insert Buffer 需要持久化到磁盘，以防止 MySQL 崩溃时数据丢失。其元数据和部分数据存储在共享表空间文件 `ibdata1` 中。

###### Insert Buffet 流程【重要】

- 当插入一条记录时，将记录按照主键顺序写入到聚簇索引中（缓冲止中完成）；
- 检查辅助索引是否在缓冲池中，若在直接更新索引，否则进入 Insert Buffer 流程；
- 将辅助索引页记录（辅助索引列+主键列），写入到 Insert Buffer中；
- 当使用辅助索引列查询时，从磁盘加载辅助索引数据页到缓存中，然后 merge 数据页；
- merge 后的页被标记为脏页，后续CheckPoint 进行刷盘；

###### Change Buffer

- **Insert Buffer 的升级版**，一种优化机制，用于**延迟对非唯一二级索引的写入操作**，从而减少磁盘 I/O。
- 可以对 DML 操作INSERT、DELETE、UPDATE 都进行缓冲，他们分别是：Insert Buffer、Delete Buffer、Purge buffer。
- 参数
  - **innodb_change_buffer_max_size**：用于开启各种 Buffer 的选项。
  -  **innodb_change_buffer_max_size** ：Change Buffer 最大使用内存的数量，值默认为25，表示最多使用1/4的缓冲池内存空间。


###### Insert Buffer 的内部实现

- 负责对所有的表的辅助索引进行 Insert Buffer。而这棵 B+ 树**存放在共享表空间**中，默认也就是ibdatal 中。
- Insert Buffer 是一棵B+树。非叶节点存放的是查询的 searchkey（键值），工占有 9 个字节，有**space、marker、offset** 三个字段组成。叶子节点存储辅助索引要插入的辅助索引记录。
- <img src="/Users/likang/work/kungFu/Middleware/09：MySQL/photos/Insert-Buffer.png" alt="Insert-Buffer" style="zoom:40%;" />
  1. space：表示待插人记录所在表的表空间 id，在InnoDB 存储引擎中，每个表有一个唯一的space id，可以通过 space id 查询得知是哪张表。space 占用4字节。
  2. marker：用来兼容老版本的 Insert Buffer，占用1字节。
  3. offset：表示页所在的偏移量，占用4字节。
  4. metadata：辅助索引页的记录的原数据，占用 4 字节。
     - IBUF_REC_OFFSET_COUNT：是保存两个字节的整数，用来排序每个记录进人
       Insert Buffer 的顺序。


###### Insert Buffer Bitmap

- 启用 Insert Buffer 索引后，辅助索引页（space,offset）中的记录可能被插人到 Insert Buffer B+ 树中，所以为了保证每次 Merge Insert Buffer 页必须成功，还需要有**一个特殊的页用来标记每个辅助索引页（space,page_no）的可用空间**。这个页的类型为 Insert Buffer Bitmap。
  - IBUF_BITMAP_FREE：表示该辅助索引页中的可用空间数量
    - 0：表示无可用剩余空间；
    - 1：表示剩余空间大于1/32页（512字节）；
    - 2：表示剩余空间大于1/16页；
    - 3：表示剩余空间大于 1/8页；
  - IBUF_BITMAP_BUFFERED
    - 1：表示**该辅助索引页有记录被级存在 Insert Buffer B+树中；**
  - IBUF_BITMAP_IBUF 
    - 1：表示该页为 Insert Buffer B+ 树的索引页；

###### Merge Insert Buffer

- Merge Insert Butter 的操作可能发生在以下几种情況
  1. 辅助索引页被读取到缓冲池时；
  2. Insert Buffer Bitmap 页追踪到该辅助索引页已无可用空间时：
  3. Master Thread。

##### 07：二次写（double write）

###### 两部分组成

1. 一部分：double write buffer，大小为 2M
   - 备份的作用，如果写入到共享表空间时出现异常，可以通过double write buffer中的数据进行恢复；
2. 另一部分：物理磁盘上的共享表空间中连续的128个页，即两个区，大小同样为2M；
   - 保证数据页的可靠性；
   - 对缓冲池的脏页进行刷新时，并不直接写硬盘，而是通过**memcpy函数将脏页先拷贝到内存中的 doubleWrite buffer，之后通过 double write buffer 再分两次，每次写入1M到共享表空间的物理磁盘上，然后马上调用 fsync 函数，同步磁盘**，避免数据的丢失；

###### 为什么两次写

- 防止宕机时，部分写失败导致的数据页损坏；Double Write 的共享表空间是一种顺序写，开销小。磁盘的数据文件是随机写，开销大。
- **第一次写**：在Double Write buffer 中写入数据，并且在共享表空间中创建脏页的 “安全副本”，确保后续写失败时有恢复依据。
- **第二次写**：在数据文件中执行真正的更新，可能因硬件故障导致部分写失败。
- <img src="/Users/likang/work/kungFu/Middleware/09：MySQL/photos/DoubleWrite.png" alt="DoubleWrite" style="zoom:50%;" />

##### 08：自适应性哈希索引 (Adaptive Hash Index，AHI)

- InnoDB 会监控表上各项索引页的查询，如果**观察到适合建立哈希索引就会行动**，因此被称为自适应性哈希索引，AHI 通过索引页构造而来，InnoDB 会**根据访问的频率和模式来自动为某些热点页建立 AHI**；
- AHI 是通过缓冲池的B+树页构造而来，因此建立的速度很快，而且不需要对整张表构建哈希索引。

##### 09：异步 IO（AIO）

- 在 InnoDB 存储引擎中，read ahead（读取预读）、脏页的刷新、日志写入（Redo Log），全部由 AIO 完成。
- innodb_use_native_aio：查看是否开启 AIO；

##### 10：刷新相邻页（ Flush Neighbor Page）

- 当刷新一个脏页时，InnoDB 存储引擎会**检测该页所在区（extent）的所有页，如果是脏页，那么一起进行刷新**。这样做的好处显而易见，通过AIO 可以将多个写人操作合并为一个操作，故该工作机制在传统机械磁盘下有着显著的优势。

- 对于传统机械硬盘建议启用该特性，而对于固态硬盘有着超高IOPS性能的磁盘，则建议将该参数设置为O，即关闭此特性。

- ```sql
  show variables LIKE 'innodb_flush_neighbors';
  ```

##### 11：启动、关闭与恢复

###### 关闭

- 在关闭时，参数 innodb_fast_shutdown 影响着表的存储引擎为 InnoDB 的行次。该参
  数可取值为O、1、2，默认值为1。
- 0：表示在MySQL 数据库关闭时，InnoDB 需要完成所有的 full purge 和 merge
  insert buffer，并且将所有的脏页刷新回磁盘。
- **1：默认值**，表示不需要完成上述的 full purge 和merge insert buffer 操作，但是在缓冲池中的一些数据脏页还是会刷新回磁盘。
- 2：表示不完成 faull purge 和 merge insert buffer操作，也不将缓冲池中的数据脏页写回磁盘，而是将日志都写人日志文件。这样不会有任何事务的丢失，但是下次MySQL 数据库启动时，会进行恢复操作 （recovery）。

###### 恢复

- 参数 innodb_force_recovery 影响了整个 InnoDB 存储引擎恢复的状况。该参数值**默认为0，代表当发生需要恢复时，进行所有的恢复操作**，当不能进行有效恢复时，如数据页发生了损坏（corruption），MySQL 数据库可能发生宕机（crash），并把错误写入错误日志中去。
- innodb_force_recovery： 还可以改置为6个非答值：1～6。大的效字表不包含了前面所有小数字表示的影响。
  - 1（SRV_FORCE_IGNORE_CORRUPT）：忽略检查到的 corrupt 页。
  - 2（SRV_FORCE_NO_BACKGROUND）： 阻止 Master Thread 线程的运行，如 Master Thread 线程需要进行 full purge 操作，而这会导致 crash。
  - 3（SRV _FORCE_NO_TRX_UNDO）： **不进行事务的回滚操作**。
  - 4（SRV_FORCE_NO_IBUF_MERGE）：不进行插入缓冲的合并操作。
  - 5（SRV_FORCE_NO_UNDO_LOG_SCAN）：不查看撤销日志（Undo Log），InnoDB
    存储引擎会将未提交的事务视为已提交。
  - 6（SRV_FORCE._NO_ LOG_REDO）：不进行前滚的操作。







