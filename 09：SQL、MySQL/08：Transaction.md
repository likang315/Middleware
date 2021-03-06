### Transaction

------

[TOC]

##### 01：事务

- 访问并更新数据库中各种数据项的**最小执行单元**；

###### ACID

1. **原子性(Atomic)：**是一个不可分割的执行单元，要么全部成功，要么全部失败；
2. **一致性(Consistency)：**数据库的角度，数据库从一种一致性状态到另一种一致性状态；
   - 原子性，一致性用 Undo Log 来完成
3. **隔离性(Isolation)：**事物之间相互隔离；
   - 由锁来实现
4. **持久性（Durabiliy）**：事物一旦提交，持久化到磁盘中；
   - 由 Redo Log 实现

##### 02：事物的实现

1. ###### Redo Log (重做日志）：用来实现事务中的持久性, 由两部分组成：

   - 内存中的重做日志缓冲(redo log buffer) ；
   - 磁盘中的重做日志文件(redo log file) ，其是持久的；
   - 当事务提交时，必须先将该事务的所有日志落盘到重做日志文件进行持久化，待事务提交结束才算完成；

2. ###### Undo Log（回滚日志）：记录了事务的行为, 可以对页进行 "回滚" 操作；

   -  undo log 存放在数据库内部的 undo段中，位于共享表空间；
   - 回滚日志的主要工作是将数据库逻辑地恢复到原来的样子，但数据结构和页本身在回滚之后可能和事务开始前不太相同，因为与此同时有大量的并发事务存在, 不能简单的将一个页回滚到事务开始时的样子, 否则会影响其他事务，恢复行记录；

##### 03：事务隔离级别

- 隔离级别有 4 个，由低到高依次为 

  1. Read uncommitted

  2. Read committed

     - 解决更新丢失，脏读；

  3. Repeatable read

     - 解决了不可重复读；
     - 没有解决幻读，只是用历史数据规避了部分幻读，若需完全避免，需要**手动加锁**将快照读调整为当前读，MySQL使用**next-key**完全避免了幻读；

     - 默认的隔离级别；

  4. Serializable

     - 幻读；
     - 使用串行化，事务一个一个按顺序执行，每次读都会加锁，导致快照读失效；

##### 04：主从复制

![](https://github.com/likang315/Middleware/blob/master/09%EF%BC%9ASQL%E3%80%81MySQL/photos/%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6.png?raw=true)

- 从库生成两个线程，**一个IO线程，一个SQL线程**
- 主库会通过**bin log 记录每行实际数据的变更**，IO 线程去请求 主库 的bin log，主库会生成一个 **log dump 线程**，用来给从库 IO 线程传bin log，从库将得到的 bin log日志**写入 Relay log（中继日志） 文件中**，**SQL 线程负责读取**Relay log文件中的日志，并解析成具体操作，来实现主从的数据一致；

###### 两种复制的方式

1. 异步复制原理 ：
   - 主库**提交事务后，立即返回客户端**，它的同步是当有从库的I/O线程请求才传送 Bin log 的。
2. 半同步复制原理：
   - 主库在执行完客户端提交的事务后不是立刻返回给客户端，而是**等待至少一个从库接收并写到relay log中才返回给客户端**，相对于同步复制，半同步复制提高了数据的安全性，同时它也造成了一定程度的延迟； 

###### 主从复制优缺点：

1. 灾容性好，用于故障切换、恢复数据库；
2. 读写分离，提供查询服务，降低主库负载；
3. 从库**只有一个sql Thread**，主库写压力大，复制很可能延时，通过并行复制解决；

######  主从库架构

1. 一主一从
2. 一主多从，读是在从库读取的
3. 主主复制
4. 多主一从
5. 联级复制

