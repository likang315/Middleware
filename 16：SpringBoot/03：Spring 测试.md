### Spring 测试

------

[TOC]

##### 01：集成测试自动配置

- **@SpringJUnit4ClassRunner**：是一个JUint 类运行期，会为 JUnit 测试加载 Spring 应用程序上下文，并为测试类自动注入所需的Bean。

- @ContextConfiguration：指定了如何加载应用程序上下文。但是并没有完全加载SpringBoot，不会加载应用程序上下文，开启日志，外部属性等。

- **@SpringApplicationConfiguration**：完全加载SpringBoot，用于替代ContextConfiguration。

- ```java
  @RunWith(SpringJUnit4ClassRunner.class)
  @SpringApplicationConfiguration(classes=AddressBookConfiguration.class)
  public class AddressServiceTests {
      ...
  } 
  ```

##### 02：测试运行中的应用程序

- 在测试类上添加 **@WebIntegrationTest** 注解，可以声明你不仅希望 Spring Boot 为测试创建应用程序上下文，还要**启动一个嵌入式的Servlet容器**。一旦应用程序运行在嵌入式容器里，就可以发起真实的HTTP请求，断言结果了。

- ```java
  @RunWith(SpringJUnit4ClassRunner.class)
  @SpringApplicationConfiguration(classes=AddressBookConfiguration.class)
  @WebIntegrationTest
  public class SimpleWebTest {
      @Test(expected=HttpClientErrorException.class)
      public void pageNotFound() {
          try {
              RestTemplate rest = new RestTemplate();
              rest.getForObject(
                  "http://localhost:8080/bogusPage", String.class);
              fail("Should result in HTTP 404");
          } catch (HttpClientErrorException e) {
              assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
              throw e;
          }
      }
  }
  ```










