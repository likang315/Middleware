## Spring MVC ��ͼ������

##### 1����ͼ��������

   Spring MVC ���Ƶ���Դ��������ʱ���������󶼻ᱻ DispatcherServlet �������� Spring ���������һ��**HandlerMapping**���������������ӳ���д��ڶԸ������������ӳ��,Ȼ��ͨ����HandlerMapping ȡ�����Ӧ�� **Handler(�������ӳ��)**��������ͨ����Ӧ�� **HandlerAdapter ����� Handler**������֮��᷵��һ�� **ModelAndView** ����(�߼���ͼ��),���ModelAndView ����֮��Spring ����Ҫ�Ѹ� View ��Ⱦ�󷵻ظ��û��������ظ������

   �������Ⱦ�Ĺ����У��������õľ���ViewResolver �� View���� Handler ���ص� ModelAndView �в�������������ͼ��ֻ����һ���߼���ͼ���Ƶ�ʱ�򣬸����߼���ͼƥ����Ӧ��ViewResolver���������Ϊ��������ͼ View����,View������ͼ��Ⱦ���ѽ�����ظ������

##### 2��ViewResolver �� View��Spring MVC ������ͼ����Ҫ�������ӿ�

ViewResolver����һ���߼��ϵ���ͼ���ƽ���Ϊһ����������ͼ��ֻ�ǰ��߼���ͼ���ƽ���Ϊ�����View����
View �����ڴ�����ͼ��Ȼ�󷵻ظ��ͻ���

##### 3����ͼ������(ViewResolver)

###### AbstractCachingViewResolver(�����֧࣬����ͼ�����)

�������������������ͼ����������Ȼ��ÿ��Ҫ������ͼ��ʱ���ȴӻ��������ң�����ҵ��˶�Ӧ����ͼ��ֱ�ӷ��أ����û�оʹ���һ���µ���ͼ����Ȼ������ŵ�һ�����ڻ���� map �У������ٰ��½�����ͼ���أ���������

###### UrlBasedViewResolver

�� �Ƕ� ViewResolver �� һ �� �� �� ʵ �� �� ���Ҽ̳���AbstractCachingViewResolver����Ҫ�����ṩ��һ��ƴ�� URL
�ķ�ʽ��������ͼ��������ͨ�� prefix ����ָ��һ��ָ����ǰ׺��ͨ�� suffix ����ָ��һ��ָ���ĺ�׺��Ȼ��ѷ��ص��߼���ͼ���Ƽ���ָ����ǰ׺�ͺ�׺����ָ������ͼ URL��Ĭ�ϵ� prefix �� suffix ���ǿմ���֧�ַ��ص���ͼ�����а���
redirect:ǰ׺(RedirectView) ,forword:ǰ׺( InternalResourceView )

ʹ�� UrlBasedViewResolver ��ʱ�����ָ������**viewClass**����ʾ������������ͼ��һ��ʹ�ý϶�ľ��� InternalResourceView
��������չ�� jsp�����ǵ�����ʹ�� JSTL ��ʱ�����Ǳ���ʹ�� JstlView

   �����ͼ�����������ͼ��Ⱦ����������ͨ���� order ������ָ���� ViewResolver ������������λ�ã�order ��ֵԽС���ȼ�Խ��

```xml
xml����ViewResolver
<bean class="org.springframework.web.servlet.view.UrlBasedViewResolver">
		<property name="prefix" value="/WEB-INF/" />
		<property name="suffix" value=".jsp" />
		<property name="viewClass" value="org.springframework.web.servlet.view.InternalResourceView"/>
		<property name="order" value="1"/>
</bean>
```



###### InternalResourceViewResolver��URLBasedViewResolver �����࣬����URLBasedViewResolver ֧�ֵ���������֧��(���)

   InternalResourceViewResolver(�ڲ���Դ��ͼ������)�ѷ��ص���ͼ���ƶ�����ΪInternalResourceView ���󣬿��Խ�������View
   InternalResourceView����� Controller�������������ص�ģ�����Զ���ŵ���Ӧ��request�����У�Ȼ��ͨ��RequestDispatcher  �ڷ������˰����� forward �ض���Ŀ�� URL

```xml
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
	<property name="prefix" value="/WEB-INF/"/>
	<property name="suffix" value=".jsp" />
</bean>
```

##### 4��FreeMarkerViewResolver �� VolocityViewResolver 

������ͼ���������̳��� UrlBasedViewResolver ,FreeMarkerViewResolver ��� Controller ���������ص��߼���ͼ����ΪFreeMarkerView���� VolocityViewResolver ��ѷ��ص��߼���ͼ����Ϊ VolocityView

###### FreeMarkerViewResolver�����ᰴ�� UrlBasedViewResolver ƴ�� URL �ķ�ʽ������ͼ·���Ľ���,������ʹ�� FreeMarkerxxxʱ������Ҫ����ָ���� viewClass����Ϊ FreeMarkerViewResolver ���Ѿ���viewClass�涨ΪFreeMarkerView

```xml
 <bean class="org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver">
	<property name="prefix" value="fm_"/>
	<property name="suffix" value=".ftl"/>
	<property name="order" value="1"/>
</bean>
```

   ����FreeMarkerView ������Ҫ����һ�� FreeMarkerConfig �� bean ���������� FreeMarker ��������Ϣ
   FreeMarkerConfig ��һ���ӿڣ�Spring �Ѿ�Ϊ�����ṩ��һ��ʵ���࣬FreeMarkerConfigurer�����ǿ���ͨ���� SpringMVC �������ļ����涨��� bean ����������FreeMarker ��������Ϣ����������Ϣ������ FreeMarkerView ������Ⱦ��ʱ��ʹ��

######  ����FreeMarkerConfigurer ���ԣ���򵥵����þ�������һ�� templateLoaderPath������ SpringӦ�õ�����Ѱ�� FreeMarker��ģ���ļ������ templateLoaderPath Ҳ֧��ʹ�á�classpath:���͡�file:��ǰ׺��

 �� FreeMarker ��ģ���ļ����ڶ����ͬ��·�������ʱ�����ǿ���ʹ��templateLoaderPaths ������ָ�����·�� 

```xml
<bean class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
	<property name="templateLoaderPath" value="/WEB-INF/freemarker/template"/>
</bean>
```

##### 5��ViewResolver ��

��SpringMVC�п���ͬʱ������ViewResolver��ͼ��������Ȼ�����ǻ����һ�� ViewResolver ������ Controller ��������������һ���߼���ͼ���ƺ�ViewResolver ������������ViewResolver �����ȼ������д���