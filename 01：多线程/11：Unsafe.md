### Unsafe 

------

[TOC]

##### 	 01：public final class Unsafe 

​		它提供了**直接操作内存和执行特定机器指令**的功能。这个类通常被标记为**不安全**，因为它允许绕过 Java 语言的内置安全检查和控制，直接操纵内存和执行一些底层操作。这些操作包括**手动管理内存、执行CAS操作（Compare and Swap）、操作数组、创建实例**等。

- Unsafe 被设计成**单例模式**，构造方法私有；
- 非**启动类加载器**直接调用 Unsafe.getUnsafe() 方法会抛出 SecurityException 异常；
- 它的设计就只被标准库使用，因此不建议在生产环境中使用；

##### 02：原码剖析

```java
package sun.misc;

public final class Unsafe {
    static {
        Reflection.registerMethodsToFilter(Unsafe.class, "getUnsafe");
    }
    private Unsafe() {}

    private static final Unsafe theUnsafe = new Unsafe();
    private static final jdk.internal.misc.Unsafe theInternalUnsafe = 
        jdk.internal.misc.Unsafe.getUnsafe();

    @CallerSensitive
    public static Unsafe getUnsafe() {
        Class<?> caller = Reflection.getCallerClass();
        if (!VM.isSystemDomainLoader(caller.getClassLoader()))
            throw new SecurityException("Unsafe");
        return theUnsafe;
    }

}
```

