### Web 的三大组件

------

Spring 容器初始化时，加载顺序：监听器—过滤器—拦截器

##### 1：Imterface javax.servlet.Filter

​	filter 作用于在 intreceptor 之前，不像 intreceptor 一样依赖于 springmvc 框架，只需要依赖于 servlet

- 实现此接口，重写dofilter（）
- **@WebFilter** 标记一个类为filter，被spring进行扫描

```xml
<!-- 配置过滤器，实例化这个类 -->
<filter>
	<filter-name>filter</filter-name>
	<filter-class>com.xupt.filter</filter-class>
  <!--若要保留Filter中init，destroy方法的调用，需要配置初始化参数targetFilterLifecycle为true，			 该参数默认为false-->
  <init-param>
		<param-name>targetFilterLifecycle</param-name>
		<param-value>true</param-value>
	</init-param>
</filter>

<!--映射过滤器-->
<filter-mapping>
<filter-name>filter</filter-name>
	<!--"/*":表示拦截所有的请求 -->
	<url-pattern>/*</url-pattern>
</filter-mapping>
```

##### 2：Interceptor：拦截器

###### HandlerInterceptor：Interface

​	自定义拦截器时，需实现此接口，重写一下三个方法

- boolean **preHandle**(HttpServletRequest request, HttpServletResponse response, Object handler)
  - 在请求调用Controller之前，拦截处理，依据返回结果，来判断是否继续向下执行
- void **postHandle**(HttpServletRequest request, HttpServletResponse response, Object handler，ModelAndView modelAndView)
  - 该方法在Controller 调用之后，且解析视图之前执行
- void **afterCompletion**(HttpServletRequest request, HttpServletResponse response, Object handler，Exception ex)  throws Exception
  - 该方法在整个请求完成，即视图渲染结束之后，准备返回数据时执行

###### Interceptotr 的配置

```xml
<!--配置一组拦截器-->
<mvc:interceptors>
  <!--全局拦截器，拦截所有请求-->
  <bean class="com.xupt.AllInterceptor" />
  <!--拦截器1-->
  <mvc:interceptor>
    <!--配置拦截器的作用路径-->
    <mvc:mapping path="/*"/>
    <!--剔除不拦截的请求URL-->
    <mvc:exclude-mapping path=""/>
    <bean class="com.xupt.Intercptor1"/>
  </mvc:interceptor>
  <!--拦截器2-->
  <mvc:interceptor>
    <mvc:mapping path="/hello"/>
    <bean class="com.xupt.Interceptor2"/>
  </mvc:interceptor>
</mvc:interceptors>
```

##### 3：过滤器和拦截器的区别

- 过滤器在拦截器之前执行，过滤一些非法URL等；
- 过滤器可以对所有的请求起作用，而拦截器只能对Controller 层有用；
- 过滤器依赖于servlet，拦截器依赖Spring MVC框架；
- 拦截器基于反射机制的，过滤器依赖于方法的回调实现；

##### 4：Listener：监听器

###### java.util Interface EventListener：事件监听器

- 所有监听器的基类
- 用于监听事件源，当有相应的事件触发时，即调用相应的方法

###### WEB中所有的监听器

- **ServletContextListener**：监听Web的启动及关闭
  - `void`  contextDestroyed (ServletContextEvent )
    - Receives notification that the ServletContext is about to be shut down
  -  voidcontextInitialized(ServletContextEvent sce)
    - Receives notification that the web application initialization process is starting
- **ServletContextAttributeListener**：监听ServletContext范围内属性的改变
  - void   attributeAdded(ServletContextAttributeEvent event)
    - Receives notification that an attribute has been added to the ServletContext
  - void   attributeRemoved(ServletContextAttributeEvent event)
    - Receives notification that an attribute has been removed from the ServletContext
  - void   attributeReplaced (ServletContextAttributeEvent event)
    - Receives notification that an attribute has been replaced to the ServletContext 
- **ServletRequestListener**：监听用户请求
- **ServletRequestAttributeListener**：监听ServletRequest范围属性的改变
- **HttpSessionListener**：监听用户session的开始及结束
- **HttpSessionAttributeListener**：监听HttpSession范围内的属性改变

###### Listener 配置：配置在Web.xml 中

- @WebListener：注解配置

- xml 配置

  ```xml
  <listener>
  	<listener-class>com.xupt.MyServletContenxtAttributeListener</listener-class>
  </listener>
  ```



