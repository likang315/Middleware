### 1��Interface Map<K,V>��һ��ӳ���ϵ������ ӳ�䵽 ֵ�Ķ��󣬴洢����ֵ������˫�����ݵļ���

######  �ص㣺һ��ӳ�䲻�ܰ����ظ��ļ�,ÿ�������ֻ��ӳ�䵽һ��ֵ

**K - ӳ��������ͣ�����**
**V - ӳ��ֵ������**

```java
public interface Map<K,V> 
{
    V get(Object key);
	V put(K key, V value);
	boolean containsKey(Object key);
	...
	interface Entry<K,V> 
    {
    	K getKey();
        V getValue();
    }
}
```

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
         		

### 2��Class HashMap<K,V>�����ڹ�ϣ��� Map �ӿڵ�ʵ����,���̰߳�ȫ���࣬���Ҳ���֤ӳ���˳�򣬵��ǲ��Ҹ�Ч������ null ֵ �� null ���Ĵ���

###### 1��ʵ�ֵײ�ԭ��

jdk1.7:��������+������jdk1.8������,����ͺ������ʵ�֣����Ĳ�ѯ�ٶȿ���Ҫ����Ϊ����ͨ������hashֵ�������洢��λ��

```java
public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable 
{
	//���key-value��ֵ��
    transient Node<K,V>[] table;
    //map��С
    transient int size;
    //�޸�����
    transient int modCount;
	//��ֵ
    int threshold;
    //��������
    final float loadFactor;
    //Ĭ�ϵļ�������
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;  //��ʼ���� 16
    
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; 
    }
    
    //��̬�ڲ��� Node<K,V>
    static class Node<K,V> implements Map.Entry<K,V> {
            final int hash;   //rehash ʱ�����ڼ���hash
            final K key;
            V value;
            Node<K,V> next;
            .....
    }
     
     //ָ����ʼ�����ͼ�������
     public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
     }
     
    //ָ�����µ���ֵ
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
    
}
```

###### 2��hash��ͻ

   HashMap��ͨ��keyֵ��hashCode����������hashֵ����λ����������ֲ���Ծ��ȵ�λ�ã�����洢�Ķ���Զ��ˣ����п��ܲ�ͬ�Ķ������������hashֵ����ͬ�ģ���ͳ�������ν��hash��ͻ

HashMap �ײ���ͨ�����������hash��ͻ�ģ�**��������Ͱ�����** �����ŵ�ַ�����ٹ�ϣ��

######   3��Ӱ��hashmap�����ܣ�Capacity �� loadFactor

���ڵ�������(threshold)��ֵ����Ҫ���ݣ����ֵ�ļ��㷽ʽ�� **capacity * load factor**

###### 4���������ݣ�HashMap����ʱ  ����ǰ����X2������������ʱ��Ҫ���¼���hash

����һ���µ������ԭ�������鸳ֵ��ȥ����ԭ�����������������ϵİ��������洢

######  5��HashMap Ϊʲô��ʼ����Ϊ  16

1��**Ϊ�˼���hashֵ����ײ,**��Ҫʵ��һ���������ȷֲ���hash����,�� HashMap ��ͨ������key��hashcodeֵ,������λ����
��ʽ:  index = e.hash & (newCap - 1)

����**����16��������2����**, length - 1��ֵ��������������λȫΪ1,���������,index �Ľ����ͬ��hashcode��λ��ֵ��ֻҪ�����hashcode����ֲ�����,hash�㷨�Ľ�����Ǿ��ȵģ�����,HashMap��Ĭ�ϳ���Ϊ16,��Ϊ�˽���hash��ײ�ļ���

2��**�����Ƶ���λ��������ǳ���**

###### 6��Hash()

���������λ�����㣬��key.hashcode() ��һ�����м�������֤���ջ�ȡ�Ĵ洢λ��**�����ֲ�����**

```java
static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16); //�޷�������
}
```

###### 7��ʲôʱ���ú����ʲôʱ��������

��ͰԪ�أ�Ͱ����ȣ�����8�����ұ�����64�Ὣ����ת��Ϊ����������������Ԫ��С��6��ʱ���Ὣ�����ת��Ϊ����

��Ϊ�������Ҫ�������������������� ����������Ҫ
���Ԫ**��С��8��**����ѯ�ɱ��ߣ������ɱ���
���**Ԫ�ش���8��**����ѯ�ɱ��ͣ������ɱ���

###### 8��ΪʲôҪ��������

��Ϊ֮ǰhashmap�ײ�ṹ��������������ǵ����ݴ�һ���̶ȵ�ʱ�򣬼�ʹ��**������洢Ҳ�ǱȽϳ���������ɾ�Ĳ飬������Ĳ���Ч�ʸߣ��൱�ڶ���**



###### 9��jdk1.7 ����ʱ��ͷ����ɻ������� (�߲���ʱ)

����ʱ���������߳�ͬʱ����put�Ĳ���ʱ���պ�Ҫ���ݣ�һ���̸߳����ݾ����ߣ���һ���߳�ִ�����ݣ���hash��ʱ����һ���̼߳�������ʱ�͵��»�������

![HashMapѭ������.png](https://github.com/likang315/Java/blob/master/Java_note/4%EF%BC%9A%E6%B3%9B%E5%9E%8B%EF%BC%8C%E9%9B%86%E5%90%88%EF%BC%8CMap/HashMap%E5%BE%AA%E7%8E%AF%E9%93%BE%E8%A1%A8.png?raw=true)



###### 10��put(K key, V value)  ������ jdk1.8 ��

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict)
{
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // table�Ƿ�Ϊnull ���� length����0, ����������resize�������г�ʼ��
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    
    // ͨ��hashֵ��������λ��, ���table�������λ�ýڵ�Ϊ��������һ��
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else { 
        // table �������λ�ò�Ϊ��
        Node<K,V> e; K k;
        // �ж�p�ڵ��hashֵ��keyֵ�Ƿ�������hashֵ��keyֵ���
        if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) 
            e = p;
        // �ж�p�ڵ��Ƿ�ΪTreeNode, ���������ú������putTreeVal��������Ŀ��ڵ�
        else if (p instanceof TreeNode) 
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else 
        {	
            //�ߵ������p�ڵ�Ϊ��ͨ����ڵ�,����������, binCount����ͳ�ƽڵ���
            for (int binCount = 0; ; ++binCount)
            {  
                // p.next Ϊ�մ�������Ŀ��ڵ�������һ���ڵ��������β��
                if ((e = p.next) == null)
                {
                    p.next = newNode(hash, key, value, null);
                    // ����ڵ��Ƿ񳬹�8��, ��һ����Ϊѭ���Ǵ�p�ڵ����һ���ڵ㿪ʼ��
                    if (binCount >= TREEIFY_THRESHOLD - 1)
                        treeifyBin(tab, hash); // �������8��������treeifyBin������������ת��Ϊ�����
                    break;
                }
                // e�ڵ��hashֵ��keyֵ���봫������, ��e��ΪĿ��ڵ�,����ѭ��
                if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) 
                    break;
                p = e;  // ��pָ����һ���ڵ�
            }
        }
        // e��Ϊ���������ݴ����hashֵ��keyֵ���ҵ��˽ڵ�,���ýڵ��value����,����oldValue
        if (e != null) { 
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e); // ����LinkedHashMap
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold) // ����ڵ�󳬹���ֵ���������
        resize();
    afterNodeInsertion(evict);  // ����LinkedHashMap
    return null;
}
```

1����һ��put ��ʱ���ж��Ƿ�Ϊnull ���߳���Ϊ 0����ᴥ������� resize()
**��ʼ���ǵ�һ�� resize �ͺ�����������Щ��һ������Ϊ���������� null ��ʼ����Ĭ�ϵ� 16 ���Զ���ĳ�ʼ����**

2���ж�**��һ������Ƿ�Ϊ��**��Ϊ�������

3���ж�**��һ������ hash �� key�Ƿ����** ������ȣ�ֱ���滻�µ�value

4���ж��Ƿ�Ϊ**������Ľ��**����Ϊ���������ú������ putTreeVal ����

5����������**���뵽β��(β��)**��ͬʱ�ж��Ƿ�Ϊ�ھŸ���㣬ת��Ϊ�����

6���ж��Ƿ񳬳���ֵ����������Ӧresize() ;



###### 11��get(Object key) ����

```java
public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
}

final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null) 
    {
       //�ж�Ͱ�ĵ�һ������ǲ��ǣ���hash����key
       if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
        if ((e = first.next) != null)
        {
             //�ж��ǲ��Ǻ�����Ľ��
             if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
             //���������Ҷ�Ӧ�Ľ��
             do {
                  if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
         }
     }
     return null;
}
```

1. ���� **key �� hash ֵ**������ hash ֵ�ҵ�**��Ӧ�����±�**: hash & (length-1)
2. �ж�**�����λ�ô���Ԫ���Ƿ�պþ�������Ҫ�ҵ�**��������ǣ��ߵ�����
3. **�жϸ�Ԫ�������Ƿ��� TreeNode**������ǣ��ú�����ķ���ȡ���ݣ�������ǣ��ߵ��Ĳ�
4. **��������ֱ���ҵ����**



###### 12��resize��������

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    //�����±����������ֵ
    if (oldCap > 0) {//  ��table��Ϊ��
        if (oldCap >= MAXIMUM_CAPACITY) {   // ��table�����������������ֵ
            threshold = Integer.MAX_VALUE;  // ������ֵΪInteger.MAX_VALUE
            return oldTab;
        }
        // �������*2< ����������� >=16, ����ֵ����Ϊԭ��������
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)   
            newThr = oldThr << 1; 
    }
    else if (oldThr > 0)   //�ɱ������Ϊ 0,  �ɱ�����ֵ����0
        newCap = oldThr;	// ���±����������Ϊ�ɱ����ֵ 
    else {	
        // �ɱ������Ϊ0, �ɱ����ֵΪ0, ��Ϊ�ձ�����Ĭ����������ֵ
        newCap = DEFAULT_INITIAL_CAPACITY; 
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    
    if (newThr == 0) {  // ����±����ֵΪ��, ��ͨ���µ�����*�������ӻ����ֵ
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr; // ����ǰ��ֵ��ֵΪ�ռ���������µ���ֵ
//-------------------------------------------------------------------------------------------------
    
    @SuppressWarnings({"rawtypes","unchecked"})
    // �����±�,����Ϊ�ռ��������������
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab; // ����ǰ�ı�ֵΪ�¶���ı�
    
    //����ϱ�Ϊ��, ����������ڵ㸳ֵ���±�
    if (oldTab != null) {  
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {  // ������ֵΪj���ϱ�ͷ�ڵ㸳ֵ��e
                oldTab[j] = null; // ���ϱ�Ľڵ�����Ϊ��, �Ա������ռ������տռ�
                // ���e.nextΪ��, ������ϱ�ĸ�λ��ֻ��1���ڵ�, 
                // ͨ��hashֵ�����±������λ��, ֱ�ӽ��ýڵ���ڸ�λ��
                if (e.next == null) 
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                	 // ����treeNode��hash�ֲ�(���������һ��else�����ݼ�����ͬ)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap); 
                
                else { 
                    Node<K,V> loHead = null, loTail = null; // �洢��ԭ����λ����ͬ�Ľڵ�
                    Node<K,V> hiHead = null, hiTail = null; // �洢����λ��Ϊ:ԭ����+oldCap�Ľڵ�
                    Node<K,V> next;
                    
                    do {
                        next = e.next;
                 //���e��hashֵ���ϱ����������������Ϊ0,�����ݺ������λ�ø��ϱ������λ��һ��
                        if ((e.hash & oldCap) == 0) {   
                            if (loTail == null) // ���loTailΪ��, ����ýڵ�Ϊ��һ���ڵ�
                                loHead = e; // ��loHead��ֵΪ��һ���ڵ�
                            else    
                                loTail.next = e;    // ���򽫽ڵ������loTail����
                            loTail = e; // ����loTail��ֵΪ�����Ľڵ�
                        }
                 //���e��hashֵ���ϱ����������������Ϊ1,�����ݺ������λ��Ϊ:�ϱ������λ�ã�oldCap
                        else {  
                            if (hiTail == null) // ���hiTailΪ��, ����ýڵ�Ϊ��һ���ڵ�
                                hiHead = e; // ��hiHead��ֵΪ��һ���ڵ�
                            else
                                hiTail.next = e;    // ���򽫽ڵ������hiTail����
                            hiTail = e; // ����hiTail��ֵΪ�����Ľڵ�
                        }
                    } while ((e = next) != null);
                    
                    if (loTail != null) {
                        loTail.next = null; // ���һ���ڵ��next��Ϊ��
                        newTab[j] = loHead; // ��ԭ����λ�õĽڵ�����Ϊ��Ӧ��ͷ���
                    }
                    if (hiTail != null) {
                        hiTail.next = null; // ���һ���ڵ��next��Ϊ��
                        newTab[j + oldCap] = hiHead; // ������λ��Ϊԭ����+oldCap�Ľڵ�����Ϊͷ���
                    }
                }
            }
        }
    }
    return newTab;
}
```

1���ж� **�ɱ������Ϊ0, �ɱ����ֵΪ0**, ���ǣ���Ϊ�ձ�����Ĭ����������ֵ

2�� ��**�ɱ������Ϊ 0,  �ɱ�����ֵ����0**�����±������Ϊ�ɱ����ֵ

3����**�ɱ�Ϊ��**������ ����*2< ����������� >=16, ����ֵ����Ϊԭ��������

4�� �ѽ���ǰ��ֵ��ֵΪ�ռ���������µ���ֵ�����϶��Ǽ������������ֵ

5�������ɱ����������ÿ��Ͱ��Ӧ��Ԫ�أ��ж��ǲ��Ǻ������㣬���ǣ����ú��������Ӧ�ķ���

6�������ǣ���������ķ�ʽ�������µ�Ͱ�У�ÿһ��Ԫ���µ�index ���ܲ�һ������һ��������Ϊͷ�����룬��һ�����ͺ;ɱ�����Node

7��������ɱ�����newTab



##### ������

###### V get(Object key)            ------------------����ָ������ӳ���ֵ�������ӳ�䲻�����ü���ӳ���ϵ���򷵻� null

###### V put(K key, V value)          ----------------��ָ����ֵ���ӳ���е�ָ��������

###### V remove(Object key)             --------------�������һ������ӳ���ϵ������Ӵ�ӳ�����Ƴ� 

###### boolean isEmpty()               ---------------�����ӳ��δ������-ֵӳ���ϵ���򷵻� true

###### int size()                      ---------------���ش�ӳ���еļ�-ֵӳ���ϵ�� 




### 3��Class LinkedHashMap<K,V>��Ϊ�˽�� hashmap ����֤ӳ��˳��ģ��������⣬����˳��

![LinkedHashMap.jpg](https://github.com/likang315/Java/blob/master/Java_note/4%EF%BC%9A%E6%B3%9B%E5%9E%8B%EF%BC%8C%E9%9B%86%E5%90%88%EF%BC%8CMap/LinkedHashMap.jpg?raw=true)

```java
public class LinkedHashMap<K,V> extends HashMap<K,V> implements Map<K,V>
{
	// ����ָ��˫�������ͷ��
    transient LinkedHashMap.Entry<K,V> head;
    //����ָ��˫�������β��
    transient LinkedHashMap.Entry<K,V> tail;
    //����ָ��LinkedHashMap�ĵ���˳��true���ʾ���ջ��ڷ��ʵ�˳�������У���˼�������ʹ�õ�entry��������		�����ĩβ,false���ʾ���ղ���˳���������뵽β��
    //��������Ĭ��ΪFalse,ʹ�ò���ʵ������������ture��ʹ�÷���˳��������� �����������ʵ�� LAU �㷨
    final boolean accessOrder;
    public LinkedHashMap(int initialCapacity,float loadFactor,boolean accessOrder) {
   		super(initialCapacity, loadFactor) ;//HashMap
   		this.accessOrder = accessOrder ;
    }
    //ȡֵ
    public V get(Object key) {
          Node<K,V> e;
          //����HashMap��getNode�ķ���
          if ((e = getNode(hash(key), key)) == null)
            return null;
          //��ȡֵ��Բ���accessOrder�����жϣ����Ϊtrue��ִ��afterNodeAccess
          if (accessOrder)
            afterNodeAccess(e);  //�����ʹ�õ�Entry�������������ĩβ
          return e.value;
	}
	void afterNodeInsertion(boolean evict) { // possibly remove eldest
        LinkedHashMap.Entry<K,V> first;
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            removeNode(hash(key), key, null, false, true);
        }
    }
	//�Ƴ��˽�㵽β��
    void afterNodeAccess(Node<K,V> e) { 
        LinkedHashMap.Entry<K,V> last;
        if (accessOrder && (last = tail) != e) {
            LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            p.after = null;
            if (b == null)
                head = a;
            else
                b.after = a;
            if (a != null)
                a.before = b;
            else
                last = b;
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;  //����+1
        }
    }
    
    static class Entry<K,V> extends HashMap.Node<K,V> {
            //����ά��˫������
            Entry<K,V> before, after;
            Entry(int hash, K key, V value, Node<K,V> next) {
                super(hash, key, value, next);
            }
    }
    
    abstract class LinkedHashIterator {
          //��¼��һ��Entry
          LinkedHashMap.Entry<K,V> next;
          //��¼��ǰ��Entry
          LinkedHashMap.Entry<K,V> current;
          //��¼�Ƿ����˵��������е��޸�
          int expectedModCount;

          LinkedHashIterator() {
            //��ʼ����ʱ���head��next
            next = head;
            expectedModCount = modCount;   //Fail-Fast
            current = null;
          }

          public final boolean hasNext() {
            return next != null;
          }

          //������õ�������ʽ�ı�����ʽ
          final LinkedHashMap.Entry<K,V> nextNode() {
            LinkedHashMap.Entry<K,V> e = next;
            if (modCount != expectedModCount)
              throw new ConcurrentModificationException();
            if (e == null)
              throw new NoSuchElementException();
            //��¼��ǰ��Entry
            current = e;
            //ֱ����after��next
            next = e.after;
            return e;
          }

          public final void remove() {
                Node<K,V> p = current;
                if (p == null)
                  throw new IllegalStateException();
                if (modCount != expectedModCount)
                  throw new ConcurrentModificationException();
                current = null ;
                K key = p.key;
                removeNode(hash(key), key, null, false, false);
                expectedModCount = modCount;
          }
	}
}
```

**HashMap �� ˫������ �϶�Ϊһ** ����LinkedHashMap
���ǽ�����Entry�ڵ�����һ��˫�������HashMap,ÿ�� **put ���� Entryӳ���ϵ�����˽��䱣�浽��ϣ���ж�Ӧ��λ����֮�⣬���Ὣ����뵽˫�������β��**���ڲ���������ӵ�����������ά����һ��˫������**��before��After**������ά��Entry������Ⱥ�˳���

next ����ά�� HashMap �����洢λ�õ� Entry ������ϣ��ͻʱ �ⲿ������ӵ���������Header,AccessOrder���ԣ���



### 4��Class Hashtable<K,V> ��HashMap �������棬����,���̵߳�����£�ʹ��Hashmap����put������������ѭ��,����CPU�����ʽӽ�100%


public class Hashtable<K,V> extends Dictionary<K,V> implements Map<K,V>, Cloneable, java.io.Serializable 

###### 1���̰߳�ȫ(synchronized)�ͷ��̰߳�ȫ��

 **Hashtable���̰߳�ȫ��ÿ����������ͬ����**�������ڵ��̻߳���������HashMapҪ����Ч�ʵͣ���HashMapû��

###### 2��֧��֧��nullֵ��null��

**HashTable��֧��nullֵ��null��**,��HashMap����Ϊ��null�������⴦��**����null��hashCodeֵ��Ϊ��0**���Ӷ��������ڹ�ϣ��ĵ�0��bucket��

###### 3��������ʽ��ͬ��HashMap�ĵ�����(Iterator)��fail-fast����������Hashtable �� enumerator������

###### 4����ʼ���������ݻ��Ʋ�ͬ

HashTable�ĳ�ʼ������11��HashMap�ĳ�ʼ������16.���ߵ��������Ĭ�϶���0.75
**HashMap����ʱ  ����ǰ����X2��**����������ʱ��Ҫ���¼���hash
**Hashtable����ʱ����ǰ����X2+1**



### 5��ConcurrentHashMap��HashTable�������棬HashTable����ʹ��synchronized����֤�̰߳�ȫ�������߳̾������ҵ������ HashTable ��Ч�ʷǳ�����


��һ���̷߳���HashTable��ͬ������ʱ�������̷߳���HashTable��ͬ������ʱ�����ܻ������������ѯ״̬

public class ConcurrentHashMap<K,V> extends AbstractMap<K,V> implements ConcurrentMap<K,V>, Serializable

######   JDK1.7ʵ�� ConcurrentHashMap �����ֶμ���

�����ݷֳ�һ��һ�εĴ洢��Ȼ���ÿһ��������һ��������һ���߳�ռ������������һ�������ݵ�ʱ�������ε�����Ҳ�ܱ������̷߳���

HashTable�����ڲ��������±��ֳ�Ч�ʵ��µ�ԭ������Ϊ**���з���HashTable���̶߳����뾺��ͬһ����**������������ж����ÿһ������������������һ�������ݣ���ô�����̷߳��������ﲻͬ���ݶε�����ʱ���̼߳�Ͳ���������������Ӷ�������Ч����߲�������Ч�ʣ������ConcurrentHashMap��ʹ�õ����ֶμ����ṹ
��Segment(��)����ṹ��HashEntry����ṹ���

###### Segment��һ�ֿ�����������ConcurrentHashMap��������Ľ�ɫ��HashEntry�����ڴ洢��ֵ��ӳ���ϵ

Segment�Ľṹ��HashMap���ƣ���һ�����������ṹ�� һ��Segment�����һ��HashEntry���飬ÿ��HashEntry��һ������ṹ��Ԫ��,ÿ��Segment�ػ���һ��HashEntry�������Ԫ��,����HashEntry��������ݽ����޸�ʱ���������Ȼ������Ӧ��Segment��

###### JDK1.8ʵ��ConcurrentHashMap 

**�� Node����+����+����������ݽṹ��ʵ�֣���������ʹ��Synchronized��CAS������**�������������������Ż������̰߳�ȫ��HashMap����Ȼ��JDK1.8�л��ܿ���Segment�����ݽṹ�������Ѿ��������ԣ�ֻ��Ϊ�˼��ݾɰ汾	



###### Ĭ���� 16��Ҳ����˵ ConcurrentHashMap �� 16 �� Segments�����������ϣ����ʱ��������ͬʱ֧�� 16 ���̲߳���д��ֻҪ���ǵĲ����ֱ�ֲ��ڲ�ͬ�� Segment �ϡ����ֵ�����ڳ�ʼ����ʱ������Ϊ����ֵ������һ����ʼ���Ժ����ǲ��������ݵ�

###### ���캯�����г�ʼ���ģ���ô��ʼ����ɺ�

- Segment Ϊ 16��������������
- Segment[i] ��Ĭ�ϴ�СΪ 2������������ 0.75���ó���ʼ��ֵΪ 1.5��Ҳ�����Ժ�����һ��Ԫ�ز��ᴥ�����ݣ�����ڶ�������е�һ������
- �����ʼ���� segment[0]������λ�û��� null������ΪʲôҪ��ʼ�� segment[0]

###### ���ݻ��ƣ�

**segment �������ݣ������� segment �����ڲ������� HashEntry\[] �������ݣ����ݺ�����Ϊԭ���� 2 ��**



### WeakHashMap

��ĳ������������������ʹ��ʱ���ᱻ��WeakHashMap�б��Զ��Ƴ���������������������



### 6��Interface SortedMap<K,V>��map���ӽӿڣ�����������Ĺ���(comparator), TreeMapʵ�������ļ̳нӿ�



### 7��Class TreeMap<K,V>����ӳ������������Ȼ˳��(����)�������򣬻��߸��ݴ���ӳ��ʱ�ṩ�� Comparator ��������

 TreeMap ��֧��null�������� ֧��nullֵ������ʱ��ÿ����㶼��һ��Entry<K,V>

 **TreeMap������һ�ź���� , R-B Tree��һ�ֶ�������������з��϶�����������ص�**

TreeMap����**�������Red-Black tree��ʵ��**����ӳ�����**�������Ȼ˳���������**�����߸���**����ӳ��ʱ�ṩ�� Comparator ��������**������ȡ����ʹ�õĹ��췽��

���췽����
	TreeMap()				    -------------ʹ�ü�����Ȼ˳����һ���µġ��յ���ӳ��
        TreeMap(Comparator<? super K> comparator)   ------------ ����һ���µġ��յ���ӳ�䣬��ӳ����ݸ����Ƚ����������� 
       	TreeMap(Map<? extends K,? extends V> m)     ------------ ����һ�������ӳ�������ͬӳ���ϵ���µ���ӳ�䣬
								 ��ӳ������������Ȼ˳�� ��������

  �������ṩһЩ�Լ�ֵ�Խ��й���˳��Ĳ�������

###### SortedMap<K,V> subMap(K fromKey, K toKey) --------------���ؼ�ֵ�ķ�Χ�� fromKey���������� toKey�����������Ĳ�����ͼ

###### SortedMap<K,V> tailMap(K fromKey)             ---------------���ش�ӳ��Ĳ���Entry��������ڵ��� fromKey





### 8��Map�ı��������֣���ת��Set���ϣ��õ���������	

1������key�������� Set<K> **keySet()**  �����ص�set����
2���������е�value������ Collection<V> **values()** ������collection����
3��������ֵ�ԣ����� Set<Map.Entry<K,V>> **entrySet()** ����������ÿһ���ֵ�Ե�set���ϣ�
     **entry.getKey(), entry.getValue()**







