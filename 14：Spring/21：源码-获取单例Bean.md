### Spring-获取单例 Bean

------

[TOC]

##### 01：Bean 加载（getBean）的核心流程【九步】

- 核心类：org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean

###### 转换对应beanName

- name 可能会以 & 字符开头，表明调用者想获取 FactoryBean 本身，而非 FactoryBean 实现类所创建的 bean。
- 将别名转换为最基本的beanName值；

###### 从缓存中加载单例【重要】

- **尝试加载：**首先尝试从缓存中加载，如果加载不成功则再次尝试从 singletonFactories 中加载
  - 在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候**为了避免循环依赖**，在Spring中创建bean的原则是**不等 bean 创建完成就会将创建 bean 的ObjectFactory提早曝光加入到缓存中，一旦下一个 bean 创建时候需要依赖上一个bean 则直接使用ObjectFactory**。

###### bean的实例化 

- 如果从缓存中得到了bean的原始状态，则需要对bean进行实例化。因为缓存中记录的只是最原始的bean状态，并不一定是我们最终想要的bean。

###### 原型模式的依赖检查 

- 只有在单例情况下才会尝试解决循环依赖，如果存在A中有B的属性，B中有A的属性，那么当依赖注入的时候，就会产生当A还未创建完的时候，因为对于B的创建再次返回创建A，造成循环依赖， 也就是情况： isPrototypeCurrentlyInCreation(beanName)判断true。

###### 从父容器（parentBeanFactory）中查找 Bean 实例

- 如果当前加载的XML配置文件中不包含beanName所对应的配置，就到 parentBeanFactory去尝试下，然后再去递归的调用getBean方法，找到直接返回。

###### 合并父类BeanDefination & 转化成RootBeanDefinition【重要】

- 转换为RootBeanDefinition，因为从XML配置文件中读取到的bean信息是存储在GernericBeanDefinition中的，但是所有的bean 后续处理都是针对于 **RootBeanDefinition** 的，所以这里需要进行一个转换，转换的同时如果父类bean不为空的话，则会合并父类的属性。

###### 检查是否有依赖，递归实例化依赖的Bean

- 因为bean的初始化过程中很可能会用到某些属性，而某些属性很可能是动态配置的，并且配置成依赖于其他的bean，那么这个时候就有必要先加载依赖的bean，所以，**在Spring的加载顺序中，在初始化某一个bean的时候首先会初始化这个bean所对应的依赖**。

###### 针对不同的scope进行bean的创建【重要】

-  Spring会根据不同的Scope 配置进行不同的初始化策略。singleton —>prototype —> 其他

###### 类型转换

- 使用类型转换器，将返回的 bean 转换为 requiredType 所指定的类型。

##### 02：beanName 转换

- 处理以字符 & 开头的 name，防止 BeanFactory 无法找到与 name 对应的 bean 实例；

- 处理别名问题，Spring 不会存储 <别名, bean 实例> 这种映射，仅会存储<beanName, bean实例>；

- ```java
  protected String transformedBeanName(String name) {
      // 调用了两个方法：BeanFactoryUtils.transformedBeanName(name) 和 canonicalName
      return canonicalName(BeanFactoryUtils.transformedBeanName(name));
  }
  
  /** 该方法用于处理 & 字符 */
  public static String transformedBeanName(String name) {
      Assert.notNull(name, "'name' must not be null");
      String beanName = name;
      // 循环处理 & 字符。比如 name = "&&&&&helloService"，最终会被转成 helloService
      while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
          beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
      }
      return beanName;
  }
  
  /** 该方法用于转换别名 */
  public String canonicalName(String name) {
      String canonicalName = name;
      String resolvedName;
      /*
       * 这里使用 while 循环进行处理，原因是：可能会存在多重别名的问题，即别名指向别名。比如下面
       * 的配置：
       *   <bean id="hello" class="service.Hello"/>
       *   <alias name="hello" alias="aliasA"/>
       *   <alias name="aliasA" alias="aliasB"/>
       *
       * 上面的别名指向关系为 aliasB -> aliasA -> hello，对于上面的别名配置，aliasMap 中数据
       * 视图为：aliasMap = [<aliasB, aliasA>, <aliasA, hello>]。通过下面的循环解析别名
       * aliasB 最终指向的 beanName
       */
      do {
          resolvedName = this.aliasMap.get(canonicalName);
          if (resolvedName != null) {
              canonicalName = resolvedName;
          }
      } while (resolvedName != null);
      return canonicalName;
  }
  ```

##### 03：从缓存中获取Bean

- org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#getSingleton(java.lang.String, boolean)

- 单例在 Spring 的同一个容器内只会被创建一次，后续再获取 bean 直接从**单例缓存中获取**，当然这里也只是尝试加载，首先尝试从缓存中加载，然后**再次尝试从singletonFactories中加载**。

- Spring创建bean的原则：**不等bean创建完成就会将创建bean的ObjectFactory提早曝光加入到缓存中**，一旦下一个bean创建时需要依赖上个bean，则直接使用ObjectFactory。

- 缓存集合

  - | 缓存                  | 用途                                                         |
    | :-------------------- | :----------------------------------------------------------- |
    | singletonObjects      | 用于存放完全初始化好的 bean，从该缓存中取出的 bean 可以直接使用 |
    | earlySingletonObjects | 用于存放还在初始化中的 bean，用于**解决循环依赖**            |
    | singletonFactories    | 用于存放 bean 工厂。bean 工厂所产生的 bean 是还未完成初始化的 bean。如代码所示，bean 工厂所生成的对象最终会被缓存到 earlySingletonObjects 中 |
    | registeredSingletons  | 用来保存当前所有已注册的bean。                               |

- ```java
  public Object getSingleton(String beanName) {
      return getSingleton(beanName, true);
  }
  
  /**
   * 这里解释一下 allowEarlyReference 参数，allowEarlyReference 表示是否允许其他 bean 引用
   */
  protected Object getSingleton(String beanName, boolean allowEarlyReference) {
      // 从 singletonObjects 获取实例，singletonObjects 中缓存的实例都是完全实例化好的 bean，可以直接使用
      Object singletonObject = this.singletonObjects.get(beanName);
      /*
       * 如果 singletonObject = null，表明还没创建，或者还没完全创建好。
       * 这里判断 beanName 对应的 bean 是否正在创建中
       */
      if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
          synchronized (this.singletonObjects) {
              // 从 earlySingletonObjects 中获取提前曝光的 bean，用于处理循环引用
              singletonObject = this.earlySingletonObjects.get(beanName);
              // 如果如果 singletonObject = null，且允许提前曝光 bean 实例，
              // 则从相应的 ObjectFactory 获取一个原始的（raw）bean（尚未填充属性）
              if (singletonObject == null && allowEarlyReference) {
                  // 获取相应的工厂类
                  ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                  if (singletonFactory != null) {
                      // 提前曝光 bean 实例，用于解决循环依赖
                      singletonObject = singletonFactory.getObject();
                      // 放入缓存中，如果还有其他 bean 依赖当前 bean，其他 bean 可以直接
                      // 从 earlySingletonObjects 取结果
                      this.earlySingletonObjects.put(beanName, singletonObject);
                      this.singletonFactories.remove(beanName);
                  }
              }
          }
      }
      return (singletonObject != NULL_OBJECT ? singletonObject : null);
  }
  ```

##### 04：合并父 BeanDefinition & 转换成RootBeanDefination

- org.springframework.beans.factory.support.AbstractBeanFactory#getMergedLocalBeanDefinition

- 对于BeanDefinition的合并，**Spring都会创建一个新的RootBeanDefinition来进行接收**, 而不是用原来的 BeanDefinition, **如果原始BeanDefinition没有父BeanDefinition了, 那么就直接创建一个RootBeanDefinition**, 并将原始BeanDefinition作为参数传入构造方法中, 如果原始BeanDefinition存在BeanDefinition, Spring 除了会做上述的操作外, 还会**调用overrideFrom方法进行深入的合并**, 其实就是一系列的setXXX方法的调用而已, 在合并完成后, **对于合并后的BeanDefinition如果没有作用域, 则设置为单例, 并且将合并的BeanDefinition 放入到mergedBeanDefinitions这个map中缓存起来**。

- ```java
  protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
      // 检查缓存中是否有merge 过的RootBeanDefinition
      RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
      if (mbd != null && !mbd.stale) {
          return mbd;
      }
      return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
  }
  // 合并父子 BeanDefinition，转化成 RootBeanDefinition
  protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd,
          @Nullable BeanDefinition containingBd) throws BeanDefinitionStoreException {
      synchronized (this.mergedBeanDefinitions) {
          RootBeanDefinition mbd = null;
          RootBeanDefinition previous = null;
          if (containingBd == null) {
              mbd = this.mergedBeanDefinitions.get(beanName);
          }
          // 是否可以merge
          if (mbd == null || mbd.stale) {
              previous = mbd;
              // 如果一个BeanDefinition没有设置parentName,表示没有父类，直接克隆返回
              if (bd.getParentName() == null) {
                  if (bd instanceof RootBeanDefinition) {
                      mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
                  } else {
                      mbd = new RootBeanDefinition(bd);
                  }
              } else {
                  BeanDefinition pbd;
                  try {
                      // 转换父beanName 
                      String parentBeanName = transformedBeanName(bd.getParentName());
                      // 用来防止由于程序员将当前beanName设置为当前BeanDefinition的父类而导致死循环的
                      // 如果真正存在父类, Spring就会先将父类合并了, 因为父类可能还有父类,用递归方法
                      if (!beanName.equals(parentBeanName)) {
                          pbd = getMergedBeanDefinition(parentBeanName);
                      }
                      else {
                          // 对存在有父容器情况的处理(Spring的父容器)
                          BeanFactory parent = getParentBeanFactory();
                          if (parent instanceof ConfigurableBeanFactory) {
                              pbd = ((ConfigurableBeanFactory)parent)
                                  .getMergedBeanDefinition(parentBeanName);
                          } else {
                              throw new NoSuchBeanDefinitionException(parentBeanName,"Parent name '" + parentBeanName + "' is equal to bean name '" + beanName + "': cannot be resolved without a ConfigurableBeanFactory parent");
                          }
                      }
                  } catch (NoSuchBeanDefinitionException ex) {
                      throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
  "Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
                  }
                  // 当完成了初步的合并后,进行了深入的复制, 这里面的代码也很简单, 大量的 mbd.setXXX(bd.getXX)
                  mbd = new RootBeanDefinition(pbd);
                  mbd.overrideFrom(bd);
              }
              // 如果用户未配置 scope 属性，则默认将该属性配置为 singleton
              if (!StringUtils.hasLength(mbd.getScope())) {
                  mbd.setScope(SCOPE_SINGLETON);
              }
              if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
                  mbd.setScope(containingBd.getScope());
              }
  
              // 如果允许缓存 Bean 的情况下，将合并后的 BeanDefinition 缓存到 mergedBeanDefinitions 中
              if (containingBd == null && isCacheBeanMetadata()) {
                  this.mergedBeanDefinitions.put(beanName, mbd);
              }
          }
          if (previous != null) {
              copyRelevantMergedBeanDefinitionCaches(previous, mbd);
          }
          return mbd;
      }
  }
  ```

##### 05：从 FactoryBean 中获取 bean 实例

- Spring提供了一个 org.Springframework.bean.factory.FactoryBean的工厂类接口，用户可以通过**实现该接口定制实例化bean** 的逻辑。 是一种可以产生Bean的工厂。

- org.springframework.beans.factory.support.AbstractBeanFactory#getObjectForBeanInstance

- ```java
  protected Object getObjectForBeanInstance(
      Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {
      //  如果 BeanName 是 FactoryBean<?> 相关的 BeanName
      // 如果 name 以 & 开头（工厂引用），但 beanInstance 却不是 FactoryBean，则抛异常
      if (BeanFactoryUtils.isFactoryDereference(name)) {
          if (beanInstance instanceof NullBean) {
              return beanInstance;
          }
          if (!(beanInstance instanceof FactoryBean)) {
              throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
          }
          // 用于判断是否从缓存中加载的
          if (mbd != null) {
              mbd.isFactoryBean = true;
          }
          return beanInstance;
      }
      // 在拥有了一个新的 BeanInstance, beanInstance 可能是一个普通的 bean，也可能是一个 FactoryBean。
      // 如果是一个普通的 bean，直接返回 beanInstance。如果是 FactoryBean，则要调用工厂方法生成一个 bean 实例。
      if (!(beanInstance instanceof FactoryBean)) {
          return beanInstance;
      }
  
      // 证明是FactoryBean
      Object object = null;
      if (mbd != null) {
          mbd.isFactoryBean = true;
      } else {
          // 如果 mbd 为空，则从缓存中加载 bean。FactoryBean 生成的单例 bean 会被缓存在
          // factoryBeanObjectCache 集合中，不用每次都创建
          object = getCachedObjectForFactoryBean(beanName);
      }
      if (object == null) {
          // 经过前面的判断，到这里可以保证 beanInstance 是 FactoryBean 类型的，所以进行类型转换
          FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
          if (mbd == null && containsBeanDefinition(beanName)) {
              mbd = getMergedLocalBeanDefinition(beanName);
          }
          // 是否自定义BeanDefinition
          boolean synthetic = (mbd != null && mbd.isSynthetic());
          // 从工厂Bean中获取对象
          object = getObjectFromFactoryBean(factory, beanName, !synthetic);
      }
      return object;
  }
  
  protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
      /*
       * FactoryBean 也有单例和非单例之分，针对不同类型的 FactoryBean，这里有两种处理方式：
       *   1. 单例 FactoryBean 生成的 bean 实例也认为是单例类型。需放入缓存中，供后续重复使用
       *   2. 非单例 FactoryBean 生成的 bean 实例则不会被放入缓存中，每次都会创建新的实例
       */
      if (factory.isSingleton() && containsSingleton(beanName)) {
          synchronized (getSingletonMutex()) {
              Object object = this.factoryBeanObjectCache.get(beanName);
              if (object == null) {
                  object = doGetObjectFromFactoryBean(factory, beanName);
                  Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                  if (alreadyThere != null) {
                      object = alreadyThere;
                  } else {
                      if (shouldPostProcess) {
                          if (isSingletonCurrentlyInCreation(beanName)) {
                              return object;
                          }
                          beforeSingletonCreation(beanName);
                          try {
                              // 后置处理器
                              object = postProcessObjectFromFactoryBean(object, beanName);
                          } catch (Throwable ex) {
                              throw new BeanCreationException(beanName,
                               "Post-processing of FactoryBean's singleton object failed", ex);
                          } finally {
                              afterSingletonCreation(beanName);
                          }
                      }
                      if (containsSingleton(beanName)) {
                          this.factoryBeanObjectCache.put(beanName, object);
                      }
                  }
              }
              return object;
          }
      } else {
          Object object = doGetObjectFromFactoryBean(factory, beanName);
          if (shouldPostProcess) {
              try {
                  object = postProcessObjectFromFactoryBean(object, beanName);
              } catch (Throwable ex) {
                  throw new BeanCreationException(beanName,
                  "Post-processing of FactoryBean's object failed", ex);
              }
          }
          return object;
      }
  }
  
  private Object doGetObjectFromFactoryBean(FactoryBean<?> factory, String beanName) throws BeanCreationException {
      Object object;
      try {
          // spring 权限检查
          if (System.getSecurityManager() != null) {
              AccessControlContext acc = getAccessControlContext();
              try {
                  object = AccessController.doPrivileged(
                      (PrivilegedExceptionAction<Object>) factory::getObject, acc);
              }
              catch (PrivilegedActionException pae) {
                  throw pae.getException();
              }
          } else {
              // 从FactoryBean 中获取对象
              object = factory.getObject();
          }
      } catch (FactoryBeanNotInitializedException ex) {
          throw new BeanCurrentlyInCreationException(beanName, ex.toString());
      } catch (Throwable ex) {
          throw new BeanCreationException(beanName,
                                          "FactoryBean threw exception on object creation", ex);
      }
      if (object == null) {
          if (isSingletonCurrentlyInCreation(beanName)) {
              throw new BeanCurrentlyInCreationException(beanName,
               "FactoryBean which is currently in creation returned null from getObject");
          }
          object = new NullBean();
      }
      return object;
  }
  ```







