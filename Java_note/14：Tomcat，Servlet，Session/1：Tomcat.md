### Servlet�����л�������Servlet����

### Tomcat����һ��Servlet��JSP������������ WEB ���������

##### Ŀ¼�ṹ

   Bin     ���������ֹͣ tomcat ����Ľű�
   Conf    ������ص������ļ�
   Lib     ����tomcat �õ��� java �� *.jar
   Logs    ��������־
   Temp    ��ʱĿ¼
   Webapps ���� web Ӧ�ã���վ����Ŀ¼
   Work    ����Ŀ¼��jsp �������� java �ļ������ڴ�Ŀ¼

##### ���� tomcat

Ҫ��
  �Ѱ�װ jdk
  ���� path �� classpath

   Step1:���� java_home �䵽 jdk �İ�װĿ¼�Ϳ���
   Step2:%tomcat_home%/lib/servlet-api.jar ���� classpath
   Step3:����%tomcat_home%/bin/startup.bat
   Step4:������� http://localhost:8080(Tomcat��Ĭ�϶˿�)

 ��tomcat �����˿ڸ���Ϊ http Ĭ�϶˿ں� 80
	��%tomcat_home%/conf/server.xml

###### ��Ŀ�У�WebContent����RootĿ¼����Ŀ¼�µ�WEB-INF �ǰ�ȫĿ¼������ֱ�ӷ���

���õ�URL�����Ѿ��е�·������վ�����Դ


















