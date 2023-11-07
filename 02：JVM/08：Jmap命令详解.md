### Jmap

------

[TOC]

##### 01：概述

- 是JDK自带的工具软件。它可以**生成 java 程序的 dump 文件**， 也可以**查看堆内对象示例的统计信息**、查看 ClassLoader 的信息以及 **finalizer 队列**。


##### 02：Jmap 使用方式

- ```
  jmap [option] <pid>
          (to connect to running process)
  jmap [option] <executable <core>
          (to connect to a core file)
  ```
  
- **option：** 选项参数。

  - **no option：** 查看进程的内存映像信息；
  - **heap：** 显示Java堆详细信息；
  - **histo:live：** 显示堆中对象的统计信息；
  - **clstats：**打印类加载器信息；
  - **finalizerinfo：** 显示在 F-Queue 队列等待 Finalizer 线程执行 finalizer 方法的对象；
  - **`dump:<dump-options>`: 生成堆转储快照**；
  - **F：** 当 -dump 没有响应时，使用 -dump 或者 -histo 参数，在这个模式下，live子参数无效；
  - **help：**打印帮助信息；
  - `J<flag>`：指定传递给运行 jmap 的 JVM 的参数；

- **pid：** 需要打印配置的进程ID；

- **executable：** 产生核心 dump 的Java可执行文件；

- **core：** 需要打印配置信息的核心文件；

- **server-id**：可选的唯一id，如果相同的远程主机上运行了多台调试服务器，用此选项参数标识服务器；

- **remote server IP or hostname**：远程调试服务器的IP地址或主机名；

##### 02：示例

1. jmap pid
   - 查看进程的内存映像信息，需要 root 权限执行；
   - 使用不带选项参数的jmap打印共享对象映射，将会打印目标虚拟机中加载的每个共享对象的起始地址、映射大小以及共享对象文件的路径全称；
2. **jmap -heap pid**
   - 显示 Java 堆详细信息;
   - 打印一个堆的摘要信息，包括使用的GC算法、堆配置信息和各内存区域内存使用信息;
3. jmap -histo:live pid
   - 显示堆中对象的统计信息；
   - 包括每个Java类、对象数量、内存大小(字节)、完全限定的类名。打印的虚拟机内部的类名称将会带有一个’*’前缀。
   - 如果指定了 live 子选项，则只计算活动的对象；
4. jmap -clstats pid
   - 打印类加载器信息；
5. jmap -finalizerinfo pid
   - 打印等待销毁的对象信息；
6. **jmap -dump:format=b,file=heapdump.hprof pid**
   - 生成 Java 进程的堆转储文件（heap dump），以 **hprof 二进制格式转储 Java 堆到指定 filename 的文件**。live子选项是可选的。如果指定了live子选项，堆中只有活动的对象会被转储；
   - 该过程较为耗时，并且执行的过程中为了保证 dump 的信息是可靠的，所以会**暂停应用， 线上系统慎用**；
   - 使用 IDEA 进行分析，找出大对象，定位出内存泄漏；



