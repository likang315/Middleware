### 源码 - finishRefresh

------

[TOC]

##### 01：reFresh#initApplicationEventMulticaster：初始化应用的事件广播器

- 如果当前 BeanFactory 中已经存在 **beanName = applicationEventMulticaster** 的 bean 实例或者 BeanDefinition，那么就使用该 bean 作为 applicationEventMulticaster。否则，新建一个默认的事件广播器 SimpleApplicationEventMulticaster 作为 applicationEventMulticaster，并且会注册到 BeanFactory 中。

  ```java
  protected void initApplicationEventMulticaster() {
      ConfigurableListableBeanFactory beanFactory = getBeanFactory();
      // 1.判断BeanFactory是否已经存在事件广播器（固定使用beanName=applicationEventMulticaster）
      if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
          // 1.1 如果已经存在，则将该bean赋值给applicationEventMulticaster
          this.applicationEventMulticaster =
              beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
                                  ApplicationEventMulticaster.class);
          }
      } else {
          // 1.2 如果不存在，则使用SimpleApplicationEventMulticaster
          this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
          // 并将SimpleApplicationEventMulticaster作为默认的事件广播器，注册到BeanFactory中
          beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, 
                                        this.applicationEventMulticaster);
      }
  }
  ```

##### 02：registerListeners

- ```java
  protected void registerListeners() {
      // 1.通过硬编码调用addApplicationListener方法添加的监听器处理
      // （可以通过自定义ApplicationContextInitializer添加）
      for (ApplicationListener<?> listener : getApplicationListeners()) {
          getApplicationEventMulticaster().addApplicationListener(listener);
      }
      // 2.通过配置文件或注解注入BeanFactory的监听器处理
      String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
      for (String listenerBeanName : listenerBeanNames) {
          getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
      }
      // 3.使用事件广播器，发布早期应用程序事件到相应的监听器
      Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
      this.earlyApplicationEvents = null;
      if (earlyEventsToProcess != null) {
          for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
              getApplicationEventMulticaster().multicastEvent(earlyEvent);
          }
      }
  }
  ```

##### 03：finishRefresh

- ```java
  protected void finishRefresh() {
      // Clear context-level resource caches 
   	clearResourceCaches();
      // 1.为此上下文初始化生命周期处理器
      initLifecycleProcessor();
      // 2.首先将刷新完毕事件传播到生命周期处理器（触发isAutoStartup方法返回true的SmartLifecycle的start方法）
      getLifecycleProcessor().onRefresh();
      // 3.推送上下文刷新完毕事件到相应的监听器
      publishEvent(new ContextRefreshedEvent(this));
      LiveBeansView.registerApplicationContext(this);
  }
  ```

##### 04：initLifecycleProcessor

- ```java
  protected void initLifecycleProcessor() {
      ConfigurableListableBeanFactory beanFactory = getBeanFactory();
      // 1.判断BeanFactory是否已经存在生命周期处理器（固定使用beanName=lifecycleProcessor）
      if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
          // 1.1 如果已经存在，则将该bean赋值给lifecycleProcessor
          this.lifecycleProcessor =
              beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
          if (logger.isTraceEnabled()) {
              logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
          }
      } else {
          // 1.2 如果不存在，则使用DefaultLifecycleProcessor
          DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
          defaultProcessor.setBeanFactory(beanFactory);
          this.lifecycleProcessor = defaultProcessor;
          // 并将DefaultLifecycleProcessor作为默认的生命周期处理器，注册到BeanFactory中
          beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
          if (logger.isTraceEnabled()) {
              logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using " +
                           "[" + this.lifecycleProcessor.getClass().getSimpleName() + "]");
          }
      }
  }
  ```

##### 05：onRefresh#startBeans

- ```java
  @Override
  public void onRefresh() {
      startBeans(true);
      this.running = true;
  }
  // autoStartupOnly=true代表是ApplicationContext刷新时容器自动启动；autoStartupOnly=false代表是通过显示的调用启动,当autoStartupOnly=false，也就是通过显示的调用启动，会触发全部的Lifecycle；
  private void startBeans(boolean autoStartupOnly) {
      // 1.获取所有的Lifecycle bean
      Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
      // 将Lifecycle bean 按阶段分组，阶段通过实现Phased接口得到
      Map<Integer, LifecycleGroup> phases = new HashMap<Integer, LifecycleGroup>();
      // 2.遍历所有Lifecycle bean，按阶段值分组
      lifecycleBeans.forEach((beanName, bean) -> {
          // 3.当autoStartupOnly=true，也就是ApplicationContext刷新时容器自动启动，
          // 只会触发isAutoStartup方法返回true的SmartLifecycle
          if (!autoStartupOnly || (bean instanceof SmartLifecycle
                                   && ((SmartLifecycle) bean).isAutoStartup())) {
              // 3.1 获取bean的阶段值（如果没有实现Phased接口，则值为0）
              int phase = getPhase(bean);
              // 3.2 拿到存放该阶段值的LifecycleGroup
              LifecycleGroup group = phases.get(phase);
              if (group == null) {
                  // 3.3 如果该阶段值的LifecycleGroup为null，则新建一个
                  group = new LifecycleGroup(phase, this.timeoutPerShutdownPhase,
                                             lifecycleBeans, autoStartupOnly);
                  phases.put(phase, group);
              }
              // 3.4 将bean添加到该LifecycleGroup
              group.add(beanName, bean);
          }
      });
      // 4.如果phases不为空
      if (!phases.isEmpty()) {
          List<Integer> keys = new ArrayList<Integer>(phases.keySet());
          // 4.1 按阶段值进行排序(升序)
          Collections.sort(keys);
          // 4.2 按阶段值顺序，调用LifecycleGroup中的所有Lifecycle的start方法
          for (Integer key : keys) {
              phases.get(key).start();
          }
      }
  }
  ```

##### 06：publishEvent：广播上下文刷新事件

- ```java
  @Override
  public void publishEvent(ApplicationEvent event) {
      publishEvent(event, null);
  }
  
  protected void publishEvent(Object event, ResolvableType eventType) {
      Assert.notNull(event, "Event must not be null");
      // 1.如有必要，将事件装饰为ApplicationEvent
      ApplicationEvent applicationEvent;
      if (event instanceof ApplicationEvent) {
          applicationEvent = (ApplicationEvent) event;
      } else {
          applicationEvent = new PayloadApplicationEvent<Object>(this, event);
          if (eventType == null) {
              eventType = ((PayloadApplicationEvent) applicationEvent).getResolvableType();
          }
      }
      if (this.earlyApplicationEvents != null) {
          this.earlyApplicationEvents.add(applicationEvent);
      } else {
          // 2.使用事件广播器广播事件到相应的监听器
          getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
      }
      // 3.同样的，通过parent发布事件......
      if (this.parent != null) {
          if (this.parent instanceof AbstractApplicationContext) {
              ((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
          } else {
              this.parent.publishEvent(event);
          }
      }
  }
  ```

##### 07：multicastEvent：使用应用上下文广播器广播事件

- ```java
  @Override
  public void multicastEvent(final ApplicationEvent event, ResolvableType eventType) {
      ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
      // 1.返回此广播器的当前任务执行程序
      Executor executor = getTaskExecutor();
      // 2.getApplicationListeners：返回与给定事件类型匹配的应用监听器集合
      for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
          if (executor != null) {
              executor.execute(new Runnable() {
                  @Override
                  public void run() {
                      // 3.1 executor不为null，则使用executor调用监听器
                      invokeListener(listener, event);
                  }
              });
          } else {
              // 3.2 否则，直接调用监听器
              invokeListener(listener, event);
          }
      }
  }
  ```

##### 08：doInvokeListener

- ```java
  private void doInvokeListener(ApplicationListener listener, ApplicationEvent event) {
      try {
          // 触发监听器的onApplicationEvent方法，参数为给定的事件
          listener.onApplicationEvent(event);
      } catch (ClassCastException ex) {
          String msg = ex.getMessage();
          if (msg == null || msg.startsWith(event.getClass().getName())) {
              Log logger = LogFactory.getLog(getClass());
              if (logger.isDebugEnabled()) {
                  logger.debug("Non-matching event type for listener: " + listener, ex);
              }
          } else {
              throw ex;
          }
      }
  }
  ```

##### 09：自定义监听器示例

- 如果想在 **Spring IoC 容器构建完毕之后进行一些逻辑**，就可以通过监听器来实现。

- 创建一个自定义监听器，实现 ApplicationListener 接口，监听 ContextRefreshedEvent（上下文刷新完毕事件），并且将该监听器注册到 Spring IoC 容器即可。

  ```java
  /**
  * 当上下文刷新完成时，通过上下文应用广播器广播该时间到监听器
  */
  @Component
  public class MyRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {
      @Override
      public void onApplicationEvent(ContextRefreshedEvent event) {
          // 自己的逻辑处理
      }
  }
  ```