## ���Ͽ��

### 1:���ϣ�һ���������̶�����ȱ�ݵĶ�̬������java�����һ������������

java.util
Interface Collection<E>

  Collection��������ĸ��ӿڣ�Collection ��ʾһ���������ã���Щ����Ҳ��Ϊ collection ��Ԫ�أ����;���������Ԫ��Ϊ������

  Java��û���ṩ����ӿڵ�ֱ�ӵ�ʵ���ࡣ����ȴ���䱻�̳в����������ӿڣ�����Set��List

###   Collection���Ż��ӽӿڣ�List���ӿڣ�+ Set���ӿ�) +��Queue--���нӿڣ�	

#####    List����һ������ļ��ϣ����԰����ظ���Ԫ�أ��ṩ�˰��������ʵķ�ʽ

#####    Set������ģ��������ظ���Ԫ��

   ������
boolean	add(E e)   -------------  �����������Ԫ��

boolean remove(Object o) -------  �Ӵ� collection ���Ƴ�ָ��Ԫ�صĵ���ʵ����������ڵĻ�����ѡ������
void clear() -------------------  �Ƴ��� collection �е�����Ԫ�أ���ѡ������
boolean isEmpty() --------------  ����� collection ������Ԫ�أ��򷵻� true

int size() ---------------------  ���ش� collection �е�Ԫ����
boolean contains(Object o) -----  ����� collection ����ָ����Ԫ�أ��򷵻� true 

Object[] toArray() -------------  ���ذ����� collection ������Ԫ�ص�����
Iterator<E> iterator() ---------  ���ؼ��ϵĵ���������ʵ����Iterator ��ʵ����

### 2��Collections

java.util
Class Collections �����ϵĹ����࣬�����飨Arrays��һ������������һ��    

static <T> extends Comparable<? super T>> void  sort(List<T> list) 
    ����Ԫ�ص���Ȼ˳�� ��ָ���б������������ 
static <T> void sort(List<T> list, Comparator<? super T> c) 
����ָ���Ƚ���������˳���ָ���б��������
static <T extends Object & Comparable<? super T>> T	max(Collection<? extends T> coll)   ������Ȼ˳�򣬷������ֵ
static <T extends Object & Comparable<? super T>> T	min(Collection<? extends T> coll)   ������Ȼ˳�򣬷�����Сֵ
static void	reverse(List<?> list)	�������򼯺�

###### ʹ���������̰߳�ȫ����

######     static <T> List<T> synchronizedList(List<T> list) 

����ָ���б�֧�ֵ�ͬ�����̰߳�ȫ�ģ��б�

######     static <K,V> Map<K,V> synchronizedMap(Map<K,V> m) 

������ָ��ӳ��֧�ֵ�ͬ�����̰߳�ȫ�ģ�ӳ��

######     static <T> Set<T>  synchronizedSet(Set<T> s) 

 ����ָ�� set ֧�ֵ�ͬ�����̰߳�ȫ�ģ�set

### 3��List���ӿڣ������У�������ظ�������null����

java.util 
Interface List<E>

Method��
void add(int index, E element) ----------------  ���б��ָ��λ�ò���ָ��Ԫ�أ���ѡ������

E get(int index)      -------------------------  �����б���ָ��λ�õ�Ԫ��
E set(int index, E element) -------------------  ��ָ��Ԫ���滻�б���ָ��λ�õ�Ԫ�أ���ѡ������ 		
int indexOf(Object o) -------------------------  ���ش��б��е�һ�γ��ֵ�ָ��Ԫ�ص�����

List<E>	subList(int fromIndex, int toIndex) ---  ���غ��������߽�������м���
Iterator<E> iterator() ------------------------  ���ذ��ʵ�˳�����б��Ԫ���Ͻ��е����ĵ�����

boolean equals(Object o) ----------------------  �Ƚ�ָ���Ķ���(�б�)���б��Ƿ���� 

##### List�����У����Ա��ӿڵ�ʵ���ࣺ

######    Class Vector<E>��   ��������,Vector�̰߳�ȫЧ�ʵͣ�����Ӧ���� Vector �������ӻ��Ƴ���Ĳ���

######    Class ArrayList<E>����������,���ʶ���Object[]���飬ֻ����ArrayList�̲߳���ȫЧ�ʸߣ�������������в�����������ʵ�ֵ�

   ���췽����

ArrayList<E>() --------- ����һ����ʼ����Ϊ 10 �Ŀ��б�,1.5������

ArrayList<E>(int initialCapacity) -------- ����һ������ָ����ʼ�����Ŀ��б�

   ������
	boolean add(E e)               ��ָ����Ԫ����ӵ����б��β��
        void add(int index, E element) ��ָ����Ԫ�ز�����б��е�ָ��λ��,�ȵ���ensureCapacity��size+1),Ȼ�󿽱����飬��ֵ
 	boolean addAll(int index, Collection<? extends E> c)��ָ����λ�ÿ�ʼ����ָ�� collection �е�����Ԫ�ز��뵽���б��� 
		

E remove(int index)          --------------------  �Ƴ����б���ָ��λ���ϵ�Ԫ�� 
boolean remove(Object o)    ---------------------  �Ƴ����б����״γ��ֵ�ָ��Ԫ�أ�������ڣ�

E set(int index, E element) ---------------------  ��ָ����Ԫ��������б���ָ��λ���ϵ�Ԫ��

E get(int index)           ----------------------  ���ش��б���ָ��λ���ϵ�Ԫ�ء�			
int indexOf(Object o)     -----------------------  ���ش��б����״γ��ֵ�ָ��Ԫ�ص����������б�����Ԫ��,����-1 

int size()                   --------------------  ���ش��б��е�Ԫ����
boolean isEmpty()             -------------------  ������б���û��Ԫ�أ��򷵻� true 
void clear()               ----------------------  �Ƴ����б��е�����Ԫ�ء�����������Ԫ�ظ�ֵNUll��size�޸�Ϊ0
boolean contains(Object o) ----------------------  ������б��а���ָ����Ԫ�أ��򷵻� true 

void trimToSize()           ---------------------  ���� ArrayList ʵ������������Ϊ�б�ĵ�ǰ��С
Iterator<E> iterator()      ---------------------  ���ذ��ʵ�˳�����б��Ԫ���Ͻ��е����ĵ������� 
Object[] toArray()          ---------------------  ���ʵ�˳�򣨴ӵ�һ�������һ��Ԫ�أ����ذ������б���
							   ����Ԫ�ص�����	

######   Class LinkedList<E>���������У���������ӣ�ɾ��ʱЧ�ʸ� ����Ҫִ��ɾ�������룬��������Ԫ�صĲ����������̰߳�ȫ�� 

###### 		       			        ��˫������ʵ�ֵģ��̲߳���ȫ�࣬û�м�synchronized

���췽����
	LinkedList() ------------- ����һ�������� 
     	LinkedList(Collection<? extends E> c) -------- ����һ������ָ�� collection �е�Ԫ�ص��б�
						       ��ЩԪ�ذ��� collection �ĵ��������ص�˳������
    	
������
void addFirst(E e) --------------   ��ָ��Ԫ�ز�����б�Ŀ�ͷ
void addLast(E e) ---------------   ��ָ��Ԫ����ӵ����б�Ľ�β
	

E remove(int index)																					E set(int index, E element)

E get(int index)  --------------   ����ָ���±��Ԫ�� 
E getFirst()     ---------------    ���ش��б�ĵ�һ��Ԫ��
E getLast()     -----------------   ���ش��б�����һ��Ԫ�� 

ListIterator<E> listIterator(int index)   -------- ���ش��б��е�Ԫ�ص��б�����������ʵ�˳�򣩣����б���ָ��λ�ÿ�ʼ 

##### CopyOnWriteArrayList��дʱ���Ƶ�����������д�ٵĳ���																				public class CopyOnWriteArrayList<E>  implements List<E>, RandomAccess, Cloneable, java.io.Serializable

- ʵ����List�ӿ�
- �ڲ�����һ��ReentrantLock lock = new ReentrantLock();��������
- �ײ�����volatile transient ���������� array
- ��д���룬дʱ���Ƴ�һ���µ����飬��ɲ��롢�޸Ļ����Ƴ����������������ָ�������飬������̲߳�����д����ͨ���������ƣ���ֹ���������������
- ������̲߳����Ķ�������Ҫ��������ּ��������
  1�����д����δ��ɣ���ôֱ�Ӷ�ȡԭ���������
  2�����д������ɣ��������û�δָ�������飬��ôҲ�Ƕ�ȡԭ��������
  3�����д������ɣ����������Ѿ�ָ�����µ����飬��ôֱ�Ӵ��������ж�ȡ����

##### ConcurrentSkipListMap��

�ṩ��һ���̰߳�ȫ�Ĳ������ʵ�����ӳ����ڲ���SkipList�������ṹʵ�֣����������ܹ�O(log(n))ʱ������ɲ��ҡ����롢ɾ������

SkipList�����롢����ΪO(logn)����������Ⱥ����Ҫ�󣻵ײ�ṹΪ����������ʵ�֣�������Ȼ����

### 4��Interface Set<E>�����򣬲��ظ�.����������һ�� null Ԫ�أ�����ͨ���±��������ʵ�ײ�ʵ������ map

   ������ͬlist�������±�����ķ���
         Iterator<E> iterator() ---------�����ڴ� set �е�Ԫ���Ͻ��е����ĵ�����

  Set���ϵ�ʵ���ࣺ�������������̰߳�ȫ���࣬û�и��������ͬ������

#####   Class HashSet<E>(ɢ�м�)��ֻ�ܱ�֤Set����Ԫ��Ψһ�������ܱ�֤�����ɹ�ϣ��ʵ������һ�� HashMap ʵ����֧��,��û�����ӷ������̲߳���ȫ��

  ���캯��:
	HashSet()---------------- ����һ���µĿ� set������HashMap ʵ����Ĭ�ϳ�ʼ������ 16������������ 0.75
	HashSet(int initialCapacity)---����һ���µĿ� set����ײ� HashMap ʵ������ָ���ĳ�ʼ������Ĭ�ϵļ������ӣ�0.75���������������ʼ�������򴴽��µ������ļ��ϣ����Ұ�ԭ�������ݸ��ƽ�ȥ������ɾ��ԭʼ�ļ���	
  ����:
	 boolean add(E e) ------- ---- ����� set ����δ����ָ��Ԫ�أ������ָ��Ԫ��
	 boolean isEmpty() ----------- ���ж�������֮ǰ���пմ���
	 int size()       ------------ ���ش� set �е�Ԫ�ص�������Ϊ��ʱ������0
 	 Iterator<E> iterator() ------ ���ضԴ� set ��Ԫ�ؽ��е����ĵ�����

##### LinekdHashSet

LinkedHashSe t�̳���HashSet��Դ����١����򵥣�Ψһ��������LinkedHashSet�ڲ�ʹ�õ���LinkedHashMap,��������������ߺô�����LinkedHashSet�е�Ԫ��˳����Ψһ�ǿ��Ա�֤�ģ�Ҳ����˵������Ͳ�������һ�µ�

#####  Class TreeSet<E>(����):

��֤Set���ϵ�Ԫ��Ψһ,�������򣬵ײ���TreeMap��Ԫ�ر�������������Ԫ�ص��࣬����ʵ��Comparable<T>��Comparator<T>,��Ϊ��Ԫ�ؽ�������ʱ��Ҫ���մ�ԭ������һ��TreeMap

 ���캯����
	TreeSet() ------------- ����һ���µĿ� set���� set ������Ԫ�ص���Ȼ˳���������
    	TreeSet(Comparator<? super E> comparator) -------����һ���µĿ� TreeSet��������ָ���Ƚ�����������
			
 ������
	boolean add(E e) ------------------- ��ָ����Ԫ����ӵ��� set�������Ԫ����δ������ set ��)
        void clear()      ------------------ �Ƴ��� set �е�����Ԫ��
	boolean contains(Object o) --------- ����� set ����ָ����Ԫ�أ��򷵻� true
 	boolean remove(Object o)   --------- ��ָ����Ԫ�ش� set ���Ƴ��������Ԫ�ش����ڴ� set �У�
      	Iterator<E> descendingIterator() --- �����ڴ� set Ԫ���ϰ�������е����ĵ�����
       	Iterator<E> iterator()    ---------- �����ڴ� set �е�Ԫ���ϰ�������е����ĵ����� 
       	int size()     --------------------- ���� set �е�Ԫ����
          

 TreeSet���и�HashSet��һ��Ҳû��get()��������ȡָ��λ�õ�Ԫ�أ�����Ҳֻ��ͨ����������������ȡ for eachѭ��
 	

### 5��Interface Queue<E>�����У�FIFO��ʵ����Collection�ӿ�

boolean offer(E e)  ���һ��Ԫ�ز�����true       ��������������򷵻�false

E poll() 
      ��ȡ���Ƴ��˶��е�ͷ������˶���Ϊ�գ��򷵻� null 

E peek() 
      ��ȡ�����Ƴ��˶��е�ͷ������˶���Ϊ�գ��򷵻� null

ʵ���ࣺLinkedlist,PriorityQueue

```java
//add()��remove()������ʧ�ܵ�ʱ����׳��쳣(���Ƽ�)
Queue<String> queue = new LinkedList<String>();
//���Ԫ��
queue.offer("a");
```

### public class Stack<E> extends Vector<E> ��ջ���̳� ������ʵ�ֵļ���,2������

###### boolean  empty()     ���ж�ջΪ��Ϊ��

###### E   pop()    ����ջ������ǿ�ջ�����׳��쳣��EmptyStackException

###### E   push  (E item)  ����ջ

###### int  search(Object o) ������ĳԪ����ջ�е�λ��,������1��ʼ

```java
Stack<Integer> st = new Stack<Integer>();
ast.push(8);
System.out.println("stack: " + st);
```



### 6����ͨ���ϡ�ͬ�����̰߳�ȫ���ļ��ϡ���������

��ͨ����ͨ��������ߣ����ǲ���֤���̵߳İ�ȫ�ԺͲ����Ŀɿ���

�̰߳�ȫ���Ͻ����Ǹ����������synchronized(ͬ����)���������������ܣ����ҶԲ�����Ч�ʾ͸�����

����������ͨ���ڲ�ʹ�����ֶμ�����������֤�˶��̵߳İ�ȫ����ߵĲ���ʱ��Ч��
ConcurrentHashMap��ConcurrentLinkedQueue��ConcurrentLinkedDeque

### 7��Arrays���е�asList���� 

 * �÷������ڻ����������͵�����֧�ֲ�����,�������ǻ�����������ʱ������ʹ�� 

 *  ��ʹ��asList()����ʱ������ͺ��б�������һ����.������������֮һʱ����һ�����Զ���ø���

 *  asList�õ��������ǵ�û��add��remove������ 

   

   ͨ���鿴Arrays���Դ�����֪��,asList���ص�List��Array�е�ʵ�ֵ� �ڲ���,�����ಢû�ж���add��remove����.����,Ϊʲô�޸�����һ��,��һ��Ҳ�Զ� ��ø�����,��ΪasList���Listʵ�����õľ������� 





