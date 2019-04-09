### DataSource��Spring ���ṩ�� 5 �ֲ�ͬ������Դ

spring �Դ�������Դ (DriverManagerDataSource)��druid��JNDI���ݣ�DBCP ����Դ��C3P0 ����Դ

##### 1��DriverManagerDataSource

```xml
<bean id="dataSource"  class="org.springframework.jdbc.datasource.DriverManagerDataSource">
		<property name="driverClassName" value="mysql.jdbc.driver.MysqlDriver"/>
		<property name="url" value=""jdbc:mysql://localhost:3306/xupt" />
		<property name="username" value="root" />
		<property name="password " value="mysql" />
</bean>
```

##### 2��druid 

Druid ���ӳؼ������ spring �������£�

```xml
<bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
	<!-- �������� url��user��password -->
	<property name="url" value="${jdbc_url}" />
	<property name="username" value="${jdbc_user}" />
	<property name="password" value="${jdbc_password}" />

	<!-- ���ó�ʼ����С����С����� -->
	<property name="initialSize" value="1" />	
	<property name="minIdle" value="1" />
	<property name="maxActive" value="20" />

---------------------------------------------------------------------------------------------
    <������������>
        
<!-- ���û�ȡ���ӵȴ���ʱ��ʱ�� -->
<property name="maxWait" value="60000" />

<!-- ���ü����òŽ���һ�μ�⣬�����Ҫ�رյĿ������ӣ���λ�Ǻ��� -->
<property name="timeBetweenEvictionRunsMillis" value="60000" />

<!-- ����һ�������ڳ�����С�����ʱ�䣬��λ�Ǻ��� -->
<property name="minEvictableIdleTimeMillis" value="300000" />

<!-��������Ƿ���Ч��sql,Ҫ����һ����ѯ���,���validationQueryΪnull,testOnBorrow,testOnReturn,testWhileIdle����������-->
		<property name="validationQuery" value="SELECT 'x'" />
		<!-- ��������ʱ��ִ��validationQuery��������Ƿ���Ч�������ûή������ -->
		<property name="testOnBorrow" value="false" />
		<!-- �黹����ʱִ�У�validationQuery��������Ƿ���Ч�������ûή������-->
		<property name="testOnReturn" value="false" />
		<!-- ��������ʱ��⣬�������ʱ�����timeBetweenEvictionRunsMillis��ִ��validationQuery��������Ƿ���Ч-->
		<property name="testWhileIdle" value="true" />
	

<!-- �� PSCache������ָ��ÿ�������� PSCache �Ĵ�С -->
<property name="poolPreparedStatements" value="true" />
<property name="maxPoolPreparedStatementPerConnectionSize" value="20" />

<!-- ���ü��ͳ�����ص� filters��ȥ�����ؽ��� sql �޷�ͳ�� -->
<property name="filters" value="stat" />
        
</bean>
```




```xml
��ص����ã�web.xml
<span style="white-space:pre"> </span>
<filter>
	<filter-name>DruidWebStatFilter</filter-name>
	<filter-class>com.alibaba.druid.support.http.WebStatFilter</filter-class>
	<init-param>
		<param-name>exclusions</param-name>
		<param-value>*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*</param-value>
	</init-param>
</filter>
<filter-mapping>
	<filter-name>DruidWebStatFilter</filter-name>
	<url-pattern>/</url-pattern>
</filter-mapping>
```

filter ���Լ�� webURl ����

```xml
<span style="white-space:pre"> </span>
<servlet>
	<servlet-name>DruidStatView</servlet-name>
	<servlet-class>com.alibaba.druid.support.http.StatViewServlet</servlet-class> 
</servlet>
<servlet-mapping>
	<servlet-name>DruidStatView</servlet-name>
	<url-pattern>/druid/*</url-pattern>
</servlet-mapping>
```

�����ÿ��Է��ʼ�ؽ��棬���úú󣬷��� http://ip:�˿ں�/��Ŀ��/druid/index.html ���ɼ�����ݿ��������


