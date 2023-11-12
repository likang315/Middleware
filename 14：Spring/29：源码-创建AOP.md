### 源码-创建 AOP

------

[TOC]

##### 00：概述

- 解析 AOP 的注解，并注册对应的内部管理的自动代理创建者的 bean，**AnnotationAwareAspectJAutoProxyCreator**，其他的还有 InfrastructureAdvisorAutoProxyCreator 和 AspectJAwareAdvisorAutoProxyCreator。
- 当我们的 bean 初始化完毕后，会触发所有 BeanPostProcessor 的 postProcessAfterInitialization 方法，此时就会调用我们的 **AnnotationAwareAspectJAutoProxyCreator 的 postProcessAfterInitialization 方法**。该方法会查找我们定义的切面类（使用 @Aspect 注解），创建切面类中定义的增强器（使用 @Before、@After、@Around 等注解），并根据 @Pointcut 的 execution 表达式筛选出适用于当前遍历的 bean 的增强器， 将适用于当前遍历的 bean 的增强器作为参数之一创建对应的 AOP 代理。
- 当调用到被 AOP 代理的方法时，会走到对应的代理方法：JdkDynamicAopProxy#invoke 或  **DynamicAdvisedInterceptor#intercept**，该方法会创建 ReflectiveMethodInvocation，通过**责任链的方式来执行所有的增强器和被代理的方法**。

##### 01：@Aspect

- Spring 2.0采用@AspectJ注解对POJO进行标注，从而定义一个包含切点信息和**增强横切逻辑的切面**。

- 当使用该注解开启 AOP 功能时，Spring会从**“META-INF/spring.handlers”** 配置文件中拿到该注解对应的 NamespaceHandlerSupport：AopNamespaceHandler

  - ```xml
    <aop:aspectj-autoproxy />
    ```

##### 02：AOP 注解的解析

- 在 AopNamespaceHandler 的 init 方法会给该注解注册对应的解析器

- ```java
  public class AopNamespaceHandler extends NamespaceHandlerSupport {
  	@Override
  	public void init() {
  		// In 2.0 XSD as well as in 2.5+ XSDs
  		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
          // aspectj-autoproxy 的解析器
  		registerBeanDefinitionParser("aspectj-autoproxy",
                                       new AspectJAutoProxyBeanDefinitionParser());
  		registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());
  		// Only in 2.0 XSD: moved to context namespace in 2.5+
  		registerBeanDefinitionParser("spring-configured",
                                       new SpringConfiguredBeanDefinitionParser());
  	}
  }
  ```

##### 03：AspectJAutoProxyBeanDefinitionParser#parse

- ```java
  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
      // 1.注册AspectJAnnotationAutoProxyCreator
      AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext,
                                                                             element);
      // 2.对于注解中子节点的处理
      extendBeanDefinition(element, parserContext);
      return null;
  }
  ```

##### 04：registerAspectJAnnotationAutoProxyCreatorIfNecessary

- ```java
  public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(
      ParserContext parserContext, Element sourceElement) {
      // 1.注册AspectJAnnotationAutoProxyCreator
      BeanDefinition beanDefinition = AopConfigUtils
          .registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext.getRegistry(), 
                                                       parserContext.extractSource(sourceElement));
      // 2.对于proxy-target-class以及expose-proxy属性的处理
      useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
      // 3.注册组件并通知，便于监听器做进一步处理
      registerComponentIfNecessary(beanDefinition, parserContext);
  }
  ```

##### 05：AopConfigUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary

- org.springframework.aop.config.internalAutoProxyCreator：是内部管理的自动代理创建者的 bean 名称，可能对应的 beanClassName 有三种，对应的注解如下：

  - InfrastructureAdvisorAutoProxyCreator：<tx:annotation-driven />
  - AspectJAwareAdvisorAutoProxyCreator：<aop:config />
  - **AnnotationAwareAspectJAutoProxyCreator**：<aop:aspectj-autoproxy />

  ```java
  public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
  												BeanDefinitionRegistry registry, Object source) {
      return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
  }
  
  private static BeanDefinition registerOrEscalateApcAsRequired(Class<?> cls,
                                                                BeanDefinitionRegistry registry,
                                                                Object source) {
      Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
      // 1.如果注册表中已经存在beanName=org.springframework.aop.config.internalAutoProxyCreator的bean，则按优先级进行选择。
      // 可能存在的beanClass有三种，按优先级排序如下：
      // InfrastructureAdvisorAutoProxyCreator
      // AspectJAwareAdvisorAutoProxyCreator
      // AnnotationAwareAspectJAutoProxyCreator
      if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
          // 拿到已经存在的bean定义
          BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
          // 如果已经存在的bean的className与当前要注册的bean的className不相同，则按优先级进行选择
          if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
              // 拿到已经存在的bean的优先级
              int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
              // 拿到当前要注册的bean的优先级
              int requiredPriority = findPriorityForClass(cls);
              // 如果当前要注册的bean的优先级大于已经存在的bean的优先级，
              // 则将bean的className替换为当前要注册的bean的className，
              if (currentPriority < requiredPriority) {
                  apcDefinition.setBeanClassName(cls.getName());
              }
          }
          // 如果已经存在的bean的className与当前要注册的bean的className相同，则无需进行任何处理
          return null;
      }
      // 2.如果注册表中还不存在，则新建一个Bean定义，并添加到注册表中
      RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
      beanDefinition.setSource(source);
      // 设置了order为最高优先级
      beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
      beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
      // 注册BeanDefinition，beanName为org.springframework.aop.config.internalAutoProxyCreator
      registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
      return beanDefinition;
  }
  ```

##### 06：useClassProxyingIfNecessary

- JDK本身就提供了动态代理，强制使用CGLIB代理需要将 proxy-targetclass 属性设为true；

- exposeProxy=true：解决自我调用问题；

  ```java
  private static void useClassProxyingIfNecessary(BeanDefinitionRegistry registry,
                                                  Element sourceElement) {
      if (sourceElement != null) {
          boolean proxyTargetClass = Boolean.valueOf(
              sourceElement.getAttribute(PROXY_TARGET_CLASS_ATTRIBUTE));
          if (proxyTargetClass) {
              // 如果节点设置了proxy-target-class=true，则给beanName为		
              // org.springframework.aop.config.internalAutoProxyCreator
              // 的BeanDefinition添加proxyTargetClass=true的属性，之后创建代理的时候将强制使用Cglib代理
              AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
          }
          boolean exposeProxy = Boolean.valueOf(sourceElement.getAttribute(EXPOSE_PROXY_ATTRIBUTE));
          if (exposeProxy) {
              // 如果节点设置了expose-proxy=true，则给beanName为
              // org.springframework.aop.config.internalAutoProxyCreator
              // 的BeanDefinition添加exposeProxy=true的属性，之后创建拦截器时会根据该属性选择是否暴露代理类
              AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
          }
      }
  }
  ```

##### 07：创建代理AOP

###### AnnotationAwareAspectJAutoProxyCreator

- AOP对于AOP的实现，基本上都是由他完成，根据@Point注解定义的切点来自动代理相匹配的bean。
- 实现了几个重要的扩展接口（可能是在父类中实现）
  1. 实现了 BeanPostProcessor 接口：**postProcessAfterInitialization** 方法。
  2. 实现了 InstantiationAwareBeanPostProcessor 接口：实现了 postProcessBeforeInstantiation 方法。
  3. 实现了 SmartInstantiationAwareBeanPostProcessor 接口：实现了 predictBeanType 方法、getEarlyBeanReference 方法。
  4. 实现了 BeanFactoryAware 接口，实现了 setBeanFactory 方法。

###### AbstractAutoProxyCreator#postProcessAfterInitialization

- 当Spring加载这个Bean时，会在**实例化前调用其 postProcessAfterInitialization方法**，AOP也将由此开始。

  ```java
  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
      if (bean != null) {
          Object cacheKey = getCacheKey(bean.getClass(), beanName);
          // 1.判断当前bean是否需要被代理，如果需要则进行封装
          if (!this.earlyProxyReferences.contains(cacheKey)) {
              return wrapIfNecessary(bean, beanName, cacheKey);
          }
      }
      return bean;
  }
  ```

##### 08：wrapIfNecessary

- 获取增强器和创建返回代理对象；

- ```java
  protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
      // 1.判断当前bean是否在targetSourcedBeans缓存中存在（已经处理过），如果存在，则直接返回当前bean
      if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
          return bean;
      }
      // 2.在advisedBeans缓存中存在，并且value为false，则代表无需处理
      if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
          return bean;
      }
      // 3.bean的类是aop基础设施类 || bean应该跳过，则标记为无需处理，并返回
      if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
          this.advisedBeans.put(cacheKey, Boolean.FALSE);
          return bean;
      }
      // 4.获取当前bean的Advices和Advisors
      Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
      // 5.如果存在增强器则创建代理
      if (specificInterceptors != DO_NOT_PROXY) {
          this.advisedBeans.put(cacheKey, Boolean.TRUE);
          // 5.1 创建代理对象：这边SingletonTargetSource的target属性存放的就是我们原来的bean实例
          //（也就是被代理对象），
          // 用于最后增加逻辑执行完毕后，通过反射执行我们真正的方法时使用（method.invoke(bean, args)）
          Object proxy = createProxy(
              bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
          // 5.2 创建完代理后，将cacheKey -> 代理类的class放到缓存
          this.proxyTypes.put(cacheKey, proxy.getClass());
          // 返回代理对象
          return proxy;
      }
      // 6.标记为无需处理
      this.advisedBeans.put(cacheKey, Boolean.FALSE);
      return bean;
  }
  ```
  

##### 09：getAdvicesAndAdvisorsForBean

- ```java
  @Override
  protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
      // 1.找到符合条件的Advisor
      List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
      if (advisors.isEmpty()) {
          // 2.如果没有符合条件的Advisor，则返回null
          return DO_NOT_PROXY;
      }
      return advisors.toArray();
  }
  
  protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
      // 1.查找所有的候选Advisor
      List<Advisor> candidateAdvisors = findCandidateAdvisors();
      // 2.从所有候选的Advisor中找出符合条件的
      List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass,
                                                                beanName);
      // 3.扩展方法，留个子类实现
      extendAdvisors(eligibleAdvisors);
      if (!eligibleAdvisors.isEmpty()) {
          // 4.对符合条件的Advisor进行排序
          eligibleAdvisors = sortAdvisors(eligibleAdvisors);
      }
      return eligibleAdvisors;
  }
  
  protected List<Advisor> findCandidateAdvisors() {
      Assert.state(this.advisorRetrievalHelper != null, 
                   "No BeanFactoryAdvisorRetrievalHelper available");
      // 找到所有Advisor
      return this.advisorRetrievalHelper.findAdvisorBeans();
  }
  @Override
  protected List<Advisor> findCandidateAdvisors() {
      // 1.添加根据父类规则找到的所有advisor
      List<Advisor> advisors = super.findCandidateAdvisors();
      // 2.为bean工厂中的所有AspectJ方面构建advisor
      advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
      return advisors;
  }
  ```

###### 09 - 1：findAdvisorBeans

- 遍历从所有 class 类型为Advisor的所有bean，通过 beanName 获取Bean，添加到 Advisor 集合中返回；

  ```java
  public List<Advisor> findAdvisorBeans() {
      // 1.确认advisor的beanName列表，优先从缓存中拿
      String[] advisorNames = this.cachedAdvisorBeanNames;
      if (advisorNames == null) {
          //  1.1 如果缓存为空，则获取class类型为Advisor的所有bean名称
          advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
              this.beanFactory, Advisor.class, true, false);
          this.cachedAdvisorBeanNames = advisorNames;
      }
      if (advisorNames.length == 0) {
          return new ArrayList<>();
      }
      // 2.遍历处理advisorNames
      List<Advisor> advisors = new LinkedList<Advisor>();
      for (String name : advisorNames) {
          // 空接口，总是返回True
          if (isEligibleBean(name)) {
              // 2.1 跳过当前正在创建的advisor
              if (this.beanFactory.isCurrentlyInCreation(name)) {
                  if (logger.isDebugEnabled()) {
                      logger.debug("Skipping currently created advisor '" + name + "'");
                  }
              } else {
                  try {
                      // 2.2 通过beanName获取对应的bean对象，并添加到advisors
                      advisors.add(this.beanFactory.getBean(name, Advisor.class));
                  } catch (BeanCreationException ex) {
                      Throwable rootCause = ex.getMostSpecificCause();
                      if (rootCause instanceof BeanCurrentlyInCreationException) {
                          BeanCreationException bce = (BeanCreationException) rootCause;
                          if (this.beanFactory.isCurrentlyInCreation(bce.getBeanName())) {
                              if (logger.isDebugEnabled()) {
                                  logger.debug("Skipping advisor '" + name +
                                               "' with dependency on currently created bean: " 
                                               + ex.getMessage());
                              }
                              continue;
                          }
                      }
                      throw ex;
                  }
              }
          }
      }
      // 3.返回符合条件的advisor列表
      return advisors;
  }
  ```

###### 09 - 2：buildAspectJAdvisors

- 根据 aspectBeanNames 判断是否解析过，若解析过，直接从缓存中获取增强器，若没有，则拿到多有beanName，遍历判断是否有 AspectJ 注解，则添加到增强器集合中，同时放到缓存中；

  ```java
  public List<Advisor> buildAspectJAdvisors() {
      List<String> aspectNames = this.aspectBeanNames;
      // 1.如果aspectNames为空，则进行解析
      if (aspectNames == null) {
          synchronized (this) {
              aspectNames = this.aspectBeanNames;
              if (aspectNames == null) {
                  List<Advisor> advisors = new LinkedList<Advisor>();
                  aspectNames = new LinkedList<String>();
                  // 1.1 获取所有的beanName
                  String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                      this.beanFactory, Object.class, true, false);
                  // 1.2 循环遍历所有的beanName，找出对应的增强方法
                  for (String beanName : beanNames) {
                      // 1.3 不合法的beanName则跳过，默认返回true，子类可以覆盖实现，
                      // AnnotationAwareAspectJAutoProxyCreator,实现了自己的逻辑
                      if (!isEligibleBean(beanName)) {
                          continue;
                      }
                      // 获取beanName对应的bean的类型
                      Class<?> beanType = this.beanFactory.getType(beanName);
                      if (beanType == null) {
                          continue;
                      }
                      // 1.4 如果beanType存在Aspect注解则进行处理
                      if (this.advisorFactory.isAspect(beanType)) {
                          // 将存在Aspect注解的beanName添加到aspectNames列表
                          aspectNames.add(beanName);
                          // 新建切面元数据
                          AspectMetadata amd = new AspectMetadata(beanType, beanName);
                          // 获取per-clause的类型是SINGLETON
                          if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                              // 使用BeanFactory和beanName创建一个BeanFactoryAspectInstanceFactory，
                              // 主要用来创建切面对象实例
                              MetadataAwareAspectInstanceFactory factory =
                                  new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
                              // 1.5 解析标记AspectJ注解中的增强方法
                              List<Advisor> classAdvisors=this.advisorFactory.getAdvisors(factory);
                              // 1.6 放到缓存中
                              if (this.beanFactory.isSingleton(beanName)) {
                                  // 如果beanName是单例则直接将解析的增强方法放到缓存
                                  this.advisorsCache.put(beanName, classAdvisors);
                              } else {
                                  // 如果不是单例，则将factory放到缓存，之后可以通过factory来解析增强方法
                                  this.aspectFactoryCache.put(beanName, factory);
                              }
                              // 1.7 将解析的增强器添加到advisors
                              advisors.addAll(classAdvisors);
                          } else {
                              // 如果per-clause的类型不是SINGLETON
                              if (this.beanFactory.isSingleton(beanName)) {
                                  // 名称为beanName的Bean是单例，但切面实例化模型不是单例，则抛异常
                                  throw new IllegalArgumentException("Bean with name '" + beanName +
                               "' is a singleton, but aspect instantiation model is not singleton");
                              }
                              MetadataAwareAspectInstanceFactory factory =
                                  new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
                              // 将factory放到缓存，之后可以通过factory来解析增强方法
                              this.aspectFactoryCache.put(beanName, factory);
                              // 解析标记AspectJ注解中的增强方法，并添加到advisors中
                              advisors.addAll(this.advisorFactory.getAdvisors(factory));
                          }
                      }
                  }
                  // 1.9 将解析出来的切面beanName放到缓存aspectBeanNames
                  this.aspectBeanNames = aspectNames;
                  // 1.10 最后返回解析出来的增强器
                  return advisors;
              }
          }
      }
      // 2.如果aspectNames不为null，则代表已经解析过了，则无需再次解析
      if (aspectNames.isEmpty()) {
          return Collections.emptyList();
      }
      // 2.2 aspectNames不是空列表，则遍历处理
      List<Advisor> advisors = new LinkedList<Advisor>();
      for (String aspectName : aspectNames) {
          // 根据aspectName从缓存中获取增强器
          List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
          if (cachedAdvisors != null) {
              // 根据上面的解析，可以知道advisorsCache存的是已经解析好的增强器，直接添加到结果即可
              advisors.addAll(cachedAdvisors);
          } else {
              // 如果不存在于advisorsCache缓存，则代表存在于aspectFactoryCache中，
              // 从aspectFactoryCache中拿到缓存的factory，然后解析出增强器，添加到结果中
              MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
              advisors.addAll(this.advisorFactory.getAdvisors(factory));
          }
      }
      // 返回增强器
      return advisors;
  }
  ```

###### 09 - 2 - 1：getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory)

- ```java
  @Override
  public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
      // 1.前面我们将beanClass和beanName封装成了aspectInstanceFactory的AspectMetadata属性，
      // 这边可以通过AspectMetadata属性重新获取到当前处理的切面类
      Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
      // 2.获取当前处理的切面类的名字
      String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
      // 3.校验切面类
      validate(aspectClass);
      // 4.使用装饰器包装MetadataAwareAspectInstanceFactory，以便它只实例化一次。
      MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
          new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);
  
      List<Advisor> advisors = new LinkedList<Advisor>();
      // 5.获取切面类中的方法（也就是我们用来进行逻辑增强的方法，被@Around、@After等注解修饰的方法，使用@Pointcut的方法不处理）
      for (Method method : getAdvisorMethods(aspectClass)) {
          // 6.处理method，获取增强器
          Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, advisors.size(),
                                       aspectName);
          if (advisor != null) {
              // 7.如果增强器不为空，则添加到advisors
              advisors.add(advisor);
          }
      }
      if (!advisors.isEmpty()
          && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
          // 8.如果寻找的增强器不为空而且又配置了增强延迟初始化，那么需要在首位加入同步实例化增强器
          //（用以保证增强使用之前的实例化）
          Advisor instantiationAdvisor =
              new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
          advisors.add(0, instantiationAdvisor);
      }
      // 9.获取DeclareParents注解
      for (Field field : aspectClass.getDeclaredFields()) {
          Advisor advisor = getDeclareParentsAdvisor(field);
          if (advisor != null) {
              advisors.add(advisor);
          }
      }
  
      return advisors;
  }
  ```

###### 09 - 2 - 1 - 1：getAdvisor

- ```java
  @Override
  public Advisor getAdvisor(Method candidateAdviceMethod,
                            MetadataAwareAspectInstanceFactory aspectInstanceFactory,
                            int declarationOrderInAspect, String aspectName) {
      // 1.校验切面类
      validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());
      // 2.AspectJ切点信息的获取（例如：表达式），就是指定注解的表达式信息的获取
      AspectJExpressionPointcut expressionPointcut = getPointcut(
          candidateAdviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
      // 3.如果expressionPointcut为null，则直接返回null
      if (expressionPointcut == null) {
          return null;
      }
      // 4.根据切点信息生成增强器
      return new InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, 
                                                            candidateAdviceMethod,
                                                            this, aspectInstanceFactory, 
                                                            declarationOrderInAspect, aspectName);
  }
  ```

###### 09 - 2 - 1 - 2：getPointcut

- ```java
  private AspectJExpressionPointcut getPointcut(Method candidateAdviceMethod,
                                                Class<?> candidateAspectClass) {
      // 1.查找并返回给定方法的第一个AspectJ注解（@Before, @Around, @After, @AfterReturning, @AfterThrowing, @Pointcut）
      // 因为我们之前把@Pointcut注解的方法跳过了，所以这边必然不会获取到@Pointcut注解
      AspectJAnnotation<?> aspectJAnnotation =
          AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
      // 2.如果方法没有使用AspectJ的注解，则返回null
      if (aspectJAnnotation == null) {
          return null;
      }
      // 3.使用AspectJExpressionPointcut实例封装获取的信息
      AspectJExpressionPointcut ajexp =
          new AspectJExpressionPointcut(candidateAspectClass, new String[0], new Class<?>[0]);
      // 提取得到的注解中的表达式，execution(* com.joonwhee.open.aop.*.*(..))
      ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
      ajexp.setBeanFactory(this.beanFactory);
      return ajexp;
  }
  protected static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method method) {
      // 设置要查找的注解类
      Class<?>[] classesToLookFor = new Class<?>[] {
          Before.class, Around.class, After.class, AfterReturning.class, AfterThrowing.class,
          Pointcut.class};
      for (Class<?> c : classesToLookFor) {
          // 查找方法上是否存在当前遍历的注解，如果有则返回
          AspectJAnnotation<?> foundAnnotation = findAnnotation(method, (Class<Annotation>) c);
          if (foundAnnotation != null) {
              return foundAnnotation;
          }
      }
      return null;
  }
  ```

###### 09 - 2 - 1 - 3：new InstantiationModelAwarePointcutAdvisorImpl

- ```java
  public InstantiationModelAwarePointcutAdvisorImpl(AspectJExpressionPointcut declaredPointcut,
                                                    Method aspectJAdviceMethod,
                                                    AspectJAdvisorFactory aspectJAdvisorFactory,
                                    MetadataAwareAspectInstanceFactory aspectInstanceFactory,
                                                    int declarationOrder, String aspectName) {
      // 1.简单的将信息封装在类的实例中
      this.declaredPointcut = declaredPointcut;
      this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
      this.methodName = aspectJAdviceMethod.getName();
      this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
      // aspectJAdviceMethod 保存的是我们用来进行逻辑增强的方法（@Around、@After等修饰的方法）
      this.aspectJAdviceMethod = aspectJAdviceMethod;
      this.aspectJAdvisorFactory = aspectJAdvisorFactory;
      this.aspectInstanceFactory = aspectInstanceFactory;
      this.declarationOrder = declarationOrder;
      this.aspectName = aspectName;
      // 2.是否需要延迟实例化
      if (aspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
          Pointcut preInstantiationPointcut = Pointcuts.union(
              aspectInstanceFactory.getAspectMetadata().getPerClausePointcut(),
              this.declaredPointcut);
          this.pointcut = new PerTargetInstantiationModelPointcut(
              this.declaredPointcut, preInstantiationPointcut, aspectInstanceFactory);
          this.lazy = true;
      } else {
          this.pointcut = this.declaredPointcut;
          this.lazy = false;
          // 3.实例化增强器：根据注解中的信息初始化对应的增强器
          this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
      }
  }
  ```

###### 09 - 2 - 1 - 4：instantiateAdvice -> AbstractAspectJAdvice

- ```java
  private Advice instantiateAdvice(AspectJExpressionPointcut pcut) {
      return this.aspectJAdvisorFactory.getAdvice(this.aspectJAdviceMethod, pcut,
                                                  this.aspectInstanceFactory, this.declarationOrder,
                                                  this.aspectName);
  }
  
  // ReflectiveAspectJAdvisorFactory.java
  @Override
  public Advice getAdvice(Method candidateAdviceMethod,
                          AspectJExpressionPointcut expressionPointcut,
                          MetadataAwareAspectInstanceFactory aspectInstanceFactory,
                          int declarationOrder, String aspectName) {
      // 1.获取切面类
      Class<?> candidateAspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
      // 2.校验切面类（重复校验第3次...）
      validate(candidateAspectClass);
      // 3.查找并返回方法的第一个AspectJ注解
      AspectJAnnotation<?> aspectJAnnotation =
          AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
      if (aspectJAnnotation == null) {
          return null;
      }
      // 4.如果我们到这里，我们知道我们有一个AspectJ方法。检查切面类是否使用了AspectJ注解
      if (!isAspect(candidateAspectClass)) {
          throw new AopConfigException("Advice must be declared inside an aspect type: " +
          "Offending method '" + candidateAdviceMethod + "' in class [" + 
                                       candidateAspectClass.getName() + "]");
      }
      if (logger.isDebugEnabled()) {
          logger.debug("Found AspectJ method: " + candidateAdviceMethod);
      }
      
      AbstractAspectJAdvice springAdvice;
      // 5.根据方法使用的aspectJ注解创建对应的增强器，例如最常见的@Around注解会创建AspectJAroundAdvice
      switch (aspectJAnnotation.getAnnotationType()) {
          case AtBefore:
              springAdvice = new AspectJMethodBeforeAdvice(
                  candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
              break;
          case AtAfter:
              springAdvice = new AspectJAfterAdvice(
                  candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
              break;
          case AtAfterReturning:
              springAdvice = new AspectJAfterReturningAdvice(
                  candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
              AfterReturning afterReturningAnnotation = 
                  (AfterReturning) aspectJAnnotation.getAnnotation();
              if (StringUtils.hasText(afterReturningAnnotation.returning())) {
                  springAdvice.setReturningName(afterReturningAnnotation.returning());
              }
              break;
          case AtAfterThrowing:
              springAdvice = new AspectJAfterThrowingAdvice(
                  candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
              AfterThrowing afterThrowingAnnotation =
                  (AfterThrowing) aspectJAnnotation.getAnnotation();
              if (StringUtils.hasText(afterThrowingAnnotation.throwing())) {
                  springAdvice.setThrowingName(afterThrowingAnnotation.throwing());
              }
              break;
          case AtAround:
              springAdvice = new AspectJAroundAdvice(
                  candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
              break;
          case AtPointcut:
              if (logger.isDebugEnabled()) {
                  logger.debug("Processing pointcut '" + candidateAdviceMethod.getName() + "'");
              }
              return null;
          default:
              throw new UnsupportedOperationException(
                  "Unsupported advice type on method: " + candidateAdviceMethod);
      }
      // 6.配置增强器
      // 切面类的name，其实就是beanName
      springAdvice.setAspectName(aspectName);
      springAdvice.setDeclarationOrder(declarationOrder);
      // 获取增强方法的参数
      String[] argNames = this.parameterNameDiscoverer.getParameterNames(candidateAdviceMethod);
      if (argNames != null) {
          // 如果参数不为空，则赋值给springAdvice
          springAdvice.setArgumentNamesFromStringArray(argNames);
      }
      springAdvice.calculateArgumentBindings();
      return springAdvice;
  }
  ```

###### 09 - 3：findAdvisorsThatCanApply

- 所有 bean 实例化时，都会触发后置处理器，获取所有Advisor ，然后判断当前 bean 是不是在执行切点内；

  ```java
  public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors,
                                                       Class<?> clazz) {
      if (candidateAdvisors.isEmpty()) {
          return candidateAdvisors;
      }
      List<Advisor> eligibleAdvisors = new LinkedList<Advisor>();
      // 1.首先处理引介增强（@DeclareParents）用的比较少可以忽略
      for (Advisor candidate : candidateAdvisors) {
          if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
              eligibleAdvisors.add(candidate);
          }
      }
      boolean hasIntroductions = !eligibleAdvisors.isEmpty();
      // 2.遍历所有的candidateAdvisors
      for (Advisor candidate : candidateAdvisors) {
          // 2.1 引介增强已经处理，直接跳过
          if (candidate instanceof IntroductionAdvisor) {
              continue;
          }
          // 2.2 正常增强处理，判断当前bean是否可以应用于当前遍历的增强器
          //（bean是否包含在增强器的execution指定的表达式中）
          if (canApply(candidate, clazz, hasIntroductions)) {
              eligibleAdvisors.add(candidate);
          }
      }
      return eligibleAdvisors;
  }
  ```

##### 10：createProxy

- 

  ```java
  protected Object createProxy(Class<?> beanClass, String beanName, Object[] specificInterceptors,
      TargetSource targetSource) {
      if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
          AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory,
                                           beanName, beanClass);
      }
      // 1.初始化ProxyFactory
      ProxyFactory proxyFactory = new ProxyFactory();
      // 从当前对象复制属性值
      proxyFactory.copyFrom(this);
      // 检查proxyTargetClass属性，判断对于给定的bean使用 类代理还是接口代理
      // proxyTargetClass值默认为false，可以通过proxy-target-class属性设置为true
      if (!proxyFactory.isProxyTargetClass()) {
          // 检查preserveTargetClass属性，判断beanClass是应该基于类代理还是基于接口代理
          if (shouldProxyTargetClass(beanClass, beanName)) {
              // 如果是基于类代理，则将proxyTargetClass赋值为true
              proxyFactory.setProxyTargetClass(true);
          } else {
              // 评估bean的代理接口
              evaluateProxyInterfaces(beanClass, proxyFactory);
          }
      }
      // 将拦截器封装为Advisor（advice持有者）
      Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
      // 将advisors添加到proxyFactory
      proxyFactory.addAdvisors(advisors);
      // 设置要代理的类，将targetSource赋值给proxyFactory的targetSource属性，通过该属性拿到被代理的bean的实例
      proxyFactory.setTargetSource(targetSource);
      // 自定义ProxyFactory，空方法，留给子类实现
      customizeProxyFactory(proxyFactory);
      // 控制proxyFactory被配置之后，是否还允许修改。默认值为false（即在代理被配置之后，不允许修改代理类的配置）
      proxyFactory.setFrozen(this.freezeProxy);
      if (advisorsPreFiltered()) {
          proxyFactory.setPreFiltered(true);
      }
      // 2.使用proxyFactory获取代理
      return proxyFactory.getProxy(getProxyClassLoader());
  }
  ```

##### 11：getProxy

- ```java
  public Object getProxy(ClassLoader classLoader) {
      // 1.createAopProxy：创建AopProxy
      // 2.getProxy(classLoader)：获取代理对象实例
      return createAopProxy().getProxy(classLoader);
  }
  ```

##### 12：createAopProxy

- 选用那种策略，创建代理对象；

  ```java
  protected final synchronized AopProxy createAopProxy() {
      if (!this.active) {
          // 1.激活此代理配置
          activate();
      }
      // 2.创建AopProxy
      return getAopProxyFactory().createAopProxy(this);
  }
  
  @Override
  public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
      // 1.判断使用JDK动态代理还是Cglib代理
      // optimize：用于控制通过cglib创建的代理是否使用激进的优化策略。除非完全了解AOP如何处理代理优化，
      // 否则不推荐使用这个配置，目前这个属性仅用于cglib代理，对jdk动态代理无效
      // proxyTargetClass：默认为false，设置为true时，强制使用cglib代理
      // hasNoUserSuppliedProxyInterfaces：config是否存在代理接口或者只有SpringProxy一个接口
      if (config.isOptimize() || config.isProxyTargetClass()
          || hasNoUserSuppliedProxyInterfaces(config)) {
          // 拿到要被代理的对象的类型
          Class<?> targetClass = config.getTargetClass();
          // TargetSource无法确定目标类：代理创建需要接口或目标。
          if (targetClass == null) {
              throw new AopConfigException("TargetSource cannot determine target class: " +
                     "Either an interface or a target is required for proxy creation.");
          }
          // 要被代理的对象是接口 || targetClass是Proxy class
          // 当且仅当使用getProxyClass方法或newProxyInstance方法动态生成指定的类作为代理类时，才返回true。
          if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
              // JDK动态代理，这边的入参config(AdvisedSupport)实际上是ProxyFactory对象
              return new JdkDynamicAopProxy(config);
          }
          // Cglib代理
          return new ObjenesisCglibAopProxy(config);
      } else {
          // JDK动态代理
          return new JdkDynamicAopProxy(config);
      }
  }
  ```

##### 13：CgLib代理（CglibAopProxy）

- 无论是创建 JDK 动态代理还是 CGLIB 代理，都会传入 config 参数，该参数会被保存在 advised（AdvisedSupport）变量中。

- ```java
  // CglibAopProxy.java
  public CglibAopProxy(AdvisedSupport config) throws AopConfigException {
      Assert.notNull(config, "AdvisedSupport must not be null");
      if (config.getAdvisors().length == 0 
          && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
          throw new AopConfigException("No advisors and no TargetSource specified");
      }
      this.advised = config;
      this.advisedDispatcher = new AdvisedDispatcher(this.advised);
  }
  ```

##### 14：CglibAopProxy#getProxy

- 与 11 呼应

- ```java
  @Override
  public Object getProxy(ClassLoader classLoader) {
      if (logger.isDebugEnabled()) {
          logger.debug("Creating CGLIB proxy: target source is " + this.advised.getTargetSource());
      }
      try {
          // 1.拿到要代理目标类
          Class<?> rootClass = this.advised.getTargetClass();
          Assert.state(rootClass != null,
                       "Target class must be available for creating a CGLIB proxy");
          // proxySuperClass默认为rootClass
          Class<?> proxySuperClass = rootClass;
          if (rootClass.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
              // 如果rootClass是被Cglib代理过的，获取rootClass的父类作为proxySuperClass
              proxySuperClass = rootClass.getSuperclass();
              Class<?>[] additionalInterfaces = rootClass.getInterfaces();
              for (Class<?> additionalInterface : additionalInterfaces) {
                  // 将父类的接口也添加到advised的interfaces属性
                  this.advised.addInterface(additionalInterface);
              }
          }
          // 2.校验proxySuperClass，主要是校验方法是否用final修饰、跨ClassLoader的包可见方法
          // 如果有将警告写入日志
          validateClassIfNecessary(proxySuperClass, classLoader);
          // 3.创建和配置Cglib Enhancer(cgliab 生成的子类)
          Enhancer enhancer = createEnhancer();
          if (classLoader != null) {
              enhancer.setClassLoader(classLoader);
              if (classLoader instanceof SmartClassLoader &&
                  ((SmartClassLoader) classLoader).isClassReloadable(proxySuperClass)) {
                  enhancer.setUseCache(false);
              }
          }
          // superclass为被代理的目标类proxySuperClass，通过名字可以看出，生成的代理类实际上是继承了被代理类
          enhancer.setSuperclass(proxySuperClass);
          enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
          enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
          enhancer.setStrategy(new ClassLoaderAwareUndeclaredThrowableStrategy(classLoader));
          // 4.获取所有要回调的拦截器
          Callback[] callbacks = getCallbacks(rootClass);
          Class<?>[] types = new Class<?>[callbacks.length];
          for (int x = 0; x < types.length; x++) {
              types[x] = callbacks[x].getClass();
          }
          // 在上面调用getCallbacks之后，此时仅填充 fixedInterceptorMap
          enhancer.setCallbackFilter(new ProxyCallbackFilter(
              this.advised.getConfigurationOnlyCopy(), this.fixedInterceptorMap, 
              this.fixedInterceptorOffset));
          enhancer.setCallbackTypes(types);
          // 5.生成代理类并创建代理实例，返回代理实例
          return createProxyClassAndInstance(enhancer, callbacks);
      } catch (CodeGenerationException ex) {
          throw new AopConfigException("Could not generate CGLIB subclass of class [" +
          this.advised.getTargetClass() + "]: "
  		+ "Common causes of this problem include using a final class or a non-visible class", ex);
      } catch (Throwable ex) {
          throw new AopConfigException("Unexpected AOP exception", ex);
      }
  }
  ```

##### 15：getCallbacks

- ```java
  private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
      // 1.用于优化选择的参数
      boolean exposeProxy = this.advised.isExposeProxy();
      boolean isFrozen = this.advised.isFrozen();
      boolean isStatic = this.advised.getTargetSource().isStatic();
      // 2.使用AdvisedSupport作为参数，创建一个DynamicAdvisedInterceptor（“aop”拦截器，用于AOP调用）
      // this.advised就是之前创建CglibAopProxy时传进来的ProxyFactory(ProxyCreatorSupport子类)
      Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);
      Callback targetInterceptor;
      if (exposeProxy) {
          targetInterceptor = isStatic ?
              new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
          new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource());
      } else {
          targetInterceptor = isStatic ?
              new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
          new DynamicUnadvisedInterceptor(this.advised.getTargetSource());
      }
      Callback targetDispatcher = isStatic ?
          new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp();
  
      // 3.将aop拦截器添加到mainCallbacks中
      Callback[] mainCallbacks = new Callback[]{
          aopInterceptor,  // for normal advice aop拦截器，因此当代理类被执行时，会走到该拦截器中
          targetInterceptor,  // invoke target without considering advice, if optimized
          new SerializableNoOp(),  // no override for methods mapped to this
          targetDispatcher, this.advisedDispatcher,
          new EqualsInterceptor(this.advised),    // 针对equals方法的拦截器
          new HashCodeInterceptor(this.advised)   // 针对hashcode方法的拦截器
      };
      Callback[] callbacks;
      if (isStatic && isFrozen) {
          Method[] methods = rootClass.getMethods();
          Callback[] fixedCallbacks = new Callback[methods.length];
          this.fixedInterceptorMap = new HashMap<String, Integer>(methods.length);
          for (int x = 0; x < methods.length; x++) {
              List<Object> chain = this.advised
                  .getInterceptorsAndDynamicInterceptionAdvice(methods[x], rootClass);
              fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(
                  chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass());
              this.fixedInterceptorMap.put(methods[x].toString(), x);
          }
  
          // 合并callback
          callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];
          System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
          System.arraycopy(fixedCallbacks, 0, callbacks, mainCallbacks.length, 
                           fixedCallbacks.length);
          this.fixedInterceptorOffset = mainCallbacks.length;
      } else {
          callbacks = mainCallbacks;
      }
      return callbacks;
  }
  ```



