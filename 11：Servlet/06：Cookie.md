### 单点登录

------

[TOC]

##### 01：Class Cookie

- javax.servlet.http
- Cookie 信息通过 key-value 存储的，**服务器把客户端状态信息写到客户端硬盘上的一种技术**；

###### Cookie 特性

- 主要内容：名字，值，过期时间，路径和域；
- 路径：就是跟在域名后面的URL路径，存储到本地什么地方；
- 作用范围：路径与域合在一起就构成了 Cookie 的作用范围；
- **过期时间：**默认是使用会话Cookie；
  - **会话Cookie：**如果不设置过期时间，则表示这个 Cookie 的生命周期为**浏览器会话期间**，只要关闭浏览器窗口，Cookie 就销毁了，会话 Cookie 一般不存储在硬盘上而是保存在内存里；
  - 如果设置了过期时间，浏览器就会把 Cookie 保存到硬盘上，关闭后再次打开浏览器，这些 **Cookie 仍然有效直到超过设定的过期时间**；
  - 若要删除原本存在的Cookie，则再次新建一个同名的 Cookie，并设置其过期时间为0，表示浏览器立即删除当前及之前保存的同名Cookie；

###### 方法： 

- Cookie (java.lang.String name, java.lang.String value) 
  - 构造 Cookie，传入key和value
- int  getMaxAge() ：得到生命周期
- java.lang.String`  `**getDomain()：获取Cookie 的作用域**
- java.lang.String	getName() ：返回 Cookie 名
- java.lang.String   getValue （） ：获取Cookie 的值 
- java.lang.String	getPath() ：得到访问Cookie 对应的绝对路径
- void	setMaxAge(int expiry) ：得知Cookie的生命周期（秒 为单位）
- void	setPath(java.lang.String uri) ：设置路径

######   HttpResponse

- void addCookie (Cookie cookie) ：在报头中添加 Cookie (Set-Cookie 属性)


######   HttpResquest

- Cookie[]	getCookies()：得到Cookie 数组，通过 Cookie 名来筛选出Cookie


```Java
// 添加cookie，若已有 Cookie 是 map 存储
Cookie cookie = new Cookie("user_name",uname);
cookie.setMaxAge(60 * 60 * 24 * 7);
cookie.setPath("/");
resp.addCookie(cookie);
```

##### 02：Session

- HTTP 通过**令牌的机制**完成和客户端的会话，是基于 Cookie 技术实现的，**存储在服务器中的文件系统**或者数据库中；


###### 原理：

- 客户端会根据从服务器端发送的**响应报文内一个Set-Cookie的首部字段信息通知客户端保存此次 Cookie，**当下次客户端再往该相同的服务器发送请求时客户端会自动在请求报文中**加入Cookie首部字段，值 SessionID 后发送出去**，服务器会检查是哪一个客户端发送的请求，然后对比服务器上的记录，得到客户的状态信息（Session）；


###### Session 的存储

- 一般情况下，Session 都是存储在内存里，当服务器进程被停止或者重启的时候，内存里的session也会被清空，**如果设置了 session 的持久化特性，服务器就会把 session 保存到硬盘上**，当服务器进程重新启动或这些信息将能够被再次使用；


##### 03：Interface HttpSession：本质也是一个Map存储

- java.lang.Object getAttribute (java.lang.String name) ：通过Session的key 得到其value 

- void setAttribute(java.lang.String name, java.lang.Object value) 

- int getMaxInactiveInterval() ：得到 Session 在服务器的生命周期

- void setMaxInactiveInterval(int interval)：设置Session的生命周期，秒为单位

- boolean isNew() ：判断是不是新建的

- java.lang.String   getId() ：得到Session的ID

- long	getCreationTime() ：得到Session的创建时间，返回值得用Date包装

-  long getLastAccessedTime() ：返回最后一次访问的时间，Date包装

- void **invalidate**() ：销毁Session

######  HttpServletRequest

- HttpSession getSession() ：得到HttpSession 对象，如果对象没有则创建一个并返回；


```java
// 重写 Session，验证用户登录
HttpSession session = req.getSession();
// 设置属性来控制直接访问成功页面
session.setAttribute("loged", uname);
```

##### 04：单点登录（SSO）

- 在多个系统中，**用户只需一次登录**，各个系统即可感知该用户已经登录；
- SSO服务：多系统即有多个Tomcat，而Session是依赖当前系统的Tomcat，所以系统A的Session和系统B的Session是**不共享**的，因此把**Session 数据存入到 redis 中**，即可实现单点登录；

##### 05：统计网站的访问量

- 统计网站的访问量（PV）
- 统计用户的访问量（UV）
- 统计某个用户的访问量（JsessionID）

##### 06：Cookie 禁用

- 如果浏览器禁用 cookie ，那么 session 失效，浏览发的所有请求中都不会带 cookie 请求报头，服务器认为每个请求都是一个新的请求，不能保存会话状态；

###### 解决方式

1. 重写URL：

   - http://...../xx?jsessionid=ByOK3vjFD75aPnrF7C2Hmdn

2. 表单隐藏字段：浏览器会自动修改表单，添加一个隐藏字段，以便在表单提交时能够把session id传递回服务器

   - ```xml
     <input type="hidden" name="jsessionid" 	value="ByOK3vjFD75aPnrF7C2Hmdn"> 
     ```

##### 07：验证码

- 由程序随机生成字母、数字或汉字的图片，并将图片上的**随机内容记在 session 中**，让用户看到图片后将随机值填写在表单项中提交给服务器，服务端程序将用户填写的信息与 Session 中的信息进行对比，关键在于不让能程序自动识辨出图片上的信息，所以一般我们都加干扰线、点，或旋转、缩放等。



