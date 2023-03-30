### Spring IOC - 创建单例Bean

------

[TOC]

##### 01：创建 Bean 对象

- org.springframework.beans.factory.support.**AbstractAutowireCapableBeanFactory**#createBean(java.lang.String, org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Object[])

- ```java
  @Override
  protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
      throws BeanCreationException {
  
      if (logger.isTraceEnabled()) {
          logger.trace("Creating instance of bean '" + beanName + "'");
      }
      RootBeanDefinition mbdToUse = mbd;
  
      // 1.解析beanName对应的Bean的类型，例如：com.xup.service.impl.UserServiceImpl
      Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
      if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
          // 如果resolvedClass存在，并且mdb的beanClass类型不是Class，并且mdb的beanClass不为空
          // （则代表beanClass存的是Class的name）
          // 则使用mdb深拷贝一个新的RootBeanDefinition副本，并且将解析的Class赋值给拷贝的副本的beanClass属性
          mbdToUse = new RootBeanDefinition(mbd);
          mbdToUse.setBeanClass(resolvedClass);
      }
      
      try {
          // 2. 验证及准备覆盖的方法（对override属性进行标记及验证）
          mbdToUse.prepareMethodOverrides();
      } catch (BeanDefinitionValidationException ex) {
          throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
               beanName, "Validation of method overrides failed", ex);
      }
  
      try {
          // 3.实例化前处理，给InstantiationAwareBeanPostProcessor一个机会返回代理对象来替代真正的bean实例
          // 从而达到短路的效果
          Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
          // 4.如果bean不为空，则会跳过Spring默认的实例化过程，直接使用返回的bean
          if (bean != null) {
              return bean;
          }
      } catch (Throwable ex) {
          throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                "BeanPostProcessor before instantiation of bean failed", ex);
      }
  
      try {
          // 5.真正创建Bean实例
          Object beanInstance = doCreateBean(beanName, mbdToUse, args);
          if (logger.isTraceEnabled()) {
              logger.trace("Finished creating instance of bean '" + beanName + "'");
          }
          return beanInstance;
      } catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
          throw ex;
      } catch (Throwable ex) {
          throw new BeanCreationException(mbdToUse.getResourceDescription(),
                      beanName, "Unexpected exception during bean creation", ex);
      }
  }
  ```

##### 02：实例化前处理

- org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#resolveBeforeInstantiation

- ```java
  protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
     	Object bean = null;
      if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
          // 1.mbd不是合成的，并且BeanFactory中存在InstantiationAwareBeanPostProcessor
          if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
              // 2.解析beanName对应的Bean实例的类型
              Class<?> targetType = determineTargetType(beanName, mbd);
              if (targetType != null) {
                  // 3.实例化前的后置处理器应用（处理InstantiationAwareBeanPostProcessor）
                  bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                  if (bean != null) {
                      // 4.如果返回的bean不为空，会跳过Spring默认的实例化过程，
                      // 所以只能在这里调用BeanPostProcessor实现类的postProcessAfterInitialization方法
                      bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                  }
              }
          }
          // 5.如果bean不为空，则将beforeInstantiationResolved赋值为true，代表在实例化之前已经解析
          mbd.beforeInstantiationResolved = (bean != null);
      }
      return bean;
  }
  ```

###### 02 - 1：实例化前处理，返回代理对象，达到短路效果

- org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInstantiation

- ```java
  protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
      // 1.遍历当前BeanFactory中的BeanPostProcessor
      for (BeanPostProcessor bp : getBeanPostProcessors()) {
          // 2.应用InstantiationAwareBeanPostProcessor后置处理器，
          // 允许postProcessBeforeInstantiation方法返回bean对象的代理
          if (bp instanceof InstantiationAwareBeanPostProcessor) {
              InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
              // 3.执行postProcessBeforeInstantiation方法，在Bean实例化前操作，
              // 返回一个构造完成的Bean实例，从而不会继续执行创建Bean实例的“正规的流程”，达到“短路”的效果。
              Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
              if (result != null) {
                  // 4.如果result不为空，也就是有后置处理器返回了bean实例对象，则会跳过Spring默认的实例化过程
                  return result;
              }
          }
      }
      return null;
  }
  ```

###### 02 - 2：实例化后处理

- org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsAfterInitialization

- ```java
  @Override
  public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
      throws BeansException {
      Object result = existingBean;
      // 1.遍历所有注册的BeanPostProcessor实现类，调用postProcessAfterInitialization方法
      for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
          // 2.在bean初始化后，调用postProcessAfterInitialization方法
          result = beanProcessor.postProcessAfterInitialization(result, beanName);
          if (result == null) {
              // 3.如果返回null，则不会调用后续的BeanPostProcessors
              return result;
          }
      }
      return result;
  }
  ```

##### 03：真正的创建对象【核心方法】

- org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#doCreateBean

- ```java
  protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd,
                                final Object[] args) throws BeanCreationException {
      // 1.新建Bean包装类
      BeanWrapper instanceWrapper = null;
      if (mbd.isSingleton()) {
          // 2.如果是FactoryBean，则需要先移除未完成的FactoryBean实例的缓存
          instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
      }
      if (instanceWrapper == null) {
          // 3.根据beanName、mbd、args，使用对应的策略创建Bean实例，并返回包装类BeanWrapper
          instanceWrapper = createBeanInstance(beanName, mbd, args);
      }
      // 4.拿到创建好的Bean实例
      final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);
      // 5.拿到Bean实例的类型
      Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);
      mbd.resolvedTargetType = beanType;
      synchronized (mbd.postProcessingLock) {
          if (!mbd.postProcessed) {
              try {
                  // 6.应用后置处理器MergedBeanDefinitionPostProcessor，允许修改MergedBeanDefinition，
                  // Autowired注解 正是通过此方法实现注入类型的预解析
                  applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
              } catch (Throwable ex) {
                  throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                       "Post-processing of merged bean definition failed", ex);
              }
              mbd.postProcessed = true;
          }
      }
      // 7.判断是否需要提早曝光实例：单例 && 允许循环依赖 && 当前bean正在创建中
      boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
                                        isSingletonCurrentlyInCreation(beanName));
      if (earlySingletonExposure) {
          if (logger.isDebugEnabled()) {
              logger.debug("Eagerly caching bean '" + beanName +
                           "' to allow for resolving potential circular references");
          }
          // 8.提前曝光beanName的 ObjectFactory，用于解决循环引用
          // 8.1 应用后置处理器SmartInstantiationAwareBeanPostProcessor
          // 允许返回指定bean的早期引用，若没有则直接返回bean
         addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
      }
  
      // 初始化bean实例。
      Object exposedObject = bean;
      try {
          // 9.对bean进行属性填充；其中，可能存在依赖于其他bean的属性，则会递归初始化依赖的bean实例
          populateBean(beanName, mbd, instanceWrapper);
          if (exposedObject != null) {
              // 10.对bean进行初始化
              exposedObject = initializeBean(beanName, exposedObject, mbd);
          }
      } catch (Throwable ex) {
          if (ex instanceof BeanCreationException
              && beanName.equals(((BeanCreationException) ex).getBeanName())) {
              throw (BeanCreationException) ex;
          } else {
              throw new BeanCreationException(
                  mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
          }
      }
      
      if (earlySingletonExposure) {
          // 11.如果允许提前曝光实例，则进行循环依赖检查
          Object earlySingletonReference = getSingleton(beanName, false);
          // 11.1 earlySingletonReference只有在当前解析的bean存在循环依赖的情况下才会不为空
          if (earlySingletonReference != null) {
              if (exposedObject == bean) {
                  // 11.2 如果exposedObject没有在initializeBean方法中被增强，则不影响之前的循环引用
                  exposedObject = earlySingletonReference;
              } else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                  // 11.3 如果exposedObject在initializeBean方法中被增强
                  // && 不允许在循环引用的情况下使用注入原始bean实例 && 当前bean有被其他bean依赖
                  // 11.4 拿到依赖当前bean的所有bean的beanName数组
                  String[] dependentBeans = getDependentBeans(beanName);
                  Set<String> actualDependentBeans = 
                      new LinkedHashSet<String>(dependentBeans.length);
                  for (String dependentBean : dependentBeans) {
                      // 11.5 尝试移除这些bean的实例，因为这些bean依赖的bean已经被增强了
                      // 他们依赖的bean相当于脏数据
                      if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                          // 11.6 移除失败的添加到 actualDependentBeans
                          actualDependentBeans.add(dependentBean);
                      }
                  }
                  if (!actualDependentBeans.isEmpty()) {
                      // 11.7 如果存在移除失败的，则抛出异常，因为存在bean依赖了“脏数据”
                      throw new BeanCurrentlyInCreationException(beanName,"Bean with name '" + 							beanName + "' has been injected into other beans ...for example.");
                  }
              }
          }
      }
      try {
          // 12.注册用于销毁的bean，执行销毁操作的有三种：
          // 自定义destroy方法、DisposableBean接口、DestructionAwareBeanPostProcessor
          registerDisposableBeanIfNecessary(beanName, bean, mbd);
      } catch (BeanDefinitionValidationException ex) {
          throw new BeanCreationException(
              mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
      }
      // 13.完成创建并返回
      return exposedObject;
  }
  ```

##### 04：选择创建实例的方法

- org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBeanInstance

- 选择创建实例的方法：工厂方法、构造函数自动装配（通常指带有参数的构造函数）、简单实例化（默认的构造函数）

- ```java
  protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
      // 解析bean的类型信息
      Class<?> beanClass = resolveBeanClass(mbd, beanName);
      if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers())
          && !mbd.isNonPublicAccessAllowed()) {
          // beanClass不为空 && beanClass不是public修饰 && 该bean不允许访问非公共构造函数和方法，则抛异常
          throw new BeanCreationException(mbd.getResourceDescription(), beanName,
             "Bean class isn't public, and non-public access not allowed: " +  beanClass.getName());
      }
      // 1.如果存在工厂方法则使用工厂方法实例化bean对象
      if (mbd.getFactoryMethodName() != null) {
          return instantiateUsingFactoryMethod(beanName, mbd, args);
      }
  
      // resolved: 构造函数或工厂方法是否已经解析过
      boolean resolved = false;
      // autowireNecessary: 是否需要自动注入（即是否需要解析构造函数参数）
      boolean autowireNecessary = false;
      if (args == null) {
          // 2.加锁
          synchronized (mbd.constructorArgumentLock) {
              if (mbd.resolvedConstructorOrFactoryMethod != null) {
                  // 2.1 如果resolvedConstructorOrFactoryMethod缓存不为空，则将resolved标记为已解析
                  resolved = true;
                  // 2.2 根据constructorArgumentsResolved判断是否需要自动注入
                  autowireNecessary = mbd.constructorArgumentsResolved;
              }
          }
      }
  
      if (resolved) {
          // 3.如果已经解析过，则使用resolvedConstructorOrFactoryMethod缓存里解析好的构造函数方法
          if (autowireNecessary) {
              // 3.1 需要自动注入，则执行构造函数自动注入
              return autowireConstructor(beanName, mbd, null, null);
          } else {
              // 3.2 否则使用默认的构造函数进行bean的实例化
              return instantiateBean(beanName, mbd);
          }
      }
      // 4.应用后置处理器SmartInstantiationAwareBeanPostProcessor，拿到bean的候选构造函数
      Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
      if (ctors != null ||
          mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
          mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
          // 5.如果ctors不为空 || mbd的注入方式为AUTOWIRE_CONSTRUCTOR || mdb定义了构造函数的参数值
          // || args不为空，则执行构造函数自动注入
          return autowireConstructor(beanName, mbd, ctors, args);
      }
      // 6.没有特殊处理，则使用默认的构造函数进行bean的实例化
      return instantiateBean(beanName, mbd);
  }
  ```

###### 04 - 1：获取实例化的所有候选构造函数

- ```java
  protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(Class<?> beanClass,
                            String beanName) throws BeansException {
      if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
          // 1.遍历所有的BeanPostProcessor
          for (BeanPostProcessor bp : getBeanPostProcessors()) {
              if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                  SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
                  // 2.该方法可以返回要用于beanClass的候选构造函数
                  Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);
                  if (ctors != null) {
                      // 3.如果ctors不为空，则不再继续执行其他的SmartInstantiationAwareBeanPostProcessor
                      return ctors;
                  }
              }
          }
      }
      return null;
  }
  ```

###### 04 - 2：构造注入

- org.springframework.beans.factory.support.**ConstructorResolver**#autowireConstructor

- 找到最终实例化的参数，最终实例化的构造器，然后根据实例化策略实例化对象；

- ```java
  protected BeanWrapper autowireConstructor(
      String beanName, RootBeanDefinition mbd, Constructor<?>[] ctors, Object[] explicitArgs) {
      return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
  }
  
  public BeanWrapper autowireConstructor(final String beanName, final RootBeanDefinition mbd,
                                     Constructor<?>[] chosenCtors, final Object[] explicitArgs) {
      // 定义bean包装类
      BeanWrapperImpl bw = new BeanWrapperImpl();
      this.beanFactory.initBeanWrapper(bw);
      // 最终用于实例化的构造函数
      Constructor<?> constructorToUse = null;
      // 最终用于实例化的参数Holder
      ArgumentsHolder argsHolderToUse = null;
      // 最终用于实例化的构造函数参数
      Object[] argsToUse = null;
  
      // 1.解析出要用于实例化的构造函数参数
      if (explicitArgs != null) {
          // 1.1 如果explicitArgs不为空，则构造函数的参数直接使用explicitArgs
          // 通过getBean方法调用时，显示指定了参数，则explicitArgs就不为null
          argsToUse = explicitArgs;
      } else {
          // 1.2 尝试从缓存中获取已经解析过的构造函数参数
          Object[] argsToResolve = null;
          synchronized (mbd.constructorArgumentLock) {
              // 1.2.1 拿到缓存中已解析的构造函数或工厂方法
              constructorToUse = (Constructor<?>) mbd.resolvedConstructorOrFactoryMethod;
              // 1.2.2 如果constructorToUse不为空 && mbd标记了构造函数参数已解析
              if (constructorToUse != null && mbd.constructorArgumentsResolved) {
                  // 1.2.3 从缓存中获取已解析的构造函数参数
                  argsToUse = mbd.resolvedConstructorArguments;
                  if (argsToUse == null) {
                      // 1.2.4 constructorArgumentsResolved为true时，resolvedConstructorArguments和
                      // preparedConstructorArguments 然有一个缓存了构造函数的参数
                      argsToResolve = mbd.preparedConstructorArguments;
                  }
              }
          }
          if (argsToResolve != null) {
              // 1.2.5 如果argsToResolve不为空，则对构造函数参数进行解析，
              // 如给定方法的构造函数 A(int,int)则通过此方法后就会把配置中的("1","1")转换为(1,1)
              argsToUse = resolvePreparedArguments(beanName, mbd, bw,
                                                   constructorToUse, argsToResolve);
          }
      }
  
      // 2.如果构造函数没有被缓存，则通过配置文件获取
      if (constructorToUse == null) {
          // 2.1 检查是否需要自动装配：chosenCtors不为空 || autowireMode为AUTOWIRE_CONSTRUCTOR
          // 例子：当chosenCtors不为空时，代表有构造函数通过@Autowire修饰，因此需要自动装配
          boolean autowiring = (chosenCtors != null ||
            				mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
          ConstructorArgumentValues resolvedValues = null;
          // 构造函数参数个数
          int minNrOfArgs;
          if (explicitArgs != null) {
              // 2.2 explicitArgs不为空，则使用explicitArgs的length作为minNrOfArgs的值
              minNrOfArgs = explicitArgs.length;
          } else {
              // 2.3 获得mbd的构造函数的参数值
              //（indexedArgumentValues：带index的参数值；genericArgumentValues：通用的参数值）
              ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
              // 2.4 创建ConstructorArgumentValues对象resolvedValues，用于承载解析后的构造函数参数的值
              resolvedValues = new ConstructorArgumentValues();
              // 2.5 解析mbd的构造函数的参数，并返回参数个数
              minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
              // 注：这边解析mbd中的构造函数参数值，主要是处理我们通过xml方式定义的构造函数注入的参数，
              // 但是如果我们是通过@Autowire注解直接修饰构造函数，则mbd是没有这些参数值的
          }
  
          // 3.确认构造函数的候选者
          // 3.1 如果入参chosenCtors不为空，则将chosenCtors的构造函数作为候选者
          Constructor<?>[] candidates = chosenCtors;
          if (candidates == null) {
              Class<?> beanClass = mbd.getBeanClass();
              try {
                  // 3.2 如果入参chosenCtors为空，则获取beanClass的构造函数
                  // （mbd是否允许访问非公共构造函数和方法 ? 所有声明的构造函数：公共构造函数）
                  candidates = (mbd.isNonPublicAccessAllowed() ?
                                beanClass.getDeclaredConstructors() : beanClass.getConstructors());
              } catch (Throwable ex) {
                  throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                     "Resolution of declared constructors on bean Class failed", ex);
              }
          }
          // 3.3 对给定的构造函数排序：
          // 先按方法修饰符排序：public排非public前面，再按构造函数参数个数排序：参数多的排前面
          AutowireUtils.sortConstructors(candidates);
          // 最小匹配权重，权重越小，越接近我们要找的目标构造函数
          int minTypeDiffWeight = Integer.MAX_VALUE;
          Set<Constructor<?>> ambiguousConstructors = null;
          LinkedList<UnsatisfiedDependencyException> causes = null;
          // 4.遍历所有构造函数候选者，找出符合条件的构造函数
          for (Constructor<?> candidate : candidates) {
              // 4.1 拿到当前遍历的构造函数的参数类型数组
              Class<?>[] paramTypes = candidate.getParameterTypes();
              if (constructorToUse != null && argsToUse.length > paramTypes.length) {
                  // 4.2 如果已经找到满足的构造函数 && 目标构造函数需要的参数个数大于当前遍历的参数个数则终止，
                  // 因为遍历的构造函数已经排过序，后面不会有更合适的候选者了
                  break;
              }
              if (paramTypes.length < minNrOfArgs) {
                  // 4.3 如果当前遍历到的构造函数的参数个数小于我们所需的参数个数，则直接跳过该构造函数
                  continue;
              }
  
              ArgumentsHolder argsHolder;
              if (resolvedValues != null) {
                  // 存在参数则根据参数值来匹配参数类型
                  try {
                      // 4.4 resolvedValues不为空，
                      // 4.4.1 获取当前遍历的构造函数的参数名称
                      // 4.4.1.1 解析使用ConstructorProperties注解的构造函数参数
                      String[] paramNames = 
                          ConstructorPropertiesChecker.evaluate(candidate, paramTypes.length);
                      if (paramNames == null) {
                          // 4.4.1.2 获取参数名称解析器
                          ParameterNameDiscoverer pnd = 
                              this.beanFactory.getParameterNameDiscoverer();
                          if (pnd != null) {
                              // 4.4.1.3 使用参数名称解析器获取当前遍历的构造函数的参数名称
                              paramNames = pnd.getParameterNames(candidate);
                          }
                      }
                      // 4.4.2 创建一个参数数组以调用构造函数或工厂方法，
                      // 通过参数类型和参数名解析构造函数或工厂方法所需的参数
                      argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw, 
                      	paramTypes, paramNames,getUserDeclaredConstructor(candidate), autowiring);
                  } catch (UnsatisfiedDependencyException ex) {
                      // 4.4.3 参数匹配失败，则抛出异常
                     	// ...
                      continue;
                  }
              } else {
                  // 4.5 resolvedValues为空，则explicitArgs不为空，即给出了显式参数
                  // Explicit arguments given -> arguments length must match exactly.
                  // 4.5.1 如果当前遍历的构造函数参数个数与explicitArgs长度不相同，则跳过该构造函数
                  if (paramTypes.length != explicitArgs.length) {
                      continue;
                  }
                  // 4.5.2 使用显式给出的参数构造ArgumentsHolder
                  argsHolder = new ArgumentsHolder(explicitArgs);
              }
  
              // 4.6 根据mbd的解析构造函数模式（true: 宽松模式(默认)，false：严格模式），
              // 将argsHolder的参数和paramTypes进行比较，计算paramTypes的类型差异权重值
              int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
                                    argsHolder.getTypeDifferenceWeight(paramTypes) : 
                                    argsHolder.getAssignabilityWeight(paramTypes));
              // 4.7 类型差异权重值越小,则说明构造函数越匹配，则选择此构造函数
              if (typeDiffWeight < minTypeDiffWeight) {
                  // 将要使用的参数都替换成差异权重值更小的
                  constructorToUse = candidate;
                  argsHolderToUse = argsHolder;
                  argsToUse = argsHolder.arguments;
                  minTypeDiffWeight = typeDiffWeight;
                  // 如果出现权重值更小的候选者，则将ambiguousConstructors清空，允许之前存在权重值相同的候选者
                  ambiguousConstructors = null;
              } else if (constructorToUse != null && typeDiffWeight == minTypeDiffWeight) {
                  // 4.8 如果存在两个候选者的权重值相同，并且是当前遍历过权重值最小的
                  // 将这两个候选者都添加到ambiguousConstructors
                  if (ambiguousConstructors == null) {
                      ambiguousConstructors = new LinkedHashSet<Constructor<?>>();
                      ambiguousConstructors.add(constructorToUse);
                  }
                  ambiguousConstructors.add(candidate);
              }
          }
  
          if (constructorToUse == null) {
              // 5.如果最终没有找到匹配的构造函数，则进行异常处理
              if (causes != null) {
                  UnsatisfiedDependencyException ex = causes.removeLast();
                  for (Exception cause : causes) {
                      this.beanFactory.onSuppressedException(cause);
                  }
                  throw ex;
              } throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                              "Could not resolve matching constructor ");
          } else if (ambiguousConstructors != null && !mbd.isLenientConstructorResolution()) {
           // 6.如果找到了匹配的构造函数，但是存在多个（ambiguousConstructors不为空）
           // && 解析构造函数的模式为严格模式，则抛出异常
              throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                              "Ambiguous constructor matches found in bean " +
                                              ambiguousConstructors);
          }
  
          if (explicitArgs == null) {
              // 7.将解析的构造函数和参数放到缓存
              argsHolderToUse.storeCache(mbd, constructorToUse);
          }
      }
      
      Assert.state(argsToUse != null, "Unresolved constructor arguments");
      // 8.将构造的实例加入BeanWrapper中，并返回
  	bw.setBeanInstance(instantiate(beanName, mbd, constructorToUse, argsToUse));
  }
  ```

###### 04 - 2 - 1：根据实例化策略实例化Bean

- ```java
  private Object instantiate(String beanName, RootBeanDefinition mbd,
                             Constructor<?> constructorToUse, Object[] argsToUse) {
      try {
           // 根据实例化策略以及得到的构造函数及构造函数参数实例化bean
          InstantiationStrategy strategy = this.beanFactory.getInstantiationStrategy();
          if (System.getSecurityManager() != null) {
              return AccessController.doPrivileged((PrivilegedAction<Object>) () -> 
                     strategy.instantiate(mbd, beanName, this.beanFactory, constructorToUse, 
                                          argsToUse),this.beanFactory.getAccessControlContext());
          } else {
              return strategy.instantiate(mbd, beanName, this.beanFactory,
                                          constructorToUse, argsToUse);
          }
      } catch (Throwable ex) {
          throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                          "Bean instantiation via constructor failed", ex);
      }
  }
  ```

###### 04 - 2 - 2：将解析出来的构造函数和参数放到缓存

- 将解析出来的构造函数和参数放到缓存，在createBeanInstance 方法中，就直接从缓存缓存中获取

- ```java
  public void storeCache(RootBeanDefinition mbd, Object constructorOrFactoryMethod) {
      synchronized (mbd.constructorArgumentLock) {
          // 将构造函数或工厂方法放到resolvedConstructorOrFactoryMethod缓存
          mbd.resolvedConstructorOrFactoryMethod = constructorOrFactoryMethod;
          // constructorArgumentsResolved标记为已解析
          mbd.constructorArgumentsResolved = true;
          if (this.resolveNecessary) {
              // 如果参数需要解析，则将preparedArguments放到preparedConstructorArguments缓存
              mbd.preparedConstructorArguments = this.preparedArguments;
          } else {
              // 如果参数不需要解析，则将arguments放到resolvedConstructorArguments缓存
              mbd.resolvedConstructorArguments = this.arguments;
          }
      }
  }
  ```

##### 05：应用后置处理器MergedBeanDefinitionPostProcessor，修改MergeBeanDefinition 【从序号5 的 1.3 继续向下分析】

- org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyMergedBeanDefinitionPostProcessors

- ```java
  protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
      // 1.获取BeanFactory中已注册的BeanPostProcessor
      for (BeanPostProcessor bp : getBeanPostProcessors()) {
          if (bp instanceof MergedBeanDefinitionPostProcessor) {
              // 2.调用MergedBeanDefinitionPostProcessor的postProcessMergedBeanDefinition方法，
              // 对指定bean的给定MergedBeanDefinition进行后置处理，@Autowire注解在这边对元数据进行预解析
              MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
              bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
          }
      }
  }
  ```

##### 06：缓存 singletonFactory，解决缓存依赖

- 在 Spring IoC：finishBeanFactoryInitialization 详解中，我们通过提前曝光的 ObjectFactory 获得 “不完整” 的 bean 实例，从而解决循环引用的问题，ObjectFactory 就是通过这边的 singletonObjects 缓存来进行曝光的；

- ```java
  protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
      Assert.notNull(singletonFactory, "Singleton factory must not be null");
      synchronized (this.singletonObjects) {
          // 1.如果beanName不存在于singletonObjects缓存中
          if (!this.singletonObjects.containsKey(beanName)) {
              // 2.将beanName和singletonFactory注册到singletonFactories缓存（beanName -> 该beanName的单例工厂）
              this.singletonFactories.put(beanName, singletonFactory);
              // 3.移除earlySingletonObjects缓存中的beanName（beanName -> beanName的早期单例对象）
              this.earlySingletonObjects.remove(beanName);
              // 4.将beanName注册到registeredSingletons缓存（已经注册的单例集合）
              this.registeredSingletons.add(beanName);
          }
      }
  }
  
  protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
      Object exposedObject = bean;
      // 1.如果bean不为空 && mbd不是合成 && 存在InstantiationAwareBeanPostProcessors
      if (bean != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
          for (BeanPostProcessor bp : getBeanPostProcessors()) {
              // 2.应用所有SmartInstantiationAwareBeanPostProcessor，调用getEarlyBeanReference方法
              if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
                  SmartInstantiationAwareBeanPostProcessor ibp = 
                      (SmartInstantiationAwareBeanPostProcessor) bp;
                  // 3.允许SmartInstantiationAwareBeanPostProcessor返回指定bean的早期引用
                  exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
                  if (exposedObject == null) {
                      return null;
                  }
              }
          }
      }
      // 4.返回要作为bean引用公开的对象，如果没有SmartInstantiationAwareBeanPostProcessor修改，则返回的是入参的bean对象本身
      return exposedObject;
  }
  ```

##### 07：对 Bean 进行属性填充，解析 Autowire 注入

- org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#populateBean

- ```java
  protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
      // 1.bw为空时的处理
      if (bw == null) {
          if (mdb.hasPropertyValues) {
              // 1.1 如果bw为空，属性不为空，抛异常，无法将属性值应用于null实例
              throw new BeanCreationException(
                  mbd.getResourceDescription(), beanName,
                  "Cannot apply property values to null instance");
          } else {
              // 1.2 如果bw为空，属性也为空，则跳过
              return;
          }
      }
      // 2.1 如果mbd不是合成的 && 存在InstantiationAwareBeanPostProcessor，则遍历处理
      if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
          for (BeanPostProcessor bp : getBeanPostProcessors()) {
              if (bp instanceof InstantiationAwareBeanPostProcessor) {
                  InstantiationAwareBeanPostProcessor ibp =
                      (InstantiationAwareBeanPostProcessor) bp;
                  // 2.2 在bean实例化后，属性填充之前被调用，允许修改bean的属性
                  // 如果返回false，则跳过之后的属性填充
                  if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                      // 2.3 如果返回false，代表要跳过之后的属性填充
                      return;
                  }
              }
          }
      }
  	// 3. 此 Bean 需要填充属性的容器
      PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);
      // 4.解析自动装配模式为AUTOWIRE_BY_NAME和AUTOWIRE_BY_TYPE（现在几乎不用）
      if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
          mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
          MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
          if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
              // 4.1 解析autowireByName的注入
              autowireByName(beanName, mbd, bw, newPvs);
          }
          if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
              // 4.2 解析autowireByType的注入
              autowireByType(beanName, mbd, bw, newPvs);
          }
          pvs = newPvs;
      }
  
      // 5.BeanFactory是否注册过InstantiationAwareBeanPostProcessors
      boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
      // 6.是否需要依赖检查
      boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);
  
      PropertyDescriptor[] filteredPds = null;
      // 7.注册过InstantiationAwareBeanPostProcessors
      if (hasInstAwareBpps) {
          if (pvs == null) {
              pvs = mbd.getPropertyValues();
          }
          // 7.1 应用后置处理器InstantiationAwareBeanPostProcessor
          for (BeanPostProcessor bp : getBeanPostProcessors()) {
              if (bp instanceof InstantiationAwareBeanPostProcessor) {
                  InstantiationAwareBeanPostProcessor ibp =
                      (InstantiationAwareBeanPostProcessor) bp;
                  PropertyValues pvsToUse = ibp.postProcessProperties(pvs,
                                                            bw.getWrappedInstance(), beanName);
                  if (pvsToUse == null) {
                      if (filteredPds == null) {
                          filteredPds = filterPropertyDescriptorsForDependencyCheck(
                              bw, mbd.allowCaching);
                      }
                      // 7.1.1 应用后置处理器InstantiationAwareBeanPostProcessor的方法
                      // postProcessPropertyValues,进行属性填充前的再次处理。
                      pvsToUse = ibp.postProcessPropertyValues(pvs, filteredPds, 
                                                               bw.getWrappedInstance(), beanName);
                      if (pvsToUse == null) {
                          return;
                      }
                  }
                  pvs = pvsToUse;
              }
          }
      }
      // 8. 需要依赖检查
      if (needsDepCheck) {
          if (filteredPds == null) {
              filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
          }
          // 8.1 依赖检查，对应depends-on属性
          checkDependencies(beanName, mbd, filteredPds, pvs);
      }
      // 9.将所有PropertyValues中的属性填充到bean中
      if (pvs != null) {
          applyPropertyValues(beanName, mbd, bw, pvs);
      }
  }
  ```

###### 07 - 1：autowireByName 通过属性名注入

- ```java
  protected void autowireByName(
      String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
      // 1.寻找bw中需要依赖注入的属性
      String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
      for (String propertyName : propertyNames) {
          // 2.校验是否存在 beanName = propertyName 的bean实例或者 BeanDefinition
          if (containsBean(propertyName)) {
              // 3.获取propertyName的bean实例对象
              Object bean = getBean(propertyName);
              // 4.将属性名和属性值添加到pvs
              pvs.add(propertyName, bean);
              // 5.注册依赖关系到缓存（beanName依赖propertyName）
              registerDependentBean(propertyName, beanName);
              if (logger.isDebugEnabled()) {
                  logger.debug("Added autowiring by name from bean name '" + beanName +
                               "' via property '" + propertyName + "' to bean named '" + 
                               propertyName + "'");
              }
          } else {
              if (logger.isTraceEnabled()) {
                  logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName 
                               + "' by name: no matching bean found");
              }
          }
      }
  }
  ```


###### 07 - 1 - 1：containsBean

- ```java
  @Override
  public boolean containsBean(String name) {
      // 1.将name转换为真正的beanName
      String beanName = transformedBeanName(name);
      // 2.检查singletonObjects缓存和beanDefinitionMap缓存中是否存在beanName
      if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
          // 3.name不带&前缀，或者是FactoryBean，则返回true
          return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name));
      }
      // Not found -> check parent.
      // 4.没有找到则检查parentBeanFactory
      BeanFactory parentBeanFactory = getParentBeanFactory();
      return (parentBeanFactory != null && parentBeanFactory.containsBean(originalBeanName(name)));
  }
  ```

###### 07 - 2：autowireByType：通过类型注入

- ```java
  protected void autowireByType(
      String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
      TypeConverter converter = getCustomTypeConverter();
      if (converter == null) {
          converter = bw;
      }
  
      Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
      // 1.寻找bw中需要依赖注入的属性
      String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
      // 2.遍历所有需要依赖注入的属性
      for (String propertyName : propertyNames) {
          try {
              PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
              if (Object.class != pd.getPropertyType()) {
                  // 3.获取指定属性的set方法，封装成MethodParameter（必须有set方法才能通过属性来注入）
                  MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
                  boolean eager = !PriorityOrdered.class.isAssignableFrom(bw.getWrappedClass());
                  // 4.将MethodParameter的方法参数索引信息封装成DependencyDescriptor
                  DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(
                      methodParam, eager);
                  // 5.解析当前属性所匹配的bean实例，把解析到的bean实例的beanName存储在autowiredBeanNames中
                  Object autowiredArgument = resolveDependency(desc, beanName,
                                                               autowiredBeanNames, converter);
                  if (autowiredArgument != null) {
                      // 6.如果找到了依赖的bean实例，将属性名和bean实例放到pvs中
                      pvs.add(propertyName, autowiredArgument);
                  }
                  for (String autowiredBeanName : autowiredBeanNames) {
                      // 7.注册依赖关系，beanName依赖autowiredBeanName
                      registerDependentBean(autowiredBeanName, beanName);
                      if (logger.isDebugEnabled()) {
                          logger.debug("Autowiring by type from bean name '" + beanName
                                       + "' via property '" + propertyName + "' to bean named '");
                      }
                  }
                  autowiredBeanNames.clear();
              }
          } catch (BeansException ex) {
              throw new UnsatisfiedDependencyException(mbd.getResourceDescription(),
                                                       beanName, propertyName, ex);
          }
      }
  }
  ```

###### 07 - 3：applyPropertyValues：遍历每个需要注入的属性，将其对象进行转换，批量填充

- ```java
  protected void applyPropertyValues(String beanName, BeanDefinition mbd,
                                     BeanWrapper bw, PropertyValues pvs) {
      if (pvs == null || pvs.isEmpty()) {
          return;
      }
      MutablePropertyValues mpvs = null;
      List<PropertyValue> original;
      if (System.getSecurityManager() != null) {
          if (bw instanceof BeanWrapperImpl) {
              ((BeanWrapperImpl) bw).setSecurityContext(getAccessControlContext());
          }
      }
  
      // 1.获取属性值列表
      if (pvs instanceof MutablePropertyValues) {
          mpvs = (MutablePropertyValues) pvs;
          // 1.1 如果mpvs中的值已经被转换为对应的类型，那么可以直接设置到BeanWrapper中
          if (mpvs.isConverted()) {
              // Shortcut: use the pre-converted values as-is.
              try {
                  bw.setPropertyValues(mpvs);
                  return;
              } catch (BeansException ex) {
                  throw new BeanCreationException(
                      mbd.getResourceDescription(), beanName, "Error setting property values", ex);
              }
          }
          original = mpvs.getPropertyValueList();
      } else {
          // 1.2 如果pvs并不是使用MutablePropertyValues封装的类型，那么直接使用原始的属性获取方法
          original = Arrays.asList(pvs.getPropertyValues());
      }
  
      TypeConverter converter = getCustomTypeConverter();
      if (converter == null) {
          converter = bw;
      }
      // 2.1 获取对应的解析器
      BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this,
                                                                         beanName, mbd, converter);
      // 2.2 创建深层拷贝副本，用于存放解析后的属性值
      List<PropertyValue> deepCopy = new ArrayList<PropertyValue>(original.size());
      boolean resolveNecessary = false;
      // 3.遍历属性，将属性转换为对应类的对应属性的类型
      for (PropertyValue pv : original) {
          if (pv.isConverted()) {
              // 3.1 如果pv已经包含转换的值，则直接添加到deepCopy
              deepCopy.add(pv);
          } else {
              // 3.2 否则，进行转换
              // 3.2.1 拿到pv的原始属性名和属性值
              String propertyName = pv.getName();
              Object originalValue = pv.getValue();
              // 3.2.2 使用解析器解析原始属性值
              Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
              Object convertedValue = resolvedValue;
              // 3.2.3 判断该属性是否可转换
              boolean convertible = bw.isWritableProperty(propertyName) &&
                  !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
              if (convertible) {
                  // 3.2.4 如果可转换，则转换指定目标属性的给定值
                  convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
              }
              // 3.2.5 在合并的BeanDefinition中存储转换后的值，以避免为每个创建的bean实例重新转换
              if (resolvedValue == originalValue) {
                  if (convertible) {
                      pv.setConvertedValue(convertedValue);
                  }
                  deepCopy.add(pv);
              } else if (convertible && originalValue instanceof TypedStringValue &&
                         !((TypedStringValue) originalValue).isDynamic() &&
                         !(convertedValue instanceof Collection || 
                           ObjectUtils.isArray(convertedValue))) {
                  pv.setConvertedValue(convertedValue);
                  deepCopy.add(pv);
              } else {
                  resolveNecessary = true;
                  deepCopy.add(new PropertyValue(pv, convertedValue));
              }
          }
      }
      if (mpvs != null && !resolveNecessary) {
          mpvs.setConverted();
      }
      try {
          // 4.设置bean的属性值为deepCopy
          bw.setPropertyValues(new MutablePropertyValues(deepCopy));
      } catch (BeansException ex) {
          throw new BeanCreationException(
              mbd.getResourceDescription(), beanName, "Error setting property values", ex);
      }
  }
  ```

##### 08：initializeBean：（序号 5 的 10）

- ```java
  protected Object initializeBean(final String beanName, final Object bean,
                                  RootBeanDefinition mbd) {
      // 1.激活Aware方法
      if (System.getSecurityManager() != null) {
          AccessController.doPrivileged(new PrivilegedAction<Object>() {
              @Override
              public Object run() {
                  invokeAwareMethods(beanName, bean);
                  return null;
              }
          }, getAccessControlContext());
      } else {
          invokeAwareMethods(beanName, bean);
      }
      Object wrappedBean = bean;
      if (mbd == null || !mbd.isSynthetic()) {
          // 2.在初始化前应用BeanPostProcessor的postProcessBeforeInitialization方法，对bean实例进行包装
          wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
      }
      try {
          // 3.调用初始化方法
          invokeInitMethods(beanName, wrappedBean, mbd);
      } catch (Throwable ex) {
          throw new BeanCreationException((mbd != null ? mbd.getResourceDescription() : null),
              beanName, "Invocation of init method failed", ex);
      }
  
      if (mbd == null || !mbd.isSynthetic()) {
          // 4.在初始化后应用BeanPostProcessor的postProcessAfterInitialization方法，允许对bean实例进行包装
          wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
      }
      // 5.返回wrappedBean
      return wrappedBean;
  }
  ```

###### 08 - 1：applyBeanPostProcessorsBeforeInitialization

- ```java
  @Override
  public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean,
                                                            String beanName) throws BeansException {
      Object result = existingBean;
      // 1.遍历所有注册的BeanPostProcessor实现类，调用postProcessBeforeInitialization方法
      for (BeanPostProcessor processor : getBeanPostProcessors()) {
          // 2.在bean初始化方法执行前，调用postProcessBeforeInitialization方法
          // ApplicationContextAwareProcessor applicationContext 就是在这里设置的
          Object current = processor.postProcessBeforeInitialization(result, beanName);
          if (current == null) {
              return result;
          }
          result = current;
      }
      return result;
  }
  
  @Override
  public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
      													 throws BeansException {
      Object result = existingBean;
      for (BeanPostProcessor processor : getBeanPostProcessors()) {
          Object current = processor.postProcessAfterInitialization(result, beanName);
          if (current == null) {
              return result;
          }
          result = current;
      }
      return result;
  }
  ```
  

###### 08 - 2：invokeInitMethods

- ```java
  protected void invokeInitMethods(String beanName, final Object bean, RootBeanDefinition mbd)
      throws Throwable {
      // 1.首先检查bean是否实现了InitializingBean接口，如果是的话调用afterPropertiesSet方法
      boolean isInitializingBean = (bean instanceof InitializingBean);
      if (isInitializingBean && (mbd == null
                                 || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
          if (logger.isDebugEnabled()) {
              logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
          }
          // 2.调用afterPropertiesSet方法
          if (System.getSecurityManager() != null) {
              try {
                  AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                      @Override
                      public Object run() throws Exception {
                          ((InitializingBean) bean).afterPropertiesSet();
                          return null;
                      }
                  }, getAccessControlContext());
              } catch (PrivilegedActionException pae) {
                  throw pae.getException();
              }
          } else {
              ((InitializingBean) bean).afterPropertiesSet();
          }
      }
  
      if (mbd != null) {
          String initMethodName = mbd.getInitMethodName();
          if (initMethodName != null
              && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName))
              && !mbd.isExternallyManagedInitMethod(initMethodName)) {
              // 3.调用自定义初始化方法
              invokeCustomInitMethod(beanName, bean, mbd);
          }
      }
  }
  ```

##### 09：registerDisposableBeanIfNecessary：注册用于销毁的 Bean

- ```java
  protected void registerDisposableBeanIfNecessary(String beanName, Object bean,
                                                   RootBeanDefinition mbd) {
      AccessControlContext acc = (System.getSecurityManager() != null
                                  ? getAccessControlContext() : null);
      // 1.mbd的scope不是prototype && 给定的bean需要在关闭时销毁
      if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
          if (mbd.isSingleton()) {
              // 2.注册用于销毁的bean 到 disposableBeans 缓存中，执行给定bean的所有销毁工作：
              // DestructionAwareBeanPostProcessors，DisposableBean接口，自定义销毁方法
              // 2.1 DisposableBeanAdapter：使用DisposableBeanAdapter来封装用于销毁的bean
              registerDisposableBean(beanName,
                    new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
          } else {
              // 3.自定义的 scope 处理
              Scope scope = this.scopes.get(mbd.getScope());
              if (scope == null) {
                  throw new IllegalStateException(
                      "No Scope registered for scope name '" + mbd.getScope() + "'");
              }
              scope.registerDestructionCallback(beanName,
                 new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
          }
      }
  }
  ```
  

##### 09 - 1：requiresDestruction.hasDestroyMethod

- 如果 bean 实现了 DisposableBean 接口或 bean 是 AutoCloseable 实例，则返回 true，因为这两个接口都有关闭的方法。

  ```java
  public static boolean hasDestroyMethod(Object bean, RootBeanDefinition beanDefinition) {
      if (bean instanceof DisposableBean || closeableInterface.isInstance(bean)) {
          // 1.如果bean实现了DisposableBean接口 或者 bean是AutoCloseable实例，则返回true
          return true;
      }
      // 2.拿到bean自定义的destroy方法名
      String destroyMethodName = beanDefinition.getDestroyMethodName();
      if (AbstractBeanDefinition.INFER_METHOD.equals(destroyMethodName)) {
          // 3.如果自定义的destroy方法名为“(inferred)”（该名字代表需要我们自己去推测destroy的方法名），
          // 则检查该bean是否存在方法名为“close”或“shutdown”的方法，如果存在，则返回true
          return (ClassUtils.hasMethod(bean.getClass(), CLOSE_METHOD_NAME) ||
                  ClassUtils.hasMethod(bean.getClass(), SHUTDOWN_METHOD_NAME));
      }
      // 4.如果destroyMethodName不为空，则返回true
      return StringUtils.hasLength(destroyMethodName);
  }
  ```

