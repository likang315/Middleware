## Arthas

------

##### 01：概述

​	Arthas 是一款线上监控诊断产品，通过全局视角实时查看应用 load、内存、gc、线程的状态信息，并能在**不修改应用代码的情况下，对业务问题进行诊断，包括查看方法调用的出入参、异常，监测方法执行耗时**，类加载信息等，大大提升线上问题排查效率。

##### 02：Demo

- ```java
  // 下载 Arthas
  curl -O https://arthas.aliyun.com/arthas-boot.jar
  java -jar arthas-boot.jar
    
  // 输入进程序号，attach 目标进程
  // 输入dashboard，按回车/enter，会展示当前进程的信息，按ctrl+c可以中断执行。
    dashboard
  // 退出 arthas
    exit
  ```

##### 03：常见命令

- ###### dashboard

  - 查询 Java 线程、JVM、GC 情况；
  - ID: Java 级别的线程 ID，注意这个 ID 不能跟 jstack 中的 nativeID 一一对应。

- ###### thread

  - 查看当前线程信息，查看线程的堆栈
  - 参数
    - *id*：线程 id
    - [n:]  ：指定最忙的前 N 个线程并打印堆栈,占位符作用
      - **n**：代表一个数值变量（如整数）
      - **：**常用于分隔参数名称与值的类型说明，例如 -n:10
      - **[ ]** ：表示参数是可选的，非必填项
    - [b] ：找出**当前阻塞其他线程的线程**，发现应用卡住了， 通常是由于某个线程拿住了某个锁， 并且其他线程都在等待这把锁造成。
      - 目前只支持找出 synchronized 关键字阻塞住的线程， 如果是`java.util.concurrent.Lock`， 目前还不支持。
    - [--all] ：显示所有匹配的线程
    - --state：查看指定状态的线程
      - thread --state WAITING

- ###### jvm

  - 查看当前 JVM 信息，JVM 参数、GC 次数、线程量；

- ###### memory

  - 查看 JVM 内存信息；

- ###### monitor

  - 方法执行监控，cnt、rt;

- ###### stack

  - 输出当前方法被调用的调用路径;
  - 示例：stack 类路径名 方法名

- ###### trace

  - 方法内部调用路径，并输出方法路径上的每个节点上耗时
  - 参数
    - *class-pattern*：类名表达式匹配
    - *method-pattern*：方法名表达式匹配
    - `#cost`：方法执行耗时
  - 示例：`trace *StringUtils isBlank '#cost>100'：表示当执行时间超过 100ms 的时候，才会输出 trace 的结果

- ###### jad

  - 反编译指定已加载类的源码
  - 参数
    - *class-pattern*：类名表达式匹配
    - method-pattern： 方法名
    - --source-only：可以只打印源代码，不展示类加载器；
  - 示例
    - jad --source-only com.xupt.Demo main
      - 反编译某个函数

- ###### watch 【重要】

  - 指定函数的调用情况。能观察到的范围为：**入参、返回值、抛出异常**，通过编写 OGNL 表达式进行对应变量的查看。
  - 参数
    - class-pattern：类名表达式匹配
    - method-pattern：函数名表达式匹配
    - **express：观察表达式，默认值：{params, target, returnObj}**
    - condition-express：条件表达式
    - [b]：在函数调用之前观察
    - [e]：在函数异常之后观察
    - [s]：在函数返回之后观察
    - [f]：**默认值，在函数结束之后(正常返回和异常返回)观察**
    - [E]：开启正则表达式匹配，默认为通配符匹配
    - [x:]：**指定输出结果的属性遍历深度，默认为 1，最大值是 4**
    - [m <arg>]：指定 Class 最大匹配数量，默认值为 50。长格式为[maxMatch <arg>]。
  - 返回结构：`location`有三种可能值：
    - `AtEnter`，`AtExit`，`AtExceptionExit`。对应函数入口，函数正常 return，函数抛出异常；
  - 示例
    - watch demo.MathGame primeFactors "{params,target,returnObj}" -x 3 -b -s
      - 同时观察函数调用前和函数返回后
    - watch demo.MathGame primeFactors "{params[0],throwExp}" -e -x 2
      - 观察异常信息

- ###### `profiler` 【重要】

  - 支持**生成应用热点的火焰图**。本质上是通过不断的采样，然后把收集到的采样结果生成火焰图。
  - 参数
    - action：要执行的操作
    - actionArg：属性名模式
    - [i:]：采样间隔（单位：ns）（默认值：10'000'000，即 10 ms）
    - [f:]：将输出转储到指定路径
    - [d:]：运行评测指定秒
    - [e:]：要跟踪哪个事件（cpu, alloc, lock, cache-misses 等），默认是 cpu
  - 支持的 event
    - `cpu`：分析代码 CPU 热点。
    - `alloc`：追踪内存分配情况，优化 GC 压力。
    - `lock`：检测线程锁争用导致的性能瓶颈。
    - `wall`：适用于多线程应用，统计线程实际运行时间。
  - 示例
    - profiler start
      - 启动 profiler，默认情况下，生成的是 cpu 的火焰图，即 event 为`cpu，可以用`--event参数指定其他性能分析模式。
    - profiler status
      - 查看当前 profiler 在采样哪种`event`和采样时间
    - profiler stop
      - 停止 profiler，默认情况下，结果是Flame Graph 格式的 `html` 文件。自动输出到指定目录。
    - profiler start -t
      - 使用 `-t` 或 `--threads` 标志选项令 profiling 对各线程分别进行，每个栈轨迹都会以指示单个线程的帧结束

##### 04：其他特性

###### 后台异步执行任务【仿 Linux 命令】

- 使用&在后台执行任务

  - ```
    trace Test t &
    ```

- 任务输出重定向

  - ```
    trace Test t >> test.out &
    ```

- 通过 jobs 查询当前任务

  - ```shell
    $ jobs
    [10]*
           Stopped           watch com.taobao.Test test "params[0].{? #this.name == null }" -x 2
           execution count : 19
           start time      : Fri Sep 22 09:59:55 CST 2017
           timeout date    : Sat Sep 23 09:59:55 CST 2017
           session         : 3648e874-5e69-473f-9eed-7f89660b079b (current)
    ```

    - job id 是 10, `*` 表示此 job 是当前 session 创建
    - 状态是 Stopped
    - execution count 是执行次数，从启动开始已经执行了 19 次
    - timeout date 是超时的时间，到这个时间，任务将会自动超时退出

- fg 将命令转到前台继续执行

  - ```
    fg job-id
    ```

- 停止异步任务执行

  - ```
    kill job-id
    ```

###### 将命令的结果完整保存在日志文件中

- ```bash
  options save-result true
  ```

- 开启该功能后，结果会异步保存在：`{user.home}/logs/arthas-cache/result.log`，请定期进行清理，以免占据磁盘空间

###### arthas.properties 文件

- 文件在 arthas 的目录下

- 如果是自动下载的 arthas，则目录在`~/.arthas/lib/3.x.x/arthas/`下面

- 如果是下载的完整包，在 arthas 解压目录下

  - ```
    #arthas.config.overrideAll=true
    arthas.telnetPort=3658
    arthas.httpPort=8563
    arthas.ip=127.0.0.1
    # seconds
    arthas.sessionTimeout=1800
    ```

- 如果配置 `arthas.telnetPort`为 -1 ，则不 listen telnet 端口**。`arthas.httpPort`类似。

- 如果配置 `arthas.telnetPort`为 0 ，则随机 telnet 端口**，在`~/logs/arthas/arthas.log`里可以找到具体端口日志。`arthas.httpPort`类似。

###### Arthas 日志文件

- 日志文件路径： `~/logs/arthas/arthas.log`

