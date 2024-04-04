### Data Validator（数据校验）

------

[TOC]

##### 01：概述

- Spring MVC 拥有自己独立的**数据校验框架**，同时支持 JSR 303 标准的校验框架，**用于对 web 组件的 Java Bean 中的字段的值进行验证**。 
- Spring 框架通常使用 **Validator 接口**和它的实现类来进行数据验证；

##### 02：使用方式

###### 引入 Jar 包

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

###### 注解

- 把注解标记在**需要校验的实体类的属性上或是其对应的get方法上即可，并且在 Controller 处使用 @Valid 注解；**
- @NotNull 指定此 username 字段不允许为空，当验证失败时将从之前指定的 messageSource 中，获取“username.not.empty” **对应的错误信息**，此处只有通过"{错误消息键值}"格式指定的才能从 messageSource 获取；

```java
@NotNull(message = "#{stuGlobalKey} property is null !}")
public String stuGlobalKey;
```

###### 校验

```java
@Controller
@RequestMapping("/validate")
public class HelloWorldController {
  	@RequestMapping("/hello")
    public String validate(@Valid @ModelAttribute("user") UserModel user, BindingResult errors){
      if(errors.hasErrors()) {
        // ...
        return "validate/error";
      } else {
        // ...
        return "redirect:/success";
      }
    }
}
// @Valid注解用于告诉 Spring 执行数据验证，并将验证结果存储在BindingResult中
// 如果验证失败，取错误信息，然后将错误信息添加到 errors 错误对象中
```

##### 03：约束注解

1. 验证约束注解 ( hibernate validator reference )
   - @AssertFalse：验证注解的元素值是 false
   - @AssertTrue：验证注解的元素值是 true
   - **@NotNull**：验证注解的元素值不是 null
   - @NotEmpty：检查元素是否为 Null 或 Empty
   - @Null：验证注解的元素值是 null
   - @Min(value=值)：验证注解的元素值大于等于@Min 指定的 value 值
   - @Max（value=值）：验证注解的元素值小于等于@Max 指定的 value 值
   - @Digits(integer=整数位数, fraction=小数位数) ：验证注解的元素值的整数位数和小数位数上
   - @Size(min=下限，max=上限）：验证注解的元素值的在 min 和 max（包含）指定区间之内，如字符长度、集合大小
   - @Range(min=最小值, max=最大值) ：验证数字是否在指定范围
   - @Pattern(regexp=正则表达式flag=标志的模式) ：用于验证字符串对象，验证是否符合指定的正则表达式
   - **@Valid ：**指定**递归验证**关联的对象(验证属性对象)	
2. 错误信息
   - 当验证出错时，需要向用户展示错误消息告知出错原因，因此要**为验证约束注解指定错误信息**；
   - 错误消息：通过在**验证约束注解的 message 属性指定**；
      - 硬编码错误消息 @NotNull(message = "用户名不能为空")

