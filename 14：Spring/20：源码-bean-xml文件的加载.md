### Spring-bean xml 文件的解析

------

[TOC]

##### 01：bean xml文件 解析（XmlBeanFactory）

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

##### 02：默认标签的解析

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








