### Jstack

------

[TOC]

##### 01：概述

- 是 JDK 自带的一种**堆栈跟踪工具**。
- Jstack 用于打印出**给定的 java 进程ID**或 core file 或远程调试服务的Java堆栈信息；
- Jstack 用于生成 java 虚拟机当前时刻的**线程快照**。线程快照是当前 java 虚拟机内每一条线程正在执行的方法堆栈的集合，生成线程快照的主要目的是**定位线程出现长时间停顿的原因**，如线程间死锁、死循环、请求外部资源导致的长时间等待等。

```
jstack [-l] pid
```

###### 参数

- -l ：用于打印与每个线程相关的锁信息，包括拥有的锁，等待的锁以及等待锁的线程；
  - 自动打印出死锁相关信息；

##### 02：线程堆栈信息分析

```shell
"xupt-2" #66 prio=5 os_prio=0 tid=0x00007f26940c4800 nid=0x76 waiting on condition [0x00007f26ef3a3000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000006417bd160> (a 
	java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(
											AbstractQueuedSynchronizer.java:2039)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
	
	Locked ownable synchronizers:
        - None
```

###### 第一行

- **线程名**：xupt-2，程序自定义的；
- **Native ID (nid)**：线程的本地 ID，由**操作系统分配**的用于标识线程在操作系统内部的唯一标识符。与 top 命令 id 一致；
-  prio ：线程的 Java 优先级，是一个整数值，取值范围为 1 到 10，数值越大表示优先级越高。
- os_prio：线程的操作系统优先级，是一个整数值，取值范围和操作系统有关，数值越大表示优先级越高。
- **Thread number (#)：**线程的序号，由 **Java 虚拟机分配**的用于标识线程的唯一标识符。
  - Thread.currentThread().getId(); 打印该值；
- Thread ID (tid)：线程的 Java 虚拟机 ID，由 Java 虚拟机分配的用于标识线程的唯一标识符。

###### 第二行

-  **线程状态：**java.lang.Thread.State: WAITING (parking) ；

###### 最后一行

- **持有锁信息：**没有持有任何锁；

##### 02：负载过高定位过程

1. 查看GC 情况（频率，耗时）；
2. 查看占用CPU 最大的进程；
   - top
3. 查看占用 CPU 最大的线程；
   - top -Hp pid 
4. 查看线程堆栈；
   - printf "%x\n" tid; 输出 tid （线程）的 16 进制
   - jstack pid | grep tid(16进制)
5. 根据日志，查看线程打印日志时间间隔或者卡在哪个方法处；
