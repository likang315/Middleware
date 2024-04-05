### MyBatis Interceptor 

------

[TOC]

##### 01：概述

- MyBatis 提供了一种**插件（plugin）**的功能，虽然叫做插件，但其实这是拦截器功能。
- MyBatis 允许在已映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法调用包括：
  1. Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
     - 拦截执行器的方法
  2. **ParameterHandler** (getParameterObject, setParameters)
     - 拦截参数的处理
  3. ResultSetHandler (handleResultSets, handleOutputParameters)
     - 拦截参数的处理
  4. StatementHandler (prepare, parameterize, batch, update, query)
     - 拦截Sql语法构建的处理
- Mybatis 拦截器用到责任链模式 + 动态代理 + 反射机制；

##### 02：MyBatis 的拦截器接口定义

- MyBatis 默认没有一个拦截器接口的实现类，开发者们可以实现符合自己需求的拦截器。

- ```java
  public interface Interceptor {
  	// 重写该方法进行拦截处理
      Object intercept(Invocation invocation) throws Throwable;
      // 插件用于封装目标对象的，通过该方法我们可以返回目标对象本身，也可以返回一个它的代理
      // 可以决定是否要进行拦截进而决定要返回一个什么样的目标对象
      default Object plugin(Object target) {
          // 返回代理对象
          return Plugin.wrap(target, this);
      }
      // 在Mybatis进行配置插件的时候可以配置自定义相关属性，即：接口实现对象的参数配置
      default void setProperties(Properties properties) {
      }
  }
  ```

###### @Signature

- 使用注解时，type 和 method、args 是一组固定的值；
- 参数MyBatis 文档；

###### 示例

- ```java
  @Slf4j
  @Component
  @Intercepts({
          @Signature(
                  type = Executor.class,
                  method = "query",
                  args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
          ),
          @Signature(
                  type = Executor.class,
                  method = "update",
                  args = {MappedStatement.class, Object.class}
          )
  })
  public class SqlTimeInterceptor implements Interceptor {
      public Object intercept(Invocation invocation) throws Throwable {
          long startTime = System.currentTimeMillis();
          Object result = invocation.proceed();
          long endTime = System.currentTimeMillis();
          // 获取执行的 SQL 语句
          MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
          String mapperId = mappedStatement.getId();
          BoundSql boundSql = mappedStatement.getBoundSql(invocation.getArgs()[1]);
          String sql = boundSql.getSql();
          Map<String, Object> paramMap = (Map<String, Object>) boundSql.getParameterObject();
          // 输出执行时间和 SQL 语句
          log.info(String.format("Mapper: %s\nSQL: %s\nparams: %s\n耗时：%d 毫秒",
                  mapperId, sql, paramMap.toString(), endTime - startTime));
  
          return result;
      }
  
      public Object plugin(Object target) {
          return Plugin.wrap(target, this);
      }
  }
  
  // 因为自定义了数据源，所以需要自己添加Plugins
  @Resource
  private Interceptor[] interceptors;
  
  bean.setPlugins(interceptors);
  ```

##### 03：适用场景

###### 敏感字段加解密

1. 利用注解标明需要加密解密的类对象的字段；
2. mybatis拦截Executor.class对象中的query，update方法；
3. 在方法执行前对parameter进行加密解密，在拦截器执行后，解密返回的结果；