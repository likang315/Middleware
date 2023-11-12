### 线程池

------

[TOC]

##### 01：线程池

- 降低资源消耗，**提高响应速度**，提高线程可管理性；
- **重复利用已经创建的线程，减少创建线程和销毁线程的开销**，不需要等到线程创建就能立即执行；
- 使用线程池可以对线程**统一分配，调优和监控**；
- 当我们的应用需要创建大量线程或者发现线程会频繁的创建和销毁时就应当考虑使用线程池来维护线程；

##### 02：线程池原理

![](https://github.com/likang315/Middleware/blob/master/%E5%A4%9A%E7%BA%BF%E7%A8%8B/%E5%A4%9A%E7%BA%BF%E7%A8%8B/%E7%BA%BF%E7%A8%8B%E6%B1%A0.png?raw=true)

1. 向线程池提交任务后，处理任务流程;
2. 判断核心线程池（corePoolSize：核心线程数）是否都处于工作状态，若没有，则拿一个已有的线程执行任务，若是，则进入下一个流程；
3. 查看阻塞队列（BlockingQueue）是否已满，未满的话，将任务存储在队列里，若满的话，进入下一个流程；
4. 查看线程池（maximumPoolSize：最大线程数量）中的线程数量是否达到最大线程数，若没有，创建线程执行任务，若线程池满了，则按照某种拒绝策略（handler）处理无法执行的任务；

##### 03：ThreadPoolExcutor

- ThreadPoolExcutor 执行excute( ) 示意图
- 在步骤一、三创建线程都会要求**获取全局锁**，要尽可能的避免获取全局锁

![](https://github.com/likang315/Middleware/blob/master/%E5%A4%9A%E7%BA%BF%E7%A8%8B/%E5%A4%9A%E7%BA%BF%E7%A8%8B/ThreadPoolExcutor.png?raw=true)

##### 04：创建线程池的参数

- ###### 创建线程池【推荐的方式】,七大参数

  ```java
  public new ThreadPoolExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler)
  ```

- ###### corePoolSize

  - 核心线程池的大小
  - 构建线程池后，并不会立即创建线程，当执行任务时，如果当前线程数如果小于corePoolSize，则创建一个线程，当前线程数等于corePoolSize，会将任务放入队列中；

- ###### workQueue：阻塞队列（4种，属于阻塞队列）

  - ArrayBlockingQueue：
    -  基于数组结构的有界阻塞队列，按FIFO排序任务
  - LinkedBlockingQuene：
    - 基于链表结构的有界阻塞队列，按FIFO排序任务，**吞吐量高于ArrayBlockingQuene**；
    - ExecutorService newFixedThreadPool( ) 使用此队列
      - Executors.newFixedThreadpool（固定线程数）:禁用 
    - ThreadPoolTaskExecutor 默认也是此队列；
      - LinkedBlockingQueue时有一个大小限制，其默认为Integer.MAX_VALUE；
      - LinkedBlockingQueue不接受null值，当添加null的时候，会直接抛出NullPointerException；
  - SynchronousQuene：
    - 没有容量的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQuene
    - ExecutorService newCachedThreadPool( )  使用此队列
  - priorityBlockingQuene：
    - 具有优先级的无界阻塞队列

- ###### maximumPoolSize：

  - 线程池最大数量
  - 如果任务队列已满时，并且创建的线程数小于最大线程数，则会创建线程执行任务；
  - 若使用无界队列则此参数没有意义

- ###### keepAliveTime：

  - 线程存活时间，工作线程空闲后，保持存活的时间；
  - 默认情况下，如果当前线程数大于corePoolSize，并且存在线程如果没有任务执行，当空闲的时间大于keepAliveTime时，会终止该线程，直到线程数不超过corePoolSize；

- ###### TimeUnit：

  - 线程活动保持时间的单位
  - 毫秒：MILLISECONDS
  - 微妙：MICROSECONDS
  - 纳秒：NANOSECONDS

- ###### rejectedExecutionHandler：拒绝策略（4种，类）

  - AbortPolicy：直接抛出异常，默认策略；
  - CallerRunsPolicy：只用调用者所在的线程来执行任务；
  - DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务；
  - DiscardPolicy：不处理，丢弃掉；
  
- ###### ThreadFactory：

  - 用于设置创建线程的工厂，通过线程工厂可以使创建的线程具有意义的名字；

  ```java
  new BasicThreadFactory.Builder().namingPattern(ip + "-" + threadName + "-%d").build()
  ```

##### 05：操作线程池

- execute( )：
  - 用于提交**不需要返回值**的任务，所以无法判断是否被线程池执行成功
  - 输入的任务一个 Runnable 的实例
  
- submit( ):
  - 提交**需要有返回值**的任务，线程池会返回一个 Future 对象，通过Future 可以知道线程是否执行成功
  - Future 的get( ) 方法：阻塞当前线程直到任务完成
    
    - get（long timeOut，TimeUnit unit）：阻塞timeout时间，返回，任务可能为执行完成，但是超时了；
    
    - ```java
      Future<Object> future = excutor.submit(task);
      try {
          Object obj = future.get();
      } catch (InterruptedException e) {
          // 处理中断异常
      } catch (ExceptionException e) {
          // 处理无法执行的任务异常
      } finally {
          // 关闭线程池
          excutor.shutdown();
      }
      ```
  
- **shutdown( )：**
  
  - 将线程池的状态设置为SHUTDOW，然后遍历工作线程，调用interupt（），**中断没有正在执行任务的线程，若有现成正在执行任务，等待任务执行完毕**；
  
- **shutdownNow( )：**
  
  - 将线程池的状态设置为STOP，**中断所有工作线程**，不区分是否正在执行任务
  
- boolean **awaitTermination**(long timeout, TimeUnit unit)

  - 阻塞等待timeout时间，判断线程池是否 TERMINATED 状态;

- isShutdown( )：
  
  - 只要调用关闭方法中任意一个就会返回ture，当所有任务执行完成后，才表示线程池关闭成功
  
- **isTerminaed( ):**
  
  - 线程池关闭成功，才会返回True

##### 07：合理的配置线程池

1. **CPU密集型任务**应配置尽可能少的线程，如配置 **N cpu + 1** 个线程的线程池；
2. 由于**IO密集型任务**线程并不是一直在执行任务，则应配置尽可能多的线程，如 **2 * N cpu**；
3. 依赖数据库连接池的任务，因为线程提交SQL后需要等待数据库返回结果，等待的时间越长，则CPU空闲时间就越长，那么线程数应该设置得越大，这样才能更好地利用CPU；
4. 建议使用有界队列，可能会撑爆内存；

##### 08：线程池的监控

- 必需对线程池进行监控，方便在出现问题时，可以根据线程池的使用状况快速定位问题。可以通过线程池提供的参数进行监控；
- taskCount：线程池需要执行的任务数量。
- completedTaskCount：线程池在运行过程中**已完成的任务数量**，小于或等于taskCount；
- **largestPoolSize**：线程池里曾经创建过的最大线程数量。通过这个数据**可以知道线程池是否曾经满过**；
- getPoolSize：线程池的线程数量。如果线程池不销毁的话，线程池里的线程不会自动销毁；

##### 09：如何优雅的关闭线程池

- **钩子函数**：父类定义的空实现的方法，子类通过实现这些方法，在程序运行的声明周期中的某个阶段来回调这些方法，实现我们自定义的功能（操作系统的概念）。

- java.lang.Shutdown：控制JVM正常关闭的类；

  - 最多只能注册10 个钩子；

- ```java
  // 初始化钩子
  @PostConstruct
  private void addHook() {
      Runtime.getRuntime().addShutdownHook(new Thread(this::closeRegisterThreadPool));
  
  }
  private void closeRegisterThreadPool() {
      ThreadPoolMonitor.BUSINESS_THREAD_POOL.forEachEntry(1,
          entry -> close(entry.getKey(), entry.getValue()));
  }
  	/**
       * 优雅的关闭线程池
       * 注意：关闭线程过程中，该线程可能被中断
       *
       * @param threadName
       * @param pool
       */
  private void close(String threadName, ExecutorService pool) {
      log.info("CustomThreadPool_close: start to shutdown the theadPool: {}", threadName);
      pool.shutdown();
      try {
          if (!pool.awaitTermination(NumberConstants.ONE, TimeUnit.SECONDS)) {
              log.warn("CustomThreadPool_close: interrupt the worker for {}, which may cause some task inconsistent!", threadName);
              pool.shutdownNow();
              if (!pool.awaitTermination(NumberConstants.FIFTY, TimeUnit.SECONDS)) {
                  log.error("CustomThreadPool_close: {} pool can't be shutdown even with interrupting worker threads," +
                            " which may cause some task inconsistent", threadName);
              }
          }
      } catch (InterruptedException ie) {
          pool.shutdownNow();
          log.error("CustomThreadPool_close: the current server thread is interrupted" + " when it is trying to stop the worker threads of {}", pool);
          // 保留中断位
          Thread.currentThread().interrupt();
      }
      log.info("CustomThreadPool_close: the threadPool of {} is successful closed!!!", threadName);
  }
  ```

##### 10：ThreadPoolExecutor

```java
public class ThreadPoolExecutor extends AbstractExecutorService {
    // 通过一个整型变量保存状态和workerCount（有效线程数）
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    // 前3位表示状态，后29为表示线程数
    private static final int COUNT_BITS = Integer.SIZE - 3;
    // 线程池容量大小为 1 << 29 - 1 = 00011111111111111111111111111111
    private static final int COUNT_MASK = (1 << COUNT_BITS) - 1;

    // 线程池的5中状态
    // RUNNING状态 -1 << 29 = 11111111111111111111111111111111 << 29 = 11100000000000000000000000000000(前3位为111 是补码)
    private static final int RUNNING    = -1 << COUNT_BITS;
    // SHUTDOWN状态 0 << 29 = 00000000000000000000000000000000 << 29 = 00000000000000000000000000000000(前3位为000)
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;

    // 得到状态，COUNT_MASK 非操作，然后做在一个与操作，相当于直接取前3位的的值
    private static int runStateOf(int c)     { return c & ~COUNT_MASK; }
    // 得到线程数，也就是后29位的数字。 直接跟CAPACITY做一个与操作
    private static int workerCountOf(int c)  { return c & COUNT_MASK; }
    // 或操作。相当于更新数量和状态两个操作
    private static int ctlOf(int rs, int wc) { return rs | wc; }
    
    /**
     * Set containing all worker threads in pool. Accessed only when
     * holding mainLock.
     */
    private final HashSet<Worker> workers = new HashSet<>();
    
    // 是否允许核心线程超时关闭
    private volatile boolean allowCoreThreadTimeOut;

    
    // 工作者线程（有效线程数）：Worker类除了实现了Runnable，也继承了AQS，本身也是一把锁
    private final class Worker
        extends AbstractQueuedSynchronizer
        implements Runnable {
        private static final long serialVersionUID = 6138294804551838833L;

        /** Thread this worker is running in.  Null if factory fails. */
        final Thread thread;
        /** Initial task to run.  Possibly null. */
        Runnable firstTask;
        // 每个Worker完成的任务数
        volatile long completedTasks;

        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        /** Delegates main run loop to outer runWorker. */
        public void run() {
            runWorker(this);
        }

        // The value 0 represents the unlocked state.
        // The value 1 represents the locked state.
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        // tryAcquire()尝试将AQS的state从0-->1，返回true代表上锁成功，并设置当前线程为锁的拥有者， 可以看到compareAndSetState(0, 1)只尝试了一次获取锁，且不是每次state+1，而是0-->1，说明锁不是可重入的
        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }
    
    // 设置超时关闭
    public void allowCoreThreadTimeOut(boolean value) {
        // 核心线程存活时间必须大于0，一旦开启，keepAliveTime 也必须大于0
        if (value && keepAliveTime <= 0)
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            // 开启后，清理所有的超时空闲线程，包括核心线程
            if (value)
                interruptIdleWorkers();
        }
    }
    
    // 闲置worker: 如果getTask返回的是null，那说明阻塞队列已经没有任务并且当前调用getTask的Worker需要被回收
    private Runnable getTask() {
        boolean timedOut = false;
        for (;;) {
            int c = ctl.get();
            // Check if queue empty or threadpoll status 
            if (runStateAtLeast(c, SHUTDOWN)
                && (runStateAtLeast(c, STOP) || workQueue.isEmpty())) {
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);
            // 从队列获取任务的超时时间
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }
            try {
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }
    
    // worker 执行任务
    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        // 当前正在执行的Worker数量比corePoolSize要小。创建一个新的Worker执行任务，会调用addWorker
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        // 如果当前正在执行的Worker数量大于等于corePoolSize。将任务放到阻塞队列里，如果阻塞队列没满并且状态是RUNNING的话，直接丢到阻塞队列，否则,丢到阻塞队列之后，重新检查(丢到阻塞队列之后可能另外一个线程关闭了线程池或者刚刚加入到队列的线程死了)。如果这个时候线程池不在RUNNING状态，把刚刚丢入队列的任务remove掉，调用reject方法，否则查看Worker数量，如果Worker数量为0，起一个新的Worker去阻塞队列里拿任务执行
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
		// 丢到阻塞失败的话，会调用addWorker方法尝试起一个新的Worker去阻塞队列拿任务并执行任务，如果这个新的Worker创建失败，调用reject方法
        else if (!addWorker(command, false))
            reject(command);
    }
    
    // firstTask表示需要跑的任务，core参数为true表示使用线程池的基本大小，为false使用线程池最大大小
	// 返回值是boolean类型，true表示新任务被接收并且执行了。否则是false
    private boolean addWorker(Runnable firstTask, boolean core) {

    }
    
    // shutDown() 会关闭线程池
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            // 1. 线程池还在运行，不能终止
            // 2. 线程池处于TIDYING或TERMINATED状态，说明已经在关闭了，不允许继续处理
            // 3. 线程池处于SHUTDOWN状态并且阻塞队列不为空，这时候还需要处理阻塞队列的任务
            if (isRunning(c) ||
                runStateAtLeast(c, TIDYING) ||
                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            // 走到这一步说明线程池已经不在运行，阻塞队列已经没有任务，但是还要回收正在工作的Worker
            if (workerCountOf(c) != 0) {
                // 中断闲置Worker
                interruptIdleWorkers(ONLY_ONE); 
                return;
            }
            // worker已经全部回收了，并且线程池已经不在运行，阻塞队列已经没有任务。可以准备结束线程池了
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // cas操作，将线程池状态改成TIDYING
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated();
                    } finally {
                         // terminated方法调用完毕之后，状态变为TERMINATED
                        ctl.set(ctlOf(TERMINATED, 0));
                        // 通知前面阻塞在awaitTerminatiocn的所有调用者线程
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
        }
    }

    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        // 关闭的时候需要加锁，防止并发
        mainLock.lock();
        try {
            // 检查调用线程是否有权限关闭线程池
            checkShutdownAccess();
            // 把线程池状态更新到SHUTDOWN
            advanceRunState(SHUTDOWN);
            // 中断闲置的Worker
            interruptIdleWorkers();
            // 钩子方法，默认不处理
            onShutdown();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }
    
    // 重载方法，参数false，表示要中断所有的正在运行的闲置Worker，如果为true表示只打断一个闲置Worker
    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    // 先tryLock()获取worker锁，正在运行的worker都是获取锁的，因为worker.tryLock()失败，且锁是不可重入的，只能获取到worker锁的空闲线程发送中断信号
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        // 中断闲置Worker需要加锁，防止并发
        mainLock.lock(); 
        try {
            for (Worker w : workers) { 
                // 拿到worker中的线程
                Thread t = w.thread;
                // Worker中的线程没有被打断并且Worker可以获取锁
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                // 如果只打断1个Worker的话，直接break退出，否则，遍历所有的Worker
                if (onlyOne) 
                    break;
            }
        } finally {
            mainLock.unlock();
        }    
    }
    
    // 中断所有运行的worker
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            // 把线程池状态更新到STOP
            advanceRunState(STOP);
            // 中断所有运行的Worker
            interruptWorkers();
            // 返回丢弃的任务
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }
    
    private void interruptWorkers() {
        for (Worker w : workers)
            w.interruptIfStarted();
    }
    
    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<>();
        // 移除队列中所有的任务，并且添加到被给的集合中
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }
}
```
