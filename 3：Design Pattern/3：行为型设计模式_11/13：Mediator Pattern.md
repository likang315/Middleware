### �н���ģʽ��Mediator Pattern)�����Ͷ���������֮�����϶�,������Ϊ��ģʽ

  ���ã���һ���н��������װһϵ�еĶ��󽻻����н���ʹ��������Ҫ��ʽ���໥���ã��Ӷ�����ż�϶ȣ����ҿ��Զ����ظı�����֮��Ľ���

  ��Ҫ�����																																1�����������֮����ڴ����Ĺ�����ϵ�������ᵼ��ϵͳ�Ľṹ��úܸ��ӣ�ͬʱ��һ���������ı䣬����Ҳ��Ҫ������֮����������ж���ͬʱ������Ӧ�Ĵ���

2������֮����������ӻᵼ�¶���ɸ����Խ���

```java
package com.mediator;
public abstract class Base {
	protected Mediator mediator;
	public abstract void set(Mediator m);
	public abstract void send(String msg);
	public abstract void receive(String msg);
}

package com.mediator;
public class Mediator {
	private Base pboy;
	private Base pgirl; 
	public void set(Base pb,Base pg) {
		pboy=pb;
		pgirl=pg;
	}
	public void send(String msg,Base t) {
		if(t!=pboy) 
		{
			pboy.receive(msg);
		}else
		{
			pgirl.receive(msg);
		}
	} 
	
}

package com.mediator;
public class Boy extends Base {

@Override
public void set(Mediator m) {
	mediator=m;
}

@Override
public void send(String msg) {
	mediator.send(msg,this);
}

@Override
public void receive(String msg) {
	System.out.println("Boy"+"�յ���");		
}

}

package com.mediator;
public class Girl extends Base {

@Override
public void set(Mediator m) {
	mediator=m;
}

@Override
public void send(String msg) {
	mediator.send(msg, this);
}

@Override
public void receive(String msg) {
	System.out.println("Girl"+"�յ���");
}

}

package com.mediator;
public class Client {

public static void main(String[] args) {
	Boy boy=new Boy();
	Girl girl=new Girl();
	Mediator mediator=new Mediator();
	boy.set(mediator);//���н��߽�������
	girl.set(mediator);
	mediator.set(boy, girl);//����˫������
	boy.send("I Love you ��");
}

}
```





