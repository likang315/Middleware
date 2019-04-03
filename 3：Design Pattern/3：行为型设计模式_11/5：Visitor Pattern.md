### ������ģʽ��Visitor Pattern����ʹ����һ���������࣬���ı��˱����������ִ���㷨���������͵����ģʽ������Ϊ��ģʽ

**�����ߵı�����Ԥ��ͨ·���ص�ʵ��**

Ŀ�ģ����ݽṹ�����ݲ�������
Ӧ�ó�������Ҫ��һ������ṹ�еĶ�����кܶ಻ͬ�Ĳ��Ҳ���صĲ���������Ҫ��������Щ����"��Ⱦ"��Щ������࣬ʹ�÷�����ģʽ����Щ��װ������

Visitor�������߽ӿڣ�Ϊ���з�������������Ԫ�ص� visit ����
ConcreteVisitor������ķ�����ʵ�ֶ���ʵ��Ҫ��������ӵ�����ṹ�еĹ���
Interviewee���������߽ӿڣ����屻������������ʵķ���
ConcreteInterviewee�����屻���ʵĶ���ͨ����ص������ߵ���ʵ���ܣ�ͬʱ������������ݹ�������ʹ��ObjectStructure������ṹ��ͨ��������������ʵĶ����÷����߷�����������Ԫ��

```java
package com.visitor;
//��������
public interface Interviewee {
	public void accept(Visitor v);
	public void fun();
}

package com.visitor;
public class Book implements Interviewee {
@Override
public void fun() {
	System.out.println("����Book��fun....");
}
@Override
public void accept(Visitor v) {
	v.visit(this);
}
}

package com.visitor;
public class Article implements Interviewee {
@Override
public void accept(Visitor v) {
	v.visit(this);
}
@Override
public void fun() {
	System.out.println("����Article��fun....");
}
}

--------------------------------------------------------------------------------
package com.visitor;
public interface Visitor {
	public void visit(Interviewee i);
}

package com.visitor;
public class Visitor1 implements Visitor {
@Override
public void visit(Interviewee in) {
	in.fun();
}

}

package com.visitor;

import java.util.ArrayList;

  //�������߼���,һ�ö�
  public class ManagerInterviewee {
  private ArrayList<Interviewee> arraylist=new ArrayList<Interviewee>();

  public void add(Interviewee in) {
  	arraylist.add(in);
  }

  public void request(Visitor v) {
  	for(Interviewee arr:arraylist)
  	{
  		arr.accept(v);
  	}
  }
  }


package com.visitor;

public class Client {
	public static void main(String[] args) {
		Book book=new Book();
		Article article=new Article();
		ManagerInterviewee mie=new ManagerInterviewee();
		Visitor1 visitor1=new Visitor1();
		
		mie.add(book);
		mie.add(article);
		mie.request(visitor1);
	}
}
```


