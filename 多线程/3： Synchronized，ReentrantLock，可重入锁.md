### 并发编程

------

​	多线程并发访问同一资源时，就会形成“抢”的现象，由于线程切换实际不确定，可能导致执行代码顺序的混乱，严重时会导致系统瘫痪，例：上厕所、上锁

- 线程安全的核心：对共享且可变状态的资源访问进行管理，例：加锁、final、volitile

##### 1：并发编程的的三个特性

1. 原子性：在一个原子操作中，cpu 不可以在中途暂停然后在调度，即不被中断操作，要不执行完成，要不就不执行
2. 有序性：在本线程内观察，所有操作都是有序的
3. 可见性：指当多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即得到修改的值
   - 当一个变量被 volatile 修饰后，当一个线程在私有内存中修改此共享变量后，共享变量会立即被更新到主内存中，同时使其他线程缓存的此变量无效，其他线程读取共享变量时，会直接从主内存中读取
   - synchronized和Lock都可以保证可见性。synchronized和Lock能保证同一时刻只有一个线程获取锁然后执行同步代码，并且在释放锁之前会将对变量的修改刷新到主存当中，因此可以保证可见性

##### 2：乐观锁与悲观锁：

- 分类依据：线程要不要锁住资源
- 悲观锁：假设最坏的情况，自己每次取数据时都认为其他线程会修改，因此在获取数据的时候会先加锁，确保数据不会被别的线程修改，当其他线程想要访问数据时，都需要阻塞挂起，等待持有锁的线程释放锁
  - synchronized
  - Lock 的实现类
- 乐观锁：认为自己在使用数据时不会有别的线程修改数据，所以不会添加锁，只是在更新数据的时候去判断之前有没有别的线程更新了这个数据。如果这个数据没有被更新，当前线程将自己修改的数据成功写入。如果数据已经被其他线程更新，则根据不同的实现方式执行不同的操作（例如报错或者自动重试）
  - 通过使用无锁编程来实现
    - CAS算法，Java 原子类（Automic）中的递增操作就通过CAS自旋实现的

```java
// 悲观锁:synchronized
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

##### 3：synchronized

​	Java中的关键字，是一种同步锁，为重量级锁，即锁住了当前对象也把锁给了当前对象

###### 锁原理

- synchronized：是通过**字节码指令**来实现的
  - synchronized 同步块：编译后会在同步块前后形成 monitorenter 和 monitorexit 两个字节码指令
    - 执行 monitorenter 指令时需要先获得对象的锁(每个对象有一个监视器锁monitor)，如果这个对象获取锁或者当前线程已经获得此锁（可重入锁），那么锁的计数器+1。如果获取失败，那么当前线程阻塞，直到锁被另一个线程释放，执行monitorexit指令时，计数器 -1，当为 0 的时候锁释放
  - synchronize 同步方法：编译后会在方法访问处，添加字节码指令 ACC_SYNCHRONIZED  标志
    - 不管是monitorenter，还是ACC_SYNCHRONIZED都是用于获取对象锁

###### synchronized：同步块

​	要求多个线程对该块内的代码依次排队执行，前提条件是同步监视器对象，可以有效的缩小同步范围，并保证并发安全的同时尽可能的提高效率

- 当一个线程访问对象中的synchronized(this)同步代码块时，另一个线程仍然可以访问该对象中的非synchronized(this)同步代码块，因为非synchronized不需要monitor锁

```java
// this锁：指monitor对象
synchronized(this) {
	// ...
}

// 有一个明确的对象作为锁时
public void  method3(SomeObject obj) {
   //非this锁：monitor对象
   synchronized(obj) {
      // todo
   }
}

// 当没有明确的对象作为锁，只是想让一段代码同步时，可以创建一个特殊的对象来充当锁
private Byte[] local= new Byte[0];
synchronized(local)
{
  // todo...
}
```

###### synchronized：同步方法

​	为同步方法，即：多个线程不能同时进入方法内部执行

- synchronized 同步方法：在一个线程调用该方法时将该方法所属对象加锁，其他线程在执行此方法时，由于执行此方法的线程没有释放锁，所以只能在方法外阻塞，直到持有同步锁的线程将方法执行完毕，释放锁，此线程获取同步锁
- 线程同步：解决多线程并发问题的办法就是讲"抢"变为"排队"
- **同一个锁对象可以产生互斥作用，不同锁对象不能产生互斥作用**
- 若修饰静态方法：属于类的，具有同步效果，与对象无关

```java
public synchronized void synchronizedMethod() {
  // ...
}
// synchronized 修饰的静态方法锁定的是这个类的所有对象
public static synchronized void method() {
	// ...
}	
```

###### 注意：

1. 被 synchronized修饰方法，不能被继承，若需要同步，在子类的重写该方法添加 synchronized 关键字
2. 在接口中定义方法时不能使用 synchronized 关键字
3. 构造方法不能使用 synchronized关键字，但可以使用 synchronized 代码块来进行同步

###### synchronized 类锁：Class锁

- synchronized 作用于一个类时，每个类有且只有一个Class对象，即类的 Class 对象锁，类的所有对象都是同一把锁

```java
synchronized(ClassName.class) {
	// todo....
}
```



##### 4：重入锁(Lcok)：根据一个线程中的多个流程能不能获得同一把锁

- 重进入：指任意线程在获取到锁之后能够再次获取该锁而不会被锁阻塞
- 可重入锁：指的是可重复递归调用的锁，在外层使用锁之后，在内层仍然可以获得此锁，并且不发生死锁（前提得是同一个对象或者class）
  - ReentrantLock 和synchronized 都是可重入锁
- 不可重入锁：与可重入锁相反，不可递归调用，递归调用就发生死锁

```java
public class Solution {
  // 同步方法，时同一个对象锁
  public synchronized void firstInvoke() {
    System.out.println("方法1执行...");
    doOthers();
  }
	// 同一个对象锁
  public synchronized void secondInvoke() {
    System.out.println("方法2执行...");
  }
}
```

###### 为什么支持重复加锁 ？

​	因为源码中用变量 c (计数器)来保存当前锁被获取了多少次，故在释放时，对 c 变量进行减操作，只有 c 变量为 0 时，才算锁的最终释放。所以可以 lock() 多次，同时 unlock 也必须与 lock 同样的次数

##### 5：Lock (JUC)

```java
public interface Lock {
    // 获得当前线程的对象锁
    void	lock();
    // 两个线程分别执行以上两个方法，但此时中断这两个线程，前者不会抛出异常，而后者会抛出异常
    void	lockInterruptibly();
    // 用于让线程 wait 或者 唤醒
    Condition	newCondition();
    boolean	tryLock()
    boolean	tryLock(long time, TimeUnit unit);
    // 能获得锁就返回true，不能就立即返回false，tryLock(long timeout,TimeUnit unit)，可以增加等待时间限制时间限制，如果超过该时间段还没获得锁，返回false
    // 释放锁
    void	unlock();
}
```

##### 6：Condition （JUC）

```java
public interface Condition {
  // 处于等待状态，中断时，会抛出异常
  void await() throws InterruptedException;
  // 让其等待，中断时，不会抛出异常
  void awaitUninterruptibly();
  // 任意唤醒一个处于等待状态的线程
  void signal();
  // 唤醒所有处于等待状态的线程
  void signalAll();
}
```

##### 7：ReentrantLock：

​	是可重入锁、实现了Lock接口的锁，与synchronized作用一致，并且扩展了其能力

###### 原理：

- ReenTrantLock 是一种自旋锁，通过循环调用CAS操作来实现加锁


```java
public class Test {
    private final Reentrantlock lock = new Reentrantlock()；
    public void testMethod() {
        lock.lock(); //先获得锁，再处理业务
        for (int i = 0; i < 5; i++) {
            System.out.println("ThreadName =" + Thread.currentThread().getName()
                     + (i + 1));
        }
        lock.unlock();
    }
}
```

```java
//创建Condition对象来使线程 wait 或者 唤醒，必须先执行 lock.lock()获得锁
public class MyService {
    private final Reentrantlock lock = new Reentrantlock（）；
    private Condition condition = lock.newCondition();
    public void testMethod() {
        try {
            lock.lock();
            System.out.println("开始wait");
            condition.await();
            for (int i = 0; i < 5; i++) {
                System.out.println("ThreadName=" + Thread.currentThread().getName()
                       + (i + 1));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }
}
```

##### 8：ReentranLock 和 Synchronized 的区别

###### 1：ReentrantLock可以实现公平锁

​	Lock lock=new ReentrantLock(true);//公平锁

###### 2：ReentrantLock可响应中断

当使用synchronized实现锁时,阻塞在锁上的线程除非获得锁否则将一直等待下去，而ReentrantLock给我们提供了**一个可以响应中断的获取锁的方法`lockInterruptibly()`**，该方法可以用来解决死锁问题，**被中断的线程将抛出异常，而另一个线程将能获取锁后正常结束**

###### 3：获取锁时限时等待

ReentrantLock还给我们提供了**获取锁限时等待的方法`tryLock()**`,可以选择传入时间参数,表示等待指定的时间,无参则表示立即返回锁申请的结果:true表示获取锁成功,false表示获取锁失败



### 公平锁和 非公平锁：多个线程竞争锁时需不需要排队

```java
Lock lock=new ReentrantLock(true);//公平锁
Lock lock=new ReentrantLock(false);//非公平锁，设置优先级，使用效率高
```

###### 公平锁：指的是线程获取锁的顺序是按照申请锁顺序来的，而非公平锁指的是抢锁机制，先lock的线程不一定先获得锁

公平锁是严格的以FIFO的方式进行锁的竞争，但是非公平锁是无序的锁竞争，刚释放锁的线程很大程度上能比较快的获取到锁，队列中的线程只能等待，所以非公平锁可能会有“饥饿”的问题。但是重**复的锁获取能减小线程之间的切换，而公平锁则是严格的线程切换，这样对操作系统的影响是比较大的**，所以非公平锁的吞吐量是大于公平锁的，这也是为什么JDK将非公平锁作为默认的实现



### ReadWriteLock：管理一组锁，一个是只读的锁，一个是写锁

```java
public interface ReadWriteLock
{
    //Returns the lock used for reading
    Lock readLock();
    // Returns the lock used for writing
    Lock writeLock();
}
```



### 共享锁 和 排他锁 ：多个线程能不能共享同一把锁

**通过AQS来实现的**，通过实现不同的方法，来实现独享或者共享

**独享锁也叫排他锁：**该锁一次只能被一个线程所持有，JDK中的 synchronized 和 JUC 中 Lock的实现类就是互斥锁

**共享锁：**指该锁**可被多个线程所持有，如果线程T对数据A加上共享锁后，则其他线程只能对A再加共享锁**，不能加排它锁，获得共享锁的线程只能读数据，不能修改数据



### ReentrantReadWriteLock 有两把锁：

ReadLock和WriteLock，一个读锁一个写锁，合称“读写锁”。再进一步观察可以发现ReadLock和WriteLock是靠内部类Sync实现的锁，Sync是AQS的一个子类

在ReentrantReadWriteLock里面，读锁和写锁的锁主体都是Sync，但读锁和写锁的加锁方式不一样

读锁是共享锁，写锁是独享锁。读锁的共享锁可保证并发读非常高效，而读写、写读、写写的过程互斥，因为读锁和写锁是分离的

当有**读锁时，写锁就不能获得**，而当**有写锁时，除了获得写锁的这个线程可以获得读锁外，其他线程不能获得读锁**

```java
public class ReentrantReadWriteLock implements ReadWriteLock, java.io.Serializable {
    private static final long serialVersionUID = -6992448646407690164L;
    /** Inner class providing readlock */
    private final ReentrantReadWriteLock.ReadLock readerLock;
    /** Inner class providing writelock */
    private final ReentrantReadWriteLock.WriteLock writerLock;
	//Sync是AQS的一个子类
    final Sync sync;

    public ReentrantReadWriteLock() {
        this(false);//默认时非公平锁
    }

    public ReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    public ReentrantReadWriteLock.WriteLock writeLock() { return writerLock; }
    public ReentrantReadWriteLock.ReadLock  readLock()  { return readerLock; }
    
     //inner class ReadLock
     public static class ReadLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;

        protected ReadLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }
        public void lock() {
            sync.acquireShared(1);
        }
        public void unlock() {
            sync.releaseShared(1);
        }
     }
  
     //inner class WriteLock
     public static class WriteLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;

        protected WriteLock(ReentrantReadWriteLock lock) {
            sync = lock.sync;
        }
        public void lock() {
            sync.acquire(1);
        }
     }
}
```
