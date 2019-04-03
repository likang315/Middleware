##### �۲���ģʽ��Observer Pattern)�����������һ��һ�Զ��������ϵ����һ�������״̬�����ı�ʱ���������������Ķ��󶼵õ�֪ͨ�����Զ����£�������Ϊ��ģʽ

 ע�⣺
	1���۲���֪���Լ��۲��Ǹ�����
	2�����۲���֪�����Ǽ�������۲��ң�ֻһ��˫���������⣩

 �ĸ���ɫ��
���󱻹۲��߽�ɫ��һ������ӿڣ��������жԹ۲��߶�������ñ�����һ�������У�ÿ�����۲��߶����������������Ĺ۲��߿������Ӻ�ɾ���۲��߽�ɫ,һ����һ��������ͽӿ���ʵ��

����۲��߽�ɫ��Ϊ���еľ���۲��߶���һ���ӿڣ��ڵõ����۲������ݸı�ʱ֪ͨʱ�����Լ����屻�۲��߽�ɫ���ھ��屻�۲��ߵ��ڲ�״̬�ı�ʱ��Ҫ�򼯺��е����й۲��߷���֪ͨ

����۲��߽�ɫ��ʵ�ֳ���۲��߽�ɫ����Ҫ�ĸ��½ӿڣ�֪ͨ���۲����ҹ۲�����

Ӧ�ã���������Listener��

```java
package com.observer;

public abstract class Subject {
	public abstract void add(Observer obs);
}

package com.observer;

import java.util.ArrayList;

public class ConcreteSubjectA extends Subject {
	private int x;
	private int y;
	private ArrayList<Observer> list=new ArrayList<Observer>();
	public ConcreteSubjectA(){
		

}
public ConcreteSubjectA(int x, int y) {
	super();
	this.x = x;
	this.y = y;
}

@Override
public void add(Observer obs) {
	list.add(obs);
}

public void notifyObserver() {
	for(Observer obs:list) {
		obs.update(x, y);
	}
}

public void dataChange(int x,int y) {
	this.x=x;
	this.y=y;
	notifyObserver();
}

}

package com.observer;

public abstract class Observer {
	private Subject s;
	public Observer() {
		

}
public Observer(Subject s) {
	this.s=s;
	s.add(this);
}
public abstract void update(int x,int y);

}

package com.observer;

public class ConcreteObserverA extends Observer {
	private int x;
	private int y;
	

public ConcreteObserverA(Subject sub) {
	super(sub);
}

@Override
public void update(int x, int y) {
	this.x=x;
	this.y=y;
}

public void print() {
	System.out.println("x:"+x);
	System.out.println("y:"+y);
}

}

package com.observer;

public class client {
	public static void main(String[] args) {
		ConcreteSubjectA cs1=new ConcreteSubjectA();
		ConcreteObserverA cb1=new ConcreteObserverA(cs1);
		

	ConcreteObserverA cb2=new ConcreteObserverA(cs1);//����۲���
	cs1.dataChange(3, 5);
	cb1.print();
	cb2.print();
}

}
```








