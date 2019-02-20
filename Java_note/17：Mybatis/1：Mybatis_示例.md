## Mybatis ��

ͨ������ײ��JDBC���룬�Զ��� SQL ��������� Java ����Java ��������ݳ־û����ݿ��еĹ��̣�ʹ�ö� SQL ��ʹ�ñ�����ף�Ҳ��һ�����Զ����� ORM ��ܣ���Ҫ�ֹ�ƥ���ṩ POJO��SQL ��ӳ���ϵ

##### 1��inport jar��

mybatis-3.x.x.jar
mysql-connector-java-5.1.22.jar

##### 2��New mybatis �����ļ���mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-config.dtd">
	<configuration>
		<environments default="development">
			<environment id="development">
				<transactionManager type="JDBC"/>
				<dataSource type="POOLED">
					<property name="driver" value="${driver}"/>
					<property name="url" value="${url}"/>
					<property name="username" value="${username}"/>
					<property name="password" value="${password}"/>
				</dataSource>
			</environment>
		</environments>
		<mappers>
			<mapper resource="com/zxy/mapper/StudentMapper.xml"/>
		</mappers>
	</configuration>
```

##### 3��New ӳ���ļ�:Mapper.xml

���ȶ���Java�ļ�(�ӿ�),����������ݿ�ķ�����ʵ����Mybatis���Զ�������

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
 	PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
	 "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
	<mapper namespace="com.zxy.mapper.StudentMapper">
 		<select id="findAllStudents" resultType="com.xzy.pojo.Students">
   			 select * from students
 		</select>
 	</mapper>
```




##### 4������mybatis�������ļ����õ�SqlSessionFactory �� sqlSession,Mapper����

```java
	//����mybatis�������ļ�
	InputStream inputStream = Resources.getResourceAsStream("/mybatis-config.xml");
	//�൱�ڸ���mybatis-config.xml�������ӳ�
	SqlSessionFactory sqlSessionFactory =new SqlSessionFactoryBuilder().build(inputStream);
	// SqlSession�൱�����ǵ�Connection
	SqlSession sqlSession = sqlSessionFactory.openSession();
	StudentMapper sm=session.getMapper(StudentMapper.class);
```






