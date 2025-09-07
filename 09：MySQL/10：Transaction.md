### Transaction

------

[TOC]

##### 01：数据库并发问题

1. **更新丢失：**加锁（互斥锁），必须等待事物执行完成之后，另一个事物才可以进行；
   1. 回滚丢失：一个写事务成功的时候，另一个事务写失败了，导致了回滚，读已提交解决
   2. 覆盖丢失：一个成功的事务，覆盖了另一个成功的事务的结果，可重复读解决
2. **脏读：**一个事物读到了另一个事物未提交的数据（脏数据）；
   - 读已提交解决；
3. **不可重复读：**一个事物两次读数据之间，由于第二个事物的修改，导致**两次读到的结果不一致**；
   - 是一种**当前读**；
4. **幻读：**同一个事物，**前后两次读取行数不一致**；
   - 在可重复读隔离级别下，**普通查询是快照读**，是不会看到别的事务插入的数据的，幻读只在**当前读**下才会出现；
   - **出现幻读的场景**
     1. 查询之后，更新，再查询；
     2. 先不加锁，后加锁；
        - T1 时刻：事务 A 先执行快照读语句：select * from test where id > 100 得到了 3 条记录。
        - T2 时刻：事务 B 往插入一个 id= 200 的记录并提交；
        - T3 时刻：事务 A 再执行**当前读语句**： select * from test where id > 100 for update 就会得到 4 条记录；

##### 02：事务

- 是访问并更新数据库中各种数据项的**最小执行单元**；

###### ACID 特性

1. **原子性（Atomic）**：是一个不可分割的执行单元，要么全部完成，要么全部不完成；
   - Undo log 保证；

2. **一致性（Consistency）**：数据库的角度，数据库从一种一致性状态到另一种一致性状态；
   - 通过 原子性 + 隔离性 + 持久性来保证；
3. **隔离性（Isolation）**：事物之间相互隔离，即该事物提交前对其他事务不可见；
   - 由锁 和 MCVV 来实现；
4. **持久性（Durabiliy）**：事物一旦提交，持久化到磁盘中；
   - 由 Redo Log 实现；

#### 事务的视线

##### 03：redo Log (重做日志）

- 用来实现事务的持久性，物理日志，由两部分组成
  - 内存中的重做日志缓冲（redo log buffer）；
  - 磁盘中的重做日志文件（redo log file） ，其是持久的；
- 当事务提交时，必须先将**该事务的所有日志落盘到重做日志文件进行持久化**，待事务提交结束才算完成。为了确保**每次将重做日志缓冲文件都写入重做日志文件**，在每次将重做日志缓冲写入重做日志文件后，InnoDB 存储引擎都需要调用一次 fsync 操作。
- innodb_flush_log_at_trx_commit：控制重做日志刷盘策略；
  - 0：表示事务提交时不进行写人重做日志操作，这个操作仅在 master thread 中完成，而
    在 master thread 中**每 1 秒会进行一次重做日志文件的fsync 操作**。
  - 1：默认值，表示**事务提交时必须调用一次 fsync 操作**。
  - 2：事务提交时将重做日志写人重做日志文件，但仅写人文件系统的缓存中，不进行fsync 操作。在这个
    设置下，当MySQL 数据库发生宕机而操作系统不发生宕机时，并不会导致事务的丢失。

###### LSN（Log Sequence Number）

- 其代表的是日志序列号。在InnoDB 存储引擎中，LSN 占用8字节，并且单调递增。LSN 表示的含义有：
  - 重做日志写入的总量；
  - checkpoint 的位置；
  - 页的版本；
- Log sequence number：表示当前的LSN；
- Log flushed up to：表示刷新到重做日志文件的 LSN；
- Last checkpoint at：表示刷新到磁盘的LSN。

###### 恢复

- InnoDB 存储引擎在启动时不管上次数据库运行时是否正常关闭，都会尝试进行恢复操作。通过对比磁盘和缓冲中的LSN，选择恢复范围。
- <img src="/Users/likang/work/kungFu/Middleware/09：MySQL/photos/redo-log-recover.png" alt="redo-log-recover" style="zoom:30%;" />

##### 04：undo Log（回滚日志）

-  记录了事务的行为，可以对页进行 "回滚" 操作；undo log 存放在数据库内部的一个特殊段（Segment）中，回滚段，位于表空间中，是逻辑日志。
- 回滚日志：将数据库数据恢复到原来的状态，但数据结构和页本身在回滚之后可能和事务开始前不太相同，因为同时可能有大量的并发事务存在，不能简单的将一个页回滚到事务开始时的样子，否则会影响其他事务，恢复行记录。

###### undo 存储管理

- undo log segment 存储于 rollback segment 中，每个回滚段中默认有 1024 个 undo log segment；
- 参数
  - innodb_undo_directory：用于设置 rollback segment 文件所在的路径，意味着rollback segment 可以存放在共享表空间以外的位置，即可以设置为独立表空间。
  - **innodb_undo_logs**：用于设置 rollback segment 的个数，默认值：128。
  - innodb_undo_tablespaces：用于设置构成 rollback segment 文件的数量。

###### undo log格式

- insert undo log：在 insert操作中产生的 undo log。因为 insert操作的记录，只对事务本身可见，对其他事务不可见（这是事务隔离性的要求），故该 undo log 可以在事务提交后直接删除。不需要进行 purge 操作。
- update undo log：记录的是对 delete 和 update操作产生的undo log。该 undo log 可能需要提供 MVCC机制，因此不能在事务提交时就进行删除。提交时放入 undo log 链表，等待 purge线程进行最后的删除。

###### 查看 undo log 信息

- ```sql
  select * from information_schema.innodb_trx_undo;
  ```

- 查看某个事务 ID，对应的 undo log 类型和大小。

##### 05：purge

- 用于最终完成 delete 和update 操作。这样设计是因为**InnoDB 存储引擎支持MVCC，所以记录不能在事务提交时立即进行处理。这时其他事物可能正在引用这行，故InnoDB 存储引擎需要保存记录之前的版本**。而是否可以删除该条记录通过 purge 来进行判断。若该行记录已不被任何其他事务引用，那么就可以进行真正的 delete 操作。

###### history List

- 在 InnoDB 存储引擎有一个 history列表，它根据事务提交的顺序，将undo log 进行链接。
- 在执行 purge 操作是，首先会从 history list 中找到第一个需要被清理的事务。
- innodb_purge_batch_size：用来设置每次 purge 操作需要清理的 undo page 数量。 清理越多，会导致 CPU 和磁盘 IO 过于几种对 undo log 的处理，使性能下降。

##### 06：group commit

- Group Commit 允许**多个事务的提交操作合并为一次磁盘 I/O**，将原本串行的提交过程变为并行处理，一种提升事务的提交性能机制。

###### 核心原理

1. **Leader-Follower 机制**
   - 第一个到达提交阶段的事务成为**Leader**，负责执行磁盘刷盘（`fsync()`）。
   - 后续到达的事务成为**Follower**，无需等待刷盘，直接加入组。
2. **批量刷盘**
   - Leader 将自己和所有 Follower 的 **Redo Log 一次性刷盘，大幅减少磁盘 I/O 次数**。
3. **并行提交**
   - 刷盘完成后，Leader 和所有 Follower **并行执行后续操作**（如释放锁、通知客户端）。

###### 三个阶段（先 binlog 后 redo Log）

- **FLUSH 阶段**：Leader 收集所有 Follower 的**bin log**，将它们从 Binlog Buffer 刷新到操作系统缓存（但未真正刷盘）。
- **SYNC 阶段**：Leader 执行`fsync()`，将操作系统缓存中的**bin log**刷到磁盘。
- **COMMIT 阶段**：Leader 触发**redo Log**的刷新（从 InnoDB Log Buffer 到磁盘），Leader 和所有 Follower 并行执行事务提交的收尾工作（如释放锁、更新事务状态）。

###### 参数

- `binlog_group_commit_sync_delay`：Leader 等待 Follower 的时间（微秒），默认：0，不等待，立即提交。
- `binlog_group_commit_sync_no_delay_count`：当组内事务达到此数量时，立即触发刷盘，无需等待 `sync_delay`。默认值：0，不限制。

##### 07：事务控制语句

- 事务默认自动提交的， SET AUTOCOMMIT=1；

###### 显式使用

- BEGIN/START TRANSACTION：显式地开启一个事务；
- COMMIT：提交事务，对数据库操作进行持久化；
- ROLLBACK：回滚；
- SET TRANSACTION：设置事务的隔离级别；

###### 隐式使用

- DDL、DML语句，执行结束后，会有一个隐式的 COMMIT 操作；
- TRUNCATE TABLE：操作实际上类似于DROP TABLE和CREATE TABLE的组合。它会**隐式提交当前活动的事务，因此不能回滚**。

##### 08：事务隔离级别

- 事务的隔离级别：是指多个并发事务之间相互隔离的程度。隔离级别越高，性能越差；
- 隔离级别有 4 个，由低到高依次为 
  1. Read Uncommitted
     - 事务可以读取其他事务**未提交**的修改；
  2. Read Committed
  
     - 只读取其他事务**已提交**的修改，通过 Read View 实现；
     - 解决更新丢失，脏读；
  3. Repeatable Read
  
     - **默认的隔离级别**，保证同一事务内多次读取相同数据结果一致；
     - 解决不可重复读，**未彻底解决幻读，只是通过各种手段规避幻读**。
     - 快照读时，MVCC可以避免快照读的幻读（因为多次快照读使用相同的Read View，不会看到新插入的行）
     - 当前读时（SELECT ... FOR UPDATE），通过 Next-Key Lock（临键锁）来避免幻读。
  4. Serializable
  
     - 串行化，InnoDB 存储引擎会对每个 SELECT 语句后**自动加上LOCK IN SHARE MODE**，即为每个读取操作加一个共享锁。
     - 解决幻读；

##### 09：Read View（读视图）

###### 事务 ID 字段

- **m_ids ：**指的是在创建 Read View 时，当前数据库中活跃事务的**事务 id 列表**，注意是一个列表。
  - 活跃事务：指启动了但还没提交的事务。
- **min_trx_id：**指的是在创建 Read View 时，当前数据库中活跃事务中事务 **id 最小的事务**，也就是 m_ids 的最小值。
- **max_trx_id：**这个并不是 m_ids 的最大值，而是**创建 Read View 时当前数据库中应该给下一个事务的 id 值**。
- **creator_trx_id：**指的是创建该 Read View 的事务的事务 id。
  - <img src="/Users/likang/work/kungFu/Middleware/09：MySQL/photos/Read-View.png" alt="Read-View" style="zoom:30%;" />


###### 不同隔离级别下的工作原理

- 读已提交：在事务中，每次执行 SELECT 查询时，都会生成一个新的 Read View（读视图）。根据 Read View 判断数据行的可见性：只能看到在本次查询开始之前已经提交的事务所做的修改。
- 可重复读：事务启动时生成一个 Read View，通过对比事务 ID 大小，判断最新版本是否对事务可见；

##### 10：分布式事务

- 指的是多个独立的数据库参与到一个全局的事务中。
- 更新时，redo log和bin log 就是内部的分布式事务；

###### 两阶段提交

1. 准备阶段
   - 协调者询问所有参与者是否可提交；
   - 参与者执行事务但不提交，锁定资源；
2. 提交阶段
   - 所有参与者响应YES → 协调者发送COMMIT；
   - 任一参与者响应NO → 协调者发送ROLLBACK；

##### 11：长事务（Long Lived Transaction）

- 执行时间比较长的事务。切割成多个小事务，批量执行；
