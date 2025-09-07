### SpringBoot-单元测试

------

[TOC]

##### 01：JUnit 单元测试框架

- Java中最常用的单元测试框架。该框架提供了丰富的测试与断言方法。

###### 注解驱动

- @Test: 标记测试方法；
- @BeforeEach/@Before: 每个测试前执行；
- @AfterEach/@After: 每个测试后执行；
- @DisplayName: 自定义测试名称；

###### 断言机制

- ```java
  // 基本断言
  assertEquals(expected, actual);
  assertTrue(condition);
  assertNotNull(object);
  
  // JUnit 5 的流式断言
  assertAll(
      () -> assertEquals(2, result.getSize()),
      () -> assertEquals("John", result.getName())
  );
  ```

##### 02：Spring 集成

- @RunWith：用于指定测试类的运行环境（Runner），决定测试如何被执行。

- **SpringJUnit4ClassRunner**：为 JUnit 测试启动 Spring 应用程序上下文，并为测试类自动注入所需的Bean。

  - **SpringRuner**：别名，功能一样。Spring 4 引入的。@RunWith(SpringRunner.class)  

- @ContextConfiguration：指定了如何加载应用程序上下文。但是并没有完全加载 SpringBoot，不会加载应用程序上下文，开启日志，外部属性等。

- **@SpringApplicationConfiguration**：完全加载 SpringBoot，用于替代ContextConfiguration。

- ```java
  @RunWith(SpringJUnit4ClassRunner.class)
  @SpringApplicationConfiguration(classes=AddressBookConfiguration.class)
  public class AddressServiceTests {
      ...
  }
  ```

###### 单元测试

- SpringBootTest 的 classes 属性，用于初始化容器；

- ```java
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

##### 03：Mockito

- Java 模拟框架，用于单元测试中**创建和配置模拟对象**(mock objects)。
- 不需要启动 Spring 容器，自动初始化 @Mock 注解的对象，自动执行 @InjectMocks 依赖注入。

###### 与 Mockito 结合

- @RunWith(MockitoJUnitRunner.class)：是Junit中的一个注解，用来指定测试运行环境(MockitoJUnitRunner)。

###### @`Mock` 

- 用于创建模拟对象（Mock Object），替代被测试类（SUT，System Under Test）所依赖的真实对象（如外部服务、DAO 层等）。

###### @InjectMocks

- 自动将 `@Mock` 或 `@Spy` 创建的模拟对象**注入到被测试类的实例中**，解决其依赖关系。

