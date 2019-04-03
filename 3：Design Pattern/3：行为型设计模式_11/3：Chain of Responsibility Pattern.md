#### ������ģʽ��Chain of Responsibility Pattern����Ϊ���󴴽���һ�������߶��������ÿ�������߶���������һ�������ߵ����ã���һ�������ܴ����������ô�������ͬ�����󴫸���һ�������ߣ�����������������������ֱ���ж�������Ϊֹ��������Ϊ��ģʽ

��ɫ��

?	1��������������

?	2��������������

?	3���������ߣ��ͻ���

Ӧ�ã�servlet �Ĺ�����filter

```java
public abstract class Base {
private Base next;	
public Base() {
}
public Base(Base t) {
	this.next=t;
}

public abstract void method();
public void fun() {
	if(next!=null)
		next.method();
}
}


public class A1 extends Base {
	public A1() {
		super();
	}
	public A1(Base t) {
		super(t);
	}
    @Override
    public void method() {
        System.out.println("class A1 ʵ��...");
        fun();
    }	
}

public class A2 extends Base {
public A2(){
}
public A2(Base t) {
	super(t);
}
@Override
public void method() {
	System.out.println("class A2  ʵ��...");
	fun();
}
}

public class Client {
	public static void main(String[] args) {
		Base a2= new A2();
		Base a1= new A1(a2);
		a1.method();
	}
}


```












