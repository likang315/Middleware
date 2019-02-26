##### ����ģʽ��Proxy Pattern����ĳ�������������һ����Ĺ���,���ҿ����ڲ��Ķ�Ŀ�����Ļ����ϣ����Ӷ���Ĺ���,���ڽṹ��ģʽ

Ŀ�ģ�Ϊ���������ṩһ�ִ����Կ��ƶ��������ķ���

Ӧ�ã�serlvet filter

1����������ģʽ������������ģʽ��Ҫ�ı������Ƕ���Ľӿڣ�������ģʽ���ܸı���������Ľӿ�
2����װ����ģʽ������װ��ģʽΪ����ǿ���ܣ�������ģʽ��Ϊ�˼��Կ���

���ִ���ģʽ��

##### 1����̬������ʹ��ʱ,��Ҫ����ӿڻ��߸���,���������Ŀ�������������Proxy��һ��ʵ����ͬ�Ľӿ�

```java
public interface Base {
	public abstract void method();
}

public class Source implements Base {
	
@Override
public void method() {
	System.out.println("ʵ��Source����...");
}

}

public class Proxy implements Base{
	private Base pbase;
	

public Proxy(Base t){
	pbase=t;
}

@Override
public void method() {
	System.out.println("��ӿ��ƴ����ܵ��߼�");
	pbase.method();
}

}

public class Main{
	public static void main(String[] args) {
		Proxy p = new Proxy(new Source()); //���ݴ�����󣬽��������ϵ
		p.method();
	}
}
```

һ���ӿ����ӷ���,Ŀ�������������Ҫά��,����ʹ�ö�̬����ʽ�����

##### 2����̬����JDK����

1�����������Ҫʵ�ֽӿ�
2��������������,������JDK��API,��̬�����ڴ��д����������(��Ҫ���ݴ����������/Ŀ�����ʵ�ֵĽӿڵ����ͺ��������)

���������ڰ�:java.lang.reflect.Proxy
JDKʵ�ִ���ֻ��Ҫ������ʹ��newProxyInstance����,���Ǹ÷�����Ҫ������������,������д����:

static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces,InvocationHandler h )

ClassLoader loader     ָ����ǰĿ�����ʹ���������
Class<?>[] interfaces  Ŀ�����ʵ�ֵĽӿڵ�����,ʹ�÷��ͷ�ʽȷ������
InvocationHandler h    �¼�������

��������������ʵ����ķ���ʱ������Զ�����ת��������������handler�����invoke���������е���

```java
public interface Email {
    public void receive();
    public void send();
}

public class FlushEmail implements Email{
    @Override
    public void receive()
    {
        System.out.println("receive Email......");
    }

@Override
public void send()
{
    System.out.println("Send Email....");
}

}

/**
������������ʵ�ʶ��󷵻ش������
  */
  public class ProxyFactory {
  public static Object getProxy(Object obj)
  {
      MyInvocation my=new MyInvocation(obj);
      return Proxy.newProxyInstance(obj.getClass().getClassLoader(),obj.getClass().getInterfaces(),my);
  }
  }

/**
��̬�������ʵ��invovationHandler���¼�������
  */
  public class MyInvocation implements InvocationHandler {

  //�����ʵ�ʶ���
  private Object obj;
  public MyInvocation(Object obj)
  {
      this.obj=obj;
  }

@Override
public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable
{
    Object re=null;
    if("send".equals(method.getName()))
    {
        Logger.before();
        method.invoke(obj,objects);//��������ʵ�ʶ���Ͳ���,����ԭʼ����
        Logger.after();
    }else
    {
        method.invoke(obj,objects);
    }

	 return re;
}

}

public class Logger {

public static void before()
{
    System.out.println("֮ǰ���ҵ��......");
}
public static void after()
{
    System.out.println("֮�����ҵ��.....");
}

}

public class Main {

public static void main(String[] args)
{
    FlushEmail flu=new FlushEmail();
    flu.send();
    flu.receive();
    System.out.println("=================");

   Email proxy=(Email)ProxyFactory.getProxy(flu);
   proxy.send();
   proxy.receive();

}

}


```



##### 3��Cglib�����Ƕ�ָ����Ŀ��������һ�����࣬���������з���ʵ����ǿ������Ϊ���õ��Ǽ̳У����Բ��ܶ�final���ε�����д���

   JDK�Ķ�̬�������ֻ�ܴ���ʵ���˽ӿڵ��࣬������ʵ�ֽӿڵ���Ͳ���ʵ��JDK�Ķ�̬����cglib���������ʵ�ִ����

   Cglib����,Ҳ�����������,�������ڴ��й���һ���������Ӷ�ʵ�ֶ�Ŀ������ܵ���չ

Cglib�������ʵ�ַ���:
1.��Ҫ����cglib��jar�ļ�,����Spring�ĺ��İ����Ѿ�������Cglib����,����ֱ������Spring-core.jar����.
2.���빦�ܰ���,�Ϳ������ڴ��ж�̬��������
3.������಻��Ϊfinal,���򱨴�
4.Ŀ�����ķ������Ϊfinal/static,��ô�Ͳ��ᱻ����,������ִ��Ŀ���������ҵ�񷽷�








