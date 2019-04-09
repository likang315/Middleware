### Spring EL ���ʽ��(Spring Expression Language) ����Spring3�й��ܷḻǿ��ı��ʽ���ԣ���� SpEL

SpEL �������� OGNL �� JSF EL �ı��ʽ���ԣ��ܹ�������ʱ�������ӱ��ʽ����ȡ�������ԡ����󷽷����õ�

���е� SpEL ��֧��XML �� Annotation ���ַ�ʽ����ʽ��#{ SpEL expression }

##### 1������Spring EL ��������

������ Maven �� pom.xml �м������������������Զ����� SpEL ��������

```xml
xml�ļ������ã�
<bean id="itemBean" class="com.lei.demo.el.Item">
	<property name="name" value="itemA" />
	<property name="total" value="10" />
</bean>
<bean id="customerBean" class="com.lei.demo.el.Customer">
	<property name="item" value="#{itemBean}" />
	<property name="itemName" value="#{itemBean.name}" />
</bean>
```

######    Annotation��

Ҫ�� Annotation ��ʹ�� SpEL������Ҫͨ�� annotation ע�����
������� xml ��ע���� bean ���� java class �ж�����@Value��@Value ������ʱ��ʧ��

```java
@Value("#{itemBean}")
private Item item;
@Value("#{itemBean.name}")
private String itemName;
```

##### 2��SpEL �������ã�SpEL ���������� El ���з�������������������������ֵע�뵽������

```java
   Annotation
	1��@Value("#{'string'.toUpperCase()}")   	�ַ���ֱ�ӵ��ú���
	2��@Value("#{priceBean.getSpecialPrice()}") 	ʵ���������
   xml
	<bean id="customerBean" class="com.leidemo.el.Customer">
		<property name="name" value="#{'lei'.toUpperCase()}" />
		<property name="amount" value="#{priceBean.getSpecialPrice()}" />
	</bean>
```



##### 3��SpEL ������(Spring EL Operators)

Spring EL ֧�ִ��������ѧ���������߼�����������ϵ��������
1.��ϵ������
		���������� (==, eq)�������� (!=, ne)��С�� (<, lt),��С�ڵ���(<= ,le)������(>, gt)�����ڵ��� (>=, ge)
2.�߼�������
		������and��or��and not(!)
3.��ѧ������
		�������� (+)���� (-)���� (*)���� (/)��ȡģ (%)����ָ�� (^)

```java
   Annotation
	@Value("#{2 ^ 2}")
   xml
	<property name="testNotEqual" value="#{1 != 1}" />
```

##### 4��Spring EL ��Ŀ������ condition?true:false

?	
   Annotation
?	@Value("#{itemBean.qtyOnHand < 100 ? true : false}")
   xml

?	<property name="warning" value="#{itemBean.qtyOnHand &lt; 100 ? true : false}" />

##### 5��Spring EL ���� List��Map ����ȡֵ

 Annotation
	//get map where key = 'MapA'
	@Value("#{testBean.map['MapA']}")
	private String mapA;

//get first value from list, list is 0-based.
@Value("#{testBean.list[0]}")
private String list;

  xml

<property name="mapA" value="#{testBean.map['MapA']}" />								<property name="list" value="#{testBean.list[0]}" />








