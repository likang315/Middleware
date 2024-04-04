### Spring MVC

------

[TOC]

##### 01：概述

<img src="https://github.com/likang315/Middleware/blob/master/15：SpringMVC/photos/Spring-MVC.png?raw=true" style="zoom:47%;" />

- MVC（Model-View-Controller）模式提供了一种软件架构模式，用于将应用程序的逻辑层、表示层和控制层分离。提高代码的可维护性和可扩展性。
  1. **模型（Model）：**负责应用程序的业务逻辑和数据处理；
  2. **视图（View）：**负责用户界面的呈现；
  3. **控制器（Controller）：**负责处理用户输入并相应地更新模型和视图。
- Spring MVC 是 Spring Framework 中的一个模块，用于构建Web应用程序；

##### 02：MVC Request 流程【重要】

1. **前端控制器：**所有的请求先访问 Spring 的 DispatcherServlet；
   - Spring MVC 所有的请求都会通过一个前端控制器（front controller），是常用的 Web 应用程序模式；
   - DispatcherServlet：**一个单实例的Servlet** 将请求委托给WEB应用程序的其他组件来执行实际的处理；
2. **处理器映射：**DispatcherServlet 将请求转发给 Spring MVC 控制器（controller）
   - DispatcherServlet 需要知道应该将请求发送给哪个控制器，所以 DispatcherServlet 会查询一个或多个**处理器映射（Handler Mapping）**来确定请求的下一站在哪，处理器映射会**根据请求所携带的 URL 信息**来进行决策，Map的key；
3. **控制器处理：**选择合适的控制器后，DispatcherServlet 会将请求发送给选中的控制器；
   - 处理器处理请求，将处理的结果封装成 ModelAndView，发送回 DispatcherServlet，DispatcherServlet 将根据视图名选择相应的视图解析器（ ViewResolver）；
5. **视图解析器**：渲染数据；
   - 将它交付的模型数据，使用**视图解析器将模型数据渲染输出**，然后通过响应对象传递给客户端；

##### 03：配置示例

###### 使用Maven-archetype-webapp构建

- spring-web-4.3.7.RELEASE.jar
- spring-webmvc-4.3.7.RELEASE.jar

###### 配置 DispatcherServlet （WEB-INF/web.xml）

- SpringMVC 是强依赖 xml 文件加载的，所以如果对启动文件有个性化改动一定要改这里，另外 **web.xml** 还可以配置过滤器，自定义字符集，自定义请求拦截，鉴权等，还可以映射请求文件列表等；

```jsp
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.0" xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
                             http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd">

    <!-- 配置监听器ContextLoaderListener, 它的作用就是启动Web容器时，自动装配 ApplicationContext容器 -->
    <listener>
        <listener-class>
            org.springframework.web.context.ContextLoaderListener
        </listener-class>
    </listener>

    <!-- 部署applicationContext的xml文件 -->
    <!--如果在web.xml中不写任何参数配置信息，默认的路径是"/WEB-INF/applicationContext.xml，在WEB-INF目录下创建的xml文件的名称必须是applicationContext.xml。
如果是要自定义文件名可以在web.xml里的上下文初始化参数中加入contextConfigLocation这个参数：
在<param-value> </param-value>里指定相应的xml文件名，如果有多个xml文件，可以写在一起并以“,”号分隔。
也可以这样applicationContext-*.xml采用通配符。
也可以在applicationContext.xml在另外设置文件路径，如：
<import resource="classpath*:spring/spring-*.xml" />  -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:spring/applicationContext.xml</param-value>
    </context-param>

    <!-- CharacterEncodingFilter是Spring框架提供的默认字符集过滤器，当forceEncoding为True时候，
则强制覆盖之前的编码格式，避免页面乱码。-->
    <filter>
        <filter-name>characterEncodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
        <init-param>
            <param-name>forceEncoding</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>characterEncodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    <!-- DispatcherServlet是Spring默认的请求分发器，一般请求分发会根据请求Url的一些设置来进行不同资源请求的分发，分发到各个不同的处理器中，如静态资源分发和数据接口分发，但是静态资源分发完全可以由Tomcat代劳而不经过Spring，所以在Web.xml你经常会见到如下配置，激活Tomcat的defaultServlet处理静态文件：
将此配置写在DispatcherServlet之前，让DefaultServlet先拦截请求，不经过Spring
-->
    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>*.css</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>*.jpg</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>*.png</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>*.html</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern>*.xml</url-pattern>
    </servlet-mapping>
    <!-- DispatcherServlet是使用SpringMvc的重要配置，DispatcherServlet是Servlet转发器，可以配置多个DispatcherServlet来实现转发，在其配置中需要配置匹配规则，然后将拦截到的请求分发到请求目标，这里指的请求目标，实际就是Controller。
在DispatcherServlet的初始化过程中，框架会在web应用的 WEB-INF文件夹下寻找名为
[servlet-name]-servlet.xml 的配置文件，生成文件中定义的bean。也可以使用自己的配置文件名称  -->
    <servlet>
        <servlet-name>DispatcherServlet</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!--指明了配置文件的文件名，不使用默认配置文件名，而使用dispatcher-servlet.xml配置文件。-->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <!--1、不写,使用默认值:/WEB-INF/<servlet-name>-servlet.xml-->
            <!--2、<param-value>/WEB-INF/classes/dispatcher-servlet.xml</param-value>-->
            <!--3、<param-value>classpath*:dispatcher-servlet.xml</param-value>-->
            <!--4、多个值用逗号分隔-->
            <param-value>classpath:spring/dispatcher-servlet.xml</param-value>
        </init-param>
        <!--是启动顺序，让这个Servlet随容器一起启动 -->
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <!--Servlet拦截匹配规则可以自已定义，一般根据Url正则来配置-->
        <servlet-name>DispatcherServlet</servlet-name>
        <!--会拦截所有请求。-->
        <url-pattern>/</url-pattern>
    </servlet-mapping>

    <!--首页-->
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>
</webapp>
```

###### 配置视图解析器

- /WEB-INF/views/hello.jsp :前缀+视图名+后缀

```XML
<bean id="jspViewResolver"
      class="org.springframework.web.servlet.view.InternalResourceViewResolver">
 	<property name="prefix" value="/WEB-INF/views/"/>
	<property name="suffix" value=".jsp"/>
</bean>
```

###### 编写控制器

```java
@RestController
@RequestMapping（"/hello")
public class HelloWorldController { 
    @RequestMapping("/world")
    public String helloWorld(Model model) {
        model.addAttribute("message", "Hello World!");
        return "Hello";
    }
}
```
