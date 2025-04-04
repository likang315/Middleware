### Spring Annotation

------

[TOC]

##### 01：概述

- Spring 和 JSR 的 Annotaion 两套 Annotation，选择使用 Spring。

###### 启用 Annotation ：

- 注解在默认情况下在 Spring 容器中不打开；
- <context:annotation-config />

##### 02：实例化 Bean

- 四个注解功能相同，都是用在类上的 Annotation，说明**实例化此类，放入 spring 容器中**，可以设置bean的名称。


1. @Repository：声明在dao层

   - 实例化MapperScannerConfigurer或者使用**@MapperScan()** 进行多个包扫描接口类

   ```xml
   <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">    
       <property name="basePackage" value="com.xupt.ivr.dao,
                                           com.xupt.crm.dao" />
     	<!--该属性单一数据源时可以不用配，但是多个数据源时，可以为不同的数据库对应不同的mapper接口-->
       <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
   </bean>
   <!--只扫描Repository注解-->
   <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">    
       <property name="basePackage" value="com.xupt.crm" />
     	<property name="annotationClass"
                 value="org.springframework.stereotype.Repository"/>
       <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
   </bean>
   ```

2. @Service：声明在service层

3. @Controller：声明在控制器层

4. @Component ：一般声明在组件、工具类

##### 3：设置基包的扫描( 四种方式)

```java
<context:component-scan base-package="com.xupt"/>

@Configuration
@ComponentScan(“基包名”)
Public class AppConfig {}

@Configuration
@ComponentScan(basepackages="基包名")
Public class AppConfig{}		

@Configuration
@ComponentScan(basePackageClasses={App1Config.class,App2Config.class})
Public class AppConfig{}
```

##### 4：自动注入 (3个)【重要】

- **@Autowired：**

  - 用于属性、方法上
  - 通过**byType**自动注入，从容器中取出此类的实例对象赋给它，应用于 bean 属性，setter 方法，构造方法；
  - 是Spring 自己的
  
  ```java
  // 通过byType注入
  @Autowired
  private SpellChecker spellChecker;
  @Autowired
  public void setSpellChecker(SpellChecker spellChecker) {
    this.spellChecker = spellChecker;
  }
  
  // 构造函数 @Autowired 说明当创建 bean 时，即使在 xml 文件中没有使用元素配置 bean ，构造函数也会被自动注入
  @Autowired
  public TextEditor(SpellChecker spellChecker){
    this.spellChecker = spellChecker;
  }
  ```
  
- **@Resource ：**

  - 默认通过**byName**自动注入，如果使用type属性时则使用byType自动注入策略，如果既不指定name也不指定type属性，这是将通过**反射机制使用byName自动注入**；

  - 符合JSR-250规范的一种实现；

  - @Autowired 是 Spring 提供的，一旦切换到别的 IoC 框架，就无法支持注入了. 而@Resource 是 JSR-250 提供的，它是 Java 标准，我们使用的 IoC 容器应该和它兼容，所以即使换了容器，它也能正常工作，所以建议使用@Resource；

    ```java
    @Resource(name="baseDao")
    protected ArticleDao articleDao;
    ```

- **@Qualifier：**

  - 通过选择装配 bean 的**标识符来装配**，用于多个 bean 无法确定装配哪个的情况（起别名），**通过byName**获取对象；

  ```xml
  @Qualifier("dev")
  protected ArticleDao articleDao;
  
  <bean id="article" class="com.xupt">
  	<qualifier value="dev"/>
  </bean>
  ```

- **@Primary：**

  - 用于声明 bean 是首选的，用在多个 bean，无法选择装配谁的情况；

  ```xml
  <bean id="article" class="com.xupt" primary="true">
    
  @Primary
  @Component
  public class Tool{}
  ```

- **@Required :**

  - 应用于 bean 属性的 setter 方法，它表明受影响的 bean 属性在配置时必须放在 XML 配置文件中，否则容器就会抛出一个 BeanInitializationException 异常；

##### 05：Bean 初始化之后，销毁之前

###### 	@PostConstruct ，@PreDestroy

- 应用于Bean，构造之后，销毁之前


```java
@Service("ha")
public class AjaxService {	
  @PostConstruct
  public void init(){
    System.out.println("Bean is going through init.");
  }

  @PreDestroy
  public void destroy(){
    System.out.println("Bean will destroy now.");
  }
}
```

###### 	通过 xml 文件

```xml
<!--初始化此类，放入容器中-->
<bean id="student" class="com.xxx" init-method="init" destroy-method="destroy" />
```

###### 	显式配置类

```java
@Bean(initMethod = "init")
@Bean(destroyMethod = "cleanup")
public Foo foo() {
	return new Foo();
}
```

##### 06：通过 Java 代码装配 bean

-  @Configuration :

  - 表示这个类可以使用 Spring IoC 容器作为 bean 定义的来源，配置类；

- @Bean：

  - 一个带有 @Bean 的注解方法将返回一个对象，该对象应该被注册为在 Spring 应用程序上下文中的 bean，**方法名称作为 bean 的 ID**；

  ```java
  @Configuration
  public class AppConfig {
    @Bean(initMethod="init", destroyMethod="cleanup" )
    @Scope("prototype")
    public Foo foo() {
      return new Foo();
    }
  }
  ```

##### 07：显示加载配置类

- 通过：AnnotationConfigApplicationContext

```java
// 加载一个配置类
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
MyService myService = context.getBean(MyService.class);
// 加载多个配置类
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
context.register(AppConfig.class, OtherConfig.class);
context.register(AdditionalConfig.class);
context.refresh();
MyService myService = context.getBean(MyService.class);
// 扫描包下的配置类
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
ctx.scan("com.xupt");
ctx.refresh();
MyService myService = context.getBean(MyService.class);
```

##### 09：导入混合配置类

- 在典型的 Spring 应用中，我们可能会同时使用**自动和显式配置**，在 spring 中这些配置方案都不是互斥的；


###### 在 JavaConfig 中引用其他Ioc 容器的配置

- @import 注解允许从另一个配置类中加载 @Bean 定义
- @importResource 注解允许导入xml 配置

```java
@Configuration
@import(AppConfig.class)
@importResource("classpath:ApplicationContext.xml")
Public class AppConfig2 {}
```

###### 在 xml 中引用 JavaConfig

```xml
<beans>
	<import resource="ApplicationContext.xml"/>
	<bean id="Myconfig" class="com.AppConfig"/>
</beans>
```







