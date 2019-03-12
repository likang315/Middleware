## java.util.Map��ӳ���ϵ��key-value��

### 1��Interface Map<K,V>��һ��ӳ���ϵ������ ӳ�䵽 ֵ�Ķ��󣬴洢����ֵ������˫�����ݵļ���

######  �ص㣺һ��ӳ�䲻�ܰ����ظ��ļ�,ÿ�������ֻ��ӳ�䵽һ��ֵ

K - ӳ��������ͣ�����
V - ӳ��ֵ������

static interface Map.Entry<K,V>  ��ӳ���ϵ

������	
V put(K key, V value)            ----------- ��ָ����ֵ���ӳ���е�ָ��������������ɷ����˼���
V get(Object key)            ---------------  ����ָ������ӳ���ֵ�������ӳ�䲻�����ü���ӳ���ϵ���򷵻� null
V remove(Object key)             ----------�������һ������ӳ���ϵ������Ӵ�ӳ�����Ƴ�,����ֵΪ��ɾ����value

void clear()                    ------------�Ӵ�ӳ�����Ƴ�����ӳ���ϵ
boolean isEmpty()               ---------------�����ӳ��δ������-ֵӳ���ϵ���򷵻� true
int size()                      ------------���ش�ӳ���еļ�-ֵӳ���ϵ��

Set<Map.Entry<K,V>> entrySet()  ---------------���ش�ӳ���а�����ӳ���ϵ�� Set����
Set<K> keySet()                 ---------------���ش�ӳ����ֻ�����ļ��� Set ����
Collection<V>  values()         ---------------���ش�ӳ���а�����ֵ��collection����

boolean containsKey(Object key) ---------------�����ӳ�����ָ������ӳ���ϵ���򷵻� true 
boolean containsValue(Object value) -----------�����ӳ�佫һ��������ӳ�䵽ָ��ֵ���򷵻� true

void putAll(Map<? extends K,? extends V> m) ---��ָ��ӳ���н�����ӳ���ϵ���Ƶ���ӳ����
         		

### 2��Class HashMap<K,V>�����ڹ�ϣ��� Map �ӿڵ�ʵ����,���̰߳�ȫ���࣬���Ҳ���֤ӳ���˳�򣬵��ǲ��Ҹ�Ч������ null ֵ �� null ���Ĵ���

public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable 

###### 1��ʵ��ԭ��

   HashMap�ĵײ���Ҫ�ǻ��ڣ�jdk1.8������,����ͺ������ʵ�֣����Ĳ�ѯ�ٶȿ���Ҫ����Ϊ����ͨ������hashֵ�������洢��λ��

jdk1.7:��������+����

###### 2��hash��ͻ

   HashMap��ͨ��keyֵ��hashCode����������hashֵ����λ����������ֲ���Ծ��ȵ�λ�ã�����洢�Ķ���Զ��ˣ����п��ܲ�ͬ�Ķ������������hashֵ����ͬ�ģ���ͳ�������ν��hash��ͻ�����hash��ͻ�ķ����кܶ࣬HashMap �ײ���ͨ�����������hash��ͻ�ģ���������Ͱ�����

���췽����	
	HashMap() ----------- ����һ������Ĭ�ϳ�ʼ���� (16) ��Ĭ�ϼ������� (0.75) �Ŀ� HashMap����������
       	HashMap(int initialCapacity) ------����һ����ָ����ʼ������Ĭ�ϼ������� (0.75) �Ŀ� HashMap
       	HashMap(int initialCapacity, float loadFactor) ----------����һ����ָ����ʼ�����ͼ������ӵĿ� HashMap

HashMap(Map<? extends K,? extends V> m) -----------------����һ��ӳ���ϵ��ָ�� Map ��ͬ���� HashMap

######   3��Ӱ��hashmap�����ܣ�Capacity �� loadFactor

���ڵ�������(threshold)��ֵ����Ҫ���ݣ����ֵ�ļ��㷽ʽ��capacity * load factor

###### 4���������ݣ�HashMap����ʱ  ����ǰ����X2������������ʱ��Ҫ���¼���hash

����һ���µ������ԭ�������鸳ֵ��ȥ����ԭ�����������������ϵ�����洢

######  5��HashMap Ϊʲô��ʼ����Ϊ 16

Ϊ�˼���hashֵ����ײ,��Ҫʵ��һ���������ȷֲ���hash����,��HashMap��ͨ������key��hashcodeֵ,������λ����
��ʽ:index = e.hash & (newCap - 1)

���۳���16��������2����, length - 1��ֵ��������������λȫΪ1,���������,index �Ľ����ͬ��hashcode��λ��ֵ��ֻҪ�����hashcode����ֲ�����,hash�㷨�Ľ�����Ǿ��ȵģ�����,HashMap��Ĭ�ϳ���Ϊ16,��Ϊ�˽���hash��ײ�ļ���

###### 6��Hash()����Hashʱ�õ�

//���˺ܶ�������λ�����㣬��key��hashcode��һ�����м����Լ�������λ�ĵ���������֤���ջ�ȡ�Ĵ洢λ�þ����ֲ�����

```java
final int hash(Object k) {
        int h = hashSeed;
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k);
        }

	   h ^= k.hashCode();																 		   h ^= (h >>> 20) ^ (h >>> 12);
	   return h ^ (h >>> 7) ^ (h >>> 4);
}
```

###### 7��ʲôʱ���ú����ʲôʱ��������

��ͰԪ�أ�Ͱ����ȣ�����8�����ұ�����64�Ὣ����ת��Ϊ����������������Ԫ��С��6��ʱ���Ὣ�����ת��Ϊ����

��Ϊ�������Ҫ�������������������� ����������Ҫ
���Ԫ��С��8������ѯ�ɱ��ߣ������ɱ���
���Ԫ�ش���8������ѯ�ɱ��ͣ������ɱ���

###### 8��ΪʲôҪ��������

��Ϊ֮ǰhashmap�ײ�ṹ��������������ǵ����ݴ�һ���̶ȵ�ʱ�򣬼�ʹ��������洢Ҳ�ǱȽϳ���������ɾ�Ĳ飬������Ĳ���Ч�ʸߣ�����

##### 9������ʱ��ͷ����ɻ�������

��Ϊ�������߳�ͬʱ����put�Ĳ���ʱ���պ�Ҫ���ݣ�һ���߳�����ʱ���ߣ���һ���߳����ݣ���hash��ʱ����һ���̼߳�������ʱ�͵��»�������

![](G:\Java\Java_note\4�����ͣ����ϣ�Map\HashMapѭ������.png)

###### 8��put������jdk 1.7 ��jdk1.8��

```java

public V put(K key, V value) {
    // �������һ��Ԫ�ص�ʱ����Ҫ�ȳ�ʼ�������С
    if (table == EMPTY_TABLE) {
        inflateTable(threshold);
    }
    // ��� key Ϊ null������Ȥ�Ŀ������￴�����ջὫ��� entry �ŵ� table[0] ��
    if (key == null)
        return putForNullKey(value);
    // 1. �� key �� hash ֵ
    int hash = hash(key);
    // 2. �ҵ���Ӧ�������±�
    int i = indexFor(hash, table.length);
    // 3. ����һ�¶�Ӧ�±괦���������Ƿ����ظ��� key �Ѿ����ڣ�
    //    ����У�ֱ�Ӹ��ǣ�put �������ؾ�ֵ�ͽ�����
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {
        Object k;
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }
 
    modCount++;
    // 4. �������ظ��� key������ entry ��ӵ������У�ϸ�ں���˵
    addEntry(hash, key, value, i);
    return null;
}
```

jdk1.8 �����ڵ�һ��put ��ʱ�򣬻ᴥ������� resize()������ java7 �ĵ�һ�� put ҲҪ��ʼ�����鳤��
��һ�� resize �ͺ�����������Щ��һ������Ϊ���������� null ��ʼ����Ĭ�ϵ� 16 ���Զ���ĳ�ʼ����

�ҵ�����λ�ú󣬻�ȡ��һ��ֵ�ж��Ƿ�Ϊ������ڵ�

if` `(p instanceof TreeNode)
            ``e = ((TreeNode<K,V>)p).putTreeVal(``this, tab, hash, key, value);

������룬Ȼ�����ǵ�9 ����㣬��ת��Ϊ�������java���Ȳ��룬�����ݣ��������β��,jdk.1.7����ͷ��

###### 9��get������jdk 1.7 ��jdk1.8��

jdk 1.7																																		���� key ���� hash ֵ
�ҵ���Ӧ�������±꣺hash & (length �C 1)
����������λ�ô�������ֱ���ҵ����(==��equals)�� key

```java
public V get(Object key) {
    // ֮ǰ˵����key Ϊ null �Ļ����ᱻ�ŵ� table[0]������ֻҪ������ table[0] ��������Ϳ�����
    if (key == null)
        return getForNullKey();
    // 
    Entry<K,V> entry = getEntry(key);
 
    return null == entry ? null : entry.getValue();
}
```

Jdk1.8

1. ���� key �� hash ֵ������ hash ֵ�ҵ���Ӧ�����±�: hash & (length-1)
2. �ж������λ�ô���Ԫ���Ƿ�պþ�������Ҫ�ҵģ�������ǣ��ߵ�����
3. �жϸ�Ԫ�������Ƿ��� TreeNode������ǣ��ú�����ķ���ȡ���ݣ�������ǣ��ߵ��Ĳ�
4. ��������ֱ���ҵ����(==��equals)�� key

###### ������

V get(Object key)            ------------------����ָ������ӳ���ֵ�������ӳ�䲻�����ü���ӳ���ϵ���򷵻� null
V put(K key, V value)          ----------------��ָ����ֵ���ӳ���е�ָ��������
       

boolean isEmpty()               ---------------�����ӳ��δ������-ֵӳ���ϵ���򷵻� true
V remove(Object key)             --------------�������һ������ӳ���ϵ������Ӵ�ӳ�����Ƴ� 

void clear()                    ---------------�Ӵ�ӳ�����Ƴ�����ӳ���ϵ
int size()                      ---------------���ش�ӳ���еļ�-ֵӳ���ϵ�� 
    

### 3��Class LinkedHashMap<K,V>��Ϊ�˽��hashmap����֤ӳ��˳��ģ��������⣬����˳��

   public class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>

   HashMap��˫������϶�Ϊһ����LinkedHashMap
   ���ǽ�����Entry�ڵ�����һ��˫�������HashMap,ÿ��put����Entryӳ���ϵ�����˽��䱣�浽��ϣ���ж�Ӧ��λ����֮�⣬���Ὣ����뵽˫�������β�����ڲ���������ӵ�����������ά����һ��˫������before��After������ά��Entry������Ⱥ�˳���

   next����ά��HashMap�����洢λ�õ�Entry������ϣ��ͻʱ �ⲿ������ӵ���������Header,AccessOrder���ԣ�����������Ĭ��ΪFalse,ʹ�ò���ʵ������������ture��ʹ�÷���˳��������� �����������ʵ�� LAU �㷨

   û�ж������ӷ���

### 4��Class Hashtable<K,V> ��HashMap �������棬����,���̵߳�����£�ʹ��Hashmap����put������������ѭ��,����CPU�����ʽӽ�100%


public class Hashtable<K,V> extends Dictionary<K,V> implements Map<K,V>, Cloneable, java.io.Serializable 

###### 1���̰߳�ȫ(synchronized)�ͷ��̰߳�ȫ��

 Hashtable���̰߳�ȫ��ÿ����������ͬ�����������ڵ��̻߳���������HashMapҪ����Ч�ʵͣ���HashMapû��

###### 2��֧��֧��nullֵ��null��

**HashTable��֧��nullֵ��null��**,��HashMap����Ϊ��null�������⴦����null��hashCodeֵ��Ϊ��0���Ӷ��������ڹ�ϣ��ĵ�0��bucket��

###### 3��������ʽ��ͬ��HashMap�ĵ�����(Iterator)��fail-fast����������Hashtable �� enumerator������

###### 4����ʼ���������ݻ��Ʋ�ͬ

HashTable�ĳ�ʼ������11��HashMap�ĳ�ʼ������16.���ߵ��������Ĭ�϶���0.75��
HashMap����ʱ  ����ǰ����X2������������ʱ��Ҫ���¼���hash
Hashtable����ʱ����ǰ����X2+1

### 5��ConcurrentHashMap��HashTable�������棬HashTable����ʹ��synchronized����֤�̰߳�ȫ�������߳̾������ҵ������ HashTable ��Ч�ʷǳ�����


��һ���̷߳���HashTable��ͬ������ʱ�������̷߳���HashTable��ͬ������ʱ�����ܻ������������ѯ״̬

public class ConcurrentHashMap<K,V> extends AbstractMap<K,V> implements ConcurrentMap<K,V>, Serializable

######   JDK1.7ʵ�� ConcurrentHashMap �����ֶμ���

�����ݷֳ�һ��һ�εĴ洢��Ȼ���ÿһ��������һ��������һ���߳�ռ������������һ�������ݵ�ʱ�������ε�����Ҳ�ܱ������̷߳���

HashTable�����ڲ��������±��ֳ�Ч�ʵ��µ�ԭ������Ϊ���з���HashTable���̶߳����뾺��ͬһ����������������ж����ÿһ������������������һ�������ݣ���ô�����̷߳��������ﲻͬ���ݶε�����ʱ���̼߳�Ͳ���������������Ӷ�������Ч����߲�������Ч�ʣ������ConcurrentHashMap��ʹ�õ����ֶμ����ṹ
��Segment(��)����ṹ��HashEntry����ṹ���

###### Segment��һ�ֿ�����������ConcurrentHashMap��������Ľ�ɫ��HashEntry�����ڴ洢��ֵ��ӳ���ϵ

Segment�Ľṹ��HashMap���ƣ���һ�����������ṹ�� һ��Segment�����һ��HashEntry���飬ÿ��HashEntry��һ������ṹ��Ԫ��,ÿ��Segment�ػ���һ��HashEntry�������Ԫ��,����HashEntry��������ݽ����޸�ʱ���������Ȼ������Ӧ��Segment��

###### JDK1.8ʵ��ConcurrentHashMap 

**��Node����+����+����������ݽṹ��ʵ�֣���������ʹ��Synchronized��CAS������**�������������������Ż������̰߳�ȫ��HashMap����Ȼ��JDK1.8�л��ܿ���Segment�����ݽṹ�������Ѿ��������ԣ�ֻ��Ϊ�˼��ݾɰ汾	



###### Ĭ���� 16��Ҳ����˵ ConcurrentHashMap �� 16 �� Segments�����������ϣ����ʱ��������ͬʱ֧�� 16 ���̲߳���д��ֻҪ���ǵĲ����ֱ�ֲ��ڲ�ͬ�� Segment �ϡ����ֵ�����ڳ�ʼ����ʱ������Ϊ����ֵ������һ����ʼ���Ժ����ǲ��������ݵ�

###### ���캯�����г�ʼ���ģ���ô��ʼ����ɺ�

- Segment Ϊ 16��������������
- Segment[i] ��Ĭ�ϴ�СΪ 2������������ 0.75���ó���ʼ��ֵΪ 1.5��Ҳ�����Ժ�����һ��Ԫ�ز��ᴥ�����ݣ�����ڶ�������е�һ������
- �����ʼ���� segment[0]������λ�û��� null������ΪʲôҪ��ʼ�� segment[0]

###### ���ݻ��ƣ�

segment �������ݣ������� segment �����ڲ������� HashEntry\[] �������ݣ����ݺ�����Ϊԭ���� 2 ��



### 6��Interface SortedMap<K,V>��map���ӽӿڣ�����������Ĺ���(comparator), TreeMapʵ�������ļ̳нӿ�

### 7��Class TreeMap<K,V>����ӳ������������Ȼ˳��(����)�������򣬻��߸��ݴ���ӳ��ʱ�ṩ�� Comparator ��������

 TreeMap��֧��null��������֧��nullֵ������ʱ

 **TreeMap������һ�ź���� , R-B Tree��һ�ֶ�������������з��϶�����������ص�**

���췽����
	TreeMap()				    -------------ʹ�ü�����Ȼ˳����һ���µġ��յ���ӳ��
        TreeMap(Comparator<? super K> comparator)   ------------ ����һ���µġ��յ���ӳ�䣬��ӳ����ݸ����Ƚ����������� 
       	TreeMap(Map<? extends K,? extends V> m)     ------------ ����һ�������ӳ�������ͬӳ���ϵ���µ���ӳ�䣬
								 ��ӳ������������Ȼ˳�� ��������

  �������ṩһЩ�Լ�ֵ�Խ��й���˳��Ĳ�������

 	SortedMap<K,V> subMap(K fromKey, K toKey) --------------���ش�ӳ��Ĳ�����ͼ�����ֵ�ķ�Χ�� fromKey���������� toKey����������

###### SortedMap<K,V> tailMap(K fromKey)        ---------------���ش�ӳ��Ĳ�����ͼ��������ڵ��� fromKey�� 

Map.Entry<K,V> pollFirstEntry()          ---------------�Ƴ����������ӳ���е���С�������ļ�-ֵӳ���ϵ								        ���ӳ��Ϊ�գ��򷵻� null
Map.Entry<K,V> pollLastEntry()           ---------------�Ƴ����������ӳ���е����������ļ�-ֵӳ���ϵ��
���ӳ��Ϊ�գ��򷵻� null

Set<K>	keySet()
Collection<V> values() 		
Set<Map.Entry<K,V>>	entrySet()

### 8��Map�ı��������֣���ת��Set���ϣ��õ���������	

1������key�������� Set<K> **keySet()**  �����ص�set����
2���������е�value������ Collection<V> **values()** ������collection����
3��������ֵ�ԣ����� Set<Map.Entry<K,V>> **entrySet()** ����������ÿһ���ֵ�Ե�set���ϣ�
   	entry.getKey(), entry.getValue()



