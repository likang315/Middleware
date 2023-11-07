### JVM 调优

------

[TOC]

##### 01：概述

​	一般调整**垃圾回收器与最大停顿时间，堆大小和元数据区**；

- -XX:+UseG1GC：开启G1垃圾收集器；
- -XX:MaxGCPauseMillis=200：设置GC的最大暂停时间为200ms；
- -Xmx8g：堆内存的最大内存为 9G；
- -XX:MaxMetaspaceSize=1024m：元数据区为 1000M；

##### 02：JVM 参数格式

1. 标准参数（-），**所有的JVM实现都必须实现**这些参数的功能，而且向后兼容；
2. 非标准参数（-X），**默认 JVM 实现这些参数的功能**，但是并不保证所有JVM实现都满足，且不保证向后兼容；
3. 非 Stable 参数（-XX），此类参数各个 JVM 实现会有所不同，将来可能会随时取消，需要慎重使用；

##### 03：JVM 调参

- catalina.sh

  - Apache Tomcat 的启动脚本之一，用于启动 Tomcat 服务器；

  - 路径：/Tomcat/apache-tomcat-8.5.40/bin/catalina.sh

    - ```shell
      ./catalina.sh start
      ./catalina.sh stop
      ```

- JVM 环境变量（JAVA_OPTS）

  - ```sh
    -Xmx9216m -XX:MaxMetaspaceSize=2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 
    -Xlog:gc*:file=/homw/logs/gc.log:time,level,tags:filecount=5,filesize=10M
    # OOM 是自动 dump 内存文件输出到指定目录
    -XX:+HeapDumpOnOutOfMemoryError
    -XX:HeapDumpPath=$CATALINA_BASE/logs
    ```
    
  - 堆配置

    - -Xms2048m
      - 最小堆（初始化时堆的大小）
    - **-Xmx2048m**
      - 最大堆
    - -XX:NewSize=n
      - 设置年轻代大小
    - -XX:NewRatio=n
      - 设置年轻代和年老代的比值。
      - 如：3，表示年轻代与年老代比值为1：3，年轻代占整个堆内存的1/4；
    - -XX:SurvivorRatio=n
      - 年轻代中Eden区与两个Survivor区的比值，注意Survivor区有两个。
      - 如：3，表示Eden：Survivor=3：2，一个Survivor区占整个年轻代的1/5；
    - -XX:MaxPermSize=n
      - 设置持久代大小，JDK11 已经取消该参数，用元数据区代替；

  - 栈配置

    - -Xss256k
    - 单线程的栈大小，该值会影响到深度调用；

  - 元数据区

    - -XX:MetaspaceSize=256m 
    - **-XX:MaxMetaspaceSize=256m**

  - 垃圾回收信息

    - -verbose:gc：启用 GC 日志输出；
    - -XX:+PrintGCDetails：输出详细的 GC 日志信息；
    - **-Xloggc:/home/appops/logs/gc.log**：gc 日志路径；
    - -XX:+UseGCLogFileRotation：开启 GC 日志文件轮换；
    - -XX:NumberOfGCLogFiles=10：日志文件总数量；
    -  -XX:GCLogFileSize=10M：单个 GC 日志文件的最大大小；

  