### 源码-registerBeanPostProcessors

------

[TOC]

##### 01：概述

- invokeBeanFactoryPostProcessors 方法主要用于处理 **BeanFactoryPostProcessor** 接口，而 registerBeanPostProcessors 方法主要用于处理 **BeanPostProcessor** 接口。
- BeanFactoryPostProcessor 主要作用于 bean 定义的修改，而 BeanPostProcessor 主要作用于 bean 实例的修改或扩展。
- 该方法会注册所有的 BeanPostProcessor，**将所有实现了 BeanPostProcessor 接口的类加载到 BeanFactory 中**。具体的调用是在 bean 初始化的时候。

##### 02：refresh#registerBeanPostProcessors

- ```java
  protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
      // 1.注册BeanPostProcessor
      PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
  }
  ```

##### 03：org.springframework.context.support.PostProcessorRegistrationDelegate#registerBeanPostProcessors

- ```java
  public static void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory,
      AbstractApplicationContext applicationContext) {
      // 1.找出所有实现BeanPostProcessor接口的类
      String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class,
                                                                    true, false);
      // BeanPostProcessor的目标计数
      int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + 
          postProcessorNames.length;
      // 2.添加BeanPostProcessorChecker(主要用于记录信息)到beanFactory中
      beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, 
                                                                    beanProcessorTargetCount));
      // 3.定义不同的变量用于区分: 实现PriorityOrdered接口的BeanPostProcessor、实现Ordered接口的
      // BeanPostProcessor、普通BeanPostProcessor
      // 3.1 priorityOrderedPostProcessors: 用于存放实现PriorityOrdered接口的BeanPostProcessor
      List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
      // 3.2 internalPostProcessors: 用于存放Spring内部的BeanPostProcessor
      List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
      // 3.3 orderedPostProcessorNames: 用于存放实现Ordered接口的BeanPostProcessor的beanName
      List<String> orderedPostProcessorNames = new ArrayList<>();
      // 3.4 nonOrderedPostProcessorNames: 用于存放普通BeanPostProcessor的beanName
      List<String> nonOrderedPostProcessorNames = new ArrayList<>();
      // 4.遍历postProcessorNames, 将BeanPostProcessors按3.1 - 3.4定义的变量区分开
      for (String ppName : postProcessorNames) {
          if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
              // 4.1 如果ppName对应的Bean实例实现了PriorityOrdered接口, 
              // 则拿到ppName对应的Bean实例并添加到priorityOrderedPostProcessors
              BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
              priorityOrderedPostProcessors.add(pp);
              if (pp instanceof MergedBeanDefinitionPostProcessor) {
                  // 4.2 如果ppName对应的Bean实例也实现了MergedBeanDefinitionPostProcessor接口,
                  // 则将ppName对应的Bean实例添加到internalPostProcessors
                  internalPostProcessors.add(pp);
              }
          } else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
              // 4.3 如果ppName对应的Bean实例没有实现PriorityOrdered接口, 但是实现了Ordered接口,
              // 则将ppName添加到orderedPostProcessorNames
              orderedPostProcessorNames.add(ppName);
          } else {
              // 4.4 否则, 将ppName添加到nonOrderedPostProcessorNames
              nonOrderedPostProcessorNames.add(ppName);
          }
      }
      // 5.首先, 注册实现PriorityOrdered接口的BeanPostProcessors
      // 5.1 对priorityOrderedPostProcessors进行排序
      sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
      // 5.2 注册priorityOrderedPostProcessors
      registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);
      // 6.接下来, 注册实现Ordered接口的BeanPostProcessors
      List<BeanPostProcessor> orderedPostProcessors = new ArrayList<BeanPostProcessor>(
          orderedPostProcessorNames.size());
      for (String ppName : orderedPostProcessorNames) {
          // 6.1 拿到ppName对应的BeanPostProcessor实例对象
          BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
          // 6.2 将ppName对应的BeanPostProcessor实例对象添加到orderedPostProcessors, 准备执行注册
          orderedPostProcessors.add(pp);
          if (pp instanceof MergedBeanDefinitionPostProcessor) {
              // 6.3 如果ppName对应的Bean实例也实现了MergedBeanDefinitionPostProcessor接口,
              // 则将ppName对应的Bean实例添加到internalPostProcessors
              internalPostProcessors.add(pp);
          }
      }
      // 6.4 对orderedPostProcessors进行排序
      sortPostProcessors(orderedPostProcessors, beanFactory);
      // 6.5 注册orderedPostProcessors
      registerBeanPostProcessors(beanFactory, orderedPostProcessors);
      // Now, register all regular BeanPostProcessors.
      // 7.注册所有常规的BeanPostProcessors（过程与6类似）
      List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanPostProcessor>(
          orderedPostProcessorNames.size());
      for (String ppName : nonOrderedPostProcessorNames) {
          BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
          nonOrderedPostProcessors.add(pp);
          if (pp instanceof MergedBeanDefinitionPostProcessor) {
              internalPostProcessors.add(pp);
          }
      }
      registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);
      // 8.最后, 重新注册所有内部BeanPostProcessors（相当于内部的BeanPostProcessor会被移到处理器链的末尾）
      // 8.1 对internalPostProcessors进行排序
      sortPostProcessors(internalPostProcessors, beanFactory);
      // 8.2注册internalPostProcessors
      registerBeanPostProcessors(beanFactory, internalPostProcessors);
      // 9.重新注册ApplicationListenerDetector（跟8类似，主要是为了移动到处理器链的末尾）
      beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
  }
  ```

##### 04：registerBeanPostProcessors

- ```java
  private static void registerBeanPostProcessors(
      ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {
      // 1.遍历postProcessors
      for (BeanPostProcessor postProcessor : postProcessors) {
          // 2.将PostProcessor添加到BeanFactory中的beanPostProcessors缓存
          beanFactory.addBeanPostProcessor(postProcessor);
      }
  }
  ```

##### 05：beanFactory.addBeanPostProcessor

- ```java
  public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
      Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
      // 1.如果beanPostProcessor已经存在则移除，和internalPostProcessors 相呼应
      // （起到排序的效果，beanPostProcessor可能本来在前面，移除再添加，则变到最后面）
      this.beanPostProcessors.remove(beanPostProcessor);
      if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
          // 2.该变量用于指示beanFactory是否已注册过InstantiationAwareBeanPostProcessors
          this.hasInstantiationAwareBeanPostProcessors = true;
      }
      if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
          // 3.该变量用于指示beanFactory是否已注册过DestructionAwareBeanPostProcessor
          this.hasDestructionAwareBeanPostProcessors = true;
      }
      // 4.将beanPostProcessor添加到beanPostProcessors缓存
      this.beanPostProcessors.add(beanPostProcessor);
  }
  ```

##### 06：示例

- ```java
  @Component
  public class MyBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName)
          throws BeansException {
          System.out.println("MyBeanPostProcessor#postProcessBeforeInitialization");
          
          // 自己的逻辑
          return bean;
      }
  
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) 
          throws BeansException {
          System.out.println("MyBeanPostProcessor#postProcessAfterInitialization");
  
          // 自己的逻辑
          return bean;
      }
  
      @Override
      public int getOrder() {
          return 0;
      }
  }
  ```

  