##### ���ģʽ(Composite)��Ҳ�Ʋ�������ģʽ����һЩ���ƵĶ��󿴳�һ����������������ϳ����νṹ���Ա�ʾ�����Լ������Σ�

?		     ʹ���û��Ե����������϶����ʹ�þ���һ����
?	 	     ���ڽṹ��ģʽ���������˶���������νṹ���������������Ҷ�Ӷ��󣬶���������������

�ؼ����룺��֦�ڲ���ϸýӿڣ����Һ����ڲ����� List������� Component

�����ļ�ϵͳ��ÿ��Ŀ¼������װ���ݡ�Ŀ¼�����ݿ������ļ���Ҳ������Ŀ¼

```java
package com.Compsite;

public abstract class RootFile {
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public RootFile() {
		

}
public RootFile(String name) {
	this.name=name;
}

public abstract void printName() ;

}



package com.Compsite;

import java.util.ArrayList;

public class Folder extends RootFile {
	private ArrayList<RootFile> arraylist=new ArrayList<RootFile>();
	public Folder() {
		

}
public Folder(String name) {
	super(name);
}
public void add(RootFile t) {
	arraylist.add(t);
}

@Override
public void printName() {
	System.out.println("File name:"+super.getName());
	if(null != arraylist)
	{
        		for(RootFile rf:arraylist)
		{
            		rf.printName();
        		}
    	}
}

}


package com.Compsite;
public class File extends RootFile {
	

public File() {
	super();
}
public File(String name){
	super(name);
}

@Override
public void printName() {
	System.out.println("File name:"+super.getName());
}

}

package com.Compsite;
public class Client {
	public static void main(String[] args) {
		File file1=new File("1.java");
		File file2=new File("2.java");
		File file3=new File("3.java");
		Folder root=new Folder("F:\\bb");
		Folder f=new Folder("aa");
		

	root.add(file1);
	root.add(file2);
	root.add(f);
	f.add(file3);

	root.printName();
	
}

}
```



