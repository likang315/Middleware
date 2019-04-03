### java.lang ��java  ����Ļ����࣬����Ҫ������JVM�Զ�������

�ַ����������Ǹ����ֵ���ŵ�˳������ģ�**��������ĸ֮ǰ����д��ĸ��Сд��ĸ��**ǰ��

##### java�е��ַ�����String, StringBuffer,StringBuilder����������������String��

### String ���ǲ�����󣬲��������㻹��ƴ�Ӳ����µĽ������һ���µ� String ����Ĳ������ַ�������

```java
//charSequence:��һ��ֻ�����нӿ�
public final class String implements java.io.Serializable, Comparable<String>, CharSequence
{
        private final char value[];
        private int hash; // Default to 0
        public String(char value[]) {
            this.value = Arrays.copyOf(value, value.length);
        }
        String(char[] value, boolean share) {
            this.value = value;
        }
}

String ���� value[] �洢�ģ������������final���εģ��ǲ��ɱ�ģ��������� private ���εģ�û��get��set������ȷ���ⲿ�޷��޸ģ�����String�ķ�����û��ֱ�Ӷ�value����ֱ�ӵ��޸�
```



**String �� +  ������֧����ʵ����ʹ���� StringBuilder �Լ����� append��toString ��������**

### �ַ���ƴ�Ӽ��ַ�ʽ����

###### 1.ֱ���á�+���� 

ʹ����StringBuilder�Լ�����append��toString��������

###### 2.ʹ��String�ķ���concat 

concat��ʵ��������һ��char���͵�buf [ ]������Ҫƴ�ӵ��ַ����������������������ת����String����

```java
public String concat(String str)
{
        int otherLen = str.length();
        if (otherLen == 0) {
            return this;
        }
        int len = value.length;
        //��ԭ���鿽������������
        char buf[] = Arrays.copyOf(value, len + otherLen);
        str.getChars(buf, len);
        return new String(buf, true);
}

void getChars(char dst[], int dstBegin) {
        System.arraycopy(value, 0, dst, dstBegin, value.length);
}
```



###### 3.ʹ��StringBuilder �� append 

###### 4.ʹ��StringBuffer��apend

append�ķ������ǵ��ø���AbstractStringBuilder��append������ֻ����StringBuffer�ǵ�append��������sychronized�ؼ��֣�������̰߳�ȫ�ģ�append�������£�����ҪҲ������char���鱣���ַ�



### rerplace,replaceAll,replaceFirst()������

String replace(char oldChar, char newChar)  ----���µ��ַ������ַ����滻�ɵ��ַ������ַ���					����������ɾ�����пո񣩣�**�����ַ������滻**

String replaceAll(String regex, String replacement) ---���µ��ַ����滻���ַ����е�����Ŀ��λ�õ��滻��**��������ʽ��** replaceAll("\\d", "*") ����һ���ַ������е������ַ��������Ǻ�

String   replaceFirst(String regex, String replacement)  **ֻƥ���һ����ƥ���ַ���������������ʽ��**



### String.valueof( ) �� Integer.toString() �Ĳ�ͬ

Integer.toString() �� i ת��Ϊ buf �������飬Ȼ�� **new String(buf, true)**													String.valueof() ����

```java
public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
}
public static String valueOf(char data[]) {
    return new String(data);
}
```



### Switch ��String��֧��

```java
public void test(String status) {
    switch (status)
    {
        case "killed":
            break;
        case "alive": 
            break;
        default ��
            break;
    }
}
```



 ���췽����
	String()
		��ʼ��һ���´����� String ����ʹ���ʾһ�����ַ�����
	String(String original) 
		��ʼ��һ���´����� String ����ʹ���ʾһ���������ͬ���ַ�����
	String(StringBuffer buffer) 
		����һ���µ��ַ������������ַ��������������е�ǰ�������ַ�����
	String(StringBuilder builder)
		����һ���µ��ַ������������ַ��������������е�ǰ�������ַ�����

 ������

###### 	int length()  -----���ظ��ַ����ĳ���

###### 	boolean isEmpty()  ----�ж��ַ����Ƿ�Ϊ��

###### 	char[] toCharArray()  ---��String����char��������

###### char charAt(int index)  ---��������ֵ��ȡ��Ӧ���ַ�,��0��ʼ

int compareTo(String anotherString) ---�Ƚ��ַ����Ĵ�С

?	String concat(String str)   -----����ַ���ĩβƴ���ַ�����ͬ+�Ų���
?	boolean contains(CharSequence s)  -----�ж��Ƿ����Ŀ���ַ���
?	String trim() ---ȥ�����ַ�����ǰ��Ϊwightspace,ռλ��
?	boolean equals(Object anObject)  ---�жϸ��ַ�����Ŀ���ַ��������Ƿ���ȣ�����д

###### 	

?	String toLowerCase() ---ȫ��ת����Сд
?	String toUpperCase()  --ȫ��ת���ɴ�д
?	boolean  endsWith(String suffix)  ---�ж���Ŀ���ַ�����β
?	boolean  startsWith(String suffix);---�жϸ��ַ����Ƿ���Ŀ���ַ�����ʼ

?	int indexOf(int ch)  ---����ָ���ַ��ڱ��ַ����е�һ�γ��ֵ�����ֵ�ĵ�ַ��ascall ��ֵ��
?	int indexOf(int ch, int fromIndex)---  ��ָ��λ�ÿ�ʼ����Ŀ���ַ����ص�һ�γ�������ֵ�ĵ�ַ
?	int indexOf(String str) ----  ����ָ�����ַ����ڴ��ַ����е�һ�γ��ִ��������ĵ�ַ
   	int indexOf(String str, int fromIndex) ----����ָ�����ַ����ڴ��ַ����е�һ�γ��ִ������ĵ�ַ����ָ��������ʼ 
?	int lastIndexOf(String s) ----����ָ���ַ����ڱ��ַ��������һ�γ��ֵ�����ֵ�ĵ�ַ
?	int lastIndexOf(String s, int fromIndex) ----��ָ��λ�ÿ�ʼ����Ŀ���ַ����������һ�γ��ֵ�λ�� 
?	

###### 	String[] split(String sign)  ----ʹ��ָ���ĸ����ַ����з��룬�����������ַ������������

###### ?	split(String sign,int limit)----����ָ���ķָ������ַ������в�֣������޶���ֵĴ���������limit-1�� 

```java
		String str=("abc,dfg,dfg,sfd,efg");
		String[] p=str.split(",");
		for(String i:p) {
			System.out.println(i);
		}
```

```java
	String substring(int beginIndex) ----��ָ��λ�ÿ�ʼ��ȡ���ַ���
	String substring(int beginIndex, int endIndex) --��ָ��λ�ÿ�ʼ��ȡ��ָ����λ�ã���������������λ��
	
     String  static String valueOf(int i)   -----��int��������String���󣬻��ߺͿ��ַ���ƴ��
	Integet  static int parseInt(String s)  -----������Ҫ����ַ�������ת����������Ӧ�Ļ�����������
```



### StringBuffer ���̰߳�ȫ�Ŀɱ��ַ���

######  public final class StringBuffer  extends AbstractStringBuilder implements java.io.Serializable, CharSequence

ÿ���ַ�������������һ����������ֻҪ�ַ������������������ַ����еĳ���û�г���������������������µ��ڲ���������������ڲ��������������������Զ�����

### ���ݻ���

����**��ǰ���鳤�ȵ� 2��+2 �� �������ַ�������+ԭ�����鳤�� ���бȽ�,**���ǰ��С�ں���,��ô���ݺ�ĳ��Ⱦ��Ǻ���,���ǰ�ߴ��ں�����ô���ݺ�����鳤�Ⱦ���ǰ��

���췽����
StringBuffer() 
          ����һ�����в����ַ����ַ�����������**��ʼ����Ϊ 16 ���ַ�**	 

StringBuffer(String str) 
          ����һ���ַ�����������������**���ݳ�ʼ��Ϊָ�����ַ������ݣ�+16**

```java
public final class StringBuffer  extends AbstractStringBuilder implements java.io.Serializable, CharSequence
{
    private transient char[] toStringCache;
    public StringBuffer() {
        super(16);
    }
    public StringBuffer(String str) {
        super(str.length() + 16);
        append(str);
    }
    private int newCapacity(int minCapacity) {
    	//����+2 ����
        int newCapacity = (value.length << 1) + 2;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity; //��ǰ�����С��С��Stringƴ�ӳ�
        }
        return (newCapacity <= 0 || MAX_ARRAY_SIZE - newCapacity < 0)
            ? hugeCapacity(minCapacity) : newCapacity;
    }
}
```

?	 
������

###### StringBuffer append(String str) 

?	���ַ���������׷���ַ���

delete(int start, int end) 
     	ɾ����ʲôλ�õ�ʲôλ���ַ�

deleteCharAt(int index) 
        ɾ��ʲôλ���ַ�

StringBuffer insert(int offset, String str) 
      ��ʲôλ�ÿ�ʼ�����ַ������ַ�

StringBuffer reverse() 
	�����滻

void setCharAt(int index, char ch) 
          �Ѻ�����ַ�����ǰ���λ���滻

String substring(int start) 

###### String toString()  

###### StringBuffer replace(int start, int end, String str)  

###### int length()  



### StringBuilder�����߳�Ч�����

###### public final class StringBuilder  extends AbstractStringBuilder implements java.io.Serializable, CharSequence

String���͵Ķ��󾭹�������ֻ��һ���µĶ��󣬻ᵼ��ϵͳ�ڴ���˷ѣ���ʹ��StringBuilder������ַ����������������Ȼ����toString תΪString���� ������ִ��ʱ�ڴ���˷�

���췽����
StringBuilder() 
          ����һ�����в����ַ����ַ�����������**��ʼ����Ϊ 16 ���ַ�**

StringBuilder(CharSequence seq) 
          ����һ���ַ�����������������ָ���� CharSequence ��ͬ���ַ�

StringBuilder(String str) 
          ����һ���ַ���������������ʼ��Ϊָ�����ַ������ݡ�

������
 StringBuilder append(String str)  

######  char charAt(int index) 

######  StringBuilder delete(int start, int end) 

######  StringBuilder deleteCharAt(int index)  

 StringBuilder insert(int offset, String str)  

 int lastIndexOf(String str)  

 int length()  

######  StringBuilder replace(int start, int end, String str) 

######  StringBuilder reverse() 

 String substring(int start) 

 String toString() 

 

### ���ߵ�����

####   1��ִ��Ч��   StringBuilder ������ģ�> StringBuffer > String

����	String ������ԭ��

����	StringΪ�ַ�������final���εģ���StringBuilder��StringBuffer��Ϊ�ַ�����������String����һ������֮��ö�����
	���ɸ��ĵģ����ĵĻ�����һ���µ�String�������������ߵĶ����Ǳ������ǿ��Ը��ĵ�

####   2:���̰߳�ȫ�� 

StringBuilder���̲߳���ȫ�ģ���StringBuffer���̰߳�ȫ��(**���÷���ʱ����ͬ����)**

String��������**�������ַ���������������̰߳�ȫ**�� final ���Σ�

StringBuilder��������**���߳������ַ����������д������������**

StringBuffer������**���߳������ַ����������д������������**



### �ַ����أ��ַ��������أ���Ϊ�˱������������ͬ String �����˷���Դ����

���ƣ�ֱ�Ӹ�ֵ����һ��Stringʱ�����ȼ���ַ������Ƿ���������ֵ��ȵ��ַ���������У����ٴ�����ֱ�ӷ����ַ������ж�������ã���û�У��򴴽���Ȼ��ŵ��ַ������У����ҷ����½���������á���new ʱ���鳣�������Ƿ�����������ȵ��ַ��������в���Ѷ�����볣�����У���û�������

���� intern�������������鳣�������Ƿ��к͵�ǰ�Ķ���**��������ͬ�����ö���**�����У��򷵻��ַ������еĶ�����û����ŵ��ַ������У������ص�ǰ����

#####  public native String intern();

```java
String s1="123";
String s2="123";
String s = new String ("123");

System.out.println(s1==s2); //true  ��������
System.out.println(s==s1);  //false �����з���
System.out.println(s1.equals(s)); //true ���Ƚ�����
```


