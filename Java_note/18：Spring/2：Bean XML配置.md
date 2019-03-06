### 1��Bean ��ǩ�Ķ���

##### 	Property:

1��id="������"
2��class="ʵ�����࣬����Spring������"
**3��scope= "������"**
		singleton������ģʽ,Spring IOC������ֻ�����һ�������beanʵ�����������ж�bean������ֻҪid���bean������ƥ�䣬��ֻ�᷵��bean��ͬһʵ��
		prototype��ԭ��ģʽ,ÿһ�����󣨽���ע�뵽��һ��bean�У������Գ���ķ�ʽ����������getBean()�������������һ���µ�beanʵ�����൱��һ��new�Ĳ�������¡����

4��lazy-init="true" �����أ���ʼ��������ʱ�򲻻����̼��أ���ʱ�ڼ���
5��init-method="������"     ��ʼ������֮ǰ���ô˷���
6��destory-method="������"  ���ٶ���֮ǰ���ô˷���
7��abstract="true" ����ı������̳�
8��parent="idֵ"  �̳�bean����abstract����
9��primary="true"  ��ѡ�ģ���������ͬ����ʱ������ѡ��	
10��factory-bean="ʵ��������������"  factory-method="ʵ��������ķ�����"

```xml
<bean id="aa" class="com.xzy.pojo.Tear" lazy-init="true" scope="prototype" init-method="init">
   
</bean>
```

##### 2��ʵ����Bean

###### 	1��ʹ���๹����ʵ������Ĭ��ʹ�ò��������Ĺ��췽��ʵ���� Bean ����

<bean id="exampleBean" class="com.xupt.ExampleBean"/>

###### 	2��ʹ�þ�̬��������ʵ�������÷������뷵��һ��

```XML
<bean id="������"class="com.xupt.ExampleBean" factory-method="��̬������"/>
	public class ClientService {
		private static ClientService clientService = new ClientService();
		private ClientService() {}
		public static ClientService createInstance() {
			return clientService;
		}
	}
```

###### 3��.ʹ��ʵ����������ʵ����

```xml
<bean id="serviceLocator" class="com.xupt.DefaultServiceLocator">
</bean>
<bean id="clientService" factory-bean="serviceLocator" factory-method="ʵ��������"/>
</bean>
```

##### 3������ע�루DI��

  1�����췽��ע��

###### 	  1�����������Դ������л�ȡ

```xml
<bean id="foo" class="x.y.Foo">
            <constructor-arg ref="bar"/>
            <constructor-arg ref="baz"/>
</bean>

<bean id="bar" class="x.y.Bar"/>
<bean id="baz" class="x.y.Baz"/>
```

######    2�����Կ���ͨ��type,index,name,����ֵ

```XML
    <bean id="exampleBean" class="examples.ExampleBean">
	<constructor-arg type="int" value="7500000"/>
	<constructor-arg type="java.lang.String" value="42"/>
      </bean>
��
     <bean id="exampleBean" class="examples.ExampleBean">
	<constructor-arg index="0" value="7500000"/>
	<constructor-arg index="1" value="42"/>
     </bean>
��
    <bean id="exampleBean" class="examples.ExampleBean">
	<constructor-arg name="years" value="7500000"/>
	<constructor-arg name="ultimateAnswer" value="42"/>
     </bean>
```



#####  2��Setter����ע��

<bean id="exampleBean" class="examples.ExampleBean">
	<property name="beanOne" ref="anotherExampleBean"/>
	<property name="beanTwo" ref="yetAnotherBean"/>
	<property name="integerProperty" value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.AnotherBean"/>	
<bean id="yetAnotherBean" class="examples.YetAnotherBean"/>

<bean id="mappings" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">

<property name="properties">
	<value>
		jdbc.driver.className=com.mysql.jdbc.Driver
		jdbc.url=jdbc:mysql://localhost:3306/xupt
	</value>
</property>
</bean>

##### 3��IDREF ����

##### <bean id="theTargetBean" class="..."/>
<bean id="theClientBean" class="...">
	<property name="targetName">
		<idref bean="theTargetBean" />
	</property>
</bean>

###### 4��Ref:��������֤����,ע�����bean��ʵ�� 

###### 	Idref:������֤����,ע�����string



##### 5��Inner beans

<bean id="outer" class="...">
	<property name="target">
		<bean class="com.example.Person">             <!-- this is the inner bean -->
			<property name="name" value="Fiona Apple"/>
			<property name="age" value="25"/>
		</bean>
	</property>
</bean>

##### 6���������͵�װ��

```xml
	<bean id="order" class="com.service.OrderServiceBean">
		<property name="lists">
			<list>
				<value>lihuoming</value>
			</list>
		</property>
		<property name="sets">
			<set>
				<value>set</value>
			</set>
		</property>
		<property name="maps">
			<map>
				<entry key="lihuoming" value="28"/>
			</map>
		</property>
		<property name="properties">
			<props>
				<prop key="12">sss</prop>
			</props>
		</property>
	</bean>
```

##### 7��������

<bean class="ExampleBean">
	<property name="email" value=""/>
</bean>

<bean class="ExampleBean">
	<property name="email">
		<null/>
	</property>
</bean>

##### 8���ӳٳ�ʼ��

<bean id="lazy" class="com.foo.ExpensiveToCreateBean" lazy-init="true"/>

##### 9���Զ�װ�䣨Autowiring modes��

<beans><beans />�������Զ�װ������
	

##### 10��Bean������scope�������ַ�ʽ��

```java
	@Bean
	@Scope(ConfigurableBeanFactory.Scope_PROTOTYPE) 
	Public Notepad notepad()
	{
		return new Notepad();
	}
```

```java
	@Bean
 	@Scope(��prototype��)
	Public Notepad notepad()
	{
		return new Notepad();
	}
```

```xml
<bean id="" class="" scope="prototype">
```

1��singleton ������ģʽ
2��prototype ��ԭ��ģʽ
3��request
4��session �����ﳵʱӦ��	
5��globalSession	
6��application �������Ļ���
7��websocket 

## 11��Bean ����������

1��Spring �� bean ����ʵ����
2��init-method="������"     ��ʼ������֮ǰ���ô˷���
3��Spring ��ֵ�� bean������ע�뵽 bean ��Ӧ��������
4��bean �Ѿ�׼�������� ���Ա�Ӧ�ó���ʹ���ˣ� ���ǽ�һֱפ����Ӧ���������У� ֱ����Ӧ�������ı�����
5��destory-method="������"  ���ٶ���֮ǰ���ô˷���
	


