### Spring DI：依赖注入

------

[TOC]

##### 01：依赖注入

- Spring框架的核心功能之一就是通过依赖注入的方式来**管理 Bean 之间的依赖关系**。

##### 02：Spring 的注入方式【重要】

###### 构造器注入

- 当**容器调用带有一组参数的类构造方法**时，基于构造方法的 DI 就完成了，其中每个参数代表一个对其他类的依赖；

- ```java
  public class A {
      private B dependency;
      public B (B dependency) {
          this.dependency = dependency;
      }
      // other methods
  }
  ```

###### Setter 方法注入

- 当容器调用一个**无参的构造函数**或一个**无参的静态 factory 方法**来初始化你的 bean 后，**容器会调用 Setter 方法**，因此基于Setter 方法的注入就完成了。

- ```java
  public class A {
      private B dependency;
      public void setDependency(B dependency) {
          this.dependency = dependency;
      }
      // other methods
  }
  ```

###### 注解注入

- 通过在Bean类中使用`@Autowired`或`@Resource`注解**直接标记需要注入的字段**，Spring容器会自动将这些字段注入到Bean中；

###### XML 注入

- 通过XML配置文件来实现;

- ```xml
  <bean id="foo" class="x.y.Foo">
    <constructor-arg ref="bar"/>
  </bean>
  <bean id="bar" class="x.y.Bar"/>
  
  <bean id="exampleBean" class="examples.ExampleBean">
    <property name="beanOne" ref="anotherExampleBean"/>
    <property name="integerProperty" value="1"/>
  </bean>
  <bean id="anotherExampleBean" class="examples.AnotherBean"/>
  ```


##### 03：Inner beans

- 在其他 bean 的范围内定义的 bean，被称为**内部bean**；
- 使用内部 bean 为基于 setter 方法注入进行配置的配置文件 Beans.xml；

```xml
<bean id="textEditor" class="com.tutorialspoint.TextEditor">
	<property name="spellChecker">
		<bean id="spellChecker" class="com.tutorialspoint.SpellChecker"/>
	</property>
</bean>
```

##### 04：Bean 属性为集合类型的配置

- Spring 提供了四种类型的集合的配置元素，如下所示：


| 元素    | 描述                                                        |
| ------- | ----------------------------------------------------------- |
| <list>  | 它有助于连线，如注入一列值，允许重复。                      |
| <set>   | 它有助于连线一组值，但不能重复。                            |
| <map>   | 它可以用来注入名称-值对的集合，其中名称和值可以是任何类型。 |
| <props> | 它可以用来注入名称-值对的集合，其中名称和值都是字符串类型。 |

```xml
<bean id="javaCollection" class="com.tutorialspoint.JavaCollection">
  <!-- results in a setAddressList(java.util.List) call -->
  <property name="addressList">
    <list>
      <value>INDIA</value>
      <!-- Bean 引用的注入-->
      <ref bean="address2"/>
    </list>
  </property>

  <!-- results in a setAddressSet(java.util.Set) call -->
  <property name="addressSet">
    <set>
      <value>INDIA</value>
      <ref bean="address2"/>
    </set>
  </property>

  <!-- results in a setAddressMap(java.util.Map) call -->
  <property name="addressMap">
    <map>
      <entry key="1" value="INDIA"/>
      <entry key="2" value="Pakistan"/>
      <entry key="3" value-ref="address2"/>
    </map>
  </property>

  <!-- results in a setAddressProp(java.util.Properties) call -->
  <property name="addressProp">
    <props>
      <prop key="one">INDIA</prop>
      <prop key="two">Pakistan</prop>
      <prop key="three">USA</prop>
    </props>
  </property> 
</bean>
```

##### 05：注入 null 和空字符串的值

```xml
<bean id="ID" class="ExampleBean">
	<property name="email" value=""/>
</bean>

<bean id="ID" class="ExampleBean">
	<property name="email">
		<null/>
	</property>
</bean>
```

##### 06：Bean 的自动装配方式【重要】

- Spring 容器可以在不使用`<constructor-arg>`和`<property>` 元素的情况下，**自动装配**相互协作的 bean 之间的关系，这有助于减少编写一个大的基于 Spring 的应用程序的 XML 配置的数量。


| 模式            | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| **byName**      | 由**属性名**自动装配。Spring 容器看到在 XML 配置文件中 bean 的自动装配的属性设置为 byName时，然后尝试匹配，并且将它的属性与在配置文件中被定义为相同名称的 beans 的属性进行连接。 |
| **byType**      | 由**属性数据类型**自动装配。Spring 容器看到在 XML 配置文件中 bean 的自动装配的属性设置为 byType时，然后查看如果它的**类型**匹配配置文件中的一个确切的 bean 名称，它将尝试匹配和连接属性的类型。**如果存在不止一个这样的 bean，则一个致命的异常将会被抛出**；设置为首选或者名称注入； |
| **constructor** | 类似于 byType，但该类型适用于构造函数参数类型。如果在容器中没有一个构造函数参数类型的 bean，则一个致命错误将会发生。 |

```xml
<!--手动装配--->
<bean id="user" class="twm.demo.User">
  <property name="username" value="Yanglan"/>
</bean>
<!-- 自动装配 -->
<bean id="account" class="twm.demo.Account"/>
<bean id="user" class="twm.demo.User" autowire="byName">
  <property name="username" value="Yanglan"/>
</bean>
<!--注入account属性时，并不关心bean id，而是查找容器中是否有类型为twm.demo.Account的bean-->
<bean id="ac_type" class="twm.demo.Account"/>
<bean id="user" class="twm.demo.User" autowire="byType">
  <property name="username" value="Yanglan"/>
</bean>

<bean id="yanglan" class="twm.demo.User" autowire="constructor"/>
```
