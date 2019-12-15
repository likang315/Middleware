### 映射器、API

------

##### 1：Mybatis 核心API

```xml
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.4.6</version>
</dependency>
```

1. ###### SqlSessionFactoryBuilder.build(InputStream in)

   - 根据 xml 配置信息构建 SqlSessionFactory，有重载的

2. ###### Interface SqlSessionFactory (会话工厂)：两个方法

   - 两个实现类 DefaultSqlSessionFactory 和SqlSessionManager
   - 默认使用 DefaultSqlSessionFactory，可以使用 XML和代码构建
   - ###### SqlSession openSession() ：有重载的，默认会开启一个事务
   - Configuration getConfiguration() ：获取Configuration对象
   - 会话连接对象从由活动环境配置的数据源实例中得到
   - 事务隔离级别将会使用驱动或数据源的默认设置
   - 预处理语句不会被复用,也不会批量处理更新

3. ###### Interface SqlSession：会话连接，相当于Connection

   - 包含增删改查，提交或回滚事务，还有获取映射器实例
   - 通用的方式来执行映射语句是使用映射器类，映射器类就是简单的接口
   - ###### T getMapper(Class<T> type) ：使用映射器
   - void commit()
   - void rollback()	
   - void clearCache() ：清除缓存
   - void close()  ：关闭缓存
   - <T> T selectOne(String statemen,Object parameter)
   - <E> List<E> selectList(String statement, Object parameter)
   - int insert(String statement, Object parameter)
   - int update(String statement, Object parameter)
   - int delete(String statement, Object parameter)

4. ###### SQLMapper 是 MyBatis 组件：

   ​	由一个 java 接口（映射器类）和 XML文件（mapper）构成的，需要给出对应的SQL和映射规则，它负责发送 SQL，并返回结果

5. ###### Configuration ：将 mapper.xml 解析到 Configuration 对像中，以便复用

##### 2：映射器（Mapper）

​	由 **Java 接口(定以操作数据库的方法) 和 Mapper.xml** 文件共同组成,其实就是通过 XML 和反射来动态实现java接口实现类

###### 要求：

- 定义参数类型
- 描述缓存
- 描述 SQL 语句
- 绑定Dao接口 （namespace）

###### 两种方式是mapper接口和xml文件匹配

- ```xml
  <mapper namespace="com.xupt.StudentMapperInterFace">
  ```

当绑定namespace绑定接口后，你可以不用写接口实现类，mybatis会通过该绑定自动帮你找到对应要执行的SQL语句

```xml
@MapperScan("basePackages" = {"",""}) ,用于替代mappers定义的加载mapper文件

<!--映射器，用来寻找SqlMapper文件.xml 文件-->
<mappers>
  <!--通过resource加载单个mapper-->
	<mapper resource="com/mybatis3/mappers/StudentMapper.xml"/>
  <!--通过URL加载-->
	<mapper url="file:///var/mappers/StudentMapper.xml"/>
  <!--以下两种模式，需要mapper和接口在同一级目录下-->
  <!--通过类加载。通过注解方式的，不需要xml文件-->
	<mapper class="com.mybatis3.mappers.TutorMapper"/>
  <!--批量加载-->
  <package name="com.xupt.dao.mapper">
</mappers>
```

