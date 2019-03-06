##### ԭ��ģʽ��Prototype Pattern�������ڴ�����ͬ�Ķ���ͬʱ���ܱ�֤���ܣ����ڴ�����ģʽ

######  ��ͨ������һ�����ж��������¶����

ʵ����һ��ԭ�ͽӿڣ��ýӿ����ڴ�����ǰ����Ŀ�¡��ʵ�ֿ�¡����

```java
package com.Prototype;

public class Prototype implements Cloneable {

@Override
public Object clone() throws CloneNotSupportedException {
	return super.clone();
}
}

package com.Prototype;
public class Demo extends Prototype {
	private int x;
	public Demo() {
		x=0;
	}
	public Demo(int x) {
		this.x=x;
	}
	
}

package com.Prototype;
public class Client {
	public static void main(String[] args) {
		Prototype p=new Demo(3);
		try {
			Demo p1=(Demo) p.clone();
			System.out.println(p1.toString());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
}
```



