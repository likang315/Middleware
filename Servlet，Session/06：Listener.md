### Listener

------

##### 1：Listener ：监听器配置（观察者设计模式）

1. @WebListener 
2. Web.xml

```xml
<!-- 注册其监听器 -->
<web-app>
	<listener>
		<listener-class>com.xupt.WebinitListener</listener-class>
	</listener>
</web-app>
```

##### 2：Servlet Listener API : 实现其接口，重写起方法

1. Interface **ServletContextListener**	
   - 用于接收关于 ServletContext 生命周期更改的通知事件的接口	
   - void	contextDestroyed(ServletContextEvent sce) 
     - 只要 Servlet 容器，销毁 ServletContext 对象，contextDestroyed () 自动调用
   - void	contextInitialized(ServletContextEvent sce)
     - 只要 servlet 容器，实列化 ServletContext 对象，contextInitialized() 自动调用 
2. Interface **ServletContextAttributeListener**	
   - 接收关于ServletContext属性更改的通知事件的接口
   - void	attributeAdded(ServletContextAttributeEvent event) 
     - 添加属性自动调用
   - void	attributeRemoved(ServletContextAttributeEvent event) 
     - 移除属性时自动调用
   - void attributeReplaced(ServletContextAttributeEvent event)
     - 属性改变时，自动调用
3. Interface **ServletRequestListener**
   - 用于接收关于进入和超出Web应用程序范围的请求的通知事件的接口
   - void	requestDestroyed(ServletRequestEvent sre) 
     - 有 ServletRequest 超出Web应用范围时自动调用
   - void	requestInitialized(ServletRequestEvent sre) 
     - 有 ServletRequest 进入Web应用范围时自动调用
4. Interface **ServletRequestAttributeListener**
   - 接收关于ServletRequest属性更改的通知事件的接口
   - void   attributeAdded   (ServletRequestAttributeEvent srae) 
   - void   attributeRemoved  (ServletRequestAttributeEvent srae)
   - void   attributeReplaced  (ServletRequestAttributeEvent srae)
5. Interface **HttpSessionAttributeListener**
   - 用于接收关于HttpSession属性更改的通知事件的接口
   - void	attributeAdded (HttpSessionBindingEvent event) 
   - void	attributeRemoved (HttpSessionBindingEvent event) 
   - void	attributeReplaced (HttpSessionBindingEvent event) 

###### 示例：

```java
@WebListener 
public class WebInitListener implements ServletContextListener{
  @Override
	public void contextDestroyed(ServletContextEvent arg0) {
		 System.out.println(arg0.getServletContext().toString());
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		  System.out.println("------------------ServletContext init....."+arg0);
	}
}
```

