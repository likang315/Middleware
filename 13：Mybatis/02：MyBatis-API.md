### MyBatis API

------

[TOC]

##### 01：MyBatis API

- SqlSessionFactoryBuilder.build(InputStream in)
  - 根据 xml 配置信息构建 SqlSessionFactory，有重载的
- Interface **SqlSessionFactory (会话工厂)** ：
  - 两个实现类 DefaultSqlSessionFactory 和 SqlSessionManager
  - **用于加载 mybatis-config.xml 和 xml mapper 文件；**
  - SqlSession openSession() ：有重载的，默认会开启一个事务
    - 数据库连接；
  - Configuration getConfiguration() ：获取Configuration对象

- Interface **SqlSession：会话连接**，相当于数据库实例

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

- class **SqlSessionTemplate** 

  - 实现了SqlSession 接口扩展了SqlSession 功能；

- class **DataSourceTransactionManager**

  - 事务管理器；
