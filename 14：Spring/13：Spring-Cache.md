### Spring Cache

------

[TOC]

##### 01：启用对缓存的支持

- Spring对缓存的支持有两种方式： 
  - 注解驱动的缓存
  - XML声明的缓存（废弃）
- 原理：它们都会**创建一个切面（aspect）并触发 Spring 缓存注解的切点（pointcut）**。根据所使用的注解以及缓存的状态，这个切面会从缓存中获取数据，将数据添加到缓存之中或者从缓存中移除某个值。

###### 基于注解的缓存

- 使用Spring的缓存抽象时，最为通用的方式就是在方法上添 加**@Cacheable和@CacheEvict注解**；

- 在往 bean 上添加缓存注解之前，必须要**启用Spring对注解驱动缓存的支持**。如果我们使用Java配置的话，那么可以在其中的一个配置类上添加**@EnableCaching**，这样的话就能启用注解驱动的缓存。

- ```java
  /**
   * 启用spring cache, 声明缓存管理器
   *
   * @author likang02@corp.netease.com
   * @date 2022/12/23 15:36
   */
  @EnableCaching
  @Configuration
  public class CacheConfig {
  
      /**
       * 声明缓存管理器
       *
       * @return
       */
      @Bean
      public CacheManager cacheManager() {
          return new ConcurrentMapCacheManager();
      }
  }
  ```

##### 02：缓存管理器

- ConcurrentMapCacheManager：**SpringBoot 默认的缓存，缓存在内存中**，生命周期与应用关联；
- RedisCacheManager（来自于Spring Data Redis项目）

##### 03：为方法添加注解以支持缓存

- 所有注解**都能运用在方法或类上**。当将其放在单个方法上 时，注解所描述的缓存行为只会运用到这个方法上。如果注解放在类 级别的话，那么缓存行为就会应用到这个类的所有方法上

###### Spring提供了四个注解来声明缓存规则

| 注解            | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| **@Cacheable**  | 在调用方法之前，**首先应该在缓存中查找方法的返回值**。 如果这个值能够找到，就会返回缓存的值。否则的话，这个方法会被调用，返回值会放到缓存之中。 |
| **@CachePut**   | 放入缓存，表明Spring应该将方法的返回值放到缓存中。在方法的调用前并不会检查缓存，方法始终都会被调用 |
| **@CacheEvict** | 清除缓存，表明Spring应该在缓存中清除一个或多个条目           |
| @Caching        | 这是一个分组的注解，能够同时应用多个其他的缓存注解           |

##### 04：填充缓存

- @Cacheable和@CachePut有一些共有的属性

- | 属性      | 类型     | 描述                                                         |
  | --------- | -------- | ------------------------------------------------------------ |
  | **value** | String[] | 被缓存数据的缓存名称                                         |
  | **key**   | String   | SpEL表达式，用来计算自定义的缓存key，若不指定，默认是方法入参。 |
  | condition | String   | SpEL表达式，如果得到的值是false的话，不会将缓存应用到 方法调用上 |
  | unless    | String   | SpEL表达式，如果得到的值是true的话，返回值不会放到缓 存之中  |

  @Cacheable：声明在接口方法上时，所有实现类都会被缓存；

- Spring提供了多个用来定义缓存规则的SpEL扩展

  - 自定义缓存key

  - | 表达式        | 描述                                                   |
    | ------------- | ------------------------------------------------------ |
    | \#root.args   | 传递给缓存方法的参数，形式为数组                       |
    | \#root.caches | 该方法执行时所对应的缓存，形式为数组                   |
    | \#result      | 方法调用的返回值（不能用在@Cacheable注解上）           |
    | \#argument    | 任意的方法参数名（如#argName）或参数索引（如#a0或#p0） |

##### 05：清除缓存

- 如果带有@CacheEvict注解的方法被调用的话，那么会有一个或更多的条目会在缓存中移除。

- ```java
  @CacheEvict("studentInfoDO")
  void remove(long id);
  ```

