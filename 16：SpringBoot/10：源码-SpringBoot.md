### 源码-SpringBoot

------

[TOC]

##### 01：概述

- 构造一个**SpringApplication**的实例，并把我们的启动类**xxxApplication.class**作为参数传进去，然后运行它的 run 方法。

  ```java
  public static ConfigurableApplicationContext run(Object[] sources, String[] args) {
      return (new SpringApplication(sources)).run(args);
  }
  // SpringApplication 实例的初始化
  public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
      this.resourceLoader = resourceLoader;
      Assert.notNull(primarySources, "PrimarySources must not be null");
      // 把xxxApplication.class 赋值给primarySources
      this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
      // 设置应用类型是 Standard 还是 Servlet
      this.webApplicationType = deduceWebApplicationType();
      // 设置初始化器(Initializer),最后会调用这些初始化器
      setInitializers(
          (Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
      // 设置监听器(Listener)
      setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
      // 通过抛出的异常名 判断启动类名
      this.mainApplicationClass = deduceMainApplicationClass();
  }
  ```

##### 02：设置初始化器

- 从 spring.factories 配置文件中，实例化 ApplicationContextInitializer 的子类，做初始化器；

  ```java
  // 获取 key:ApplicationContextInitializer.class 的子类
  private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
      return getSpringFactoriesInstances(type, new Class<?>[] {});
  }
  
  private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes,
                                                        Object... args) {
      ClassLoader classLoader = getClassLoader();
      // 使用Set保存类路径来避免重复元素
      Set<String> names = new LinkedHashSet<>(
          SpringFactoriesLoader.loadFactoryNames(type, classLoader));
      // 通过反射实例化类
      List<T> instances = createSpringFactoriesInstances(type, parameterTypes,
                                                         classLoader, args, names);
      AnnotationAwareOrderComparator.sort(instances);
      return instances;
  }
  
  public static List<String> loadFactoryNames(Class<?> factoryType,
                                              @Nullable ClassLoader classLoader) {
      String factoryTypeName = factoryType.getName();
      // 获取key:ApplicationContextInitializer.class 的集合值
      return loadSpringFactories(classLoader)
          .getOrDefault(factoryTypeName, Collections.emptyList());
  }
  private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
      // 先查缓存
      MultiValueMap<String, String> result = cache.get(classLoader);
      if (result != null) {
          return result;
      }
  
      try {
          // 从类路径的META-INF/spring.factories中加载所有默认的自动配置类
          Enumeration<URL> urls = (classLoader != null ?
                                   classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
                                   ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
          result = new LinkedMultiValueMap<>();
          while (urls.hasMoreElements()) {
              URL url = urls.nextElement();
              UrlResource resource = new UrlResource(url);
              Properties properties = PropertiesLoaderUtils.loadProperties(resource);
              for (Map.Entry<?, ?> entry : properties.entrySet()) {
                  // 将 spring.factories 文件解析成 key : value
                  String factoryTypeName = ((String) entry.getKey()).trim();
                  for (String factoryImplementationName : 
                       StringUtils.commaDelimitedListToStringArray((String) entry.getValue())) {
                      result.add(factoryTypeName, factoryImplementationName.trim());
                  }
              }
          }
          // 缓存
          cache.put(classLoader, result);
          return result;
      } catch (IOException ex) {
          throw new IllegalArgumentException("Unable to load factories from location [" +
                                             FACTORIES_RESOURCE_LOCATION + "]", ex);
      }
  }
  ```

##### 03：设置监听器

- 与设置初始化器一样，从 spring.factories 中实例化类；

  ```java
  // 这里的入参type是：org.springframework.context.ApplicationListener.class
  private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type) {
      return getSpringFactoriesInstances(type, new Class<?>[] {});
  }
  ```

#### 04：SpringApplication.run(args)

- ```java
  public ConfigurableApplicationContext run(String... args) {
      // 计时器
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      ConfigurableApplicationContext context = null;
      Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
      configureHeadlessProperty();
      // 第一步：获取并启动监听器
      SpringApplicationRunListeners listeners = getRunListeners(args);
      listeners.starting();
      try {
          ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
          // 第二步：根据SpringApplicationRunListeners以及参数来准备环境
          ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
          // 第三步：配置反射时是否忽略BeanInfo 信息
          configureIgnoreBeanInfo(environment);
          // 第四步：打印Banner - 就是启动Spring Boot的时候打印在console上的ASCII艺术字体
          Banner printedBanner = printBanner(environment);
          // 第五步：创建Spring容器
          context = createApplicationContext();
          // 第六步：用于获取所有已注册的SpringBootExceptionReporter实例
          exceptionReporters = getSpringFactoriesInstances(
              SpringBootExceptionReporter.class,
              new Class[] { ConfigurableApplicationContext.class }, context);
          // 第七步：Spring容器前置处理
          prepareContext(context, environment, listeners, applicationArguments, printedBanner);
          // 第八步：刷新容器
          refreshContext(context);
          // 第九步：Spring容器后置处理
          afterRefresh(context, applicationArguments);
          // 第十步：发出结束执行的事件
          listeners.started(context);
          // 第十一步：执行Runners
          this.callRunners(context, applicationArguments);
          stopWatch.stop();
          return context;
      } catch (Throwable ex) {
          handleRunFailure(context, listeners, exceptionReporters, ex);
          throw new IllegalStateException(ex);
      }
      
      try {
          // 在应用程序启动完成后调用，通常用于执行一些初始化或清理操作
          listeners.running(context);
      } catch (Throwable ex) {
          handleRunFailure(context, ex, exceptionReporters, null);
          throw new IllegalStateException(ex);
      }
      return context;
  }
  ```

##### 05：getRunListeners(args) 获取并启动监听器

- ```java
  private SpringApplicationRunListeners getRunListeners(String[] args) {
      Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
      // key 为SpringApplicationRunListener
      return new SpringApplicationRunListeners(logger,
                getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
  }
  
  org.springframework.boot.SpringApplicationRunListener=\
  org.springframework.boot.context.event.EventPublishingRunListener
  ```

###### EventPublishingRunListener：获取监听器

```java
public class EventPublishingRunListener implements SpringApplicationRunListener, Ordered {
    private final SpringApplication application;
    private final String[] args;
    // 广播器，存放了 SpringApplication 初始化的所有监听器
    private final SimpleApplicationEventMulticaster initialMulticaster;

    public EventPublishingRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
        this.initialMulticaster = new SimpleApplicationEventMulticaster();
        for (ApplicationListener<?> listener : application.getListeners()) {
            // 将设置到SpringApplication的十一个监听器全部添加到SimpleApplicationEventMulticaster这个广播器中
            this.initialMulticaster.addApplicationListener(listener);
        }

    }
    //略...
}

@Override
public void addApplicationListener(ApplicationListener<?> listener) {
    // 是否可重复的，默认false
    synchronized (this.retrievalMutex) {
        // 移除监听器中代理的目标类，防止同一监听器重复调用
        Object singletonTarget = AopProxyUtils.getSingletonTarget(listener);
        if (singletonTarget instanceof ApplicationListener) {
            this.defaultRetriever.applicationListeners.remove(singletonTarget);
        }
        // 内部类，用来保存所有的监听器
        this.defaultRetriever.applicationListeners.add(listener);
        this.retrieverCache.clear();
    }
}
```

###### 启动监听器

- 在 Spring Boot 启动初始化的过程中各种状态时 执行，

  ```java
  public interface SpringApplicationRunListener {
      // 在run()方法开始执行时，该方法就立即被调用，可用于在初始化最早期时做一些工作
      void starting();
      // 当environment构建完成，ApplicationContext创建之前，该方法被调用
      void environmentPrepared(ConfigurableEnvironment environment);
      // 当ApplicationContext构建完成时，该方法被调用
      void contextPrepared(ConfigurableApplicationContext context);
      // 在ApplicationContext完成加载，但没有被刷新前，该方法被调用
      void contextLoaded(ConfigurableApplicationContext context);
      // 在ApplicationContext刷新并启动后，CommandLineRunners和ApplicationRunner未被调用前，该方法被调用
      void started(ConfigurableApplicationContext context);
      // 在run()方法执行完成前该方法被调用
      void running(ConfigurableApplicationContext context);
      // 当应用运行出错时该方法被调用
      void failed(ConfigurableApplicationContext context, Throwable exception);
  }
  
  @Override
  public void starting() {
      // 先创建application启动事件`ApplicationStartingEvent`
      this.initialMulticaster.multicastEvent(new ApplicationStartingEvent(this.application, 
                                                                          this.args));
  }
  
  @Override
  public void multicastEvent(final ApplicationEvent event, @Nullable ResolvableType eventType) {
      ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
      // 获取任务线程池，还未初始化为空
      Executor executor = getTaskExecutor();
      // 通过事件类型ApplicationStartingEvent获取对应的监听器
      for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
          if (executor != null) {
              executor.execute(() -> invokeListener(listener, event));
          } else {
              invokeListener(listener, event);
          }
      }
  }
  ```

##### 06：准备环境

- ```java
  private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
                                                     ApplicationArguments applicationArguments) {
      // 获取对应的ConfigurableEnvironment
      ConfigurableEnvironment environment = getOrCreateEnvironment();
      // 解析应用参数并且配置环境
      configureEnvironment(environment, applicationArguments.getSourceArgs());
      ConfigurationPropertySources.attach(environment);
      // 发布环境已准备事件，这是第二次发布事件
      listeners.environmentPrepared(environment);
      // 用于将SpringApplication实例与应用程序的环境对象进行关联
      bindToSpringApplication(environment);
      if (!this.isCustomEnvironment) {
          environment = new EnvironmentConverter(getClassLoader())
              .convertEnvironmentIfNecessary(environment, deduceEnvironmentClass());
      }
      ConfigurationPropertySources.attach(environment);
      return environment;
  }
  ```

###### getOrCreateEnvironment

- 前面设置为：Servlet ，返回StandardServletEnvironment

  ```java
  private ConfigurableEnvironment getOrCreateEnvironment() {
      if (this.environment != null) {
          return this.environment;
      }
      switch (this.webApplicationType) {
          case SERVLET:
              return new StandardServletEnvironment();
          case REACTIVE:
              return new StandardReactiveWebEnvironment();
          default:
              return new StandardEnvironment();
      }
  }
  ```

###### listeners.environmentPrepared(environment);

- 会触发一个非常核心的监听器：**ConfigFileApplicationListener**，用于处理配置文件。

  ```java
  @Override
  public void environmentPrepared(ConfigurableEnvironment environment) {
      this.initialMulticaster.multicastEvent(
          new ApplicationEnvironmentPreparedEvent(this.application, this.args, environment));
  }
  ```

###### bindToSpringApplication

- ```java
  protected void bindToSpringApplication(ConfigurableEnvironment environment) {
      try {
          // 将"spring.main"作为属性前缀，将当前SpringApplication实例作为绑定目标，绑定到环境对象
          Binder.get(environment).bind("spring.main", Bindable.ofInstance(this));
      } catch (Exception ex) {
          throw new IllegalStateException("Cannot bind to SpringApplication", ex);
      }
  }
  ```

##### 07：反射时是否忽略BeanInfo 信息

- 当使用Java反射机制访问Java Bean的属性和方法时，**Java会先尝试从BeanInfo中获取相关信息，如果没有找到，则会默认使用反射机制获取**，设置为True，表示直接使用反射机制。

  ```java
  private void configureIgnoreBeanInfo(ConfigurableEnvironment environment) {
      if (System.getProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME) == null) {
          // 默认是True
          Boolean ignore = environment.getProperty("spring.beaninfo.ignore",
                                                   Boolean.class, Boolean.TRUE);
          System.setProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME,
                             ignore.toString());
      }
  }
  ```

##### 08：是否打印Banner

- ```java
  private Banner printBanner(ConfigurableEnvironment environment) {
      if (this.bannerMode == Banner.Mode.OFF) {
          return null;
      }
      ResourceLoader resourceLoader = (this.resourceLoader != null)
          ? this.resourceLoader : new DefaultResourceLoader(null);
      SpringApplicationBannerPrinter bannerPrinter =
          new SpringApplicationBannerPrinter(resourceLoader, this.banner);
      if (this.bannerMode == Mode.LOG) {
          return bannerPrinter.print(environment, this.mainApplicationClass, logger);
      }
      return bannerPrinter.print(environment, this.mainApplicationClass, System.out);
  }
  ```

##### 09：通过反射创建容器

- ```java
  public static final String DEFAULT_SERVLET_WEB_CONTEXT_CLASS = "org.springframework.boot."
  			+ "web.servlet.context.AnnotationConfigServletWebServerApplicationContext";
  
  protected ConfigurableApplicationContext createApplicationContext() {
      Class<?> contextClass = this.applicationContextClass;
      if (contextClass == null) {
          try {
              switch (this.webApplicationType) {
                  case SERVLET:
                      contextClass = Class.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS);
                      break;
                  case REACTIVE:
                      contextClass = Class.forName(DEFAULT_REACTIVE_WEB_CONTEXT_CLASS);
                      break;
                  default:
                      contextClass = Class.forName(DEFAULT_CONTEXT_CLASS);
              }
          }
          catch (ClassNotFoundException ex) {
              throw new IllegalStateException("Unable create a default ApplicationContext,
                                              + "please specify an ApplicationContextClass", ex);
          }
      }
      return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
  }
  ```

##### 10：创建 Spring 应用程序上下文之前，一些准备工作

- prepareContext(context, environment, listeners, applicationArguments, printedBanner)

  ```java
  private void prepareContext(ConfigurableApplicationContext context,
                              ConfigurableEnvironment environment,
                              SpringApplicationRunListeners listeners,
                              ApplicationArguments applicationArguments,
                              Banner printedBanner) {
  	// 设置容器环境，包括各种变量
      context.setEnvironment(environment);
  	// 执行容器后置处理
      postProcessApplicationContext(context);
  	// 执行容器中的 ApplicationContextInitializer（包括 spring.factories和自定义的实例）
      applyInitializers(context);
  　　 // 发送容器已经准备好的事件，通知各监听器
      listeners.contextPrepared(context);
      if (this.logStartupInfo) {
          logStartupInfo(context.getParent() == null);
          logStartupProfileInfo(context);
      }
      // 注册启动参数bean，这里将容器指定的参数封装成单例 bean，注入容器
      ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
      beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
      // 设置banner
      if (printedBanner != null) {
          beanFactory.registerSingleton("springBootBanner", printedBanner);
      }
      if (beanFactory instanceof DefaultListableBeanFactory) {
          ((DefaultListableBeanFactory) beanFactory).setAllowBeanDefinitionOverriding(
              this.allowBeanDefinitionOverriding);
      }
      if (this.lazyInitialization) {
          context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
      }
      // 获取启动类
      Set<Object> sources = getAllSources();
      Assert.notEmpty(sources, "Sources must not be empty");
      // 加载启动类，将启动类注入容器
      load(context, sources.toArray(new Object[0]));
      // 发布容器已加载事件
      listeners.contextLoaded(context);
  }
  ```
  

###### applyInitializers：调用初始化器

- 构造SpringBoot时，设置的初始化器

  ```java
  protected void applyInitializers(ConfigurableApplicationContext context) {
      // 1. 从SpringApplication类中的initializers集合获取所有的ApplicationContextInitializer
      for (ApplicationContextInitializer initializer : getInitializers()) {
          // 2. 循环调用ApplicationContextInitializer中的initialize方法
          Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(
              initializer.getClass(), ApplicationContextInitializer.class);
          Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
          initializer.initialize(context);
      }
  }
  ```

###### getAllSources：加载启动指定类

- ```java
  public Set<Object> getAllSources() {
      Set<Object> allSources = new LinkedHashSet<>();
      // 添加启动类Class
      if (!CollectionUtils.isEmpty(this.primarySources)) {
          allSources.addAll(this.primarySources);
      }
      // 添加自定义启动类Class
      if (!CollectionUtils.isEmpty(this.sources)) {
          allSources.addAll(this.sources);
      }
      return Collections.unmodifiableSet(allSources);
  }
  ```

###### load：加载启动 Bean 到容器中【重要】

- ```java
  protected void load(ApplicationContext context, Object[] sources) {
      if (logger.isDebugEnabled()) {
          logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
      }
      // 获取容器加载器
      BeanDefinitionLoader loader = createBeanDefinitionLoader(
          getBeanDefinitionRegistry(context), sources);
      if (this.beanNameGenerator != null) {
          loader.setBeanNameGenerator(this.beanNameGenerator);
      }
      if (this.resourceLoader != null) {
          loader.setResourceLoader(this.resourceLoader);
      }
      if (this.environment != null) {
          loader.setEnvironment(this.environment);
      }
      // 加载
      loader.load();
  }
  ```

##### 11：refresh：刷新容器

- 执行到这里，springBoot相关的处理工作已经结束，接下的工作就交给了spring。

- ```java
  private void refreshContext(ConfigurableApplicationContext context) {
      // 刷新容器
      refresh((ApplicationContext) context);
      if (this.registerShutdownHook) {
          try {
              // 注册钩子关闭容器
              context.registerShutdownHook();
          }
          catch (AccessControlException ex) {
              // Not allowed in some environments.
          }
      }
  }
  
  protected void refresh(ConfigurableApplicationContext applicationContext) {
      applicationContext.refresh();
  }
  
  @Override
  public void registerShutdownHook() {
      if (this.shutdownHook == null) {
          // No shutdown hook registered yet.
          this.shutdownHook = new Thread(SHUTDOWN_HOOK_THREAD_NAME) {
              @Override
              public void run() {
                  synchronized (startupShutdownMonitor) {
                      // 容器销毁时，钩子清理资源
                      doClose();
                  }
              }
          };
          Runtime.getRuntime().addShutdownHook(this.shutdownHook);
      }
  }
  ```

##### 12：发出容器已经启动的事件

- ```java
  @Override
  public void started(ConfigurableApplicationContext context) {
      context.publishEvent(new ApplicationStartedEvent(this.application, this.args, context));
      AvailabilityChangeEvent.publish(context, LivenessState.CORRECT);
  }
  ```

##### 13：执行Runners

- ```java
  private void callRunners(ApplicationContext context, ApplicationArguments args) {
      List<Object> runners = new ArrayList<Object>();
      // 获取容器中所有的 ApplicationRunner 的 Bean 实例
      runners.addAll(context.getBeansOfType(ApplicationRunner.class).values());
      // 获取容器中所有的 CommandLineRunner 的 Bean实例
      runners.addAll(context.getBeansOfType(CommandLineRunner.class).values());
      AnnotationAwareOrderComparator.sort(runners);
      for (Object runner : new LinkedHashSet<Object>(runners)) {
          if (runner instanceof ApplicationRunner) {
              // 执行 ApplicationRunner 的 run 方法
              callRunner((ApplicationRunner) runner, args);
          }
          if (runner instanceof CommandLineRunner) {
              // 执行 CommandLineRunner 的 run 方法
              callRunner((CommandLineRunner) runner, args);
          }
      }
  }
  
  private void callRunner(ApplicationRunner runner, ApplicationArguments args) {
      try {
          runner.run(args);
      } catch (Exception var4) {
          throw new IllegalStateException("Failed to execute ApplicationRunner", var4);
      }
  }
  ```









