### Lock【显示时候锁】

------

[TOC]

##### 01：概述

- 主要介绍 **JUC 中与锁相关的API和组件**，以及这些API和组件的使用方式和实现细节;
  - 如何使用
  - 怎么实现

##### 02：Interface Lock

- 锁是用来**多线程环境下，控制访问共享且可变资源**的方式。一般来说，一个锁能够防止多个线程同时访问共享资源（但是有些锁可以允许多个线程并发的访问共享资源，比如读写锁）。


- 在 **Lock 接口**出现之前，Java程序是靠 synchronized 关键字实现锁功能的，而Java 5之后，并发包中新增了 Lock 接口（以及相关实现类）用来实现锁功能，它提供了与 synchronized 关键字类似的同步功能，只是在使用时需要**显式地获取和释放锁**，显示使用锁，扩展其功能。

```java
Lock lock = new ReentrantLock();
lock.lock();
try {
  // 不要把获取锁的代码放到这，可能会导致获取锁时发生了异常，异常抛出的同时，也会导致锁无故被释放
} finally {
	lock.unlock();
}
```

###### 源码

```java
public interface Lock {
    // 获取锁，如果锁已经被其他线程获取，那么当前线程将被阻塞，直到获取到锁为止
    void	lock();
    // 可中断的获取锁，即在锁获取的过程中可以中断当前线程
    void	lockInterruptibly();
  	// 尝试非阻塞的获取锁，调用该方法后立即返回，如果能获得锁就返回true，不能就立即返回false
  	boolean	tryLock()
    // 超时等待获取锁，如果超过该时间段还没获得锁，返回false
    boolean	tryLock(long time, TimeUnit unit) throws InterruptedException;
    // 释放锁
    void unlock();
    // 获取等待-通知组件，该组件和当前锁绑定，当前线程只有获得了锁，才能调用该组件的wait()，而调用后当前线程将释放锁
    Condition	newCondition();
}
```

##### 03：队列同步器（AQS）

- 队列同步器 AbstractQueuedSynchronizer，是用来**构建锁**或者其他同步组件的基础框架，它使用了一个**volatile int 成员变量表示同步状态**，通过**内置的队列来完成获取资源的线程的排队工作**
- private volatile int state;
  - 获取同步状态的语义是**使用 CAS 更新 status = 1，并且将 AQS 的 exclusiveOwnerThread 设置为当前线程。**


###### AQS 原理

​	同步器的主要使用方式是**继承**，子类通过**继承同步器并实现它的抽象方法来管理同步状态**，在抽象方法的实现过程中免不了要**对同步状态进行更改**，这时就需要使用同步器提供的3个方法（getState()、setState(int newState)和compareAndSetState(int expect, int update)）来进行操作，因为它们能够**保证状态的变更是安全的**；

###### 同步器和锁的关系

​	同步器是锁的一种实现方式，锁是面向用户使用的

###### 同步器允许重写的方法

- protected boolean tryAcquire(int arg)
  - 独占式的获取同步状态，实现该方法需要查询当前状态并判断同步状态是否符合预期，然后在进行CAS设置同步状态
- protected boolean tryRelease(int arg)
  - 独占式的释放同步状态，等待获取同步状态的线程将有机会获得同步状态
- protected boolean tryAcquireShared(int arg)
  - 共享式的获取同步状态，返回大于等于0的值，表示获取成功，反之获取失败
- protected boolean tryReleaseShared(int arg)
  - 共享式的释放同步状态
- protected boolean isHeldExclusively()
  - 表示是否被当前线程锁独占

```java
/**
* 独占锁的获取和释放
*/
class Mutex implements Lock {
    // 静态内部类，自定义同步器
    private static class Sync extends AbstractQueuedSynchronizer {
        // 是否处于占用状态
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }
        // 当状态为0的时候获取锁
        public boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 可重入锁
                int nextc = c + acquires;
                if (nextc < 0) 
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
        // 释放锁，将状态设置为0
        protected boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                // 当前线程释放锁
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
        // 返回一个Condition，每个condition都包含了一个condition队列
        Condition newCondition() { return new ConditionObject(); }
    }
    
    // 仅需要将操作代理到Sync上即可
    private final Sync sync = new Sync();
    public void lock() { 
        // 底层调用的还是重写的tryLock()
        sync.acquire(1);
    }
    public boolean tryLock() { return sync.tryAcquire(1); }
    public void unlock() { sync.release(1); }
    public Condition newCondition() { return sync.newCondition(); }
    public boolean isLocked() { 
        // 是否被当前线程所占用
        return sync.isHeldExclusively();
    }
    public boolean hasQueuedThreads() {
        // 队列中是否有线程等待
        return sync.hasQueuedThreads();
    }
    // 如果当前方法被中断，将会抛出InterruptedException后返回
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }
}
```

##### 04：AQS（队列同步器）源码剖析（ReentrantLock）

###### 独占锁和共享锁

- 独占锁（Exclusive Locks）：如果事务A对数据对象O加上了独占锁，那么在整个加锁期间，只许事务A对O进行读取和更新操作，其他任何事务都不能再对这个数据对象进行任何类型的操作，直到A事务释放了独占锁。
  - **保证当前有且仅有一个事务获得锁**，并且锁被释放后，所有正在等待获取锁的事务都能够被通知到。
- 共享锁（Shared Locks）：如果事务A对数据对象O，加上了共享锁，那么当前事务只能对O进行读取操作，其他事务也只
  能对这个数据对象加共享锁，直到该数据对象上的所有共享锁都被释放。
  - **不同的事务都可以同时对同一个数据对象进行读操作，而不能进行写操作**；

###### 同步队列

- 同步器依赖内部的**同步队列（一个FIFO双向队列）来完成同步状态的管理**，当前线程获取同步状态失败时，同步器会将当前线程以及等待状态等信息构造成为一个节点（Node）并将其**加入同步队列进行自旋（判断该节点的前驱节点是否是头结点，若是则获取同步状态，若不是，则阻塞，超时和中断都可以唤醒该线程）**，当同步状态释放时，首节点会唤醒后面的结点，使其再次尝试获取同步状态。
- 代码实现：java.util.concurrent.locks.ReentrantLock#lock 

###### Node节点

​	同步队列中的节点（Node），用来保存**获取同步状态失败的线程引用**、**等待状态**以及**前驱和后继节点，节点的属性类型**与名称以及描述

- int waitStatus：等待状态
  - static final int CANCELLED =  1，由于在队列中等待线程**等待超时或者被中断**，需要从队列中取消等待，之后节点进入该状态将不会变化
  - static final int SIGNAL  = -1，当前节点的线程如果释放了同步状态或者被取消，将会通知后继节点，使得后继节点得以继续运行
  - static final int CONDITION = -2，该节点在等待队列中，当其他线程调用signal（）后，该节点将会加入到对同步状态的获取中
  - static final int PROPAGATE = -3，表示下一次共享式同步状态获取将会无条件的传播下去
- Node prev：前驱节点
- Node next：后继节点
- Node nextWaiter：等待队列中的后继节点，如果当前节点是共享的，字段值为SHARED常量
- Thread thread：获取同步状态的线程

###### 同步队列的结构

​	同步器包含了两个节点类型的引用，**一个指向头节点，而另一个指向尾节点**，当一个线程成功地获取了同步状态，其他线程将无法获取到同步状态，转而被构造成为节点并加入到同步队列中，而这个**加入队列的过程必须要保证线程安全**，因此同步器提供了一个**基于CAS 的设置尾节点**的方法：compareAndSetTail(Node expect,Node update)，它需要传递当前线程“认为”的尾节点和当前节点，只有设置成功后，当前节点才正式与之前的尾节点建立关联。

- 同步队列遵循FIFO，**首节点是获取同步状态成功的节点，首节点的线程在释放同步状态时，将会唤醒后继节点，而后继节点将会在获取同步状态成功时将自己设置为首节点**
- 代码实现：java.util.concurrent.locks.AbstractQueuedSynchronizer#acquireQueued

<img src="https://github.com/likang315/Middleware/blob/master/01：多线程/photos/同步队列的基本结构.png?raw=true" style="zoom:25%;" />

###### 独占式同步状态的获取和释放

- 在获取同步状态时，同步器维护一个同步队列，获取状态失败的线程**构造成一个Node节点**，通过CAS安全的加入到队列的**尾部并在队列中进行自旋**；
- 当头节点的线程释放了同步状态之后，将会**唤醒其后继节点**，后继节点的线程被唤醒后需要检查自己的前驱节点是否是头节点，尝试**获取同步状态**，若获取成功后**将当前节点设置为头结点**；
- <img src="https://github.com/likang315/Middleware/blob/master/01：多线程/photos/独占式同步状态的获取与释放.png.png?raw=true" style="zoom:33%;" />

###### 共享式同步状态获取与释放

- 共享式获取与独占式获取最主要的区别在于**同一时刻能否有多个线程同时获取到同步状态**

- ```java
  // java.util.concurrent.locks.ReentrantReadWriteLock.Sync#tryAcquireShared
  public final void acquireShared(int arg) {
      if (tryAcquireShared(arg) < 0)
          doAcquireShared(arg);
  }
  // java.util.concurrent.locks.AbstractQueuedSynchronizer#doAcquireShared
  private void doAcquireShared(int arg) {
      final Node node = addWaiter(Node.SHARED);
      boolean interrupted = false;
      try {
          for (;;) {
              // 获取当前节点的前驱
              final Node p = node.predecessor();
              if (p == head) {
                  int r = tryAcquireShared(arg);
                  if (r >= 0) {
                      // 设置头结点且传播共享状态, 通知后继的节点可以尝试获取共享资源, 会唤醒后继的结点
                      setHeadAndPropagate(node, r);
                      p.next = null; // help GC
                      return;
                  }
              }
              // 挂起当前线程，等待共享资源的释放
              if (shouldParkAfterFailedAcquire(p, node))
                  interrupted |= parkAndCheckInterrupt();
          }
      } catch (Throwable t) {
          cancelAcquire(node);
          throw t;
      } finally {
          if (interrupted)
              selfInterrupt();
      }
  }
  ```
  
- 在doAcquireShared(int arg)方法的自旋过程中，如果当前节点的前驱为头节点时，尝试获取同步状态，如果**返回值大于等于0，表示该次获取同步状态成功，然后设置自己为头结点并且传播共享状态，从自旋过程中退出**；

- releaseShared(int arg) 在释放同步状态之后，将会唤醒后继节点，尝试获取共享状态。而且必须确保同步状态（或者资源数）线程安全释放，一般是通过循环和CAS来保证的，因为**释放同步状态的操作会同时ß来自多个线程**；

###### 独占式超时获取同步状态

- tryAcquireNanos(int arg,long nanosTimeout) 方法在**支持响应中断**的基础上，增加了**超时**获取的特性
- 针对超时获取，主要需要计算出需要睡眠的时间间隔nanosTimeout，为了防止过早通知，nanosTimeout计算公式为：**nanosTimeout -= now - lastTime**，其中 now 为当前唤醒时间，lastTime 为上次唤醒时间，如果 nanosTimeout 大于0则表示超时时间未到，需要继续睡眠 nanosTimeout 纳秒，反之，表示已经超时

<img src="https://github.com/likang315/Middleware/blob/master/01：多线程/photos/独占式超时获取同步状态.png?raw=true" style="zoom:65%;" />

##### 05：重入锁 & 不可重入锁

- 重进入：指任意线程在获取到锁之后能够再次获取该锁而不会被锁阻塞；
- 可重入锁：指的是**可重复递归调用**的锁，在外层使用锁之后，在内层仍然可以获得此锁；
  - ReentrantLock 和 synchronized 都是可重入锁
- 不可重入锁：与可重入锁相反，不可递归调用，**递归调用就发生死锁**；

##### 06：公平锁 & 非公平锁

```java
Lock lock = new ReentrantLock(true);
// 非公平锁，设置优先级，效率高
Lock lock = new ReentrantLock(false);
```

- **多个线程竞争锁时需不需要排队**
- 公平锁：指线程获取锁的顺序是按照申请锁顺序来的，先申请的先获取锁；
- 非公平锁：指的是抢锁机制，先申请的线程不一定先获得锁；
- 当线程A执行完之后，要**唤醒线程B是需要时间的**，而且线程B醒来后还要再次竞争锁，所以如果**在切换过程当中，来了一个线程C**，那么线程C是有可能获取到锁的，如果C获取到了锁，B就只能继续自旋；

###### 实现可重入锁需要满足两个特性

1. 线程再次获取锁
   - 锁需要去**识别获取锁的线程是否为当前占据锁的线程**，如果是，则再次成功获取。
2. 锁的最终释放
   - 线程**重复n次获取了锁，随后在第n次释放该锁后**，其他线程能够获取到该锁。锁对于获取进行计数自增，计数表示当前锁被重复获取的次数，而锁被释放时，计数自减，当计数等于0时表示锁已经成功释放。

###### ReentrantLock 实现公平锁

- java.util.concurrent.locks.ReentrantLock.FairSync#tryAcquire
- 判断条件多了 **hasQueuedPredecessors()** 方法，即**查询锁竞争的同步队列中是否有等待的线程**的判断，如果该方法返回 true，则表示有线程比当前线程更早地请求获取锁，因此需要**等待前驱线程获取并释放锁之后才能继续获取锁**。

##### 07：读写锁（ReentrantReadWriteLock）

​	读写锁在同一时刻只**允许多个读线程访问，并且在写线程访问时，所有的读线程和其他写线程均被阻塞**。读写锁维护了一个读锁和一个写锁，通过**读写锁分离**，使得并发性和吞吐量相比一般的排他锁较好；

- 共享的缓存结构（**读大于写的场景**）
- Java 并发包提供**读写锁的实现是 ReentrantReadWriteLock**
- 支持重进入，**读线程在获取读锁之后可以再次获取读锁，而写线程在获取写锁之后可以再次获取写锁和读锁**
- **读写锁降级**：指将写锁降级为读锁。在使用读写锁时，如果一个线程已经持有写锁，并且需要获取读锁时，可以先将写锁降级为读锁，这样可以**避免释放写锁后再重新获取读锁的资源浪费**。

###### 读写锁的接口

- ```java
  /**
  * 提供获取读写锁的方法
  */
  public interface ReadWriteLock {
      Lock readLock();
      Lock writeLock();
  }
  // 其实现类为 ReentrantReadWriteLock，其中 ReadLock 和 WriteLock 是两个内部类，实现了Lock接口
  public class ReentrantReadWriteLock implements ReadWriteLock, java.io.Serializable {
      private static final long serialVersionUID = -6992448646407690164L;
      /** Inner class providing readlock */
      private final ReentrantReadWriteLock.ReadLock readerLock;
      /** Inner class providing writelock */
      private final ReentrantReadWriteLock.WriteLock writerLock;
      /** Performs all synchronization mechanics */
      final Sync sync;
  }
  ```
  

###### 读写锁的使用示例

- ```java
  /**
  * 读写锁的使用示例
  */
  public class Cache {
      static Map<String, Object> map = new HashMap<String, Object>();
      static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
      static Lock r = rwl.readLock();
      static Lock w = rwl.writeLock();
      // 获取一个key对应的value
      public static final Object get(String key) {
          r.lock();
          try {
            	return map.get(key);
          } finally {
            	r.unlock();
          }
      }
      // 设置key对应的value，并返回旧的value
      public static final Object put(String key, Object value) {
          w.lock();
          try {
            	return map.put(key, value);
          } finally {
            	w.unlock();
          }
      }
  }
  ```

##### 08：ReentrantReadWriteLock 源码剖析

1. 读写状态的设计
2. 写锁的获取与释放
3. 读锁的获取
4. 释放以及锁降级

###### 读写状态的设计

​	读写锁的自定义同步器需要**在同步状态（一个整型变量）上维护多个读线程和一个写线程的状态**，如果在一个整型变量上维护多种状态，就一定需要**“按位切割使用”**这个变量，读写锁将变量切分成了两个部分，**高16位表示读，低16位表示写**。

- 通过**位运算**快速确定读写状态的值。
  - 假设当前同步状态值为S，写状态等于S & 0x0000FFFF（将高16位全部抹去），读状态等于S >>> 16（无符号补0右移16位）。当写状态增加1时，等于S+1，当读状态增加1时，等于 S+(1<<16)，是 S+0x00010000。
  - 若是**读写状态都有值，则表示该线程已经获取了写锁，且重进入获取了读锁**，若只有读状态有值，则获取的读锁。

###### 写锁的获取与释放

​	写锁是一个**支持重进入的排它锁**。如果当前线程已经获取了写锁，则增加写状态。如果**当前线程在获取写锁时，读锁已经被获取（读状态不为0）或者该线程不是已经获取写锁的线程，则当前线程进入等待状态**。

```java
protected final boolean tryAcquire(int acquires) {
    Thread current = Thread.currentThread();
    int c = getState();
    // 独占式锁的数量
    int w = exclusiveCount(c);
    if (c != 0) {
        // 存在读锁或者当前获取线程不是已经获取写锁的线程
        if (w == 0 || current != getExclusiveOwnerThread())
            return false;
        if (w + exclusiveCount(acquires) > MAX_COUNT)
            throw new Error("Maximum lock count exceeded");
        setState(c + acquires);
        return true;
    }
    if (writerShouldBlock() || !compareAndSetState(c, c + acquires)) {
        return false;
    }
    setExclusiveOwnerThread(current);
    return true;
}
```

###### 读锁的获取和释放

​	读锁是一个**支持重进入的共享锁**，它能够被多个线程同时获取，在没有其他写线程访问（或者写状态为0）时，读锁总会被成功地获取，而所做的也只是 CAS 增加读状态。**如果当前线程已经获取了读锁，则增加读状态。如果当前线程在获取读锁（读线程）时，写锁已被其他线程获取，则进入等待状态**。

- 读锁的每次释放（线程安全的，可能有多个读线程同时释放读锁）均减少读状态，减少的值是（1<<16）。

```java
protected final int tryAcquireShared(int unused) {
    for (;;) {
        int c = getState();
        // 获取之后同步状态的值
        int nextc = c + (1 << 16);
        if (nextc < c)
            throw new Error("Maximum lock count exceeded");
        // 独占锁的数量
        if (exclusiveCount(c) != 0 && owner != Thread.currentThread())
            return -1;
        // 依靠CAS保证增加读状态线程安全
        if (compareAndSetState(c, nextc))
            return 1;
    }
}
```

###### 读写锁降级

- 指当前线程拥有着写锁，再获取到读锁，随后释放（先前拥有的）写锁的过程。

- RentrantReadWriteLock 不支持锁升级（把持读锁、获取写锁，最后释放读锁的过程）。目的是保证数据可见性。

- ```java
  public void processData() {
      readLock.lock();
      if (!update) {
          // 必须先释放读锁
          readLock.unlock();
          // 锁降级从写锁获取到开始
          writeLock.lock();
          try {
              if (!update) {
                // 准备数据的流程（略）
                update = true;
            	}
              // 此刻并没有保持可见性，锁还没有释放，只是为了可间性提前做准备
            	readLock.lock();
          } finally {
            	writeLock.unlock();
          }
          // 锁降级完成，写锁降级为读锁
      }
      try {
        	// 使用数据的流程（略）
      } finally {
        	readLock.unlock();
      }
  }
  ```

###### java.util.concurrent.locks

##### 09：LockSupport 工具类

​	用于阻塞或唤醒一个线程的工具类

- static void  park()
  - 阻塞当前线程
- static void`  `parkNanos(long nanos)
  - 阻塞当前线程，最长不超过nanos纳秒，超时返回
- static void`  `unpark(Thread thread)
  - 唤醒处于阻塞状态的线程

###### 示例

```java
public static void main(String[] args) {
    Thread mainThread = Thread.currentThread();
    // 创建一个新线程
    Thread parkThread = new Thread(() -> {
        // 运行一些任务
        System.out.println("Child thread is running");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 唤醒主线程
        LockSupport.unpark(mainThread);
    });
    // 启动子线程
    parkThread.start();
    System.out.println("Main thread is waiting");
    // 阻塞当前线程
    LockSupport.park();
    System.out.println("Main thread is awakened");
}
```

###### java.util.concurrent.locks

##### 10：interface Condition

​	任意一个Java对象，都拥有一组监视器方法（定义在java.lang.Object上），主要包括wait()、notify()，这些方法与synchronized同步关键字配合，可以实现等待/通知模式。Condition接口也提供了类似Object的监视器方法，与Lock配合可以实现等待/通知模式，但是这两者在使用方式以及功能特性上还是有差别的。

```java
public class ConditionUseCase {
    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    public void conditionWait() throws InterruptedException {
        lock.lock();
        try {
          	// 让当前线程释放锁，并且处于等待状态
          	condition.await();
        } finally {
          	lock.unlock();
        }
    }
  	public void conditionSignal() throws InterruptedException {
        lock.lock();
        try {
          	condition.signal();
        } finally {
          	lock.unlock();
        }
    }
}
```

​	一般都会将 Condition 对象作为成员变量。当调用await()方法后，当前线程会释放锁并在此等待，而其他线程调用Condition对象的signal()方法，通知当前线程后，当前线程才从await()方法返回，并且进入就绪状态，准备获取锁资源。

###### Condition的方法

- void **await()** throws InteruptedException
  - 当前线程进入等待状态直到被通知或被中断
- boolean  await(long time, TimeUnit unit)
  - 当前线程进入等待状态直到被通知、中断、或者超时
- long  awaitNanos(long nanosTimeout)
  - 当前线程进入等待状态直到被通知、中断、或者超时，返回值表示剩余时间，若返回值是0或者负数表示已经超时
-  void awaitUninterruptibly()
  - 当前线程进入等待状态直到被通知，并且不支持中断操作
- boolean awaitUntil(Date deadline)
  - 当前线程进入等待状态直到被通知、中断或者到某个时间，如果没有到指定时间就通知返回true，否则表示到了指定时间，返回false
- void **signal()**
  - 唤醒一个等待在Condition上的线程，该线程从等待方法返回前，必须获取一个与Condition相关的锁
- void signalAll()
  - 唤醒所有等待在Condition上的线程，能够从等待方法返回的线程必须获得与Condition相关的锁

##### 11：源码剖析 Condition【等待队列】

- 一个Condition包含一个等待队列，Condition 拥有头节点（firstWaiter）和尾节点（lastWaiter）。
- 当前线程调用Condition.await() 方法，将会以当前线程构造节点，并将节点加入到等待队列尾部。
- **Condition 是 AQS 的内部类**，它的节点的构造复用了同步器中节点的定义，Lock（AQS 同步器）拥有一个同步队列和多个等待队列。

<img src="https://github.com/likang315/Middleware/blob/master/01：多线程/photos/ 同步队列和等待队列.png?raw=true" style="zoom:20%;" />

###### Condition 等待

​	调用 await() 的线程成功获取了锁的线程，也就是同步队列中的首节点，该方法会**将当前线程（同步队列）构造成新节点并加入等待队列中，唤醒同步队列中的后继节点，然后释放同步状态**，进入等待状态。

###### Condition 通知

- 调用 signal() 方法，等待队列中的头节点线程安全地移动到同步队列的尾部。当节点移动到同步队列后，当前线程再使用LockSupport 唤醒该节点的线程。
- 调用 signalAll() 方法，相当于对等待队列中的每个节点均执行一次signal()方法，效果就是将等待队列中所有节点全部移动到同步队列中，并唤醒每个节点的线程；

##### 12：乐观锁与悲观锁

- 分类依据：线程要不要锁住资源
- 悲观锁：假设最坏的情况，自己每次获取数据时都有其他线程会修改（**存在锁竞争**），因此**在获取数据的时候会先加锁**，确保数据不会被别的线程修改，当其他线程想要访问数据时，都被阻塞挂起，等待持有锁的线程释放锁。
  - synchronized
  - Lock 的实现类
- 乐观锁：认为自己在使用数据时不会有别的线程修改数据（**不存在锁竞争**），所以**不会添加锁，只是在更新数据的时候去判断之前有没有别的线程更新了这个数据**。如果这个数据没有被更新，当前线程将自己修改的数据成功写入。如果数据已经被其他线程更新，则根据 CAS 实现更新操作。

```java
// 悲观锁：synchronized 和 Lock 的实现类
public synchronized void testMethod() {
	// 操作同步资源
}
// Lock 的实现类：ReentrantLock
private ReentrantLock lock = new ReentrantLock();
public void modifyPublicResources() {
	lock.lock();
	// 操作同步资源
	lock.unlock();
}

// 乐观锁：需要保证多个线程使用的是同一个AtomicInteger
private AtomicInteger atomicInteger = new AtomicInteger(); 
// 执行自增
atomicInteger.incrementAndGet();
```

##### 13：ReentranLock 和 Synchronized 的区别（隐式使用锁和显示使用锁）

1. ###### 实现方式不同

   - volatile int state 同步状态管理；
   - monitor 锁管理；

2. ###### ReentrantLock 可以实现公平锁

   - ```java
     // Lock 实现公平锁
     public ReentrantLock(boolean fair) {
     	sync = fair ? new FairSync() : new NonfairSync();
     }
     Lock lock = new ReentrantLock(true);
     ```

3. ###### ReentrantLock 可中断响应

   - 当使用 synchronized 实现锁时，阻塞在锁上的线程**除非获得锁否则将一直等待下去**，无法被中断，而 ReentrantLock 提供了一个可以中断响应的获取锁的方法 lockInterruptibly()，该方法可以**用来解决死锁问题**，被中断的线程将抛出异常释放已获得锁，而另一个线程将获取锁后正常结束;
   - 获取 synchronized  的线程，可以调用 interrupt() 方法，但是**该线程不会响应中断操作**；

4. ###### 获取锁时超时等待

   - ReentrantLock 还提供了获取锁超时等待的方法
   - tryLock(long time, TimeUnit unit)
     - 传入时间参数：表示等待指定的时间
     - 无参则表示立即返回锁申请的结果，true表示获取锁成功，false表示获取锁失败
