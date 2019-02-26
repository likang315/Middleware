### 1�����ģʽ��Java��һ����Ϊ��23��

   ���ԭ��
		1������ԭ�򣺶���չ���ţ����޸Ĺر�
		2�����ھۣ�����ϣ��������ģ��ľۺ϶ȣ���������ģ��֮�����ϵ������������֮��������ԣ�
		3����������������

��Ϊ���ࣺ

######   ������ģʽ��5�֣�������ģʽ������ģʽ(2��)��������ģʽ��ԭ��ģʽ

?    ��Щ���ģʽ�ṩ��һ���ڴ��������ͬʱ���ش����߼��ķ�ʽ��������ʹ�� new �����ֱ��ʵ��������
?    ��ʹ�ó������ж����ĳ������ʵ����Ҫ������Щ����ʱ�������
?	

######   �ṹ��ģʽ��7�֣���������ģʽ��װ��ģʽ������ģʽ�����ģʽ���Ž�ģʽ�����ģʽ����Ԫģʽ

?    ��Щ���ģʽ��ע��Ͷ������ϡ��̳б�������ϽӿںͶ�����϶������¹��ܵķ�ʽ

######   ��Ϊ��ģʽ��11�֣�������ģʽ��ģ�巽��ģʽ���۲���ģʽ������ģʽ��������ģʽ������ģʽ������¼ģʽ��״̬ģʽ��������ģʽ���н���ģʽ��������ģʽ

���ģʽ�ر��ע����֮���ͨ��



### 2������ģʽ�����ڼ���ȫ�ֱ���ʱ��6�У�������������3�֣�

?     Ŀ�ģ���֤һ�����ֻ�ɴ���һ������
?     ʵ�֣�
?	1�����캯��˽�У����ⲻ�ɴ������󣬲��ɱ��̳У�
?	2�����static����
?	
?	

###### 1��//����ʽ�������Ƿ���������ʱ���ų�ʼ����Ҳ�����������ʱ����

```java
/**
����ʽ���������ʱ����,�̰߳�ȫ��ͬ������飬˫�ؼ��Double-Check,Ч�ʸߣ�˫������
*/
public class Singleton {
private volatile static Singleton instance=null;
public static Singleton getInstance()
{
	if(instance==null)
	{
		//ֻ����һ�����ʵ������
		synchronized(Singleton.class)
		{
			if(instance==null)
			{
				instance=new Singleton();
			}
		}
	}
	return instance;
}   
```

�ڲ�������£����û��volatile�ؼ��֣���instance = new TestInstance(����ֳ������⣬���Էֽ�Ϊ3��α����
   1.memory = allocate()   //�����ڴ�

      2. ctorInstanc(memory)   //��ʼ������
      3. instance = memory  //����instanceָ��շ���ĵ�ַ

```java
/**
�̰߳�ȫ��ͬ��������Ч��̫��
  */
  public class Singleton {
  private static Singleton singleton;
  public static synchronized Singleton getInstance() {
      if (singleton == null)
      {
          singleton = new Singleton();
      }
      return singleton;
  }
  }
```



 * ```java
    /**
    ��̬�ڲ��࣬ͨ������ػ��ƣ������أ����÷����Ż�װ�أ�Ч�ʸ�
      */
      public class Singleton {
      private static class SingletonInstance {
          private static final Singleton INSTANCE = new Singleton();
      }
    
      public static Singleton getInstance() {
          return SingletonInstance.INSTANCE;
      }
      }
    ```

    



###### 2��//����ʽ��ֱ�Ӵ�������

```java
public class Singleton {
private static Singleton instance=new Singleton();
public static Singleton getInstance() {
	return instance;
}

public static void main(String[] args) {
	Singleton p = Singleton.getInstance();
}

}
```

 * ```java
    /**
    ͨ����̬�飬װ�����ʱ��ͼ�����
      */
      public class Singleton {
      private static Singleton instance;
    
      static {
          instance = new Singleton();
      }
      public static Singleton getInstance() {
          return instance;
      }
      }
    ```

    

    ```java
    /**
    ��Ԫ��ö��ʵ�ֵ���ģʽ�������ܱ��ⷴ�����⣬���һ��Զ�֧�����л�����
    */
    public enum  EnumSingleton {
        INSTANCE;
        public EnumSingleton getInstance(){
            return INSTANCE;
        }
    }
    ```


### ö��ʵ�֣�

public final class T extends Enum{

}

enum���ҽ���private�Ĺ���������ֹ�ⲿ�Ķ��⹹�죬��ǡ�ú͵���ģʽ�Ǻ�

�������л��ͷ����л�����Ϊÿһ��ö�����ͺ�ö�ٱ�����JVM�ж���Ψһ�ģ���Java�����л��ͷ����л�ö��ʱ��������Ĺ涨��ö�ٵ�writeObject��readObject��readObjectNoData��writeReplace��readResolve�ȷ����Ǳ����������õģ����Ҳ������ʵ�����л��ӿں����readObject���ƻ�����������
