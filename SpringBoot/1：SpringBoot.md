### SpringBoot

------

​	简化了Spring开发初始搭建以及开发过程，它默认配置了很多框架的使用方式

##### 1：SpringBoot、和SpringMVC的区别

- Spring Boot 实现了自动配置，降低了项目搭建的复杂度，本省没有扩展Spring的功能，只是用于更加快速、方便地开发新一代基于Spring框架的应用程序
- 它集成了大量常用的第三方库配置(例如Jackson, JDBC, Mongo, Redis, Mail等等)，Spring Boot应用中这些第三方库几乎可以零配置的开箱即用(out-of-the-box)

##### 2：Spring 构键项目

1. 使用Spring initializer(http://start.spring.io) -->Maven(选项)-->dependency(添加依赖)
2. 所有的页面都是html，并存储在templates文件下，application.properties：配置端口，数据库等信息
3. index.html：<html xmlns:th="http://www.thymeleaf.org">
4. pom.xml：jar包：spring-boot-starter-web，spring-boot-starter-test，spring-boot-starter-thymeleaf
   - spring-boot-starter ：核心 POM，包含自动配置支持、日志库和对 YAML 配置文件的支持
   - spring-boot-starter-test ：测试模块，包括JUnit、Hamcrest、Mockito
   - spring-boot-starter-jdbc：支持使用 JDBC 访问数据库
   - spring-boot-starter-tomcat：使用 **Spring Boot 默认的 Tomcat 作为应用服务器**
   - spring-boot-starter-web：支持 Web 应用开发，包含 Tomcat 和 spring-mvc

##### 3：启动 SpringBoot 的方式

1. 直接运行启动类(Project名称+Application)
2. 在项目的根目录下，命令窗口输入：mvn install，让项目生成jar包,输入java -jar target/demo-0.0.1-SNAPSHOT.jar
3. 项目的根目录下，命令窗口输入:mvn spring-boot:run

##### 3：SpringBoot 资源路径

- 默认的classpath:/resources/，classpath:/static/
  - static：存放静态页面，可以直接访问
  - static：http://localhost:8080/hello.html
  - templates：存放动态页面(跳转过去的页面)
    - 动态页面需要先请求服务器，访问后台应用程序，然后再重定向到页面
    - 默认使用Thymeleaf来做动态页面
      - 例：http://localhost:8080/hello.html，return "hello.html"
    - 静态页面的return默认：跳转到/static/index.html
    - 当在pom.xml中引入了thymeleaf组件，动态跳转会覆盖默认的静态跳转，/templates/index.html
    - 注意看两者return代码也有区别，动态有或没有html后缀都可以
    - 如果在使用动态页面时还想跳转到/static/index.html，可以使用重定向return "redirect:/index.html"

##### 4：yaml：以数据为中心，使用空白、缩进、分行组织数据，用于指定分层配置数据

- 语法：
  1. 大小写敏感
  2. 使用缩进表示层级关系
  3. 禁止使用tab缩进，只能使用空格键 , 建议使用两个空格
  4. 缩进的空格数目不重要，只要相同层级的元素左侧对齐即可
  5. "#"：表示注释，从这个字符一直到行尾，都会被解析器忽略
  6. 字符串可以不用引号，也可以使用单引号或者双引号	
  7. 书写(Key: Value)
     - key：需要顶格写，不能有空格，冒号后面需要有一个空格然后再跟值, 相同的缩进属于同一个map 

##### 5：配置文件(application.properties)

1. Spring可以通过注解@Value(“${属性名key}”)：加载对应的配置属性，然后将属性值赋值给注解对应的实体属性
2. @ConfigurationProperties(prefix = “xxx”)：配置属性注解，可以指定一个属性的前缀，将配置文件中的key为prefix属性名的值赋值给对应的属性，这种方式适用于前缀相同的一组值，这样就不用再为每个属性配置
3. @PropertySource：来定义属性文件的位置
4. @PropertySource(value= {"classpath:/config/book.properties"})
5. 多环境配置文件：application-{profile}.properties 格式，其中{profile}对应你的环境标识，比如：
   1. application-dev.properties：开发环境
   2. application-test.properties：测试环境
   3. application-prod.properties：生产环境
6. 在application.properties文件中通过spring.profiles.active=dev属性来设置，其值对应{profile}值,配置文件会被加载
7. @Conditional：满足特定条件时才会创建一个Bean放入到IOC容器，SpringBoot就是利用这个特性进行自动配置的
8. @ImportResource({“classpath:aaa-dao.xml”,”classpath:bbb-context.mxl”})

##### 6：@SpringBootApplication：一个复合注解，包括三个

1. @Configuration:指的Java Config(Java 配置)，是一个Ioc容器类，相当于spring的xml的配置文件
2. @EnableAutoConfiguration：借助@Import来收集所有符合自动配置条件的bean定义的类(@Configuration),汇总成一个加载到IoC容器
3. @ComponentScan：若不配置base-package,则默认从当前类开始下扫描，所以一般运行类放在基包下

##### 7： SpringApplication的run（）方法执行流程

1. 加载各种条件和回调接口，例如applicationContextIntializer，Applicationlistener,调用starter()
2. 创建环境，调用environmentPrepared()
3. 初始化ApplicationContext，配置文件，调用contextprepared(),contextLoaded()
4. 调用finished()