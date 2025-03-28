### MyBatis

------

[TOC]

##### 01：概述

- MyBatis 是一个开源的**持久层框架**，它简化了在 Java 应用程序中使用 JDBC 操作数据库的过程。通过MyBatis，开发人员可以**使用 XML 或注解来配置 SQL ，映射 Java 对象和数据库记录之间的关系**（半 ORM）。

- ORM：是通过使用描述对象和数据库之间映射的元数据，将程序中的对象自动持久化到数据库中。

##### 02：Mybatis 示例

###### import jar 包

- mybatis-3.x.x.jar
- mysql-connector-java-5.1.22.jar

```xml
<!-- https://mvnrepository.com/artifact/org.mybatis/mybatis -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.4.6</version>
</dependency>
```

###### MyBatis 配置文件（mybatis-config.xml）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration
 PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
 "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <settings>
        <!-- 在进行数据库字段和类属性名映射时，下划线自动转换为驼峰-->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>
    <typeAliases>
        <package name="com.xupt.pojo"/>
    </typeAliases>
    <!-- 分模块部署时，dao层需要数据源-->
    <environments default="dev">
        <environment id="dev">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/mybatis4?useSSL=true"/>
                <property name="username" value="root"/>
                <property name="password" value="mysql"/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <!-- 引入映射器 -->
        <mapper resource = "mapper/StudentMapper.xml"/>
    </mappers>
</configuration>
```

###### Dao 接口

```java
@Repository
public interface StudentMapper {
    // 根据ID查询
    List<Students> findStudentsById(@Param("uid") int id);
}
```

###### 编写Mapper （定义操作数据库的方法）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
 "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- 通过 mapper 中 的 namespace 标签绑定接口，mybatis 会通过该绑定自动帮你找到对应要执行的SQL语句； -->
<mapper namespace="com.zxy.mapper.StudentMapper">
    <resultMap type="students" id="stumap">
        <id property="studId" column="stud_id"/>
        <result property="name" column="name"/>
        <result property="dob" column="dob"/>
        <result property="email" column="email"/>
    </resultMap>

    <!-- 简单数据类型 -->
    <select id="findStudentsById" parameterType="int" resultType="students">
        select * from students where stud_id = #{uid}
    </select>
</mapper>
```

###### 加载 mybatis 的配置文件，得到 SqlSessionFactory 和 sqlSession，Mapper对象，操作数据库

```java
public void testfindStudentsById()
    // 加载 mybatis 的配置文件
    InputStream inputStream = Resources.getResourceAsStream("/mybatis-config.xml");
    // 相当于根据 mybatis-config.xml 构建连接池
    SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    // SqlSession 相当于我们的 Connection
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
        // 根据指定的 MapperInterface 返回对应的 Mapper，并且实现其接口
        StudentMapper sm = sqlsession.getMapper(StudentMapper.class);
        List<Students> stus = sm.findStudentsById(1);
        for(Students s:stus) {
            System.out.println(s.getStud_id() +
                               "\t" + s.getName() + "\t" + s.getEmail() + "\t");
        }
        // 断言测试，两个参数一致继续执行，反之抛出异常
        assertEquals(1, stus.size());
    } finally {
        sqlsession.close();
    }
}
```