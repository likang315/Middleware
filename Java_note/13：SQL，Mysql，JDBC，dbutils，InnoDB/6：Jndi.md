##### JNDI(Java Naming and Directory Interface)��Java������Ŀ¼�ӿ�

һ��Ӧ�ó�����Ƶ�API��Ϊ������Ա�ṩ�˲��Һͷ��ʸ���������Ŀ¼�����ͨ�á�ͳһ�Ľӿ�,���е�J2EE����������
�ṩһ��JNDI�ķ���

###### DataSource��������JNDI���ϣ�Ϊÿһ��DataSource�ṩһ�����֣��ͻ���ͨ�������ҵ���JNDI���ϰ󶨵�DataSource��

����DataSource�ҵ�һ������

JNDI����
	javax.naming.InitialContext

```java
//��ʼ�����Ʋ��������Ļ���
InitialContext ic = new InitialContext(); 
//ͨ��JNDI�����ҵ�DataSource,��tomcat�±���ӣ�java:comp/env�����ƽ��ж�λ,���������DataSource��
DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/test");-------������������
```


Context.xml ����������META-INFO��