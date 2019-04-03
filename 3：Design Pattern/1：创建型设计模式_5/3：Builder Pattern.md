##### ������ģʽ(Builder Pattern)����һ�����Ӷ���Ĺ��������ʾ�����,ʹ��ͬ���Ĺ������̿��Դ�����ͬ�Ķ���(���Բ�ͬ)				���ڴ�����ģʽ

ָ����(director)������һ��ʹ��builder�ӿڵĶ���
������(builder) ������һ����Ʒ����ĸ��������ĳ���ӿ�
����Ľ����ߣ�concreteBuilder)��ʵ��Builder�ӿڣ���д����������װ��ϸ��

�빤��ģʽ�������ǣ�������ģʽ���ӹ�ע�����װ���

```java
package com.builder;

public class Product {
	public void add(String s) {
		System.out.println("��Ӳ���"+s+"��....");
	}
}

package com.builder;

public abstract interface Builder {

public abstract void buildPartA();
public abstract void buildPartB();
public abstract void buildPartC();

}

package com.builder;

public class ConcreteBuilderA implements Builder {
	protected Product p;
	public ConcreteBuilderA() {
		p=new Product();
	}

    public Product getP() {
        return p;
    }

@Override
public void buildPartA() {
	System.out.println("PartA ������...");
	p.add("partA");
}

@Override
public void buildPartB() {
	System.out.println("PartB ������...");
	p.add("partB");
}

@Override
public void buildPartC() {
	System.out.println("PartC ������...");
	p.add("partC");
}

}

package com.builder;

public class Director {
	public void construct(Builder b) {
		b.buildPartA();
		b.buildPartB();
		b.buildPartC();
	}
}



package com.builder;

public class Client {

public static void main(String[] args) {
	Director director=new Director();
	ConcreteBuilderA ca=new ConcreteBuilderA();  //ʵ����������ʱ�ʹ�����product��ÿ������ֻ�����Բ�ͬ
	director.construct(ca);
	Product p=ca.getP();
}

}
```





