###  JSTL(JSP��׼��ǩ��):��һ��JSP��ǩ���ϣ�����װ��JSPӦ�õ�ͨ�ú��Ĺ���

 JSTL ������
	����jakarta-taglibs-standard-1.1.2.zip ������ѹ����jakarta-taglibs-standard-1.1.2/lib/�µ�����jar�ļ�
	standard.jar��jstl.jar�ļ�������/WEB-INF/lib/��

1�����ı�ǩ
     �����﷨��<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

 1��<c:out>  ���һ�����ʽ�Ľ��
	<c:out value="<string>" default="<string>" escapeXml="<true|false>"/>

	���� 
	value 		Ҫ��������� 	
	default 	�����Ĭ��ֵ 	
	escapeXml  	�Ƿ����XML�����ַ�,Ĭ��ֵtrue������
 2��<c:set>   ���ñ���ֵ�Ͷ�������,���Լ�����ʽ��ֵ
	value 	Ҫ�洢��ֵ�������ֵ 
	var 	�洢��Ϣ�ı���
	scope 	var���Ե�������

 3��<c:if>  ��ǩ�жϱ��ʽ��ֵ��������ʽ��ֵΪ true ��ִ������������
	test 	�ж����� 
	var 	���ڴ洢��������ı��� 	
	scope 	var���Ե�������

 4��<c:choose> 		����ֻ����<c:when>��<c:otherwise>�ĸ���ǩ
    <c:when> 		<c:choose>���ӱ�ǩ�������ж������Ƿ����
    <c:otherwise> 	<c:choose>���ӱ�ǩ������<c:when>��ǩ�󣬵�<c:when>��ǩ�ж�Ϊfalseʱ��ִ��

	<c:choose>��ǩ��Java switch���Ĺ���һ�����������ڶ�ѡ��������ѡ��
	switch�������case����<c:choose>��ǩ�ж�Ӧ��<c:when>��switch�������default����<c:choose>��ǩ����<c:otherwise>

 5��<c:import>	����һ����ҳ
	url 	��������Դ��URL�����������·���;���·�������ҿ��Ե�������������Դ 	
	var 	���ڴ洢��������ı��ı��� 
	scope 	var���Ե������� 

 6��<c:forEach>  ����������ǩ�����ܶ��ּ�������
	items 		Ҫ��ѭ������Ϣ 	
	begin 		��ʼ��Ԫ�أ�Ĭ��0	
	end 		���һ��Ԫ��,Ĭ�����һ��Ԫ��
	step 		ÿһ�ε����Ĳ��� 	
	var 		����ǰ��Ϣ�������� 	
	varStatus 	����ѭ��״̬�ı������ƣ���index,end,begin,count
	

2����ʽ����ǩ��������ʽ��������ı������ڡ�ʱ�䡢����
   �����﷨
	<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

 1��<fmt:formatDate>   ����ʹ�ò�ͬ�ķ�ʽ��ʽ������
	value	Ҫ��ʾ������

    <fmt:formatDate type="both" dateStyle="medium" timeStyle="medium"  value="${now}" />


3��JSTL����  ����һϵ�б�׼�������󲿷���ͨ�õ��ַ���������
   �����﷨
	<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
	
	<c:if test="${fn:contains(<ԭʼ�ַ���>, <Ҫ���ҵ����ַ���>)}">
	...
	</c:if>







