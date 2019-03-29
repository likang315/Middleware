## ���Ͽ�ܣ���Java.util��

## List + Set +Map +�����ࣨArrays ��Collections��Iterator��Enumeration��



### 1�����ϣ�һ���������̶�����ȱ�ݵĶ�̬������java �����һ������������

java.util
Interface Collection<E>

Collection��������ĸ��ӿڣ�Collection ��ʾһ���������ã���Щ����Ҳ��Ϊ collection ��Ԫ�أ����;���������Ԫ��Ϊ������

Java �� û���ṩ����ӿڵ�ֱ�ӵ�ʵ���࣬����ȴ���䱻�̳в����������ӿڣ����� Set �� List

![Framework.png](https://github.com/likang315/Java/blob/master/Java_note/4%EF%BC%9A%E6%B3%9B%E5%9E%8B%EF%BC%8C%E9%9B%86%E5%90%88%EF%BC%8CMap/Framework.png?raw=true)



###   Collection ���Ż��ӽӿڣ�List���ӿڣ�+    Set���ӿ�) +  Queue�����нӿڣ�

######    List�������У����԰����ظ�Ԫ�أ��ṩ�˰���������

######    Set��      ���ϣ��������ظ���Ԫ��

###### Deque��˫����У�Queue ���ӽӿ�



**������**

###### int size() ---------------------  ���ش� collection �е�Ԫ����

###### boolean isEmpty() --------------  ����� collection ������Ԫ�أ��򷵻� true

###### boolean	add(E e)   -------------  �����������Ԫ��

###### boolean remove(Object o) -------  �Ӵ� collection ���Ƴ�ָ��Ԫ�صĵ���ʵ����������ڵĻ�����ѡ������

###### void clear() -------------------  �Ƴ��� collection �е�����Ԫ�أ���ѡ������

###### Iterator<E> iterator() ---------  ���ؼ��ϵĵ���������ʵ����Iterator ��ʵ����

###### boolean contains(Object o) -----  ����� collection ����ָ����Ԫ�أ��򷵻� true 

###### Object[] toArray() -------------  ���ذ����� collection ������Ԫ�ص�����





### 2��Collections �����ϵĹ�����

java.util
Class Collections �����ϵĹ����࣬�����飨Arrays��һ������������һ��    

static <T> extends Comparable<? super T>> void  sort(List<T> list) 
   	����**Ԫ�ص���Ȼ˳��** ��ָ���б������������ 
static <T> void sort(List<T> list, Comparator<? super T> c) 
	����**ָ���Ƚ���**������˳���ָ���б��������
static <T extends Object & Comparable<? super T>> T	**max(Collection<? extends T> coll)**   �������ֵ
static <T extends Object & Comparable<? super T>> **T	min(Collection<? extends T> coll)**    ������Сֵ
static void   reverse(List<?> list)	�������򼯺�



###### ʹ���������̰߳�ȫ����

######     static <T> List<T> synchronizedList(List<T> list) 

����ָ���б�֧�ֵ�ͬ�����̰߳�ȫ�ģ��б�

######     static <K,V> Map<K,V> synchronizedMap(Map<K,V> m) 

������ָ��ӳ��֧�ֵ�ͬ�����̰߳�ȫ�ģ�ӳ��

######     static <T> Set<T>  synchronizedSet(Set<T> s) 

 ����ָ�� set ֧�ֵ�ͬ�����̰߳�ȫ�ģ�set



#### Collection.sort(List<T> list, Comparator<? super T> ���ײ���Timsort���鲢+����

#### Arrays.sort()���鲢+����+����

������鳤��**���ڵ��� 286** �� �����Ժ� ������������������򣩵Ļ�������**�鲢����**��������ڵ���286 �� �����Բ��õĻ����� **˫���������** ���������С��286�Ҵ��ڵ���47�Ļ�����˫����������������**С��47**�Ļ�����**��������**



### 3��List���ӿڣ������У�������ظ������� ��� null ����

```java
public interface List<E> extends Collection<E>
```

Method��

###### void add(int index, E element) ----------------  ���б��ָ��λ�ò���ָ��Ԫ�أ���ѡ������

###### E get(int index)      -------------------------  �����б���ָ��λ�õ�Ԫ��

###### E set(int index, E element) -------------------  ��ָ��Ԫ���滻�б���ָ��λ�õ�Ԫ�أ���ѡ������

###### int indexOf(Object o) -------------------------  ���ش��б��е�һ�γ��ֵ�ָ��Ԫ�ص�����

List<E>	subList(int fromIndex, int toIndex) ---  ���غ��������߽�������м���

###### Iterator<E> iterator() ------------------------  ���ذ��ʵ�˳�����б��Ԫ���Ͻ��е����ĵ�����





### 4��Interface Set<E>�����򣬲��ظ�.����������һ��  null  Ԫ�أ�����ͨ���±��������ʵ�ײ�ʵ������ map

```java
public interface Set<E> extends Collection<E> 
```

������ͬlist�������±�����ķ���
  



### 5��Interface Queue<E>�����У�FIFO��ʵ����Collection�ӿ�

###### boolean offer(E e)  �����һ��Ԫ�ز�����true       ��������������򷵻�false

###### E poll() �� ��ȡ���Ƴ��˶��е�ͷ������˶���Ϊ�գ��򷵻� null 

ʵ���ࣺ**Linkedlist**,PriorityQueue

```java
//add()��remove()������ʧ�ܵ�ʱ����׳��쳣(���Ƽ�)
Queue<String> queue = new LinkedList<String>();
//���Ԫ��
queue.offer("a");
```



### 6��public class Stack<E> extends Vector<E> ��ջ���̳� ������ʵ�ֵļ���, 2������

###### boolean  empty()     ���ж�ջΪ��Ϊ��

###### E   pop()    ����ջ������ǿ�ջ�����׳��쳣��EmptyStackException

###### E   push  (E item)  ����ջ

###### int  search(Object o) ������ĳԪ����ջ�е�λ��,������1��ʼ

```java
Stack<Integer> st = new Stack<Integer>();
ast.push(8);
System.out.println("stack: " + st);
```



### 7����ͨ���ϡ�ͬ�����̰߳�ȫ���ļ��ϡ��������ϣ�JUC��

��ͨ����ͨ��������ߣ����ǲ���֤���̵߳İ�ȫ�ԺͲ����Ŀɿ���

�̰߳�ȫ���Ͻ����Ǹ����������synchronized(ͬ����)���������������ܣ����ҶԲ�����Ч�ʾ͸�����

����������ͨ���ڲ�ʹ��**���ֶμ���**��**������֤�˶��̵߳İ�ȫ����ߵĲ���ʱ��Ч��**
ConcurrentHashMap��ConcurrentLinkedQueue



### 8��Arrays ���е�  asList ���� 

 * ��ʹ�� asList() ����ʱ������ͺ��б�������һ���ˣ�**����������֮һʱ����һ�����Զ���ø���**

 * asList�õ���List �ǵ�**û�� add()  �� remove() ** 

   ͨ���鿴Arrays���Դ�����֪��,**asList���ص�List��Array�е�ʵ�ֵ� �ڲ���**,�����ಢû�ж���add��remove����.����,Ϊʲô�޸�����һ��,��һ��Ҳ�Զ� ��ø�����,��ΪasList��� **Listʵ�����õ� ���� ����** 







