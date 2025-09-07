### Swagger

------

[TOC]

##### 01：概述

- Swagger 是一套用于**设计、构建、文档化和测试 RESTful API 的开源工具集**。它基于 OpenAPI 规范，这是一种标准化的接口描述格式。Swagger 提供了一系列工具，帮助开发者更高效地开发和维护 API。
- Swagger 文档：`https://swagger.io/docs`
- OpenAPI 规范最初被称为 Swagger 规范，后来**被捐赠给 OpenAPI Initiative 并更名为 OpenAPI**。

##### 02：特性

1. **标准化 API 文档**：Swagger 提供了一种统一的格式（如 OpenAPI 规范）来描述 API，避免了文档不一致的问题。
2. **提高开发效率**：前后端开发人员可以并行工作，前端开发者无需等待后端 API 完成即可开始开发。
3. **自动化测试**：通过 Swagger UI，可以直接在浏览器中测试 API，无需额外工具。
4. **代码生成**：Swagger Codegen 可以**自动生成客户端 SDK**，减少手动编写代码的工作量。

##### 03：主要组成

- **Swagger UI**：可视化工具，可以将 OpenAPI 规范呈现为交互式 API 文档。它允许用户**直接在浏览器中查看和测试 API**。
- **Swagger Editor**：是一个基于浏览器的编辑器，用于编写 OpenAPI 规范。它提供实时预览和验证功能。
- **Swagger Codegen**：可以根据 OpenAPI 规范**生成服务器存根和客户端 SDK**，支持 40 多种语言。
- **Swagger Hub**：是一个集成的 API 设计和文档平台，提供协作功能和云存储。

##### 04：Spring Boot 集成 UI

###### 添加依赖（Swagger V3）

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>2.1.1</version>
</dependency>
```

###### 配置 Swagger

```java
@Profile("dev")
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
      return new OpenAPI()
              .info(new Info().title("xx系统API接口文档").version("1.0.0")
                      .description("API 接口详细描述")
                      .contact(new Contact().name("开发团队")                         
                               .url("https://example.com")));
  }

  /**
   * 将 API 分组，设置 API 扫描路径
   * 可以设置多个
   * @return
   */
  @Bean
  public GroupedOpenApi publicApi() {
      return GroupedOpenApi.builder()
              .group("public-apis")
              .pathsToMatch("/api/**")
              .build();
  }
}
```

###### 配置访问路径

```yaml
# application.yaml
springdoc:
  api-docs:
    path: /apis/swagger-ui/api-docs
  swagger-ui:
    path: /apis/index.html
# application-prod.yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

###### API 规范示例

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关的API")
public class UserController {
  @Operation(
      summary = "获取用户列表",
      description = "分页获取所有用户信息"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "成功"),
      @ApiResponse(responseCode = "400", description = "请求参数错误"),
      @ApiResponse(responseCode = "500", description = "服务器内部错误")
  })
  @GetMapping("/")
  public Page<User> getUsers(
          @Parameter(description = "页码", required = true) @RequestParam int page,
          @Parameter(description = "每页记录数", required = true) @RequestParam int size) {
      // 业务逻辑
      return userService.getUsers(page, size);
  }
}
```

