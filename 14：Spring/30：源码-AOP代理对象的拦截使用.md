### 源码-AOP 代理对象的拦截使用

------

[TOC]

##### 01：org.springframework.aop.framework.CglibAopProxy.DynamicAdvisedInterceptor#intercept

- DynamicAdvisedInterceptor 类的 advised 属性，包含了所有增强器；

  - private final AdvisedSupport advised;

- 同一个 AspectJ 中的 Advice 排序：AspectJAfterThrowingAdvice > AfterReturningAdviceInterceptor > AspectJAfterAdvice > AspectJAroundAdvice > MethodBeforeAdviceInterceptor

  ```java
  public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      Object oldProxy = null;
      boolean setProxyContext = false;
      Object target = null;
      // 获取目标类的 TargetSource 对象，用于获取目标类
      TargetSource targetSource = this.advised.getTargetSource();
      try {
          // 如果 `expose-proxy` 属性为 `true`，则需要暴露当前代理对象
          if (this.advised.exposeProxy) {
              // 向 AopContext 中设置代理对象，并记录 ThreadLocal 之前存放的代理对象
              oldProxy = AopContext.setCurrentProxy(proxy);
              setProxyContext = true;
          }
          // 获取目标对象，以及它的 Class 对象
          target = targetSource.getTarget();
          Class<?> targetClass = (target != null ? target.getClass() : null);
          // 1.获取能够应用于该方法的所有拦截器（有序）,不同的 AspectJ 根据 @Order 排序
          List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(
              method, targetClass);
          Object retVal;
          // 如果拦截器链为空，则直接执行目标方法
          if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
              Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
              retVal = methodProxy.invoke(target, argsToUse);
          } else {
              // 2. 创建一个方法调用器，并将前面获取到的拦截器链传入其中，该对象就是 Joinpoint 对象
              // 执行目标方法，以及所有的 MethodInterceptor 方法拦截器（Advice 通知器），并获取返回结果
              retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass,
                                                 chain, methodProxy).proceed();
          }
          // 对最终的返回结果进一步处理（返回结果是否需要为代理对象，返回结果是否不能为空）
          retVal = processReturnType(proxy, target, method, retVal);
          return retVal;
      } finally {
          if (target != null && !targetSource.isStatic()) {
              targetSource.releaseTarget(target);
          }
          if (setProxyContext) {
              // 如果暴露了当前代理对象，则需要将之前的代理对象重新设置到 ThreadLocal 中
              AopContext.setCurrentProxy(oldProxy);
          }
      }
  }
  ```

##### 02：getInterceptorsAndDynamicInterceptionAdvice

- DefaultAdvisorChainFactory

  - 获取拦截器链的默认实现类；

  ```java
  public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method,
                                                                  @Nullable Class targetClass) {
      // 根绝目标方法创建一个方法缓存 Key
      MethodCacheKey cacheKey = new MethodCacheKey(method);
      //尝试从缓存中获取
      List<Object> cached = this.methodCache.get(cacheKey);
      // 缓存未命中，则进行下一步处理
      if (cached == null) {
          /*
           * 获取能够应用于该方法的所有拦截器（有序）
           * 筛选出能够应用于该方法的所有 Advisor，并获取对应的 MethodInterceptor，也就是 Advice
           * 因为 Advisor 是排好序的，所以返回的 MethodInterceptor 也是有序的
           *
           * 为什么 `cached` 使用 `List` 存储？
           * 因为有些元素是 MethodInterceptor 和 MethodMatcher 的包装对象，并不是 MethodInterceptor
           */
          cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
              this, method, targetClass);
          //将该方法对应的拦截器链路放入缓存
          this.methodCache.put(cacheKey, cached);
      }
      //返回能够应用于该方法的所有拦截器（有序）
      return cached;
  }
  ```

##### 03：getInterceptorsAndDynamicInterceptionAdvice

- ```java
  @Override
  public ListgetInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method,
                                                         @Nullable Class targetClass) {
      // 获取 DefaultAdvisorAdapterRegistry 实例对象
      AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();
      // 获取能够应用到 `targetClass` 的 Advisor 们
      Advisor[] advisors = config.getAdvisors();
      List<Object> interceptorList = new ArrayList<>(advisors.length);
      Class actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());
      Boolean hasIntroductions = null;
      // 遍历 Advisor ,筛选出哪些 Advisor 需要处理当前被拦截的 `method`，并获取对应的
      // MethodInterceptor(Advice)
      for (Advisor advisor : advisors) {
          // 如果是 PointcutAdvisor 类型，则需要对目标对象的类型和被拦截的方法进行匹配
          if (advisor instanceof PointcutAdvisor) {
              PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
              // 判断这个 PointcutAdvisor 是否匹配目标对象的类型，无法匹配则跳过
              if (config.isPreFiltered() // AdvisedSupport 是否已经过滤过目标对象的类型
                  || pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
                  // 获取 Pointcut 的 MethodMatcher 方法匹配器对该方法进行匹配
                  // 参考 AspectJExpressionPointcut，底层借助于 AspectJ 的处理
                  MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
                  boolean match;
                  if (mm instanceof IntroductionAwareMethodMatcher) {
                      if (hasIntroductions == null) {
                          hasIntroductions = hasMatchingIntroductions(advisors, actualClass);
                      }
                      match = ((IntroductionAwareMethodMatcher) mm).matches(
                          method, actualClass, hasIntroductions);
                  } else {
                      match = mm.matches(method, actualClass);
                  }
                  if (match) {
                      //从 Advisor 中获取 Advice，并包装成 MethodInterceptor 拦截器对象（如果不是的话）
                      MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
                      // 若 `isRuntime()` 返回 `true`，表明 MethodMatcher 在运行时做一些检测
                      if (mm.isRuntime()) {
                          for (MethodInterceptor interceptor : interceptors) {
                              // 将上面获取到的 MethodInterceptor 和 MethodMatcher 包装成一个对象，
                              // 并添加至 `interceptorList`
                              interceptorList.add(
                                  new InterceptorAndDynamicMethodMatcher(interceptor, mm));
                          }
                      } else {
                          interceptorList.addAll(Arrays.asList(interceptors));
                      }
                  }
              }
          } else if (advisor instanceof IntroductionAdvisor) {
              // 如果是 IntroductionAdvisor 类型，则需要对目标对象的类型进行匹配
              IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
              if (config.isPreFiltered() 
                  || ia.getClassFilter().matches(actualClass)) {
                  Interceptor[] interceptors = registry.getInterceptors(advisor);
                  interceptorList.addAll(Arrays.asList(interceptors));
              }
          } else {
              // 不需要对目标对象的类型和被拦截的方法进行匹配
              Interceptor[] interceptors = registry.getInterceptors(advisor);
              interceptorList.addAll(Arrays.asList(interceptors));
          }
      }
      // 返回 `interceptorList` 所有的 MethodInterceptor 拦截器
      return interceptorList;
  }
  ```

##### 04：DefaultAdvisorAdapterRegistry

- 默认的 Advisor 适配器注册中心，主要是对 Advisor 中的 Advice 进行匹配处理；

- 适配器模式；

- ```java
  @Override
  public MethodInterceptor[] getInterceptors(Advisor advisor) throws UnknownAdviceTypeException {
      List<MethodInterceptor> interceptors = new ArrayList<>(3);
      // 获取 Advice 通知器
      Advice advice = advisor.getAdvice();
      // 若 Advice 是 MethodInterceptor 类型的，直接添加到 `interceptors`即可
      if (advice instanceof MethodInterceptor) {
          interceptors.add((MethodInterceptor) advice);
      }
      // 通过 Advisor 适配器将 Advice 封装成对应的 MethodInterceptor 对象，并添加至 `interceptors`
      for (AdvisorAdapter adapter : this.adapters) {
          if (adapter.supportsAdvice(advice)) {
              interceptors.add(adapter.getInterceptor(advisor));
          }
      }
      // 没有对应的 MethodInterceptor 则抛出异常
      if (interceptors.isEmpty()) {
          throw new UnknownAdviceTypeException(advisor.getAdvice());
      }
      return interceptors.toArray(new MethodInterceptor[0])
  }
  ```

##### 05：org.springframework.aop.framework.CglibAopProxy.CglibMethodInvocation：方法调用器

- 执行所有拦截器的 invoke 方法；

  ```java
  private static class CglibMethodInvocation extends ReflectiveMethodInvocation {
      @Nullable
      private final MethodProxy methodProxy;
      // 构造方法
      public CglibMethodInvocation(Object proxy, @Nullable Object target, Method method,
                                   Object[] arguments, @Nullable Class<?> targetClass,
                                   List<Object> interceptorsAndDynamicMethodMatchers,
                                   MethodProxy methodProxy) {
          super(proxy, target, method, arguments, targetClass, 
                interceptorsAndDynamicMethodMatchers);
          // 设置代理方法
          this.methodProxy = (Modifier.isPublic(method.getModifiers())
                              && method.getDeclaringClass() != Object.class
                              && !AopUtils.isEqualsMethod(method)
                              && !AopUtils.isHashCodeMethod(method)
                              && !AopUtils.isToStringMethod(method) ? methodProxy : null);
      }
  
      @Override
      @Nullable
      public Object proceed() throws Throwable {
          try {
              return super.proceed();
          } catch (RuntimeException ex) {
              throw ex;
          } catch (Exception ex) {
              if (ReflectionUtils.declaresException(getMethod(), ex.getClass())) {
                  throw ex;
              } else {
                  throw new UndeclaredThrowableException(ex);
              }
          }
      }
      @Override
      protected Object invokeJoinpoint() throws Throwable {
          if (this.methodProxy != null) {
              // 执行代理方法对象（反射）
              return this.methodProxy.invoke(this.target, this.arguments);
          } else {
              // 执行目标方法对象（反射）
              return super.invokeJoinpoint();
          }
      }
  }
  ```

