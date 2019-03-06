
����ģʽ�����ڴ�����ģʽ
	�򵥹���ģʽ��Simple Factory�� 
	��������ģʽ��Factory Method�� 
	���󹤳�ģʽ��Abstract Factory�� 

##### 1���򵥹���ģʽ �����ǽ���һ�������࣬��ʵ����ͬһ�ӿڣ�Product����һЩ�����ʵ���Ĵ���

   �ŵ㣺
	   1��һ������������봴��һ������ֻҪ֪�������ƾͿ�����
	   2����չ�Ըߣ����������һ����Ʒ��ֻҪ��չһ��������Ϳ���

   ȱ�㣺��չ��̫��������һ��Ʒ����Ҫ�޸�Factory,Υ���˿���ԭ��

   Ӧ�ã����ݿ����

```java
public interface Product {
	public abstract void fun();
}

public class ProductA implements Product{

@Override
public void fun() {
System.out.println("ProductA ʵ�֣�");
}

}

public class ProductB implements Product {

@Override
public void fun() {
	System.out.println("ProductB ʵ�֣�");
}

}

public class Factory {
	private Product product;
	public Factory(String str) {
		switch(str) 
		{
			case "A":
				product=new ProductA();
				break;
			case "B":
				product=new ProductB();	
				break;
		}
	}
	public void send() {
		product.fun();
	}

public static void main(String[] args) {
	Factory factory=new Factory("A"); //����ʲô���ͣ���ʲô����
	factory.send();
}

}
```

##### 2����������ģʽ(��̬���зǾ�̬)

ͨ������ʵ�֣�һ����������һ������,����

```java
public class Factory {
private Product product;

public static  Product createProA() 
{
	return new ProductA();
} 

public static Product createProB()
{
	return new ProductB();
} 

public static void main(String[] args) {
	Factory product=Factory.createProA();
	product.fun();
}

}
```



##### 2�����󹤳�ģʽ�����������ӿ��Ǹ��𴴽�һ����ض���Ĺ���������Ҫ��ʽָ�����ǵ��ࡣÿ��ʵ�ֽӿڵĹ����඼�ܰ��չ���ģʽ��������

  ����ģʽ����Ĵ������������࣬Ҳ����˵�������Ҫ��չ���򣬱���Թ���������޸ģ���Υ���˿���ԭ�򣬴�����������࣬����һ����Ҫ�����µĹ��ܣ�ֱ�������µĳ���������ʵ����Ϳ����ˣ�����Ҫ�޸�֮ǰ�Ĵ���

```java
public interface Product {
	public abstract void fun();
}

public class ProductA implements Product{

@Override
public void fun() {
System.out.println("ProductA ʵ�֣�");
}

}

public class ProductB implements Product {

@Override
public void fun() {
	System.out.println("ProductB ʵ�֣�");
}

}

public interface Factory {
	public abstract Product send();
}

public class FactoryA implements Factory {
	@Override
	public Product send() {
		return new ProductA();
	}
}

public class FactoryB implements Factory {
	@Override
	public Product send() {
		return new ProductB();
	}

public static void main(String[] args) {
	Factory factory=new FactoryA();
	Product product=factory.send();
	product.fun();
}

}

```














