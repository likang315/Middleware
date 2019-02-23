#### ����¼ģʽ(Memento Pattern)��

#### �ڸö���֮�⣬����һ�������ĳ��״̬���Ա����ʵ���ʱ��ָ�����,������Ϊ��ģʽ

 ����:�ڲ��ƻ���װ��ǰ���£�����һ��������ڲ�״̬�����ڸö���֮�Ᵽ�����״̬�������������Ժ󽫶���ָ���ԭ�ȱ����״̬

����¼ģʽʹ�������� Memento��Originator �� CareTaker
	Originator(������)�� ��������¼����¼������ڲ�״̬���ָ�״̬
	Memento(����¼)���洢�˷����˶���״̬
	Caretaker������¼����)�� ���汸��¼

Ӧ�ã�git�汾����

```JAVA
package com.memento;

public class Originator {
   private String state;
   public void setState(String state){
      this.state = state;
   }

   public Memento createMemento(){
      return new Memento(this.state);
   }

   public void restoreMemento(Memento m){
      state = m.getState();
   }
   public void show() {
	   System.out.println("Current state:"+state);
   }

}

package com.memento;

public class Memento {
	private String state;
	public Memento(String state){
	    this.state = state;
	}
   public String getState(){
      return state;
   }  

}

package com.memento;
import java.util.ArrayList;
import java.util.List;

public class CareTaker {
	   private List<Memento> List = new ArrayList<Memento>();
	   public void add(Memento m){
	      List.add(m);
	   }
       public Memento get(int index){
          return List.get(index);
       }
}	

package com.memento;
public class Client {

public static void main(String[] args) {
	 Originator originator = new Originator();
      CareTaker careTaker = new CareTaker();
      
      originator.setState("State #1");
      careTaker.add(originator.createMemento());
      originator.setState("State #2");
      careTaker.add(originator.createMemento());
      originator.setState("State #3");
      careTaker.add(originator.createMemento());

    	originator.restoreMemento(careTaker.get(1));
   	 	originator.show();
     
}

}


```





