### ContextLoader#refresh

------

[TOC]

##### 01：configureAndRefreshWebApplicationContext#refresh

- org.springframework.context.support.AbstractApplicationContext#refresh

  ```java
  @Override
  public void refresh() throws BeansException, IllegalStateException {
      // 用于“刷新”和“销毁”的锁
      synchronized (this.startupShutdownMonitor) {
         	// 为刷新准备新的上下文环境，设置其启动日期和活动标志以及执行一些属性的初始化
          prepareRefresh();
          // 解析所有 Spring 配置文件，将所有 Spring 配置文件中的 bean 定义封装成 BeanDefinition，加载到 	
          // BeanFactory 中
          ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
          // 配置 beanFactory 的标准上下文特征，例如上下文的 ClassLoader、后置处理器等
          prepareBeanFactory(beanFactory);
          try {
              // 允许子类对 BeanFactory 进行后续处理，默认实现为空，留给子类实现
              postProcessBeanFactory(beanFactory);
              // 实例化和调用所有 BeanFactoryPostProcessor，包括其子类 
              // BeanDefinitionRegistryPostProcessor
              invokeBeanFactoryPostProcessors(beanFactory);
              // 注册所有的 BeanPostProcessor，将所有实现了 BeanPostProcessor 接口的类加载到 BeanFactory
              registerBeanPostProcessors(beanFactory);
              // 初始化消息资源 MessageSource
              initMessageSource();
              // 初始化应用的事件广播器 ApplicationEventMulticaster
              initApplicationEventMulticaster();
              // 为模板方法，提供给子类扩展实现，可以重写以添加特定于上下文的刷新工作，默认实现为空
              onRefresh();
              // 注册监听器
              registerListeners();
              // 实例化所有剩余的非懒加载单例 bean
              finishBeanFactoryInitialization(beanFactory);
              // 完成此上下文的刷新，主要是推送上下文刷新完毕事件（ContextRefreshedEvent ）到监听器
              finishRefresh();
          } catch (BeansException ex) {
              if (logger.isWarnEnabled()) {
                  logger.warn("Exception encountered during context initialization - " +
                              "cancelling refresh attempt: " + ex);
              }
              // Destroy already created singletons to avoid dangling resources.
              destroyBeans();
              // Reset 'active' flag.
              cancelRefresh(ex);
              throw ex;
          } finally {
              resetCommonCaches();
          }
      }
  }
  ```

##### 02：obtainFreshBeanFactory

- 解析所有 Spring 配置文件（通常我们会放在 resources 目录下），将所有 Spring 配置文件中的 bean 定义封装成 BeanDefinition，加载到 BeanFactory 中。
  - beanDefinitionNames缓存：所有被加载到 BeanFactory 中的 bean 的 beanName 集合。
  - beanDefinitionMap缓存：所有被加载到 BeanFactory 中的 bean 的 beanName 和 BeanDefinition 映射。
  - aliasMap缓存：所有被加载到 BeanFactory 中的 bean 的 beanName 和别名映射。

###### 主要流程

- 根据 web.xml 中 contextConfigLocation 配置的路径，读取 Spring 配置文件，并封装成 Resource。

- 根据 Resource 加载 XML 配置文件，并解析成 Document 对象 。

- 拿到 Document 中的根节点，遍历根节点和所有子节点。

- 根据命名空间，进行不同的解析，将 bean 节点内容解析成 BeanDefinition。

- 将 BeanDefinition 注册到注册表中（也就是beanDefinitionMap、beanDefinitionNames、aliasMap缓存）。

- ```java
  protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
      // 1.刷新 BeanFactory，由AbstractRefreshableApplicationContext实现
      refreshBeanFactory();
      // 2.拿到刷新后的 BeanFactory
      return getBeanFactory();
  }
  ```

###### 02 - 1：refreshBeanFactory

- ```java
  protected final void refreshBeanFactory() throws BeansException {
      // 1.判断是否已经存在 BeanFactory，如果存在则先销毁、关闭该 BeanFactory
      if (hasBeanFactory()) {
          destroyBeans();
          closeBeanFactory();
      }
      try {
          // 2.创建一个新的BeanFactory
          DefaultListableBeanFactory beanFactory = createBeanFactory();
          beanFactory.setSerializationId(getId());
          customizeBeanFactory(beanFactory);
          // 3.加载 bean 定义。
          loadBeanDefinitions(beanFactory);
          this.beanFactory = beanFactory;
      } catch (IOException ex) {
          throw new ApplicationContextException("I/O error parsing bean definition source for "
                                                + getDisplayName(), ex);
      }
  }
  ```

###### 02 - 1 - 1：loadBeanDefinitions（DefaultListableBeanFactory beanFactory）

- 由 XmlWebApplicationContext 实现；

- configLocations：web.xml 中的配置参数；

  ```java
  @Override
  protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) 
      throws BeansException, IOException {
      // 1.为指定BeanFactory创建XmlBeanDefinitionReader
      XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
      // 2.使用此上下文的资源加载环境配置 XmlBeanDefinitionReader
      beanDefinitionReader.setEnvironment(getEnvironment());
      // resourceLoader 赋值为 XmlWebApplicationContext
      beanDefinitionReader.setResourceLoader(this);
      beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
      // Allow a subclass to provide custom initialization of the reader,
      // then proceed with actually loading the bean definitions.
      initBeanDefinitionReader(beanDefinitionReader);
      // 3.加载 bean 定义
      loadBeanDefinitions(beanDefinitionReader);
  }
  
  protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
      // 1.获取配置文件路径
      String[] configLocations = getConfigLocations();
      if (configLocations != null) {
          for (String configLocation : configLocations) {
              // 2.根据配置文件路径加载 bean 定义
              reader.loadBeanDefinitions(configLocation);
          }
      }
  }
  ```

###### 02 - 1 - 1 - 1：loadBeanDefinitions(Location location)

- 从所有的Resource中解析并加载 Bean；

- ```java
  public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws BeanDefinitionStoreException {
      // 1.获取 resourceLoader，这边为 XmlWebApplicationContext
      ResourceLoader resourceLoader = getResourceLoader();
      if (resourceLoader == null) {
          throw new BeanDefinitionStoreException(
              "Cannot load bean definitions from location [" + location 
              + "]: no ResourceLoader available");
      }
      // 2.判断 resourceLoader 是否为 ResourcePatternResolver 的实例
      if (resourceLoader instanceof ResourcePatternResolver) {
          // Resource pattern matching available.
          try {
              // 2.1 根据路径拿到该路径下所有符合的配置文件，并封装成Resource
              Resource[] resources = ((ResourcePatternResolver) resourceLoader)
                  .getResources(location);
              // 2.2 根据Resource，通过流的方式加载Bean的定义
              int count = loadBeanDefinitions(resources);
              if (actualResources != null) {
                  Collections.addAll(actualResources, resources);
              }
              if (logger.isTraceEnabled()) {
                  logger.trace("Loaded " + count + " bean definitions from location pattern ["
                               + location + "]");
              }
              return count;
          }
          catch (IOException ex) {
              throw new BeanDefinitionStoreException(
                  "Could not resolve bean definition resource pattern [" + location + "]", ex);
          }
      } else {
          // 3.只能通过绝对URL加载单个资源
          Resource resource = resourceLoader.getResource(location);
          // 3.1 根据Resource，加载Bean的定义
          int count = loadBeanDefinitions(resource);
          if (actualResources != null) {
              actualResources.add(resource);
          }
          if (logger.isTraceEnabled()) {
              logger.trace("Loaded " + count + " bean definitions from location [" 
                           + location + "]");
          }
          return count;
      }
  }
  ```

###### 02 - 1 - 1 - 1 ：loadBeanDefinitions(Resource resource)  【bean xml文件 解析（XmlBeanFactory】

###### 核心思路：

- 流加载文件；
- 解析xml文件；
- 将解析bean注册到ConcurrentHaseMap中；

###### 加载Bean 的示例

```java
@SuppressWarnings("deprecation")
public class BeanFactoryTest {
    @Test
    public void testSimpleLoad(){
        BeanFactory bf = new XmlBeanFactory(new ClassPathResource("beanFactoryTest.xml"));
        MyTestBean bean=(MyTestBean) bf.getBean("myTestBean");
        assertEquals("testStr",bean.getTestStr());
    }
}
```

###### 配置文件的封装

- Spring对其内部使用到的资源 实现了自己的抽象结构：**Resource 接口**封装底层资源，便可以低所有资源文件进行统一处理。
- 对**不同来源的资源文件都有相应的Resource实现**：文件（FileSystemResource）、Classpath资源 （ClassPathResource）、URL资源 （UrlResource）、InputStream资源 （InputStreamResource）、Byte数组 （ByteArrayResource）等。
- 当通过Resource相关类完成了对配置文件进行封装后，配置文件的读取工作就全权交给 **XmlBeanDefinitionReader** 来处理。
- XmlBeanFactory —> AbstractAutowireCapableBeanFactory
  - 如果被依赖的Bean，实现了BeanNameAware 接口，就不会被初始化，其本身通过其他的方式进行注入；

###### 加载Bean

- org.springframework.beans.factory.xml.XmlBeanFactory#XmlBeanFactory(org.springframework.core.io.Resource, org.springframework.beans.factory.BeanFactory)
- XmlBeanFactory(Resource)  —> XmlBeanDefinitionReader —> XmlBeanDefinitionReader.doLoadBeanDefinitions —> XmlBeanDefinitionReader.registerBeanDefinitions

###### 获取 XML 的验证方式

- 验证模式保证了XML文件的正确性，比较常用的两种验证模式：DTD和XSD；
- DTD 已停止更新，基本是XSD；

###### 获取Document

- EntityResolver：可以提供一 个如何寻找DTD声明的方法，即由程序来实现寻找 DTD声明的过程。
  - 分别的是识别DTD 和 XSD 的连接
    - publicId
    - systemId


###### 解析及注册 BeanDefinitions

- BeanDefinationReader
  - 读取Bean 配置元信息的接口；
- DefaultBeanDefinitionDocumentReader
- 获取Document  的 root 节点
- 核心功能：org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#doRegisterBeanDefinitions（Element root）；
  - 解析 PROFILE_ATTRIBUTE 属性，区分环境；
  - 通过xml 的 NameSpace 区分是否默认的，还是自定义xml，则使用不同的方式解析；

##### 03：默认标签的解析

- 入口：org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#parseDefaultElement
- 分别对4 种不同标签（import、alias、bean和beans）做了不同的处理。

###### import 标签的解析

- 获取resource属性所表示的路径，解析该路径对应的资源，调用监听器事件；

###### alias标签的解析

- org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#parseDefaultElement
- 解析alias 标签，注册到别名集合；

###### bean标签的解析及注册

- org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#processBeanDefinition
  - BeanDefinitionParserDelegate：xml bean 的解析的抽象类；
    - 解析id，name（别名），硬编码解析其他属性；
    - 检测 beanName 是否存在，若无，生成一个beanName；
    - 创建BeanDefinition；
    - 解析bean 的各种属性值；
    - 解析元数据 meta；
    - 解析子元素lookup-method；
  - Decorate 解析的bean，其实就是解析bean自定义的属性；
- AbstractBeanDefinition 
  - 子类：org.springframework.beans.factory.support.GenericBeanDefinition
    - 所有地XML配置都解析到了GenericBeanDefinition 中；

- **注册解析的BeanDefinition**
  - 通过BeanName注册：容器中管理对象的集合ConcurrentHaseMap
    - org.springframework.beans.factory.support.DefaultListableBeanFactory#beanDefinitionMap
    - 键：beanName，值：beanDefinition
    - 如果已注册，则更新，若为注册，则注册；

  - 通过别名注册：容器中管理对象别名的集合ConcurrentHaseMap
    - org.springframework.core.SimpleAliasRegistry#aliasMap
    - 解析bean 标签的name 属性，注册；

- 通知监听器解析及注册完成
  - 对注册事件进行监听时，可将处理逻辑写入监听器，Spring并没有对此事件做任务处理；

