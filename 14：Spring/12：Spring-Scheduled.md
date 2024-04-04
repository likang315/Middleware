### Spring Schedule 源码剖析

------

[TOC]

##### 01：概述

- 原生的`Scheduling`模块只是内存态的调度模块，不支持**任务的持久化或者配置**（配置任务通过`@Scheduled`注解进行硬编码，不能抽离到类之外）。

##### 02：Scheduling 模块

- `Scheduling`模块是`spring-context`依赖下的一个包`org.springframework.scheduling`
  - 顶层包的定义了一些通用接口和异常。
  - `org.springframework.scheduling.annotation`：定义了调度、异步任务相关的注解和解析类，常用的注解如`@Async`、`@EnableAsync`、`@EnableScheduling`和`@Scheduled`。
  - `org.springframework.scheduling.concurrent`：定义了调度任务执行器和相对应的`FactoryBean`。
  - `org.springframework.scheduling.config`：定义了配置解析、任务具体实现类、调度任务`XML`配置文件解析相关的解析类。
  - `org.springframework.scheduling.support`：定义了反射支持类、`Cron`表达式解析器等工具类。

##### 03：Scheduling 模块的工作流程

1. 通过注解`@EnableScheduling`中的`@Import`引入了`SchedulingConfiguration`，而`SchedulingConfiguration`中配置了一个类型为`ScheduledAnnotationBeanPostProcessor`名称为`org.springframework.context.annotation.internalScheduledAnnotationProcessor`的`Bean`，这里有个常见的技巧，`Spring`内部加载的`Bean`一般会定义名称为`internalXXX`，`Bean`的`role`会定义为`ROLE_INFRASTRUCTURE = 2`。
2. `Bean`后置处理器`ScheduledAnnotationBeanPostProcessor`会解析和处理每一个符合特定类型的`Bean`中的`@Scheduled`注解（注意`@Scheduled`只能使用在方法或者注解上），并且把解析完成的方法封装为不同类型的`Task`实例，缓存在`ScheduledTaskRegistrar`中的。
3. `ScheduledAnnotationBeanPostProcessor`中的钩子接口方法`afterSingletonsInstantiated()`在所有单例初始化完成之后回调触发，在此方法中设置了`ScheduledTaskRegistrar`中的任务调度器（`TaskScheduler`或者`ScheduledExecutorService`类型）实例，并且调用`ScheduledTaskRegistrar#afterPropertiesSet()`方法添加所有缓存的`Task`实例到任务调度器中执行。

##### 04：任务调度器【重要】

- `Scheduling`模块支持`TaskScheduler`或者`ScheduledExecutorService`类型的任务调度器，而`ScheduledExecutorService`其实是`JDK`并发包`java.util.concurrent`的接口，一般实现类就是调度线程池`ScheduledThreadPoolExecutor`。
- 实际上，`ScheduledExecutorService`类型的实例最终会通过**适配器模式**转变为`ConcurrentTaskScheduler`，所以这里只需要分析`TaskScheduler`类型的执行器。

###### ScheduledThreadPoolExecutor 详解

1. 当调用ScheduledThreadPoolExecutor的scheduleAtFixedRate()方法或者scheduleWithFixedDelay()方法时，会向ScheduledThreadPoolExecutor的DelayQueue添加一个实现了RunnableScheduledFuture接口的ScheduledFutureTask；
2. 线程池中的线程从**DelayQueue**中获取ScheduledFutureTask，然后执行任务；

###### 原理

- 定时任务先执行 corn，**计算出任务的执行时间，放入延迟队列中**，假如队列中有多个定时任务，**按照延迟时间最小堆排序，把延迟是时间最小的放到队列的头部**，有一个工作者线程轮询获取任务（持有时间器），判断 delayTime 是否为0 ，若是执行，否则丢弃任务，继续等待；
- @Scheduled 的定时任务执行机制；

###### org.springframework.scheduling.TaskScheduler（顶层接口）

- ```java
  public interface TaskScheduler {
       // 调度一个任务，通过触发器实例指定触发时间周期
       ScheduledFuture<?> schedule(Runnable task, Trigger trigger);
       // 指定起始时间调度一个任务 - 单次执行
       ScheduledFuture<?> schedule(Runnable task, Date startTime);
       // 指定固定频率调度一个任务，period的单位是毫秒
       ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period);
      
       // 指定起始时间和固定频率调度一个任务，period的单位是毫秒
       ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period);
       // 指定固定延迟间隔调度一个任务，delay的单位是毫秒
       ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay);
      // 指定起始时间和固定延迟间隔调度一个任务，delay的单位是毫秒
       ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay);
  }
  ```

##### 05：Task 任务的分类

- `Scheduling`模块中支持不同类型的任务，主要包括下面的3种（解析的优先顺序也是如下）：
  1. **`Cron`表达式任务**，支持通过`Cron`表达式配置执行的周期，对应的任务类型为`org.springframework.scheduling.config.CronTask`。
  2. **固定延迟间隔任务**，也就是上一轮执行完毕后间隔固定周期再执行本轮，依次类推，对应的的任务类型为`org.springframework.scheduling.config.FixedDelayTask`。
  3. **固定频率任务，**基于固定的间隔时间执行，**不会理会上一轮是否执行完毕本轮会照样执行**，对应的的任务类型为`org.springframework.scheduling.config.FixedRateTask`。

###### CronTask

- `CronTask`是通过`cron`表达式指定执行周期的，并且**不支持延迟执行**，可以使用特殊字符`-`禁用任务执行：

- ```java
  // 注解声明式使用 - 每五秒执行一次，不支持initialDelay
  @Scheduled(cron = "*/5 * * * * ?")
  public void processTask(){
  
  }
  
  // 注解声明式使用 - 禁止任务执行
  @Scheduled(cron = "-")
  public void processTask(){
  
  }
  
  // 编程式使用
  public class Tasks {
  
      static DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
      public static void main(String[] args) throws Exception {
          ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
          taskScheduler.setPoolSize(10);
          taskScheduler.initialize();
          CronTask cronTask = new CronTask(() -> {
              System.out.println(String.format("[%s] - CronTask触发...", F.format(LocalDateTime.now())));
          }, "*/5 * * * * ?");
          taskScheduler.schedule(cronTask.getRunnable(),cronTask.getTrigger());
          Thread.sleep(Integer.MAX_VALUE);
      }
  }
  // 某次执行输出结果
  [2020-03-16 01:07:00] - CronTask触发...
  [2020-03-16 01:07:05] - CronTask触发...
  ......
  ```

###### FixedDelayTask

- `FixedDelayTask`需要配置延迟间隔值（`fixedDelay`或者`fixedDelayString`）和可选的起始延迟执行时间（`initialDelay`或者`initialDelayString`），这里注意一点是`fixedDelayString`和`initialDelayString`都支持从`EmbeddedValueResolver`（简单理解为配置文件的属性处理器）读取和`Duration`（例如`P2D`就是`parses as 2 days`，表示86400秒）支持格式的解析：

- ```java
  // 注解声明式使用 - 延迟一秒开始执行，延迟间隔为5秒
  @Scheduled(fixedDelay = 5000, initialDelay = 1000)
  public void process(){
          
  }
  
  // 注解声明式使用 - spring-boot配置文件中process.task.fixedDelay=5000  process.task.initialDelay=1000
  @Scheduled(fixedDelayString = "${process.task.fixedDelay}", initialDelayString = "${process.task.initialDelay}")
  public void process(){
          
  }
  
  // 编程式使用
  public class Tasks {
  
      static DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
      public static void main(String[] args) throws Exception {
          ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
          taskScheduler.setPoolSize(10);
          taskScheduler.initialize();
          FixedDelayTask fixedDelayTask = new FixedDelayTask(() -> {
              System.out.println(String.format("[%s] - FixedDelayTask触发...", F.format(LocalDateTime.now())));
          }, 5000, 1000);
          Date startTime = new Date(System.currentTimeMillis() + fixedDelayTask.getInitialDelay());
          taskScheduler.scheduleWithFixedDelay(fixedDelayTask.getRunnable(), startTime, fixedDelayTask.getInterval());
          Thread.sleep(Integer.MAX_VALUE);
      }
  }
  // 某次执行输出结果
  [2020-03-16 01:06:12] - FixedDelayTask触发...
  [2020-03-16 01:06:17] - FixedDelayTask触发...
  ......
  ```

###### FixedRateTask

- `FixedRateTask`需要配置固定间隔值（`fixedRate`或者`fixedRateString`）和可选的起始延迟执行时间（`initialDelay`或者`initialDelayString`），这里注意一点是`fixedRateString`和`initialDelayString`都支持从`EmbeddedValueResolver`（简单理解为配置文件的属性处理器）读取和`Duration`（例如`P2D`就是`parses as 2 days`，表示86400秒）支持格式的解析：

- ```java
  // 注解声明式使用 - 延迟一秒开始执行，每隔5秒执行一次
  @Scheduled(fixedRate = 5000, initialDelay = 1000)
  public void processTask() {
  
  }
  
  // 注解声明式使用 - spring-boot配置文件中process.task.fixedRate=5000  process.task.initialDelay=1000
  @Scheduled(fixedRateString = "${process.task.fixedRate}", initialDelayString = "${process.task.initialDelay}")
  public void process(){
          
  }
  ```

##### 06：Task 的调度

- 如果没有配置`TaskScheduler`或者`ScheduledExecutorService`类型的`Bean`，那么调度模块**只会创建一个线程**去调度所有装载完毕的任务，如果任务比较多，很有可能会造成大量任务饥饿，表现为**存在部分任务不会触发调度的场景**（这个是调度模块生产中经常遇到的故障，需要重点排查是否没有设置`TaskScheduler`或者`ScheduledExecutorService`）。

###### 并发执行

```java
@EnableScheduling
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    /**
     * 使用JUC 提供的定时任务线程池，bean 销毁时自动关闭线程池
     *
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService taskExecutor() {
        String ip = NetworkUtil.queryIpAddress();
        return new ScheduledThreadPoolExecutor(5, new BasicThreadFactory.Builder().daemon(true)
                .namingPattern(ip + "--" + "scheduled" + "-%d").build());
    }

}
```

##### 07：源码分析（ScheduledAnnotationBeanPostProcessor）

- BeanPostProcessor：`Bean`实例初始化前后分别回调，其中，后回调的`postProcessAfterInitialization()`方法就是用于**解析**`@Scheduled`和**装载**`ScheduledTask`。
- DestructionAwareBeanPostanProcessor：具体的`Bean`实例销毁的时候回调，用于`Bean`实例销毁的时候**移除和取消对应的任务**实例。
- DisposableBean接口：当前`Bean`实例销毁时候回调，也就是`ScheduledAnnotationBeanPostProcessor`自身被销毁的时候回调，用于**取消和清理**所有的`ScheduledTask`。





