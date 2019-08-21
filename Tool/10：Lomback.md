### Lombok

------

​	Lombok的目的是减少代码的重复编写，并提供比较好的解决方案。当然也存在一些争议性的注解，可以根据实际场景进项使用

##### 1：引入依赖

```xml
dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.0</version>
    <scope>provided</scope>
</dependency>
```

##### 2：注解使用

1. @Gette、@Setter

   - 用于类或属性上
   - 会默认生成一个无参构造
   - 生成的getter遵循布尔属性的约定
     - 例如：boolean类型的sex,getter方法为 isSex 而不是 getSex

2. @Data

   - 使用在**类**上
   - 它结合了@ToString，@EqualsAndHashCode， @Getter和@Setter。本质上使用 @Data 注解，类默认@ToString和 @EqualsAndHashCode ，以及每个字段都有@Setter 和 @getter
   - 该注解也会生成一个公共构造函数，可以将任何 @NonNull 和 final字段作为参数

3. @NonNull

   - 使用在**属性**上
   - 用于属的非空检查，当放在setter方法的字段上，将生成一个空检查，如果为空，则抛出 NullPointerException 
   - 该注解会默认是生成一个无参构造

4. @toString

   - 使用在**类**上
   - 默认生成任何非讲台字段以名称-值的形式输出
   - 如果需要可以通过注释**参数 includeFieldNames** 来控制输出中是否包含的属性名称（key）
   - 可以通过**exclude参数**中包含字段名称，可以从生成的方法中排除特定字段
   - 可以通过callSuper参数控制使用调用父类的ToString（）

5. @EqualsAndHashCode

   - 使用在**类**上
   - 该注解在类级别注释会同时生成 equal 和 hashCode 方法

   - 存在继承关系需要设置 callSuper参数为 true

6. @AllArgsConstructor

   - 使用在**类**上
   - 该注解提供一个全参数的构造方法，默认不提供无参构造

7. @NoArgsConstructor

   - 使用在**类**上，该注解提供一个无参构造

8. @Value

   - 使用在**类**上
   - 生成包含所有参数的构造方法，get 方法，equals、hashCode、toString 方法，**注意：没有setter** 