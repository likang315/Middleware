IOͨ��������ϵͳ�������������д���ź�
IO���Դ������ݶ�д�ź�

URL��"."  ��Ŀ¼��"/"���㼶�ķָ�����Ĭ��·�����ǵ�ǰ·��

## Java IO��

###### 1��Java.io  ���� ������ IO

###### 2��Java.nio ���еķǶ�����IO��ͨ����Ϊ New IO

IO �������ڴ��е�����д�������ϻ��߰Ѵ����ϵ����ݶ����ڴ��еĲ���

### 1��java.io. File���ȿ��Ա�ʾһ���ļ� Ҳ���� ��ʾһ��Ŀ¼

###### ���ã�

1�����Է������ļ���һЩ���ԣ��磺�ļ�������С�������ǲ��ܷ����ļ�������
2��������ɾ��һ���ļ� �� Ŀ¼

###### Constructors��

?	File (String pathname)           ------------ ͨ��������·�����ַ���ת��Ϊ����·����������һ���� File ʵ��
?	File (URI uri)                          ------------ ͨ���������� file: URI ת��Ϊһ������·����������һ���µ� File ʵ�� 

###### Method��

?	boolean exists() 
?        	���Դ˳���·������ʾ���ļ���Ŀ¼�Ƿ����
?	boolean createNewFile() 
?          	���ҽ��������ھ��д˳���·����ָ�����Ƶ��ļ�ʱ�����ɷֵش���һ���µĿ��ļ�
?	boolean delete() 
?          	ɾ���˳���·������ʾ���ļ���Ŀ¼
?	String getName() 
?          	�����ɴ˳���·������ʾ���ļ���Ŀ¼������
?	String getPath() 
?		���˳���·����ת��Ϊһ��·�����ַ��� 
?	long length() 
?		�����ɴ˳���·������ʾ���ļ��ĳ���
?	
�ص����շ�����
?		��ȡĿ¼�µ�����File������������ɵ��ַ�������
?		String[] list()

```java
@FunctionalInterface
public interface FilenameFilter 
{
    boolean accept(File dir, String name);
}
```



######         ����������Ҫʵ�� FilenameFilter �ӿڣ���д accept ������ʹ�� endwiths �� startswith ����

###### 	String[] list(FilenameFilter filter)?		

 		��ȡĿ¼�µ�����File����ĳ���·��������ɵ��ļ�����
		 File[] listFiles() 
		 File[] listFiles(FileFilter filter)          //����File��������͹��ˣ��������ļ���Ŀ¼
		 File[] listFiles(FilenameFilter filter) //����File��������ƽ��й���

```java
    ɾ��Ŀ¼����������ʱ�õݹ�
	public void delete(File file)
	{		  
		  if��file.isDirectory��
		  {
			File[] subs = file.listFiles();
			for(File sub:subs)
			{
				delete(sub);	
			}
		     file.delete();
		  }
	}	  
```



### 2��Java.io.RandomAccessFile��ר��������ȡ�ı����ݵģ�����Ҫ��ֱ�ӿ��Զ�д

RAF���ǻ���ָ����ж�д�ģ���RAF������ָ��ָ���λ�ö�д�ֽڣ����Ҷ�д��ָ����Զ�����ƶ���**��ÿ���ö������һ����¼ָ��**���ȿ��Զ�ȡ���ݣ�Ҳ����д���ļ�����

���췽����
	RandomAccessFile(File file, String mode) 
          �������ж�ȡ��������д�루��ѡ������������ļ��������ļ��� File ����ָ��**��Mode����дģʽ��"r" ,"rw"**
	RandomAccessFile(String name, String mode) 
          �������ж�ȡ��������д�루��ѡ������������ļ��������ļ�����ָ������
������
int read������ÿ�ζ�ȡһ���ֽڣ����귵�� -1
int read��byte[] data��:һ���Գ��Զ�ȡ�������ֽ��������ܳ��ȵ��ֽ�����1024 * ...�������뵽�����У�
				     ����ֵΪʵ�ʶ�ȡ�����ֽ�����������ֵΪ -1�����ʾû�ж�ȡ���κ��ֽڣ��ļ�ĩβ
	

void write��byte[] data��:һ���Ը������ֽ�������������ֽ�д��
void write��byte[] data,int start,int len�� : �����������д��±�start��ʼ��������ȡlen���ֽ�һ����д��
	
void seek(long pos) 
      ���ô��ļ���ͷ���������ļ�ָ��ƫ�������ڸ�λ�÷�����һ����ȡ��д�����





