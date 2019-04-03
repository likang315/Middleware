##### װ��ģʽ��Decorator)�����Ѿ����ڵ�ĳЩ�����װ�Σ���չ��һЩ���ܣ�ͬʱ�ֲ��ı���ṹ�����ڽṹ��ģʽ

�Ƕ�̬�ģ�Ҫ��װ�ζ��󣨳���ɻ���--װ�������ͱ�װ�ζ���ʵ��ͬһ���ӿڣ�װ�������б�װ�ζ����ʵ��

Ŀ�ģ�����̳�Ϊ�����뾲̬����������������չ���ܵ����࣬���������͵�����

�ŵ㣺װ����ͱ�װ������Զ�����չ�������໥��ϣ�װ��ģʽ�Ǽ̳е�һ�����ģʽ��װ��ģʽ���Զ�̬��չһ��ʵ����Ĺ���

```java
/**��װ�ζ����װ�ζ������ʵ�ִ˽ӿ�
  */
  public interface Base {
  public void method();
  }

public class Product implements Base {
    @Override
    public void method()
    {
        System.out.println("ʵ���书��....");
    }
}

/** ����װ����
  */
  public  abstract class Decorator implements Base {
  protected Base pbase;

  public Decorator(Base t) {
      pbase=t;
  }
  }

public class ConcreteDecorator extends Decorator {

public ConcreteDecorator(Base t)
{
    super(t);
}

@Override
public void method()
{
    pbase.method();
    newMethod();
}

public void newMethod()
{
    System.out.println("װ�εĹ���...");
}

}

public class Main {
    public static void main(String[] args)
    {
        ConcreteDecorator dec = new ConcreteDecorator(new Product()); //����װ�ζ���
        dec.method();
    }
}
```













