### MyBatis-Plus

------

[TOC]

##### 01：概述

- 是一个 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，只需简单配置，可快速进行单表 CRUD 操作。

###### 特点

1. **无侵入**：只做增强不做改变，引入它不会对现有工程产生影响，如丝般顺滑；
2. **损耗小**：启动即会自动注入基本 CURD，性能基本无损耗，直接面向对象操作；
3. **强大的 CRUD 操作**：内置通用 Mapper、通用 Service，仅仅通过少量配置即可实现单表大部分 CRUD 操作；
4. **支持 Lambda 形式调用**：通过 Lambda 表达式，方便的编写各类查询条件，无需再担心字段写错；
5. **支持主键自动生成**：支持多达 4 种主键策略（内含分布式唯一 ID 生成器 - Sequence），可自由配置，完美解决主键问题；
6. **内置分页插件**：基于 MyBatis 物理分页，开发者无需关心具体操作，配置好插件之后，写分页等同于普通 List 查询；
7. **支持 ActiveRecord 模式**：支持 ActiveRecord 形式调用**，实体类只需继承 Model 类即可进行强大的 CRUD 操作**；
8. **内置代码生成器**：采用代码可快速生成 Mapper 、 Model 、 Service 、 Controller 层代码，支持模板引擎；
9. **支持自定义全局通用操作**：支持全局通用方法注入（ Write once, use anywhere ）；

##### 02：Service Interface

- IService：是 MyBatis-Plus 提供的一个通用顶级 Service 层接口，它封装了常见的 CRUD 操作，包括插入、删除、查询和分页等。通过**继承 IService 接口，可以快速实现对数据库的基本操作**，同时保持代码的简洁性和可维护性。
- 命名规范：get 用于查询单行，remove 用于删除，list 用于查询集合，page 用于分页查询

###### save API

- ```java
  // 插入一条记录（选择字段，策略插入）
  boolean save(T entity);
  // 插入（批量）
  boolean saveBatch(Collection<T> entityList);
  // 插入（批量）
  boolean saveBatch(Collection<T> entityList, int batchSize);
  ```

###### saveOrUpdate API

- 根据实体**对象的主键 ID 进行判断**，存在则更新记录，否则插入记录。

- ```java
  // TableId 注解属性值存在则更新记录，否插入一条记录
  boolean saveOrUpdate(T entity);
  // 根据updateWrapper尝试更新，否继续执行saveOrUpdate(T)方法
  boolean saveOrUpdate(T entity, Wrapper<T> updateWrapper);
  // 批量修改插入
  boolean saveOrUpdateBatch(Collection<T> entityList);
  // 批量修改插入
  boolean saveOrUpdateBatch(Collection<T> entityList, int batchSize);
  ```

###### remove API

- ```java
  // 根据 queryWrapper 设置的条件，删除记录
  boolean remove(Wrapper<T> queryWrapper);
  // 根据 ID 删除
  boolean removeById(Serializable id);
  // 根据 columnMap 条件，删除记录
  boolean removeByMap(Map<String, Object> columnMap);
  // 删除（根据ID 批量删除）
  boolean removeByIds(Collection<? extends Serializable> idList);
  ```

###### update API

- ```java
  // 根据 UpdateWrapper 条件，更新记录 需要设置sqlset
  boolean update(Wrapper<T> updateWrapper);
  // 根据 whereWrapper 条件，更新记录
  boolean update(T updateEntity, Wrapper<T> whereWrapper);
  // 根据 ID 选择修改
  boolean updateById(T entity);
  // 根据ID 批量更新
  boolean updateBatchById(Collection<T> entityList);
  // 根据ID 批量更新
  boolean updateBatchById(Collection<T> entityList, int batchSize);
  ```

###### get API

- ```java
  // 根据 ID 查询
  T getById(Serializable id);
  // 根据 Wrapper，查询一条记录。结果集，如果是多个会抛出异常，随机取一条加上限制条件 wrapper.last("LIMIT 1")
  T getOne(Wrapper<T> queryWrapper);
  // 根据 Wrapper，查询一条记录，有多个记录是否抛出异常
  T getOne(Wrapper<T> queryWrapper, boolean throwEx);
  // 根据 Wrapper，查询一条记录
  Map<String, Object> getMap(Wrapper<T> queryWrapper);
  // 根据 Wrapper，查询一条记录，通过 mapper 可以转换成自己想获取的数据
  <V> V getObj(Wrapper<T> queryWrapper, Function<? super Object, V> mapper);
  ```

###### list API

- ```java
  // 查询所有
  List<T> list();
  // 查询列表
  List<T> list(Wrapper<T> queryWrapper);
  // 查询（根据ID 批量查询）
  Collection<T> listByIds(Collection<? extends Serializable> idList);
  // 查询（根据 columnMap 条件）
  Collection<T> listByMap(Map<String, Object> columnMap);
  // 根据 Wrapper 条件，查询全部记录
  List<Object> listObjs(Wrapper<T> queryWrapper);
  // 根据 Wrapper 条件，查询全部记录
  <V> List<V> listObjs(Wrapper<T> queryWrapper, Function<? super Object, V> mapper);
  ```

###### page API

- ```java
  // 条件分页查询
  IPage<T> page(IPage<T> page, Wrapper<T> queryWrapper);
  // 无条件分页查询
  IPage<Map<String, Object>> pageMaps(IPage<T> page);
  // 条件分页查询
  IPage<Map<String, Object>> pageMaps(IPage<T> page, Wrapper<T> queryWrapper);
  ```

- 分页示例

- ```java
  // 分页查询所有 User 数据
  public void fetchUsersByPagination(int pageNum) {
    // 创建分页对象，设置每页的记录数
    Page<User> page = new Page<>(pageNum, 1000);
    // 创建查询条件，这里假设没有特定的查询条件，所以使用 LambdaQueryWrapper
    LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
    // 执行分页查询
    IPage<User> result = dbMapper.selectPage(page, queryWrapper);
    // 处理查询结果
    List<User> users = result.getRecords();
    // 总页数 pageNum == totalPage
    int totalPage = result.getPages();
  }
  ```

###### count API

- ```java
  // 查询总记录数
  long count();
  // 根据 Wrapper 条件，查询总记录数
  long count(Wrapper<T> queryWrapper);
  ```

##### 03：Mapper Interface

- BaseMapper 是 Mybatis-Plus 提供的一个通用 Mapper 接口，它封装了一系列常用的数据库操作方法，包括增、删、改、查等。**通过继承 BaseMapper，开发者可以快速地对数据库进行操作**，而无需编写繁琐的 SQL 语句。

###### insert

- ```java
  // 插入一条记录
  int insert(T entity);
  ```

###### delete

- ```java
  // 根据 entity 条件，删除记录
  int delete(@Param(Constants.WRAPPER) Wrapper<T> wrapper);
  // 删除（根据ID 批量删除）
  int deleteBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);
  // 根据 ID 删除
  int deleteById(Serializable id);
  // 根据 columnMap 条件，删除记录 <columnName, columnValue>
  int deleteByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);
  ```

###### update

- ```java
  // 根据 whereWrapper 条件，更新记录
  int update(@Param(Constants.ENTITY) T updateEntity, @Param(Constants.WRAPPER) Wrapper<T> whereWrapper);
  // 根据 ID 修改
  int updateById(@Param(Constants.ENTITY) T entity);
  ```

###### selete

- ```java
  // 根据 ID 查询
  T selectById(Serializable id);
  // 根据 entity 条件，查询一条记录
  T selectOne(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
  // 根据 entity 条件，查询全部记录
  List<T> selectList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
  // 根据 Wrapper 条件，查询全部记录（并翻页）
  IPage<T> selectPage(IPage<T> page, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
  ```

##### 04：Chain

- Chain 是 Mybatis-Plus 提供的一种链式编程风格，分为 `query` 和 `update` 两大类，分别用于查询和更新操作。每类又分为**普通链式和 lambda 链式两种风格**，其中 lambda 链式提供了类型安全的查询条件构造，但不支持 Kotlin。

###### query chain

```java
// 链式查询 普通
QueryChainWrapper<T> query();
// 链式查询 lambda 式。注意：不支持 Kotlin
LambdaQueryChainWrapper<T> lambdaQuery();

// 普通链式查询示例
query().eq("name", "John").list(); // 查询 name 为 "John" 的所有记录
// lambda 链式查询示例
lambdaQuery().eq(User::getAge, 30).one(); // 查询年龄为 30 的单条记录
```

###### update chain

```java
// 链式更改 普通
UpdateChainWrapper<T> update();
// 链式更改 lambda 式。注意：不支持 Kotlin
LambdaUpdateChainWrapper<T> lambdaUpdate();

// 普通链式更新示例
update().set("status", "inactive").eq("name", "John").update(); 
// lambda 链式更新示例
User updateUser = new User();
updateUser.setEmail("new.email@example.com");
lambdaUpdate().set(User::getEmail, updateUser.getEmail()).eq(User::getId, 1).update();
```

##### 05：ActiveRecord

- ActiveRecord 模式是一种设计模式，它允许实体类直接与数据库进行交互，实体类既是领域模型又是数据访问对象。在 Mybatis-Plus 中，**实体类只需继承 `Model` 类即可获得强大的 CRUD 操作能力**。
- 实体类中的字段需要与数据库表中的列对应，通常**通过注解（如 `@TableField`、`@TableId` 等）来指定字段与列的映射关系**。

###### 示例

```java
public class User extends Model<User> {
    // 实体类的字段定义...
    private Long id;
    private String name;
    private Integer age;
    // ... 其他字段和 getter/setter 方法
}

// 创建新用户并插入数据库
User user = new User();
user.setName("John Doe");
user.setAge(30);
boolean isInserted = user.insert(); // 返回值表示操作是否成功

// 查询所有用户
List<User> allUsers = user.selectAll();
```

