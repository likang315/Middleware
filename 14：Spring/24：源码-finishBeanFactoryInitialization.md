### 源码 - finishBeanFactoryInitialization

------

[TOC]

##### 01：概述

- finishBeanFactoryInitialization 是这四个方法中最复杂也是最重要的，是整个 Spring IoC 核心中的核心。
- 该方法**会实例化所有剩余的非懒加载单例 bean**。除了一些内部的 bean、实现了 BeanFactoryPostProcessor 接口的 bean、实现了 BeanPostProcessor 接口的 bean，其他的非懒加载单例 bean 都会在这个方法中被实例化，并且 BeanPostProcessor 的触发也是在这个方法中。

##### 02：finishBeanFactoryInitialization

- LoadTimeWeaver 是一个用于**在运行时（runtime）进行类加载和转换的机制**，它可以在应用程序启动时，通过代理或者其他机制来改变类的字节码，从而**实现类的增强或者替换**。

  ```java
  protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
      // 1.初始化此上下文的转换服务
      if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
          beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
          beanFactory.setConversionService(
              beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
      }
      // 2.如果beanFactory之前没有注册嵌入值解析器，则注册默认的嵌入值解析器：主要用于注解属性值的解析。
      if (!beanFactory.hasEmbeddedValueResolver()) {
          beanFactory.addEmbeddedValueResolver(
              strVal -> getEnvironment().resolvePlaceholders(strVal));
      }
      // 3.初始化LoadTimeWeaverAware Bean实例对象
      String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class,
                                                                  false, false);
      for (String weaverAwareName : weaverAwareNames) {
          getBean(weaverAwareName);
      }
      // Stop using the temporary ClassLoader for type matching.
      beanFactory.setTempClassLoader(null);
      // 4.冻结所有bean定义，注册的bean定义不会被修改或进一步后处理，因为马上要创建 Bean 实例对象了
      beanFactory.freezeConfiguration();
      // 5.实例化所有剩余（非懒加载）单例对象
      beanFactory.preInstantiateSingletons();
  }
  ```

##### 03：preInstantiateSingletons

```java
@Override
public void preInstantiateSingletons() throws BeansException {
    if (logger.isTraceEnabled()) {
        logger.trace("Pre-instantiating singletons in " + this);
    }
    // 1.创建beanDefinitionNames的副本beanNames用于后续的遍历，以允许init等方法注册新的bean定义
    List<String> beanNames = new ArrayList<String>(this.beanDefinitionNames);
    // 2.遍历beanNames，触发所有非懒加载单例bean的初始化
    for (String beanName : beanNames) {
        // 3.获取beanName对应的MergedBeanDefinition
        RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
        // 4.bd对应的Bean实例：不是抽象类 && 是单例 && 不是懒加载
        if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
            // 5.判断beanName对应的bean是否为FactoryBean
            if (isFactoryBean(beanName)) {
                // 5.1 通过beanName获取FactoryBean实例, 通过getBean(&beanName)拿到的是FactoryBean本身；
                // 通过getBean(beanName)拿到的是FactoryBean创建的Bean实例
                Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                if (bean instanceof FactoryBean) {
                    FactoryBean<?> factory = (FactoryBean<?>) bean;
                    // 5.2 判断这个FactoryBean是否希望急切的初始化
                    boolean isEagerInit;
                    if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
                        isEagerInit = AccessController.doPrivileged(
                            (PrivilegedAction<Boolean>) ((SmartFactoryBean<?>) factory)::isEagerInit,
                            getAccessControlContext());
                    } else {
                        isEagerInit = (factory instanceof SmartFactoryBean &&
                                       ((SmartFactoryBean<?>) factory).isEagerInit());
                    }
                    // 5.3 如果希望急切的初始化，则通过beanName获取bean实例
                    if (isEagerInit) {
                        getBean(beanName);
                    }
                }
            } else {
                // 6.如果beanName对应的bean不是FactoryBean，只是普通Bean，通过beanName获取bean实例
                getBean(beanName);
            }
        }
    }
    // 7.遍历beanNames，触发所有SmartInitializingSingleton的后初始化回调
    for (String beanName : beanNames) {
        // 7.1 拿到beanName对应的bean实例
        Object singletonInstance = getSingleton(beanName);
        // 7.2 判断singletonInstance是否实现了SmartInitializingSingleton接口
        if (singletonInstance instanceof SmartInitializingSingleton) {
            final SmartInitializingSingleton smartSingleton = 
                (SmartInitializingSingleton) singletonInstance;
            // 7.3 扩展点，触发SmartInitializingSingleton实现类的afterSingletonsInstantiated方法
            if (System.getSecurityManager() != null) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    smartSingleton.afterSingletonsInstantiated();
                    return null;
                }, getAccessControlContext());
            } else {
                smartSingleton.afterSingletonsInstantiated();
            }
        }
    }
}
```

##### 04：getMergedLocalBeanDefinition

- ```java
  protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
      // 1.检查beanName对应的MergedBeanDefinition是否存在于缓存中
      RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
      if (mbd != null) {
          // 2.如果存在于缓存中则直接返回
          return mbd;
      }
      // 3.如果不存在于缓存中
      // 3.1 getBeanDefinition(beanName)： 获取beanName对应的BeanDefinition，beanDefinitionMap缓存中获取
      // 3.2 getMergedBeanDefinition: 根据beanName和对应的BeanDefinition，获取MergedBeanDefinition
      return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
  }
  ```
  

##### 05：getMergedBeanDefinition

- ```java
  protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd, 
                      BeanDefinition containingBd) throws BeanDefinitionStoreException {
      // 1.加锁再进行操作
      synchronized (this.mergedBeanDefinitions) {
          // 用于存储bd的MergedBeanDefinition，也就是该方法的结果
          RootBeanDefinition mbd = null;
          if (containingBd == null) {
              // 2.检查beanName对应的MergedBeanDefinition是否存在于缓存中
              mbd = this.mergedBeanDefinitions.get(beanName);
          }
          // 3.如果beanName对应的MergedBeanDefinition不存在于缓存中 或者 需要重新 merge
          if (mbd == null || mbd.stale) {
              if (bd.getParentName() == null) {
                  // 4.如果bd的parentName为空，代表bd没有父定义，无需与父定义进行合并操作，
                  // 也就是bd的MergedBeanDefinition就是bd本身（可能需要转成RootBeanDefinition）
                  if (bd instanceof RootBeanDefinition) {
                      // 4.1 如果bd的类型为RootBeanDefinition，则bd的MergedBeanDefinition就是bd本身
                      mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
                  } else {
                      // 4.2 否则，将bd作为参数，构建一个RootBeanDefinition。
                      // 正常使用下，BeanDefinition在被加载后是GenericBeanDefinition
                      // 或ScannedGenericBeanDefinition
                      mbd = new RootBeanDefinition(bd);
                  }
              } else {
                  // 5.否则，bd存在父定义，需要与父定义合并
                  BeanDefinition pbd;
                  try {
                      // 5.1 获取父定义的beanName
                      String parentBeanName = transformedBeanName(bd.getParentName());
                      // 5.2 如果父定义的beanName与该bean的beanName不同
                      if (!beanName.equals(parentBeanName)) {
                          // 5.3 获取父定义的MergedBeanDefinition
                          pbd = getMergedBeanDefinition(parentBeanName);
                      } else {
                          // 5.4 如果父定义的beanName与bd的beanName相同，则拿到父BeanFactory，只有在存在
                          // 父BeanFactory的情况下，才允许父定义beanName与自己相同，否则就是将自己设置为父定义
                          BeanFactory parent = getParentBeanFactory();
                          if (parent instanceof ConfigurableBeanFactory) {
                              // 5.5 如果父BeanFactory是ConfigurableBeanFactory，
                              // 则通过父BeanFactory获取父定义的MergedBeanDefinition
                              pbd = ((ConfigurableBeanFactory) parent)
                                  .getMergedBeanDefinition(parentBeanName);
                          } else {
                              // 5.6 如果父BeanFactory不是ConfigurableBeanFactory，则抛异常
                              throw new NoSuchBeanDefinitionException(parentBeanName,
                                "Parent name '" + parentBeanName + "' is equal to bean name '" + 							beanName +"': cannot be resolved without an AbstractBeanFactory parent");
                          }
                      }
                  } catch (NoSuchBeanDefinitionException ex) {
                      throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
                      "Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
                  }
                  // 5.7 使用父定义pbd构建一个新的RootBeanDefinition对象（深拷贝）
                  mbd = new RootBeanDefinition(pbd);
                  // 5.8 使用bd覆盖父定义
                  mbd.overrideFrom(bd);
              }
              // 6.如果没有配置scope，则设置成默认的singleton
              if (!StringUtils.hasLength(mbd.getScope())) {
                  mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
              }
              // 7.如果containingBd不为空 && containingBd不为singleton && mbd为singleton，
              // 则将mdb的scope设置为containingBd的scope
              if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
                  mbd.setScope(containingBd.getScope());
              }
              // 8.将beanName与mbd放到mergedBeanDefinitions缓存，以便之后可以直接使用
              if (containingBd == null && isCacheBeanMetadata()) {
                  this.mergedBeanDefinitions.put(beanName, mbd);
              }
          }
          // 9.返回MergedBeanDefinition
          return mbd;
      }
  }
  ```

##### 06：FactoryBean 介绍

- FactoryBean 是一种特殊的 bean，它是个**工厂 bean**，允许开发人员通过提供工厂方法来创建和配置复杂对象；

- 为了**区分 “FactoryBean” 和 “FactoryBean 创建的 bean 实例”，Spring 使用了 “&” 前缀**。假设我们的 beanName 为 apple，则 getBean("apple") 获得的是 AppleFactoryBean 通过 getObject() 方法创建的 bean 实例；而 getBean("&apple") 获得的是 AppleFactoryBean 本身。

- ```java
  public class AppleFactoryBean implements FactoryBean<Apple> {
      @Override
      public Apple getObject() throws Exception {
          Apple apple = new Apple();
          apple.setName("bigApple");
          return apple;
      }
      @Override
      public Class<?> getObjectType() {
          return Apple.class;
      }
      @Override
      public boolean isSingleton() {
          return true;
      }
  }
  ```

##### 07：isFactoryBean

- ```java
  public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
      // 1.拿到真正的beanName（去掉&前缀、解析别名）
      String beanName = transformedBeanName(name);
      // 2.尝试从缓存获取Bean实例对象
      Object beanInstance = getSingleton(beanName, false);
      if (beanInstance != null) {
          // 3.beanInstance存在，则直接判断类型是否为FactoryBean
          return (beanInstance instanceof FactoryBean);
      }
  
      if (!containsBeanDefinition(beanName) 
          && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
          // 4.如果缓存中不存在此beanName && 父beanFactory是ConfigurableBeanFactory
          // 则调用父BeanFactory判断是否为FactoryBean
          return ((ConfigurableBeanFactory) getParentBeanFactory()).isFactoryBean(name);
      }
      // 6.通过MergedBeanDefinition来检查beanName对应的Bean是否为FactoryBean
      return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
  }
  ```

##### 08：getSingleton：从缓存中获取对象

- ```java
  protected Object getSingleton(String beanName, boolean allowEarlyReference) {
      // 1.从单例对象缓存中获取beanName对应的单例对象
      Object singletonObject = this.singletonObjects.get(beanName);
      // 2.如果单例对象缓存中没有，并且该beanName对应的单例bean正在创建中
      if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
          // 3.加锁进行操作
          synchronized (this.singletonObjects) {
              // 4.从早期单例对象缓存中获取单例对象（之所称成为早期单例对象，是因为earlySingletonObjects里
              // 的对象的都是通过提前曝光的ObjectFactory创建出来的，还未进行属性填充等操作）
              singletonObject = this.earlySingletonObjects.get(beanName);
              // 5.如果在早期单例对象缓存中也没有，并且允许创建早期单例对象引用
              if (singletonObject == null && allowEarlyReference) {
                  // 6.从单例工厂缓存中获取beanName的单例工厂
                  ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                  if (singletonFactory != null) {
                      // 7.如果存在单例对象工厂，则通过工厂创建一个单例对象
                      singletonObject = singletonFactory.getObject();
                      // 8.将通过单例对象工厂创建的单例对象，放到早期单例对象缓存中
                      this.earlySingletonObjects.put(beanName, singletonObject);
                      // 9.移除该beanName对应的单例对象工厂，因为该单例工厂已经创建了一个实例对象
                     	// 并且放到earlySingletonObjects缓存了，因此，后续获取beanName的单例对象，可以通过
                      // earlySingletonObjects缓存拿到，不需要在用到该单例工厂
                      this.singletonFactories.remove(beanName);
                  }
              }
          }
      }
      // 10.返回单例对象
      return singletonObject;
  }
  
  public boolean isSingletonCurrentlyInCreation(String beanName) {
      return this.singletonsCurrentlyInCreation.contains(beanName);
  }
  ```

##### 09：isFactoryBean(String beanName, RootBeanDefinition mbd)

```java
protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
   Boolean result = mbd.isFactoryBean;
   if (result == null) {
      // 1.拿到beanName对应的Bean实例的类型
      Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);    
      // 2.返回beanType是否为FactoryBean本身、子类或子接口
      result = (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
      mbd.isFactoryBean = result;
   }
   return result;
}
```





