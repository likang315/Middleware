##### ������ģʽ��Adapter Pattern������ĳ����Ľӿ�ת���ɿͻ�����������һ���ӿڱ�ʾ�������ӿڲ�ƥ�����ļ��������⡢���ڽṹ��ģʽ������������������ӿڵĹ���

�ŵ㣺
	 1���������κ�����û�й�������һ������
	 2���������ĸ���
	 3�����������͸����
	 4������Ժ�

��Ϊ��3����
   1�������������̳�
   2������������������
   3���ӿ�����������������Ҫ�ӿڵĲ��ַ�����������ȫ����Ҫ����ô�����ó�����ȥ�̳�������д���ַ�������ȥ�̳г�����

##### 1����������

```java
public interface Type_c {
    public void transfer_1();
}

public interface Andriod {
    public void transfer_2();

}

public class Type_imp implements Type_c{

@Override
public void transfer_1()
{
    System.out.println("Type_c:��������");
}

}

public class Adapter extends Type_imp implements Andriod {

@Override
public void transfer_2()
{
    System.out.println("Andriod:��������.......");
}

}

public class Main {
    public static void main(String[] args)
    {
        Adapter adapter=new Adapter();
        adapter.transfer_1();
        adapter.transfer_2();
    }
}
```



##### 2������������

```java
public class Adaptee {
	public void method() {
		System.out.println("����ʵ��...");
	}	
}

public class Adapter {
	private Adaptee adaptee;
	

public Adapter() {
	adaptee=new Adaptee();
}

public void method() {
	adaptee.method();
}	

public static void main(String[] args) {
	Adapter adapter=new Adapter();
	adapter.method();
}

}
```



## 

### 3���ӿ�����������Ҫʹ�ýӿڵļ�������ʱ�����ȫ��ʵ�֣�����̫ӷ�ף����ʹ�ó�����Ϳ���ѡ����ʵ������Ҫ�ķ���

```java
public interface Usb {
    public void transfer_01();
    public void transfer_02();
}

/**

- ʹ�ó����࣬�շ����壬������ʵ������ѡ���ʵ��
  */
  public abstract class Usb_abs implements Usb{
  @Override
  public  void transfer_01(){}

  @Override
  public  void transfer_02(){}
  }

public class Usb_absImp extends Usb_abs {

@Override
public void transfer_01()
{
    System.out.println("ѡ��ʵ��transfer_01...........");
}

}

public class Main {
    public static void main(String[] args)
    {
        new Usb_absImp().transfer_01();
    }
}

```


