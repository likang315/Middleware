### 1�����ݿ� �� ����֪ʶ

���ݿ����ϵͳ��DBMS�����������ݿ�����

   DB2���ݿ⣺��ϵ�����ݿ�
   Microsoft SQL Server ���ݿ� 
   MySQL���ݿ⣺С�͹�ϵ�����ݿ����ϵͳ����Դ�ģ�Oracle ���£�  c/c++��д�ģ�64λϵͳ֧�����ı��ļ�Ϊ8TB
   Oracle���ݿ⣬ѧϰ�ģ������շѣ���Ȩ

������---���ݿ⣨database��---���ݱ�TABLE��

���ݿ� DB��Database�����������ݵĽṹ����֯���洢�͹������ݵĲֿ�

���ݱ�Table�����ǹ�ϵ���ݿ�Ļ����洢�ṹ����ά�ģ���������ɵ�һ�ű��һ����ϵ�����ݿ��ɶ�����ݱ����

��ϵ���ݿ⣺��������Ԫ��֮��Ĺ������Ӧ��ϵ�������֮��Ĺ�ϵ

��¼��Ԫ�飩�����ݱ��е�һ��
�ֶΣ����ݱ��е�һ��

###### �����֮����ڹ�����ϵ��һ��һ��һ�Զ࣬��Զ� ���ֹ�ϵ

  ������PRIMARY KEY������ֵ��Ψһ�ر�ʶ���е�ÿһ�У����������������������������Լ�����¼���޸���ɾ��
  ���(FOREIGN KEY)  ��   �����һ������������ֵ���ֶ�����ʾ����������ֵ���������������Ѵ��ڵ�ֵ
  ����FOREIGN KEY (dep_id) REFERENCES dep(id) )

### 2��MySQL ���� ���� �� �ر�

   DOS�����У�root Ȩ����
	net start mysql
	net stop mysql
   Linux������
	service mysqld stop
	service mdsqld start

### 3: mysql���ݿ� ��¼���˳� 

��¼      mysql -uroot -pmysql(����) 
�˳�      \q
�鿴״̬  \s
�޸ķָ���   delimiter �ָ���

### 4: ���ݿ���� 

   show databases; 		�鿴��ǰ�������Ϲ�����������ݿ� 
   create database ���ݿ���;    �������ݿ�
   use ���ݿ���;                ѡ������ 
   alter database ���ݿ��� character set gbk;    �޸ı����ʽ
   drop database ���ݿ���;        ɾ�����ݿ�
   mysql -uroot -p ���ݿ��� < sql��URL    �������ݿ� 
   mysqldump -uroot -p xupt > xupt.sql

### 5���鿴���� 

   ��...   

### 6���޸�����

set password=password('mysql')��



