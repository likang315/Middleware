### Spring AOP（Aspect Oriented Programming）

------

[TOC]

##### 01：概述

- 面向切面编程，在不修改原类的基础上，**动态的添加一些逻辑代码（动态代理）**，应用于多处的功能被称为AOP；

##### 02：AOP 相关术语

1. **连接点（Joinpoint）**
   - 一个类或一段程序执行的某个特定的位置，这个特定的位置就称为"连接点"
   - Spring  仅支持方法的连接点，即仅能在方法**调用前**、方法**调用后**、方法**抛出异常时**这些程序执行点织入增强；
   
2. **切点（Pointcut ）**
- 每个程序类都拥有多个连接点，AOP通过 "切点" 定位特定的连接点，切点和连接点不是一对一的关系，**一个切点可以匹配多个连接点**
3. **增强（Advice）**
- 是织入到目标类连接点上的一段程序代码；
4. 引介（Introduction）

   - 引介是一种特殊的增强，它为类添加一些属性和方法
5. 目标对象（Target）

   - 增强逻辑的织入目标类
6. **织入（Weaving ）**
   - 将增强添加到目标类具体连接点的过程，**Spring 采用动态代理织入，而AspectJ 采用编译期织入和类装载器织入**
   - 三种织入的方式：
     1. 编译期织入，这要求使用特殊的 Java 编译器
     2. 类装载器织入，这要求使用特殊的类装载器
     3. 动态代理织入，在运行期为目标类添加增强生成子类的方式
7. **切面（Aspect ）**
   - 切面由切点和增强组成类
   - Spring AOP  就是负责实施切面的框架，它将切面所定义的横切逻辑程序织入到切面所指定的连接点中；
##### 03：Spring 与 AspectJ 的关系

- Spring 和 AspectJ 项目之间有大量的协作，而且对 AOP 的支持在很多方面都借鉴了AspectJ 项目，需要导入的jar包：aspectjrt，aspectjweaver，cglib


##### 04：启用 AspectJ 

```java
// springboot 自动配置

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {
  
}
// 通过注解，启用aop
<aop:aspectj-autoproxy />
```

##### 05：基于注解的AOP【重要】

- 首先配置@Aspect
  - @Before：方法执行前
  - @Around：环绕
  - @After  ：方法执行后
  - @AfterReturning：方法返回后
  - @AfterThrowing：方法抛出异常后
- 当多个Aspect 作用到一个切点时，指定Aspect 的执行顺序
  - 实现 org.springframework.core.Ordered 接口，重写 getOrder() 方法；
  - 添加@Order注解，该注解全称为：org.springframework.core.annotation.Order
    - 值越小，优先级越高；
  

```java
// 定义切面类
import org.aspectj.lang.annotation.Aspect;

@Aspect
@EnableAspectJAutoProxy
@Component
public class Logging {
   // 定义一个空方法作为切点，使用Pointcut定义切点时，可以使用&&、||、! 这三个运算
   @Pointcut("execution(* com.xupt.service.*(..))")
   private void selectAll(){}
  
   @Before("selectAll()")
   public void beforeAdvice(){
      System.out.println("Going to setup service profile.");
   }
  
   @After("selectAll()")
   public void afterAdvice(){
      System.out.println("service profile has been setup.");
   }
   // retVal 指切点方法返回的值 
   @AfterReturning(pointcut = "selectAll()", returning="retVal")
   public void afterReturningAdvice(Object retVal){
      System.out.println("Returning:" + retVal.toString() );
   }
   
   @AfterThrowing(pointcut = "selectAll()", throwing = "ex")
 	 public void AfterThrowingAdvice(IllegalArgumentException ex){
      System.out.println("There has been an exception: " + ex.toString());   
   }
  
   @Around("selectAll()")
	 public void AroundTask(ProceedingJoinPoint invocation){
 		System.out.println("已经记录下操作日志@Around 方法执行前");
        invocation.proceed();
        System.out.println("已经记录下操作日志@Around 方法执行后");
   }
}
```

##### 06：基于xml文件的配置

-  一个 **aspect** 是使用元素声明的，支持的 bean 是使用 **ref** 属性引用的


```xml
<aop:config>
   <aop:aspect id="myAspect" ref="Aspect">
      <aop:pointcut id="businessService"
         			expression="execution(* com.xyz.myapp.service.*.*(..))"/>
      <!-- a before advice definition -->
      <aop:before pointcut-ref="businessService" method="doRequiredTask"/>
      <!-- an after advice definition -->
      <aop:after pointcut-ref="businessService" method="doRequiredTask"/>
      <!-- an after-returning advice definition -->
      <!--The doRequiredTask method must have parameter named retVal -->
      <aop:after-returning pointcut-ref="businessService" returning="retVal"
         									 method="doRequiredTask"/>
      <!-- an after-throwing advice definition -->
      <aop:after-throwing pointcut-ref="businessService"
         									throwing="ex" method="doRequiredTask"/>
      <!-- an around advice definition -->
      <aop:around pointcut-ref="businessService" method="doRequiredTask"/>
   </aop:aspect>
</aop:config>
```

##### 07：定义切点【重要】

###### 定义切点的方式

- execution：一般用于指定方法的执行，用的最多；
- @args：当执行的**方法参数**类型匹配时生效。
- @annotation：当执行的方法上拥有**指定的注解时**生效。

###### execution()：用于匹配切点的执行方法，定义方式

- execution(modifiers-pattern? ret-type-pattern declaring-type-pattern? name-pattern(param-pattern)throws-pattern?) 
  - 访问修饰符匹配（modifier-pattern?）
  - 返回值匹配（ret-type-pattern）可以为*表示任何返回值,全路径的类名等
  - 类路径匹配（declaring-type-pattern?）
  - 方法名匹配（name-pattern）可以指定方法名 或者 代表所有, `set*` 代表以set开头的所有方法；
  - 参数匹配（(param-pattern)）可以指定具体的参数类型，多个参数间用“,”隔开，各个参数也可以用" * " 来表示匹配任意类型的参数，可以用(…)表示零个或多个任意参数；
  - 异常类型匹配（throws-pattern?）
  - ? ：代表该选项是可选的；


```java
Around("execution(public * *(..))") // 任意 public 的方法，访问权限修饰符可有可无
// 任意以 set 开始的方法
execution(* set*(..))  
execution(* com.xyz.service.AccountService.*(..)) // AccountService 接口下的任意方法
Before("execution(* com.xyz.service..*.*(..))") // service 任意子包下所有类和方法
```

##### 08：处理增强中的参数

```java
@Before("com.xupt.dataAccessOperation(int) && args(account)")
public void validateAccount(int account) {
	// 在运行此dataAccessOperation(int)时，先把他的参数传给account执行
}
```

##### 09：AOP API

###### ProceedingJoinPoint 【连接点对象】

- 作用：任何一个增强方法都可以通过**将第一个入参声明为 JoinPoint**，访问到切点上下文的信息；

- ProceedingJoinPoint 继承了JoinPoint接口

  - 它新增了两个用于执行连接点方法的方法；
  - proceed() throws java.lang.Throwable：通过反射执行目标对象的连接点处的方法； 
  - proceed(java.lang.Object[] args) throws java.lang.Throwable：通过反射执行目标对象连接点处的方法，不过**使用新的入参替换原来的入参**。 

- JoinPoint 接口的内部接口 **StaticPart**

  ```java
  public interface JoinPoint {
  		// 连接点所在位置的相关信息
    	String toString();
  		// 连接点所在位置的简短相关信息
      String toShortString();
  		// 连接点所在位置的全部相关信息
      String toLongString();
  		// 返回AOP代理对象，也就是com.sun.proxy.$Proxy18
      Object getThis();
  		// 返回目标对象，一般我们都需要它
      Object getTarget();
  		// 返回被通知方法参数列表
      Object[] getArgs();
  		// 返回当前连接点签名
      Signature getSignature();
  		// 返回连接点方法所在类文件中的位置 
      SourceLocation getSourceLocation();
  		// 连接点类型
      String getKind();
  		// 返回连接点静态部分
      JoinPoint.StaticPart getStaticPart();
      public interface StaticPart {
          Signature getSignature();
          SourceLocation getSourceLocation();
          String getKind();
          int getId();
          String toString();
          String toShortString();
          String toLongString();
      }
  }
  
  public interface ProceedingJoinPoint extends JoinPoint {
      public Object proceed() throws Throwable;
      public Object proceed(Object[] args) throws Throwable;
  }
  ```


###### Signature 签名

- MethodSignature
- FieldSignature
- ConstructorSignature
  - 通过这些接口可以**得到代理的Type和Name**；

```java
public interface Signature {
    String toString();
    String toShortString();
    String toLongString();
    String getName();
    int getModifiers();
    Class getDeclaringType();
    String getDeclaringTypeName();
}
```

###### 示例

- 注意：AOP 基于代理模式，只能拦截代理对象调用的方法；

```java
@Around("@annotation(com.xupt.base.annotation.ElapsedTime)")
public Object elapsedTime(ProceedingJoinPoint joinPoint) throws Throwable {
    Signature signature = joinPoint.getSignature();
    // 两个接口属于平等的关系...
    if (!(signature instanceof MethodSignature) {
        return joinPoint.proceed();
    } else {
        Method method = ((MethodSignature) signature).getMethod();
        ElapsedTime annotation = method.getAnnotation(ElapsedTime.class);
        String monitorName =  Objects.nonNull(annotation) 
          && StringUtils.isNotEmpty(annotation.value()) 
          ? annotation.value()
          : String.format("%s.%s", method.getDeclaringClass().getSimpleName(), method.getName());
        Stopwatch stopwatch = Stopwatch.createStarted();
        Object result = joinPoint.proceed();
        stopwatch.stop();
        log.info(monitorName, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }
}
```







