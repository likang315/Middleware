### SpringBoot

------

[TOC]

##### 01：概述

- 简化了 Spring 框架搭建以及开发过程，可以非常容易的构建独立的服务组件，是实现分布式架构、微服务架构利器；
- Spring Boot 可以把Web应用程序变为**可自执行的 Jar 文件**，不用部署到传统Java应用服务器里就能在**命令行里运行**。Spring Boot 在应用程序里内嵌了一个Servlet容器（Tomcat、Jetty或Undertow），以此实现这一功能。但这是**内嵌的Servlet容器提供的功能**，不是Spring Boot实现的。
- Spring Boot 没有引入任何形式的代码生成，而是利用了**Spring 4的条件化配置**特性， 以及**Maven和Gradle提供的传递依赖解析**，以此实现 Spring 应用程序上下文里的自动配置。

##### 02：SpringBoot 特性

- 自动配置
  - 针对常见的应用功能，SpringBoot 能自动提供相关配置；
- 起步依赖
- 命令行界面（CLI）
- Acturator
  - 提供运行时检视应用程序内部情况的能力。

##### 0：pom.xml 依赖管理

- 在 pom.xml 中引入 spring-boot-dependencies, 它可以提供 dependency management，也就是说依赖管理，引入以后在申明其它dependency的时候就不需要version了，后面可以看到，可以用于解决以来冲突；

```xml
<!--管理Springboot jar包的版本-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-dependencies</artifactId>
    <version>${org.springframework.boot.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

##### 03：安装 SpringBoot CLI

- 下载分发包：
- 添加系统路径：在解压后的目录里，你会找到一个bin 目录，其中包含了一个spring.bat脚本（用于Windows环境）和一个**spring脚本**（用于Unix环境）。 把这个bin目录添加到系统路径里。
- $ spring --version

##### 04：开发第一个SpringBoot项目

###### SpringBoot 启动引导类 & 配置类

- 复合注解 **@SpringBootApplication**

   - 一个注解相当于三个注解的配置，

   - @Configuration

     - 标明该类使用Spring基于Java的配置。

   - @ComponentScan

     - 启动组件扫描，若不配置scanBasePackages，则**默认从当前类开始下扫描，所以一般运行类放在基包下。**

   - @EnableAutoConfiguration【自动配置】

     - ```
       @Import({AutoConfigurationImportSelector.class})
       ```

     - **AutoConfigurationImportSelector**的selectImports()方法通过**SpringFactoriesLoader.loadFactoryNames()**扫描所有具有**META-INF/spring.factories**的jar包。

     - 这个spring.factories文件也是一组一组的**key=value的形式**，其中一个key是EnableAutoConfiguration类的全类名，而它的value**是一个xxxxAutoConfiguration的类名的列表**，这些类名以逗号分隔；

     - Spring启动的时候会扫描所有 jar 路径下的`META-INF/spring.factories`，将其文件包装成**Properties对象**，从Properties对象获取到key值为`EnableAutoConfiguration`的数据，然后通过`@Conditional`按需加载的配置类，添加到IOC容器里边。

   - Springboot 会自动初始化数据源，若没有配置数据源则剔除DataSourceAutoConfiguration该类；

     - ```
       @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
       ```


- 运行SpringBoot项目的方式
  - 有几种方式，其中包含**传统的WAR文件部署（需要打包部署到 tomcat 的webapp下）**。但这里的 main() 方法让你可以**在命令行里把该应用程序当作一个可执行Jar 文件来运行**。
  - 这里向 SpringApplication.run() 传递了**一个 AtlantisZeusApplication 类的引用，还有命令行参数**，通过这些东西启动应用程序。
    - 命令：java -jar 打的Jar包名

###### 示例

- ```java
  package com.atlantis.zeus;
  
  import lombok.val;
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.ImportResource;
  import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
  import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
  
  import java.io.IOException;
  
  /**
   * springboot 启动类 且加载.properties文件
   * SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
   *
   * @author kangkang.li@qunar.com
   * @date 2020-10-11 19:15
   */
  @SpringBootApplication
  @ImportResource(value = {"classpath:spring/application-context.xml"})
  public class AtlantisZeusApplication {
  
  	/**
  	 * 导入.properties文件
  	 *
  	 * @return
  	 * @throws IOException
  	 */
  	@Bean
  	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
  		val result = new PropertySourcesPlaceholderConfigurer();
  		result.setLocations(new PathMatchingResourcePatternResolver()
  				.getResources("classpath*:**/*.properties"));
  		result.setIgnoreUnresolvablePlaceholders(true);
  		return result;
  	}
  
  	public static void main(String[] args) {
  		SpringApplication.run(AtlantisZeusApplication.class, args);
  	}
  }
  ```

###### 测试 SpringBoot 应用程序

- 不止是个用于测试的占位符，还是一样测试例子

- SpringBootTest： 使用该注解从 AtlantisZeusApplicationTests 配置类例加载Spring应用程序上下文。

- ```java
  package com.atlantis.atlantiszeus;
  
  import lombok.extern.slf4j.Slf4j;
  import org.junit.jupiter.api.Test;
  import org.junit.runner.RunWith;
  import org.springframework.boot.test.context.SpringBootTest;
  import org.springframework.test.context.junit4.SpringRunner;
  
  /**
   * 单元测试
   *
   * @author likang02@corp.netease.com
   * @date 2021-08-22 16:11
   */
  @Slf4j
  @RunWith(SpringRunner.class)
  @SpringBootTest(classes = AtlantisZeusApplicationTests.class)
  public class AtlantisZeusApplicationTests {
  
  	/**
  	 * 单元测试
  	 */
  	@Test
  	void contextLoads() throws InterruptedException {
  		Thread.sleep(10 * 1000);
  		log.warn("AtlantisZeusApplicationTests_contextLoads: test !!!");
  	}
  
  }
  ```

###### 父 pom.xml

- 将 spring-boot-starter-parent 作为上一级， 这样一来就能利用Maven的依赖管理功能，继承很多常用库的依赖版本，在你声明依赖时就不用再去指定版本号了。

- 构建插件的主要功能是：把项目打包成一个可执行的超级Jar，包括把应用程序的所有依赖打入 Jar 文件内，并为Jar 添加一个描述文件，其中的内容能让你用 **java -jar** 来运行 应用程序。

- ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                               http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>
      <groupId>com.atlantis</groupId>
      <artifactId>atlantiszeus</artifactId>
      <version>1.0.0</version>
      <packaging>jar</packaging>
  
      <name>atlantis</name>
      <description>atlantis Demo</description>
      <!--从 spring-boot-starterparent继承版本号-->
      <parent>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-parent</artifactId>
          <version>{springBootVersion}</version>
          <relativePath/> <!-- lookup parent from repository -->
      </parent>
      <!--起步依赖-->
      <dependencies>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-web</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-data-jpa</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-thymeleaf</artifactId>
          </dependency>
          <dependency>
              <groupId>com.h2database</groupId>
              <artifactId>h2</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-test</artifactId>
              <scope>test</scope>
          </dependency>
      </dependencies>
  
      <build>
          <!--包名-->
          <finalName>atlantis-zeus</finalName>
          <plugins>
              <!--SpringBoot 打 War 包-->
              <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-war-plugin</artifactId>
                  <version>${maven_war_plugin_version}</version>
                  <configuration>
                      <warSourceDirectory>webapp</warSourceDirectory>
                      <failOnMissingWebXml>false</failOnMissingWebXml>
                  </configuration>
              </plugin>
              <!--SpringBoot 打 Jar 包-->
              <plugin>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-maven-plugin</artifactId>
                  <version>${maven_jar_plugin_version}</version>
                  <executions>
                      <execution>
                          <goals>
                              <goal>repackage</goal>
                          </goals>
                      </execution>
                  </executions>
              </plugin>
  
              <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-compiler-plugin</artifactId>
                  <configuration>
                      <source>${java_source_version}</source>
                      <target>${java_target_version}</target>
                  </configuration>
              </plugin>
          </plugins>
      </build>
  
      <build>
          <plugins>
              <!--运用Spring Boot插件-->
              <plugin>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-maven-plugin</artifactId>
              </plugin>
          </plugins>
      </build>
  </project> 
  ```



##### 05：起步依赖（SpringBoot 起步时需要的依赖）

- 本质上是一个 Maven项目对象模型（Project Object Model，POM），**定义了对其他库的传递依赖**，这些东西加在一起即支持某项功能。
- 起步依赖本身的版本是由正在使用的**Spring Boot的版本**来决定的，而起步依赖则会**决定它们引入的传递依赖的版本**。

###### 覆盖起步依赖引入的传递依赖

- 使用 <exclusions> 元素来来排除传递依赖

  - 假设项目不需要 jackson，为项目瘦身；

  - ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <exclusions>
            <exclusion>
                <groupId>com.fasterxml.jackson.core</groupId>
            </exclusion>
        </exclusions>
    </dependency> 
    ```

- 直接引入，覆盖原有依赖

  - 另一方面，也许项目需要Jackson，但你**需要用另一个版本的Jackson来进行构建，而不是Web 起步依赖里的那个版本**。

  - ```xml
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.4.3</version>
    </dependency> 
    ```

##### 06：使用自动配置

- Spring Boot的自动配置是**应用程序启动时的过程**， 考虑了众多因素，才**决定Spring配置应该用哪个，不该用哪个**。
- 在向应用程序加入Spring Boot时，有个名为**spring-boot-autoconfigure的JAR文件**，其中包含了 很多配置类。**每个配置类都在应用程序的Classpath里**，都有机会为应用程序的配置添砖加瓦，利用了Spring的条件化配置。

###### 示例

- 因为 Classpath 里有 Spring MVC （归功于 Web 起步依赖 ），所以会配置 Spring 的DispatcherServlet并启用Spring MVC。

##### 07： Spring Boot 的依赖

- mvn dependency:tree：执行此命令可以查看依赖树

| **spring-boot-starter**          | 核心 POM，包含自动配置支持、日志库和对 YAML 配置文件的支持。 |
| -------------------------------- | ------------------------------------------------------------ |
| spring-boot-starter-amqp         | 通过 spring-rabbit 支持 AMQP，消息队列                       |
| **spring-boot-starter-aop**      | 包含 spring-aop 和 AspectJ 来支持面向切面编程（AOP）         |
| spring-boot-starter-batch        | 支持 Spring Batch，包含 HSQLDB。                             |
| spring-boot-starter-data-jpa     | 包含 spring-data-jpa、spring-orm 和 Hibernate 来支持 JPA。   |
| spring-boot-starter-data-mongodb | 包含 spring-data-mongodb 来支持 MongoDB。                    |
| spring-boot-starter-data-rest    | 通过 spring-data-rest-webmvc 支持以 REST 方式暴露 Spring Data 仓库。 |
| **spring-boot-starter-jdbc**     | 支持使用 JDBC 访问数据库                                     |
| spring-boot-starter-security     | 包含 spring-security。                                       |
| **spring-boot-starter-test**     | 包含常用的测试所需的依赖，如 JUnit、Hamcrest、Mockito 和 spring-test 等。 |
| **spring-boot-starter-velocity** | 支持使用 Velocity 作为模板引擎。                             |
| **spring-boot-starter-web**      | 支持 Web 应用开发，包含 Tomcat 和 spring-mvc。               |
| spring-boot-starter-websocket    | 支持使用 Tomcat 开发 WebSocket 应用。                        |
| spring-boot-starter-ws           | 支持 Spring Web Services                                     |
| spring-boot-starter-actuator     | 添加适用于生产环境的功能，如性能指标和监测等功能。           |
| spring-boot-starter-remote-shell | 添加远程 SSH 支持                                            |
| spring-boot-starter-jetty        | 使用 Jetty 而不是默认的 Tomcat 作为应用服务器。              |
| **spring-boot-starter-log4j**    | 添加 Log4j 的支持                                            |
| **spring-boot-starter-logging**  | 使用 Spring Boot 默认的日志框架 Logback                      |
| **spring-boot-starter-tomcat**   | 使用 Spring Boot 默认的 Tomcat 作为应用服务器。              |
|                                  |                                                              |

##### 05： SpringBoot 项目结构

1. Spring-Boot 默认提供静态资源目录位置**需置于classpath下**(resources目录下)，目录名需符合如下规则：

   - /static
   - 默认的classpath:/resources/
     - static：存放静态页面，可以直接访问
     - static：http://localhost:8080/hello.html，静态页面的return默认：跳转到/static/hello.html
     - templates：存放动态页面(跳转过去的页面)
       - 动态页面需要先请求服务器，访问后台应用程序，然后再重定向到页面
       - 默认使用Thymeleaf来做动态页面
         - 例：http://localhost:8080/hello.html，return "hello.html"
       - 当在pom.xml中引入了thymeleaf组件，动态跳转会覆盖默认的静态跳转，/templates/index.html
       - 注意看两者return代码也有区别，动态有或没有html后缀都可以
       - 如果在使用动态页面时还想跳转到/static/index.html，可以使用重定向
         - return "redirect:/index.html"

2. **@RestController** 

   - 相当于把@Controller和@ResponseBody两个注解和起来，直接作为Json对象返回；

3. 全局异常捕获处理（AOP）

   ```java
   /**
   * 定义一个全局异常捕获处理类，只要Controller发生异常就会调到这里
   */
   @ControllerAdvice
   public class GlobalExceptionHandler {
   		// 捕获运行时异常，方法和返回值自定义的
       @ExceptionHandler(RuntimeException.class)
       @ResponseBody
       public Map<String,Object> exceptionHander(){
           Map<String, Object> map = new HashMap<String, Object>();
           map.put("errorCode", "101");
           map.put("errorMsg", "系統错误!");
           return map;
       }
   }
   ```

4. 渲染Web页面

   1.  模板引擎：基于模板来生成文本输出，实现动态的网页，相当于Jsp，SpringBoot是不推荐使用Jsp，因为其不能打包成jar包部署；
   2. 模板引擎
      - Thymeleaf
      - FreeMarker
      - Velocity
      - Groovy
   
5. **mian 方法启动成功**
   ![springboot](/Users/likang/Code/Git/Middleware/16：SpringBoot/photos/springboot.png)



