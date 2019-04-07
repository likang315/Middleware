### EL��Expression Language��: JSTL1.0�ж��壬JSP2.0���룬���ʴ洢��JavaBean�е�����

##### �ص�

1:���пɻ�õ����ƿռ䣨PageContext���ԣ�
2:����Ƕ�׵����ԣ����Է��ʼ��϶���
3:����ִ�й�ϵ�ġ��߼�������������
4:��չ�������Ժ�Java��ľ�̬����ӳ��
5:���Է���JSP���ö���

##### 1:EL���ʽ�ı�ʾ

```
$ {"xxx"}  ��{username}, ${username.firstName}
```

##### 2:���������

```
+,-,*,/,%	{3+3},6
```

##### 3:��ϵ�����

```
==,!=,<,>,<=,>=    {3<=3},true
```

##### 4:�߼������

```
&&(and),||(or),!(not)   {A&&B},ture
```

##### 5:�����������Empty������

```jsp
	�����������{A>B?B:C}
	Empty��������{emptyB}
	�ж�һ��ֵ�Ƿ�Ϊnull
```

##### 6:�����������ö���     

  �뷶Χ�йص��������� 

```
applicationScope , application��Χ�ڵ�scoped������ɵļ��� 
sessionScope  ,���лỰ��Χ�Ķ���ļ��� 
requestScope  ,��������Χ�Ķ���ļ��� 
pageScope     ,ҳ�淶Χ�����ж���ļ���
```

 ������������ 

```
cookie        ,����cookie��ɵļ���
header        ,HTTP����ͷ�����ַ��� 
headerValues  ,HTTP��������ͷ�����ַ������� 
initParam     ,ȫ��Ӧ�ó����������ɵļ��� 
pageContext   ,��ǰҳ���javax.servlet.jsp.PageContext���� 
```

 �������йص���������

```
param       ,������������ַ�����ɵļ��� 
paramValues ,������������ַ���ֵ�ļ���
   param��paramValues�����������ʲ���ֵ��ͨ��ʹ��request.getParameter������request.getParameterValues����
   ${param["username"]}
   ${header["user-agent"]}
```

#####  7:�Զ�������Ĳ���

ͨ��PageContext.getAttribute(String)��������ɵ�
	���ʽ������page,request,session,application��Χ��˳�����username,���û���ҵ�������null
	����Ҳ��������pageScope��requestScope��sessionScope��applicationScopeָ����Χ,�磺${requestScope.username}

##### 8:����

JSP EL�������ڱ��ʽ��ʹ�ú���,������Щ�������뱻�������Զ����ǩ����,���ĺ��������Ǿ�̬��
${ns:func(param1, param2, ...)}










