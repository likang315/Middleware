### SpringBoot 单元测试

------

[TOC]

##### 01：集成测试自动配置

- **@SpringJUnit4ClassRunner**：是一个JUint 类运行期，会为 JUnit 测试加载 Spring 应用程序上下文，并为测试类自动注入所需的Bean。

- @ContextConfiguration：指定了如何加载应用程序上下文。但是并没有完全加载 SpringBoot，不会加载应用程序上下文，开启日志，外部属性等。

- **@SpringApplicationConfiguration**：完全加载 SpringBoot，用于替代ContextConfiguration。

- ```java
  @RunWith(SpringJUnit4ClassRunner.class)
  @SpringApplicationConfiguration(classes=AddressBookConfiguration.class)
  public class AddressServiceTests {
      ...
  } 
  ```

##### 02：单元测试

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
