### Spring-创建单例Bean

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

##### 03：实例化前处理，返回代理对象，达到短路效果

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

##### 04：实例化后处理

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

##### 05：真正的创建对象

- 















