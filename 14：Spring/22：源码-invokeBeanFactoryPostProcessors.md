### 源码 - invokeBeanFactoryPostProcessors

------

[TOC]

##### 01：invokeBeanFactoryPostProcessors

- AbstractApplicationContext.refresh()  的重要方法之一；

- 作用是实例化和调用所有已注册的BeanFactoryPostProcessor，以便对BeanFactory进行后置处理。如果有指定的顺序，则按照顺序进行调用

- BeanFactoryPostProcessor：**工厂钩子**，在BeanFactory实例化Bean之前修改或定制BeanFactory的配置。

- ```java
  protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
      // 1.getBeanFactoryPostProcessors(): 拿到当前应用上下文中beanFactoryPostProcessors变量中的值
      // 2.invokeBeanFactoryPostProcessors: 实例化并调用所有已注册的BeanFactoryPostProcessor
      PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(
          beanFactory, getBeanFactoryPostProcessors());
      if (beanFactory.getTempClassLoader() == null
          && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
          beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
          beanFactory.setTempClassLoader(
              new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
      }
  }
  ```
  

##### 02：注册BeanFactory后置处理器示例

- web.xml 中配置自定义Context

- ```xml
  <context-param>
      <param-name>contextInitializerClasses</param-name>
      <param-value>
          com.xupt.spring.SpringApplicationContextInitializer
      </param-value>
  </context-param>
  ```

- ```java
  public class SpringApplicationContextInitializer implements 
      ApplicationContextInitializer<ConfigurableApplicationContext> {
      @Override
      public void initialize(ConfigurableApplicationContext applicationContext) {
          FirstBeanDefinitionRegistryPostProcessor firstBeanDefinitionRegistryPostProcessor = 
              new FirstBeanDefinitionRegistryPostProcessor();
          // 将自定义的firstBeanDefinitionRegistryPostProcessor添加到应用上下文中
          applicationContext.addBeanFactoryPostProcessor(firstBeanDefinitionRegistryPostProcessor);
          
          
          // ...自定义操作
          System.out.println("SpringApplicationContextInitializer#initialize");
      }
  }
  ```

##### 03：invokeBeanFactoryPostProcessors

- 调用所有注册的 beanFactoryPostProcessors

- ```java
  public static void invokeBeanFactoryPostProcessors(
      ConfigurableListableBeanFactory beanFactory,
      List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
      // 存储所有已执行的processor, 用于去重
      Set<String> processedBeans = new HashSet<String>();
      // 1.判断beanFactory是否为BeanDefinitionRegistry，beanFactory为DefaultListableBeanFactory,
      // 而DefaultListableBeanFactory实现了BeanDefinitionRegistry接口，因此这边为true
      if (beanFactory instanceof BeanDefinitionRegistry) {
          BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
          // 用于存放普通的BeanFactoryPostProcessor
          List<BeanFactoryPostProcessor> regularPostProcessors =
              new LinkedList<BeanFactoryPostProcessor>();
          // 用于存放BeanDefinitionRegistryPostProcessor
          List<BeanDefinitionRegistryPostProcessor> registryProcessors =
              new LinkedList<BeanDefinitionRegistryPostProcessor>();
          // 2.首先处理入参中的beanFactoryPostProcessors
          // 遍历所有的beanFactoryPostProcessors, 将BeanDefinitionRegistryPostProcessor和普通
          // BeanFactoryPostProcessor区分开
          for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
              if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
                  // 2.1 如果是BeanDefinitionRegistryPostProcessor
                  BeanDefinitionRegistryPostProcessor registryProcessor =
                      (BeanDefinitionRegistryPostProcessor) postProcessor;
                  // 2.1.1 直接执行BeanDefinitionRegistryPostProcessor接口的
                  // postProcessBeanDefinitionRegistry方法
                  registryProcessor.postProcessBeanDefinitionRegistry(registry);
                  // 2.1.2 添加到registryProcessors(用于最后执行postProcessBeanFactory方法)
                  registryProcessors.add(registryProcessor);
              } else {
                  // 2.2 否则，只是普通的BeanFactoryPostProcessor
                  // 2.2.1 添加到regularPostProcessors(用于最后执行postProcessBeanFactory方法)
                  regularPostProcessors.add(postProcessor);
              }
          }
          // 用于保存本次要执行的BeanDefinitionRegistryPostProcessor
          List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors =
              new ArrayList<BeanDefinitionRegistryPostProcessor>();
          // 3.调用所有实现PriorityOrdered接口的BeanDefinitionRegistryPostProcessor实现类
          // 3.1 找出所有实现BeanDefinitionRegistryPostProcessor接口的Bean的beanName
          String[] postProcessorNames = beanFactory.getBeanNamesForType(
              BeanDefinitionRegistryPostProcessor.class, true, false);
          // 3.2 遍历postProcessorNames
          for (String ppName : postProcessorNames) {
              // 3.3 校验是否实现了PriorityOrdered接口
              if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                  // 3.4 获取ppName对应的bean实例, 添加到currentRegistryProcessors中,
                  // beanFactory.getBean: 这边getBean方法会触发创建ppName对应的bean对象, 目前暂不深入解析
                  currentRegistryProcessors.add(beanFactory.getBean(
                      ppName, BeanDefinitionRegistryPostProcessor.class));
                  // 3.5 将要被执行的加入processedBeans，避免后续重复执行
                  processedBeans.add(ppName);
              }
          }
          // 3.6 进行排序(根据是否实现PriorityOrdered、Ordered接口和order值来排序)
          sortPostProcessors(currentRegistryProcessors, beanFactory);
          // 3.7 添加到registryProcessors(用于最后执行postProcessBeanFactory方法)
          registryProcessors.addAll(currentRegistryProcessors);
          // 3.8 遍历currentRegistryProcessors, 执行postProcessBeanDefinitionRegistry方法
          invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
          // 3.9 执行完毕后, 清空currentRegistryProcessors
          currentRegistryProcessors.clear();
          // 4.调用所有实现了Ordered接口的BeanDefinitionRegistryPostProcessor实现类
          // 4.1 找出所有实现BeanDefinitionRegistryPostProcessor接口的类, 这边重复查找是因为执行完上面的
          // BeanDefinitionRegistryPostProcessor, 可能会新增了其他的
          // BeanDefinitionRegistryPostProcessor, 因此需要重新查找
          postProcessorNames = beanFactory.getBeanNamesForType(
              BeanDefinitionRegistryPostProcessor.class, true, false);
          for (String ppName : postProcessorNames) {
              // 校验是否实现了Ordered接口，并且还未执行过
              if (!processedBeans.contains(ppName)
                  && beanFactory.isTypeMatch(ppName, Ordered.class)) {
                  currentRegistryProcessors.add(
                      beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                  processedBeans.add(ppName);
              }
          }
          sortPostProcessors(currentRegistryProcessors, beanFactory);
          registryProcessors.addAll(currentRegistryProcessors);
          // 4.2 遍历currentRegistryProcessors, 执行postProcessBeanDefinitionRegistry方法
          invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
          currentRegistryProcessors.clear();
          // 5.最后, 调用所有剩下的BeanDefinitionRegistryPostProcessors
          boolean reiterate = true;
          while (reiterate) {
              reiterate = false;
              // 5.1 找出所有实现BeanDefinitionRegistryPostProcessor接口的类
              postProcessorNames = beanFactory.getBeanNamesForType(
                  BeanDefinitionRegistryPostProcessor.class, true, false);
              for (String ppName : postProcessorNames) {
                  // 5.2 跳过已经执行过的
                  if (!processedBeans.contains(ppName)) {
                      currentRegistryProcessors.add(
                          beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                      processedBeans.add(ppName);
                      // 5.3 如果有BeanDefinitionRegistryPostProcessor被执行, 则有可能会产生新的
                      // BeanDefinitionRegistryPostProcessor,将reiterate赋值为true, 需要再循环查找一次
                      reiterate = true;
                  }
              }
              sortPostProcessors(currentRegistryProcessors, beanFactory);
              registryProcessors.addAll(currentRegistryProcessors);
              // 5.4 遍历currentRegistryProcessors, 执行postProcessBeanDefinitionRegistry方法
              invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
              currentRegistryProcessors.clear();
          }
  
          // 6.调用所有BeanDefinitionRegistryPostProcessor的postProcessBeanFactory方法
          invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
          // 7.最后, 调用入参beanFactoryPostProcessors中的普通BeanFactoryPostProcessor的
          // postProcessBeanFactory方法
          invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
      } else {
          invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
      }
  
      // 到这里 , 入参beanFactoryPostProcessors和容器中的所有BeanDefinitionRegistryPostProcessor已经全部处理完毕,下面开始处理容器中的所有BeanFactoryPostProcessor
      // 8.找出所有实现BeanFactoryPostProcessor接口的类
      String[] postProcessorNames =
          beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);
      // 用于存放实现了PriorityOrdered接口的BeanFactoryPostProcessor
      List<BeanFactoryPostProcessor> priorityOrderedPostProcessors =
          new ArrayList<BeanFactoryPostProcessor>();
      // 用于存放实现了Ordered接口的BeanFactoryPostProcessor的beanName
      List<String> orderedPostProcessorNames = new ArrayList<String>();
      // 用于存放普通BeanFactoryPostProcessor的beanName
      List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
      // 8.1 遍历postProcessorNames, 将BeanFactoryPostProcessor按实现PriorityOrdered、Ordered接口、普通
      // 三种区分开
      for (String ppName : postProcessorNames) {
          // 8.2 跳过已经执行过的
          if (processedBeans.contains(ppName)) {
          } else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
              // 8.3 添加实现了PriorityOrdered接口的BeanFactoryPostProcessor
              priorityOrderedPostProcessors.add(
                  beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
          } else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
              // 8.4 添加实现了Ordered接口的BeanFactoryPostProcessor的beanName
              orderedPostProcessorNames.add(ppName);
          } else {
              // 8.5 添加剩下的普通BeanFactoryPostProcessor的beanName
              nonOrderedPostProcessorNames.add(ppName);
          }
      }
      // 9.调用所有实现PriorityOrdered接口的BeanFactoryPostProcessor
      // 9.1 对priorityOrderedPostProcessors排序
      sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
      // 9.2 遍历priorityOrderedPostProcessors, 执行postProcessBeanFactory方法
      invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);
      // 10.调用所有实现Ordered接口的BeanFactoryPostProcessor
      List<BeanFactoryPostProcessor> orderedPostProcessors =
          new ArrayList<BeanFactoryPostProcessor>();
      for (String postProcessorName : orderedPostProcessorNames) {
          // 10.1 获取postProcessorName对应的bean实例, 添加到orderedPostProcessors, 准备执行
          orderedPostProcessors.add(
              beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
      }
      // 10.2 对orderedPostProcessors排序
      sortPostProcessors(orderedPostProcessors, beanFactory);
      // 10.3 遍历orderedPostProcessors, 执行postProcessBeanFactory方法
      invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);
      // 11.调用所有剩下的BeanFactoryPostProcessor
      List<BeanFactoryPostProcessor> nonOrderedPostProcessors =
          new ArrayList<BeanFactoryPostProcessor>();
      for (String postProcessorName : nonOrderedPostProcessorNames) {
          // 11.1 获取postProcessorName对应的bean实例, 添加到nonOrderedPostProcessors, 准备执行
          nonOrderedPostProcessors.add(
              beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
      }
      // 11.2 遍历nonOrderedPostProcessors, 执行postProcessBeanFactory方法
      invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);
      // 12.清除元数据缓存（mergedBeanDefinitions、allBeanNamesByType、singletonBeanNamesByType），
      // 因为后处理器可能已经修改了原始元数据，例如， 替换值中的占位符...
      beanFactory.clearMetadataCache();
  }
  ```

##### 04：sortPostProcessors

- 实现 PriorityOrdered 接口的优先级最高，如果两个对象都实现（都没实现）PriorityOrdered 接口，则根据 order 值（实现 Ordered 接口时，需要实现 getOrder() 方法，返回 order 值）来进行比较，order 值越小，优先级越高。

- ```java
  private static void sortPostProcessors(List<?> postProcessors, 
                                         ConfigurableListableBeanFactory beanFactory) {
      if (postProcessors.size() <= 1) {
          return;
      }
      Comparator<Object> comparatorToUse = null;
      if (beanFactory instanceof DefaultListableBeanFactory) {
          // 1.获取设置的比较器
          comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
      }
      if (comparatorToUse == null) {
          // 2.如果没有设置比较器, 则使用默认的OrderComparator
          comparatorToUse = OrderComparator.INSTANCE;
      }
     	// 3.使用比较器对postProcessors进行排序
      // 根据order 值进行排序，如果没有实现Order接口，则优先级最低
      postProcessors.sort(comparatorToUse);
  }
  ```

  