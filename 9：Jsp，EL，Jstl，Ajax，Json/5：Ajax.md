## Ajax(Asynchronous JavaScript And XML)���첽��js��xml����

 ʹ��DOMʵ�ֶ�̬��ʾ�ͽ���,ʹ��HTML��CSS��׼����,ʹ��**XMLHttpRequest�����ö���**�����첽���ݶ�ȡ
,�����JavaScript ���¼��ʹ�����������

��ͳ��WEBҳ��:
	ÿ�������ڼ䲻���ٴβ���
AJAX�첽������ʽ:
	�����첽�ķ�ʽ���������ݣ���������������ݲ����²�����ҳ�ļ����������¼�������ҳ��

![Ajax.png](https://github.com/likang315/Java-and-Middleware/blob/master/9%EF%BC%9AJsp%EF%BC%8CEL%EF%BC%8CJstl%EF%BC%8CAjax%EF%BC%8CJson/%E6%96%B0%E5%BB%BA%E6%96%87%E4%BB%B6%E5%A4%B9/Ajax.png?raw=true)



### 1��AJAX ��������

1:�û���ҳ��**��ִ����ĳ������**����������ƶ������ĳ�������

2:�����û��Ĳ�����ҳ�淢����Ӧ��DHTML�¼�

3:����ע�ᵽ**��DHTML�¼��Ŀͻ���JavaScript�¼�������**������**��ʼ����һ������������������첽����� XMLHttpRequest�� ��ͬʱָ����һ���ص����������������˵���Ӧ����ʱ�����Զ����øûص�����**

4:�������յ�XMLHttpRequest���������֮�󣬿�ʼ�����������һϵ�еĴ���

5:������ϣ����������ؿͻ�������Ҫ������

6:**���ݵ���ͻ���֮��ִ��JavaScript�ص�������**�����ݷ��ص����ݶ��û�������и���

����ʹ��DOM�������������ݵ��޸�



### 2��XMLHttpRequest ����

#####    ����

###### onreadystatechange ָ����readyState���Ըı�ʱ�Ĵ����¼� 

###### readyState  		 ���ص�ǰ�����״̬

?		0 ��(δ��ʼ��)   �����ѽ�����������δ��ʼ������δ����open������ 
?		1 ��(��ʼ��)      �����ѽ�������δ����send���� 
?		2 ��(��������)   send�����ѵ��ã����ǵ�ǰ��״̬��httpͷδ֪ 
?		3 ��(���ݴ�����) �ѽ��ղ������ݣ���Ϊ��Ӧ��httpͷ��ȫ����ʱͨ��responseText��ȡ�������ݻ���ִ��� 
?		4 ��(���)          ���ݽ������,��ʱ����ͨ��responseBody��responseText��ȡ�����Ļ�Ӧ���� 

###### responseText 		����Ӧ��Ϣ��Ϊ�ַ�������

###### responseXML 		����Ӧ��Ϣ��ʽ��ΪXml Document���󲢷���

###### status 				���ص�ǰ�����http״̬��

###### statusText  			���ص�ǰ�������Ӧ��״̬

#####    ����

###### open(bstrMethod, bstrUrl�� ����һ���µ�http���󣬲�ָ��������ķ�����URL

###### send( ) 		   ��������http�����������ջ�Ӧ 

setRequestHeader ����ָ�������ĳ��httpͷ 
abort ȡ����ǰ���� 
getAllResponseHeaders ��ȡ��Ӧ������httpͷ 
getResponseHeader ����Ӧ��Ϣ�л�ȡָ����httpͷ 

### 3������ж�ǰ����Ajax����

###### String type = request.getHeader("X-Requested-With");  

 requestType���õ�ֵ������ֵΪXMLHttpRequest,��ʾ�ͻ��˵�����Ϊ�첽��������Ȼ��ajax�����ˣ���֮���Ϊnull,������ͨ������





