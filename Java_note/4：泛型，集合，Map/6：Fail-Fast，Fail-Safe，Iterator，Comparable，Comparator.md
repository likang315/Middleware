### 1��Fail-Fast  ���ƣ�����ʧ��

java �����е�һ�ִ�������ơ�**������̶߳�ͬһ�����ϵ����ݽ��в���ʱ���Ϳ��ܻ����fail-fast�¼�**���׳��쳣java.util.ConcurrentModificationException

ԭ�������� **modCount ����**������¼List�޸ĵĴ�����ÿ�޸�һ��(**���/ɾ���Ȳ���**)���� modCount+1**������ next() �� remove()ʱ��** ����ִ�� checkForComodification(),��"modCount != expectedModCount"�����׳�ConcurrentModificationException �쳣�� ����fail-fast�¼�

������������ڶ��̻߳�����ʹ��fail-fast���Ƶļ��ϣ�����ʹ�� java.util.concurrent ���µ���ȥȡ��java.util���µ���,
	 **�½�Iteratorʱ���������е�Ԫ�ر��浽һ���µĿ��������У���ԭʼ���ϵ����ݸı䣬���������е�ֵҲ����仯**

### 2��fail-safe ���ƣ���ȫʧ��

fail-safe �κζԼ��Ͻṹ���޸Ķ�����һ�����Ƶļ����Ͻ����޸ģ���˲����׳�ConcurrentModificationException

fail-safe��������������																					��Ҫ���Ƽ��ϣ�������������Ч���󣬿�����															�޷���֤��ȡ��������Ŀǰԭʼ���ݽṹ�е�����

###### ArrayList �� HashMap ��LinkedHashMap �����Fail-Fastl�¼�����CopyOnWriteArrayList�������Fail-Fast�¼�

### 3������������ �֣�

######    java.util

######    Interface Iterator<E>(������)�����ڱ������ϵģ������ܶ�ȡ���ϵ�����֮�⣬Ҳ�����ݽ���ɾ������(3������)

**ArrayList �� HashMap**
�����ߣ��ʣ�ȡ��ɾ
������
	 boolean hasNext() ----------  ����ƽ�ж��Ƿ���Ԫ�ؿ��Ե��������з��� true������false 
	 E next()   -----------------  ���ص�������һ��Ԫ��
	void remove()   ------------  �ӵ�����ָ��� collection ���Ƴ����������ص����һ��Ԫ��

######   java.util

######   Interface Enumeration<E>��ֻ�ܶ�ȡ���ϵ����ݣ������ܶ����ݽ����޸�(2������)

����û��֧��ͬ��������**Hashtableʵ��Enumerationʱ**�������ͬ��

Iterator֧��fail-fast���ƣ�������̲߳�������ʱ�����׳��쳣����Enumeration��֧��

### 3���Ƚ������ڱȽ�������Ƚ�����

  �������ڲ���дһ���Ƚ�������Ϊֻ��һ�Σ��Ժ��ٲ���ʹ�ã�Ҳ����new

######  java.util

######  Interface Comparator<T>:ǿ�ж�ĳ�����󼯺Ͻ�����������ıȽϺ������������ݱȽ���------��Ƚ���

ʵ��omparator�ӿڣ�������дint compare(Object o1, Object o2)����										 			  int compare(T o1, T o2) -------------�Ƚ������������������

######  java.lang

######  Interface Comparable<T>:ǿ�ж�����ʵ�����ÿ�����������������-----------�ڱȽ���

ʵ��Comparable�ӿڣ�������дint compareTo(Object o)����												  int compareTo(T o) ------------   �Ƚϴ˶�����ָ��������бȽ�



