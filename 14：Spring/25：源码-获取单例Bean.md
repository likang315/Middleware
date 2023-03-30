### Spring IOC - 获取单例 Bean

------

[TOC]

##### 01：Bean 加载（getBean）的核心流程【九步】

- 核心类：org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean
- 通过 beanName 获取一个Bean，若不存在，则创建；

###### 转换对应beanName

- name 可能会以 & 字符开头，表明调用者想获取 FactoryBean 本身，而非 FactoryBean 实现类所创建的 bean。
- 将别名转换为最基本的beanName值；

###### 从缓存中加载单例【重要】

- **尝试加载：**首先尝试从缓存中加载，如果加载不成功则再次尝试从 singletonFactories 中加载
  - 在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候**为了避免循环依赖**，在Spring中创建bean的原则是**不等 bean 创建完成就会将创建 bean 的ObjectFactory提早曝光加入到缓存中，一旦下一个 bean 创建时候需要依赖上一个bean 则直接使用ObjectFactory**。

###### bean的实例化 

- 如果从缓存中得到了bean的原始状态，则需要对bean进行实例化。因为缓存中记录的只是最原始的bean状态，并不一定是我们最终想要的bean。

###### 原型模式的循环依赖检查 

- 只有在单例情况下才会尝试解决循环依赖，如果存在A中有B的属性，B中有A的属性，那么当依赖注入的时候，就会产生当A还未创建完的时候，因为对于B的创建再次返回创建A，造成循环依赖， 也就是情况： isPrototypeCurrentlyInCreation(beanName)判断true。

###### 从父容器（parentBeanFactory）中查找 Bean 实例

- 如果当前加载的 BeanFactory 中不包含beanName所对应的配置，就到 parentBeanFactory去尝试下，然后再去递归的调用getBean方法，找到直接返回。

###### 合并父类BeanDefination & 转化成RootBeanDefinition【重要】

- 转换为RootBeanDefinition，因为从XML配置文件中读取到的bean信息是存储在GernericBeanDefinition中的，但是所有的bean 后续处理都是针对于 **RootBeanDefinition** 的，所以这里需要进行一个转换，转换的同时如果父类bean不为空的话，则会合并父类的属性。

###### 检查是否有依赖，递归实例化依赖的Bean

- 因为bean的初始化过程中很可能会用到某些属性，而某些属性很可能是动态配置的，并且配置成依赖于其他的bean，那么这个时候就有必要先加载依赖的bean，所以，**在Spring的加载顺序中，在初始化某一个bean的时候首先会初始化这个bean所对应的依赖**。

###### 针对不同的scope进行bean的创建【重要】

-  Spring会根据不同的Scope 配置进行不同的初始化策略。singleton —>prototype —> 其他

###### 类型转换

- 使用类型转换器，将返回的 bean 转换为 requiredType 所指定的类型。

```java
protected <T> T doGetBean(
    final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)
    throws BeansException {
    // 1. 装换beanName，主要是解析别名、去掉FactoryBean的前缀“&”
    final String beanName = transformedBeanName(name);
    Object bean;

    // 2.尝试从缓存中获取beanName对应的实例
    Object sharedInstance = getSingleton(beanName);
    if (sharedInstance != null && args == null) {
        // 3.如果beanName的实例存在于缓存中
        if (logger.isDebugEnabled()) {
            if (isSingletonCurrentlyInCreation(beanName)) {
                logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
                             "' that is not fully initialized yet");
            } else {
                logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
            }
        }
        // 3.1 返回beanName对应的实例对象（用于FactoryBean的特殊处理，普通Bean会直接返回sharedInstance本身）
        bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
    } else {
        // 4.scope为prototype的循环依赖校验：如果beanName已经正在创建Bean实例中，
        // 而此时我们又要再一次创建beanName的实例，则代表出现了循环依赖，需要抛出异常。
        if (isPrototypeCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }
        // 5.获取parentBeanFactory
        BeanFactory parentBeanFactory = getParentBeanFactory();
        // 5.1 如果parentBeanFactory存在，并且beanName在当前BeanFactory不存在Bean定义，
        // 则尝试从parentBeanFactory中获取bean实例
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // 5.2 将别名解析成真正的beanName
            String nameToLookup = originalBeanName(name);
            // 5.3 尝试在parentBeanFactory中获取bean对象实例
            if (args != null) {
                return (T) parentBeanFactory.getBean(nameToLookup, args);
            } else {
                return parentBeanFactory.getBean(nameToLookup, requiredType);
            }
        }
        if (!typeCheckOnly) {
            // 6.如果不是仅仅做类型检测，而是创建bean实例，这里要将beanName放到alreadyCreated缓存
            markBeanAsCreated(beanName);
        }

        try {
            // 7.根据beanName重新获取MergedBeanDefinition
            final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
            // 7.1 检查MergedBeanDefinition
            checkMergedBeanDefinition(mbd, beanName, args);
            // 8.拿到当前bean依赖的bean名称集合，在实例化自己之前，需要先实例化自己依赖的bean
            String[] dependsOn = mbd.getDependsOn();
            if (dependsOn != null) {
                // 8.1 遍历当前bean依赖的bean名称集合
                for (String dep : dependsOn) {
                    // 8.2 检查dep是否依赖于beanName，即检查是否存在循环依赖
                    if (isDependent(beanName, dep)) {
                        // 8.3 如果是循环依赖则抛异常
                        throw new BeanCreationException(mbd.getResourceDescription(),beanName,
                                                        "Circular depends-on relationship between'"
                                                        + beanName + "'and'" + dep + "'");
                    }
                    // 8.4 将dep和beanName的依赖关系注册到缓存中
                    registerDependentBean(dep, beanName);
                    // 8.5 获取dep对应的bean实例，如果dep还没有创建bean实例，则创建dep的bean实例
                    getBean(dep);
                }
            }
            // 9. 针对不同的scope进行bean的创建
            if (mbd.isSingleton()) {
                // 9.1 scope为singleton的bean创建
                sharedInstance = getSingleton(beanName, () -> {
                    try {
                        // 9.1.1 创建Bean实例
                        return createBean(beanName, mbd, args);
                    } catch (BeansException ex) {
                        destroySingleton(beanName);
                        throw ex;
                    }
                });
                // 9.1.2 返回beanName对应的实例对象
                bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
            }
            else if (mbd.isPrototype()) {
                // 9.2 scope为prototype的bean创建
                Object prototypeInstance = null;
                try {
                    // 9.2.1 创建实例前的操作（将beanName保存到prototypesCurrentlyInCreation缓存中）
                    beforePrototypeCreation(beanName);
                    // 9.2.2 创建Bean实例
                    prototypeInstance = createBean(beanName, mbd, args);
                } finally {
                    // 9.2.3 创建实例后的操作（将创建完的beanName从prototypesCurrentlyInCreation缓存中移除）
                    afterPrototypeCreation(beanName);
                }
                // 9.2.4 返回beanName对应的实例对象
                bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
            } else {
                // 9.3 其他scope的bean创建，可能是request之类的
                // 9.3.1 根据scopeName，从缓存拿到scope实例
                String scopeName = mbd.getScope();
                if (!StringUtils.hasLength(scopeName)) {
                    throw new IllegalStateException(
                        "No scope name defined for bean ´" + beanName + "'");
                }
                Scope scope = this.scopes.get(scopeName);
                if (scope == null) {
                    throw new IllegalStateException(
                        "No Scope registered for scope name '" + scopeName + "'");
                }
                try {
					// 9.3.2 其他scope的bean创建
                    Object scopedInstance = scope.get(beanName, () -> {
                        beforePrototypeCreation(beanName);
                        try {
                            return createBean(beanName, mbd, args);
                        } finally {
                            afterPrototypeCreation(beanName);
                        }
                    });
                    bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
                } catch (IllegalStateException ex) {
                    throw new BeanCreationException(beanName,
                      "Scope '" + scopeName + "' is not active for the current thread; consider " +
                      "defining a scoped proxy for this bean if you intend to refer singleton", ex);
                }
            }
        } catch (BeansException ex) {
            // 如果创建bean实例过程中出现异常，则将beanName从alreadyCreated缓存中移除
            cleanupAfterBeanCreationFailure(beanName);
            throw ex;
        }
    }
    // 10.检查所需类型是否与实际的bean对象的类型匹配
    if (requiredType != null && bean != null && !requiredType.isInstance(bean)) {
        try {
            // 10.1 类型不对，则尝试转换bean类型
            return getTypeConverter().convertIfNecessary(bean, requiredType);
        } catch (TypeMismatchException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to convert bean '" + name + "' to required type '" +
                             ClassUtils.getQualifiedName(requiredType) + "'", ex);
            }
            throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
        }
    }
    // 11.返回创建出来的bean实例对象
    return (T) bean;
}
```

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

##### 04：从 FactoryBean 中获取 bean 实例

- Spring提供了一个 org.Springframework.bean.factory.FactoryBean的工厂类接口，用户可以通过**实现该接口定制实例化bean** 的逻辑。 是一种可以产生Bean的工厂。

- org.springframework.beans.factory.support.AbstractBeanFactory#getObjectForBeanInstance

- ```java
  protected Object getObjectForBeanInstance(
      Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {
      // 1. 如果 name 以 & 开头（工厂引用），但 beanInstance 却不是 FactoryBean，则抛异常
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
     // 2.1 如果beanInstance不是FactoryBean（也就是普通bean），则直接返回beanInstance
     // 2.2 如果beanInstance是FactoryBean，并且name以“&”为前缀，则直接返回beanInstance
     // （以“&”为前缀代表想获取的是FactoryBean本身）
      if (!(beanInstance instanceof FactoryBean)) {
          return beanInstance;
      }
      // 3.代表beanInstance是FactoryBean，但name不带有“&”前缀，表示想要获取的是FactoryBean创建的对象实例
      Object object = null;
      if (mbd != null) {
          mbd.isFactoryBean = true;
      } else {
          // 4.如果mbd为空，则尝试从factoryBeanObjectCache缓存中获取该FactoryBean创建的对象实例
          object = getCachedObjectForFactoryBean(beanName);
      }
      if (object == null) {
          // 5.只有beanInstance是FactoryBean才能走到这边，因此直接强转
          FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
          if (mbd == null && containsBeanDefinition(beanName)) {
          	// 6.mbd为空，但是该bean的BeanDefinition在缓存中存在，则获取该bean的MergedBeanDefinition
              mbd = getMergedLocalBeanDefinition(beanName);
          }
          // 7.是否合成的BeanDefinition (正常情况下都是false)
          boolean synthetic = (mbd != null && mbd.isSynthetic());
          // 8.从FactoryBean获取对象实例
          object = getObjectFromFactoryBean(factory, beanName, !synthetic);
      }
      return object;
  }
  ```

###### 04 - 1：getObjectFromFactoryBean

- ```java
  protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
      /*
       * FactoryBean 也有单例和非单例之分，针对不同类型的 FactoryBean，这里有两种处理方式：
       *   1. 单例 FactoryBean 生成的 bean 实例也认为是单例类型。需放入缓存中，供后续重复使用
       *   2. 非单例 FactoryBean 生成的 bean 实例则不会被放入缓存中，每次都会创建新的实例
       */
      if (factory.isSingleton() && containsSingleton(beanName)) {
          synchronized (getSingletonMutex()) {
   	        // 2.从FactoryBean创建的单例对象的缓存中获取该bean实例
              Object object = this.factoryBeanObjectCache.get(beanName);
              if (object == null) {
                  // 3.调用FactoryBean的getObject方法获取对象实例
                  object = doGetObjectFromFactoryBean(factory, beanName);
                  Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                  // 4.如果该beanName已经在缓存中存在，则将object替换成缓存中的
                  if (alreadyThere != null) {
                      object = alreadyThere;
                  } else {
                      if (shouldPostProcess) {
                          if (isSingletonCurrentlyInCreation(beanName)) {
                              return object;
                          }
                          beforeSingletonCreation(beanName);
                          try {
                              // 5.对bean实例进行后置处理，执行所有已注册的BeanPostProcessor
                              object = postProcessObjectFromFactoryBean(object, beanName);
                          } catch (Throwable ex) {
                              throw new BeanCreationException(beanName,
                               "Post-processing of FactoryBean's singleton object failed", ex);
                          } finally {
                              afterSingletonCreation(beanName);
                          }
                      }
                      if (containsSingleton(beanName)) {
                          // 6.将beanName和object放到factoryBeanObjectCache缓存中
                          this.factoryBeanObjectCache.put(beanName, object);
                      }
                  }
              }
              // 7.返回object对象实例
              return object;
          }
      } else {
          // 8.调用FactoryBean的getObject方法获取对象实例
          Object object = doGetObjectFromFactoryBean(factory, beanName);
          if (shouldPostProcess) {
              try {
                  // 9.对bean实例进行后置处理
                  object = postProcessObjectFromFactoryBean(object, beanName);
              } catch (Throwable ex) {
                  throw new BeanCreationException(beanName,
                  "Post-processing of FactoryBean's object failed", ex);
              }
          }
          return object;
      }
  }
  ```

###### 04 - 2：doGetObjectFromFactoryBean

- ```java
  private Object doGetObjectFromFactoryBean(FactoryBean<?> factory, String beanName) throws BeanCreationException {
      Object object;
      try {
          // 1. spring 权限检查，带权限，调用FactoryBean的getObject方法获取bean对象实例
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
              // 2.没有权限
              object = factory.getObject();
          }
      } catch (FactoryBeanNotInitializedException ex) {
          throw new BeanCurrentlyInCreationException(beanName, ex.toString());
      } catch (Throwable ex) {
          throw new BeanCreationException(beanName,
                                          "FactoryBean threw exception on object creation", ex);
      }
      // 2.getObject返回的是空值，并且该FactoryBean正在初始化中，则直接抛异常，
      // 不接受一个尚未完全初始化的FactoryBean的getObject返回的空值
      if (object == null) {
          if (isSingletonCurrentlyInCreation(beanName)) {
              throw new BeanCurrentlyInCreationException(beanName,
                                                         "FactoryBean which is currently in creation 
                                                         "returned null from getObject");
          }
          object = new NullBean();
      }
      // 3.返回创建好的bean对象实例
      return object;
  }
  ```

##### 05：markBeanAsCreated

- ```java
  protected void markBeanAsCreated(String beanName) {
      if (!this.alreadyCreated.contains(beanName)) {
          synchronized (this.mergedBeanDefinitions) {
              // 1.如果alreadyCreated缓存中不包含beanName
              if (!this.alreadyCreated.contains(beanName)) {
                  // 2.将beanName的MergedBeanDefinition从mergedBeanDefinitions缓存中移除，
                  // 在之后重新创建 MergedBeanDefinition，避免BeanDefinition在创建过程中发生变化
                  clearMergedBeanDefinition(beanName);
                  // 3.将beanName添加到alreadyCreated缓存中，代表该beanName的bean实例已经创建（或即将创建）
                  this.alreadyCreated.add(beanName);
              }
          }
      }
  }
   
  protected void clearMergedBeanDefinition(String beanName) {
      this.mergedBeanDefinitions.remove(beanName);
  }
  ```

##### 06：合并父 BeanDefinition & 转换成RootBeanDefination

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

##### 07：isDependent：是否依赖【隔层依赖】

- ```java
  private boolean isDependent(String beanName, String dependentBeanName, Set<String> alreadySeen) {
      // 已经检查过的直接跳过
      if (alreadySeen != null && alreadySeen.contains(beanName)) {
          return false;
      }
      // 1.将别名解析为真正的名称
      String canonicalName = canonicalName(beanName);
      // 2.拿到依赖canonicalName的beanName集合
      Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
      // 3.如果dependentBeans为空，则两者必然还未确定依赖关系，返回false
      if (dependentBeans == null) {
          return false;
      }
      // 4.如果dependentBeans包含dependentBeanName，则表示两者已确定依赖关系，返回true
      if (dependentBeans.contains(dependentBeanName)) {
          return true;
      }
      // 5.循环检查，即检查依赖canonicalName的所有beanName是否存在被dependentBeanName依赖的（即隔层依赖）
      for (String transitiveDependency : dependentBeans) {
          if (alreadySeen == null) {
              alreadySeen = new HashSet<String>();
          }
          // 6.已经检查过的添加到alreadySeen，避免重复检查
          alreadySeen.add(beanName);
          if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
              return true;
          }
      }
      return false;
  }
  ```

##### 08：registerDependentBean

- dependenciesForBeanMap：beanName -> beanName 对应的 bean 依赖的所有 bean 的 beanName 集合。

  ```java
  public void registerDependentBean(String beanName, String dependentBeanName) {
      // 1.解析别名
      String canonicalName = canonicalName(beanName);
      // 2.拿到依赖canonicalName的beanName集合
      Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
      // 3.如果dependentBeans包含dependentBeanName，则表示依赖关系已经存在，直接返回
      if (dependentBeans != null && dependentBeans.contains(dependentBeanName)) {
          return;
      }
      // 4.如果依赖关系还没有注册，则将两者的关系注册到dependentBeanMap和dependenciesForBeanMap缓存
      synchronized (this.dependentBeanMap) {
          // 4.1 将dependentBeanName添加到依赖canonicalName的beanName集合中
          dependentBeans = this.dependentBeanMap.get(canonicalName);
          if (dependentBeans == null) {
              dependentBeans = new LinkedHashSet<String>(8);
              this.dependentBeanMap.put(canonicalName, dependentBeans);
          }
          dependentBeans.add(dependentBeanName);
      }
      synchronized (this.dependenciesForBeanMap) {
          // 4.2 将canonicalName添加到dependentBeanName依赖的beanName集合中
          Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(dependentBeanName);
          if (dependenciesForBean == null) {
              dependenciesForBean = new LinkedHashSet<String>(8);
              this.dependenciesForBeanMap.put(dependentBeanName, dependenciesForBean);
          }
          dependenciesForBean.add(canonicalName);
      }
  }
  ```

##### 09：getSingleton

- ```java
  public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
      Assert.notNull(beanName, "'beanName' must not be null");
      // 1.加锁，避免重复创建单例对象
      synchronized (this.singletonObjects) {
          // 2.首先检查beanName对应的bean实例是否在缓存中存在，如果已经存在，则直接返回
          Object singletonObject = this.singletonObjects.get(beanName);
          if (singletonObject == null) {
              // 3.beanName对应的bean实例不存在于缓存中，则进行Bean的创建
              if (this.singletonsCurrentlyInDestruction) {
                  // 4.当bean工厂的单例处于destruction状态时，不允许进行单例bean创建，抛出异常
                  throw new BeanCreationNotAllowedException(beanName,"Singleton bean creation not 
                       "allowed while singletons of this factory are in destruction ");
              }
              if (logger.isDebugEnabled()) {
                  logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
              }
              // 5.创建单例前的操作
              beforeSingletonCreation(beanName);
              boolean newSingleton = false;
              // suppressedExceptions用于记录异常相关信息
              boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
              if (recordSuppressedExceptions) {
                  this.suppressedExceptions = new LinkedHashSet<Exception>();
              }
              try {
                  // 6.执行singletonFactory的getObject方法获取bean实例
                  singletonObject = singletonFactory.getObject();
                  // 标记为新的单例对象
                  newSingleton = true;
              } catch (IllegalStateException ex) {
                  singletonObject = this.singletonObjects.get(beanName);
                  if (singletonObject == null) {
                      throw ex;
                  }
              } catch (BeanCreationException ex) {
                  if (recordSuppressedExceptions) {
                      for (Exception suppressedException : this.suppressedExceptions) {
                          ex.addRelatedCause(suppressedException);
                      }
                  }
                  throw ex;
              } finally {
                  if (recordSuppressedExceptions) {
                      this.suppressedExceptions = null;
                  }
                  // 7.创建单例后的操作
                  afterSingletonCreation(beanName);
              }
              if (newSingleton) {
                  // 8.如果是新的单例对象，将beanName和对应的bean实例添加到缓存中
                  //（singletonObjects、registeredSingletons）
                  addSingleton(beanName, singletonObject);
              }
          }
          // 9.返回创建出来的单例对象
          return (singletonObject != NULL_OBJECT ? singletonObject : null);
      }
  }
  ```

###### 09 - 1：beforeSingletonCreation、afterSingletonCreation

- ```java
  protected void beforeSingletonCreation(String beanName) {
      // 先校验beanName是否为要在创建检查排除掉的（inCreationCheckExclusions缓存），如果不是，
      // 则将beanName加入到正在创建bean的缓存中（Set），如果beanName已经存在于该缓存，会返回false抛出异常
      //（这种情况出现在构造器的循环依赖）
      if (!this.inCreationCheckExclusions.contains(beanName)
          && !this.singletonsCurrentlyInCreation.add(beanName)) {
          throw new BeanCurrentlyInCreationException(beanName);
      }
  }
  
  protected void afterSingletonCreation(String beanName) {
      // 先校验beanName是否为要在创建检查排除掉的（inCreationCheckExclusions缓存），如果不是，
      // 则将beanName从正在创建bean的缓存中（Set）移除，如果beanName不存在于该缓存，会返回false抛出异常
      if (!this.inCreationCheckExclusions.contains(beanName)
          && !this.singletonsCurrentlyInCreation.remove(beanName)) {
          throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
      }
  }
  ```

###### 09 - 2：addSingleton

- ```java
  protected void addSingleton(String beanName, Object singletonObject) {
      synchronized (this.singletonObjects) {
          // 1.添加到单例对象缓存
          this.singletonObjects.put(beanName, (singletonObject != null ?
                                               singletonObject : NULL_OBJECT));
          // 2.将单例工厂缓存移除（已经不需要）
          this.singletonFactories.remove(beanName);
          // 3.将早期单例对象缓存移除（已经不需要）
          this.earlySingletonObjects.remove(beanName);
          // 4.添加到已经注册的单例对象缓存
          this.registeredSingletons.add(beanName);
      }
  }
  ```
