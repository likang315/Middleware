### Spring 的启动流程 （ApplicationContext）

------

[TOC]

##### 00：Spring 版本源码

- Srpingboot-2.3.3.RELEASE
  - spring-bean-5.2.8.RELEASE.jar

##### 01：Spring 启动入口

- **ContextLoaderListener** 是 Spring 的入口，而 **contextConfigLocation** 是 Spring 配置文件的路径。

- 由于 ContextLoaderListener 继承了ContextLoader；

  - ```java
    static {
        try {
            // 1.根据 ContextLoader.class 构建 ClassPathResource，
            // path在这边为相对路径，全路径为：org.springframework.web.context.ContextLoader.properties
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, 
                                                               ContextLoader.class);
            // 2. 加载resource的属性，spring 拿到了默认的WebApplicationContext，
            // 即：XmlWebApplicationContext
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException var1) {
            throw new IllegalStateException("Could not load 'ContextLoader.properties': "
                                            + var1.getMessage());
        }
    
        currentContextPerThread = new ConcurrentHashMap(1);
    }
    ```

###### ContextLoaderListener

```java
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {
    public ContextLoaderListener(WebApplicationContext context) {
        super(context);
    }
    @Override
    public void contextInitialized(ServletContextEvent event) {
        initWebApplicationContext(event.getServletContext());
    }
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        closeWebApplicationContext(event.getServletContext());
        ContextCleanupListener.cleanupAttributes(event.getServletContext());
    }
}
```

##### 02：contextInitialized

- ```java
  public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
      // 1.校验 WebApplicationContext 是否已经初始化过，如果已经初始化，则抛出异常
      if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) 
          != null) {
          throw new IllegalStateException(
             "Cannot initialize context because there is already a root application context present" 
              + "check whether you have multiple ContextLoader* definitions in your web.xml!");
      }
      Log logger = LogFactory.getLog(ContextLoader.class);
      servletContext.log("Initializing Spring root WebApplicationContext");
      if (logger.isInfoEnabled()) {
          logger.info("Root WebApplicationContext: initialization started");
      }
      long startTime = System.currentTimeMillis();
      try {
          if (this.context == null) {
              // 2.创建一个 WebApplicationContext 并保存到context属性
              this.context = createWebApplicationContext(servletContext);
          }
          if (this.context instanceof ConfigurableWebApplicationContext) {
              ConfigurableWebApplicationContext cwac =
                  (ConfigurableWebApplicationContext) this.context;
              if (!cwac.isActive()) {
                  if (cwac.getParent() == null) {
                      ApplicationContext parent = loadParentContext(servletContext);
                      cwac.setParent(parent);
                  }
                  // 3.配置和刷新web应用上下文
                  configureAndRefreshWebApplicationContext(cwac, servletContext);
              }
          }
          // 4.设置WebApplicationContext属性
          servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                                      this.context);
          ClassLoader ccl = Thread.currentThread().getContextClassLoader();
          if (ccl == ContextLoader.class.getClassLoader()) {
              currentContext = this.context;
          } else if (ccl != null) {
              currentContextPerThread.put(ccl, this.context);
          }
          if (logger.isDebugEnabled()) {
              logger.debug("Published root WebApplicationContext as ServletContext attribute with 					name [" + WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
          }
          if (logger.isInfoEnabled()) {
              long elapsedTime = System.currentTimeMillis() - startTime;
              logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime
                          + " ms");
          }
          return this.context;
      } catch (RuntimeException ex) {
          logger.error("Context initialization failed", ex);
          servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, 
                                      ex);
          throw ex;
      } catch (Error err) {
          logger.error("Context initialization failed", err);
          servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                                      err);
          throw err;
      }
  }
  ```

######  2 - 1：createWebApplicationContext

- ```java
  protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
      // 1.确定要创建的应用上下文的Class
      Class<?> contextClass = determineContextClass(sc);
      // 2.校验contextClass是否为ConfigurableWebApplicationContext或其子类、子接口
      if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
          throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
                                                "] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
      }
      // 3.实例化contextClass，并强转成ConfigurableWebApplicationContext返回
      return (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);}
  ```

##### 2 - 1 - 1：determineContextClass

- ```java
  protected Class<?> determineContextClass(ServletContext servletContext) {
      // 1.从servletContext中解析初始化参数contextClass(web.xml可以配置该参数)
      String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
      if (contextClassName != null) {
          try {
              // 2.contextClassName不为空，则使用工具类构建出contextClassName的实例
              return ClassUtils.forName(contextClassName, ClassUtils.getDefaultClassLoader());
          } catch (ClassNotFoundException ex) {
              throw new ApplicationContextException(
                  "Failed to load custom context class [" + contextClassName + "]", ex);
          }
      } else {
          // 3.如果没有配置contextClass参数，则从defaultStrategies缓存中拿到默认的WerApplicationContext
          // 对应的ClassName，
          // 即：org.springframework.web.context.support.XmlWebApplicationContext
          contextClassName = defaultStrategies.getProperty(WebApplicationContext.class.getName());
          try {
              // 4.使用工具类构建出contextClassName的实例
              return ClassUtils.forName(contextClassName, ContextLoader.class.getClassLoader());
          } catch (ClassNotFoundException ex) {
              throw new ApplicationContextException(
                  "Failed to load default context class [" + contextClassName + "]", ex);
          }
      }
  }
  ```

###### 2 - 2：configureAndRefreshWebApplicationContext

- 是 Spring 提供给开发者的一个扩展点，我们可以通过此方法对 ConfigurableApplicationContext 进行一些自定义操作；
  
- 具体做法：实现 ApplicationContextInitializer 是Spring框架中的一个接口，它定义了一个initialize方法，该方法接收一个ConfigurableApplicationContext对象作为参数，用于在Spring应用程序上下文启动时执行任何必要的初始化逻辑。
  
- 通过在Spring Boot中使用 SpringApplication.addInitializers 方法来添加ApplicationContextInitializer的实现类。
  
  ```java
  protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac, ServletContext sc) {
      // 1.如果应用上下文id是原始默认值，则根据相关信息生成一个更有用的
      if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
          // 1.1 从servletContext中解析初始化参数contextId(可以在web.xml中配置)
          String idParam = sc.getInitParameter(CONTEXT_ID_PARAM);
          if (idParam != null) {
              // 1.1.1 如果idParam不为空, 则设置为wac的Id属性
              wac.setId(idParam);
          } else {
              // 1.1.2 如果idParam为空, 则生成默认的id, 例如: 
              // org.springframework.web.context.WebApplicationContext:
              wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
                        ObjectUtils.getDisplayString(sc.getContextPath()));
          }
      }
  
      // 2.为应用上下文设置servletContext
      wac.setServletContext(sc);
      // 3.从servletContext中解析初始化参数contextConfigLocation
      String configLocationParam = sc.getInitParameter(CONFIG_LOCATION_PARAM);
      if (configLocationParam != null) {
          // 4.设置wac的configLocations属性值为configLocationParam
          wac.setConfigLocation(configLocationParam);
      }
      ConfigurableEnvironment env = wac.getEnvironment();
      if (env instanceof ConfigurableWebEnvironment) {
          // 5.初始化属性源(主要是将servletContextInitParams的占位类替换成sc)
          ((ConfigurableWebEnvironment) env).initPropertySources(sc, null);
      }
      // 6.自定义上下文
      customizeContext(sc, wac);
      // 7.应用上下文的刷新
      wac.refresh();
  }
  ```

###### 2 - 2 - 1： wac.setConfigLocation

- ```java
  public void setConfigLocation(String location) {
      // tokenizeToStringArray: 将location通过分割符（,;\t\n）分割成String数组
      // setConfigLocations: 将分割后的路径赋值给configLocations
      setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
  }
  public void setConfigLocations(String... locations) {
      if (locations != null) {
          Assert.noNullElements(locations, "Config locations must not be null");
          this.configLocations = new String[locations.length];
          // 1.遍历解析locations
          for (int i = 0; i < locations.length; i++) {
              // 2.解析给定路径，必要时用相应的环境属性值替换占位符
              this.configLocations[i] = resolvePath(locations[i]).trim();
          }
      }
      else {
          this.configLocations = null;
      }
  }
  ```

###### 2 - 2 - 1 - 1：resolvePath -> createEnvironment

- public class StandardServletEnvironment extends StandardEnvironment implements ConfigurableWebEnvironment

- public class StandardEnvironment extends AbstractEnvironment

  ```java
  protected String resolvePath(String path) {
      // 1.getEnvironment：获取环境属性
      // 2.resolveRequiredPlaceholders: 解析给定路径，必要时用相应的环境属性值替换占位符，例如${path}
      return getEnvironment().resolveRequiredPlaceholders(path);
  }
  public ConfigurableEnvironment getEnvironment() {
      if (this.environment == null) {
          // 1.创建Environment
          this.environment = createEnvironment();
      }
      return this.environment;
  }
  // 该类的实现AbstractRefreshableWebApplicationContext
  protected ConfigurableEnvironment createEnvironment() {
      // 新建StandardServletEnvironment
      return new StandardServletEnvironment();
  }
  
  // 实例化父类
  public AbstractEnvironment() {
      // 1.自定义属性源
      customizePropertySources(this.propertySources);
  }
  
  
  protected void customizePropertySources(MutablePropertySources propertySources) {
      // 1.添加servletConfigInitParams属性源(作为占位符, 之后会被替换)
      propertySources.addLast(new StubPropertySource(SERVLET_CONFIG_PROPERTY_SOURCE_NAME));
      // 2.添加servletContextInitParams属性源(作为占位符, 之后会被替换)
      propertySources.addLast(new StubPropertySource(SERVLET_CONTEXT_PROPERTY_SOURCE_NAME));
      if (JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable()) {
          // 3.添加jndiProperties属性源
          propertySources.addLast(new JndiPropertySource(JNDI_PROPERTY_SOURCE_NAME));
      }
      // 4.调用父类中的customizePropertySources方法
      super.customizePropertySources(propertySources);
  }
  protected void customizePropertySources(MutablePropertySources propertySources) {
      // 添加systemProperties属性源
      propertySources.addLast(new MapPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, 
                                                    getSystemProperties()));
      // 添加systemEnvironment属性源
      propertySources.addLast(new SystemEnvironmentPropertySource(
          SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,getSystemEnvironment()));
  }
  ```


###### 2 - 2 - 2：initPropertySources

- ```java
  public static void initServletPropertySources(MutablePropertySources sources,
                                                @Nullable ServletContext servletContext,
                                                @Nullable ServletConfig servletConfig) {
      Assert.notNull(sources, "'propertySources' must not be null");
      String name = StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME;
      // 1.如果servletContext不为null && sources中包含servletContextInitParams数据源
      // && 该数据源的类型为StubPropertySource,
      // 则将servletContextInitParams的数据源替换成servletContext
      if (servletContext != null && sources.get(name) instanceof StubPropertySource) {
          sources.replace(name, new ServletContextPropertySource(name, servletContext));
      }
      name = StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME;
      if (servletConfig != null && sources.get(name) instanceof StubPropertySource) {
          sources.replace(name, new ServletConfigPropertySource(name, servletConfig));
      }
  }
  ```

###### 2 - 2 - 3：customizeContext

- ```java
  protected void customizeContext(ServletContext sc, ConfigurableWebApplicationContext wac) {
      // 1.确定应用上下文的初始化类
      List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>
          initializerClasses = determineContextInitializerClasses(sc);
      // 2.如果initializerClasses不为空, 遍历处理initializerClasses
      for (Class<ApplicationContextInitializer<ConfigurableApplicationContext>>
           initializerClass : initializerClasses) {
          Class<?> initializerContextClass =GenericTypeResolver.resolveTypeArgument(
              initializerClass, ApplicationContextInitializer.class);
          if (initializerContextClass != null && !initializerContextClass.isInstance(wac)) {
              throw new ApplicationContextException(String.format(
                  "Could not apply context initializer [%s] since its generic parameter [%s] " +
                  "is not assignable from the type of application context used by this " +
                  "context loader: [%s]", initializerClass.getName(), 
                  initializerContextClass.getName(),
                  wac.getClass().getName()));
          }
          // 3.实例化initializerClass, 并添加到contextInitializers中
          this.contextInitializers.add(BeanUtils.instantiateClass(initializerClass));
      }
  
      AnnotationAwareOrderComparator.sort(this.contextInitializers);
      // 4.遍历实例化后的contextInitializers
      for (ApplicationContextInitializer<ConfigurableApplicationContext>
           initializer : this.contextInitializers) {
          // 5.调用initializer的initialize方法，进行自定义初始化wac操作
          initializer.initialize(wac);
      }
  }
  ```


######  2 - 2 - 3 - 1：determineContextInitializerClasses：确定自定义初始化类

- contextInitializerClasses是一个可选的servlet上下文初始化参数，它可以指定一个或多个用于初始化servlet上下文的类

- globalInitializerClasses：是可选的全局初始化参数，它可以指定一个或多个用于初始化Servlet容器的类

  ```java
  protected List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>
      determineContextInitializerClasses(ServletContext servletContext) {
      List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> classes =
          new ArrayList<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>();
      // 1.从servletContext中解析初始化参数globalInitializerClasses(可在web.xml中配置)
      String globalClassNames = servletContext.getInitParameter(GLOBAL_INITIALIZER_CLASSES_PARAM);
      if (globalClassNames != null) {
          // 1.1 如果globalClassNames不为空, 则使用分割符分割, 然后进行遍历
          for (String className : StringUtils.tokenizeToStringArray(globalClassNames, 
                                                                    INIT_PARAM_DELIMITERS)) {
              // 1.2 实例化className，获得className对应的类实例
              classes.add(loadInitializerClass(className));
          }
      }
      // 2.解析初始化参数contextInitializerClasses(可在web.xml中配置)
      String localClassNames = servletContext.getInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM);
      if (localClassNames != null) {
          // 2.1 如果localClassNames不为空, 则使用分割符分割, 然后进行遍历
          for (String className : StringUtils.tokenizeToStringArray(localClassNames, 
                                                                    INIT_PARAM_DELIMITERS)) {
              // 2.2 实例化className，获得className对应的类实例
              classes.add(loadInitializerClass(className));
          }
      }
      return classes;
  }
  ```
