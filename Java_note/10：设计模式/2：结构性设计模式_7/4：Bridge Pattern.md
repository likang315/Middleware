##### �Ž�ģʽ��Bridge�����ͳ�����ʵ��֮�����϶ȣ�ʹ�ö��߿��Զ�����չ�����ڽṹ��ģʽ

 ͨ���ṩ���󻯺�ʵ�ֻ�֮����Žӽṹ���ó��󲿷ֺ�ʵ�ֲ��ֶ����������ֱ���ӿڣ���ʵ�ֶ��ߵĽ���
�ŵ㣺 	
	1�������ʵ�ֵķ���
	2���������չ����
	3���Ž�ģʽ���̳й�ϵת���ɹ�����ϵ����������������֮�����϶�

```java
public interface Concrete{
	public abstract void method();
}
public class ConcreteA implements Concrete {

@Override
public void method() {
	System.out.println("ʵ��һ...");
}	

}

// ���󲿷�

public abstract class Abstraction {  //��Ϊ�Ž���
	private Concrete pimp;
	public Abstraction(Concrete t) {
		pimp = t;
	}
	public  void method() {
		pimp.method();
	}
}

public class AbstractionA extends Abstraction {

public AbstractionA(Concrete t) {
	super(t);
}	

}

// ʵ�ֲ���

public class Client {

public static void main(String[] args) {
	AbstractionA p=new AbstractionA(new ConcreteA());
	p.method();
}

}


```









