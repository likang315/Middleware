### IO�����Ĳ��裨5������

1����װFile����
2��ѡ��io����
3���ӻ���
4�����ж�д����
5���ر�IO

### IO�����������Ƕ��뵽�ڴ滹�Ǵ��ڴ�д����Ϊ�������������룩 +  �������д����

**��������**ȫ����InputStream/Reader������ ����������InputStream/Reader��β    ���ж��Ĳ���
**�������**ȫ����OutputStream/Writer������ ����������OutputStream/Writer��β  ����д�Ĳ���

�ֽ����������������Ƶ����ݣ�����ͼƬ����Ƶ�����ֵȵȣ��ı��ļ���ʹ���ַ���
	
�ֽ������ͼ�����
�ֽ�����ȫ����InputStream��OutputStream������ ����������OutputStream/InputStream��β

######  public abstract class InputStream

######  public abstract class OutputStream

![](F:\note\6��File��IO��\IO��.png)

### java.io.FileInputStream  ��

###### public class FileInputStream extends InputStream

FileInputStream�����ļ�ϵͳ�е�ĳ���ļ��л�������ֽڣ��ļ����������ͼ�����

```java
���췽����		
	FileInputStream(File file) 
     		ͨ����һ����ʵ���ļ�������������һ���ļ������������ļ�ͨ���ļ�ϵͳ�е� File ���� file ָ��
	FileInputStream(String name) 
    		  ͨ����һ����ʵ���ļ�������������һ���ļ������������ļ�ͨ���ļ�ϵͳ�е�·���� name ָ��

������
	 int read(byte[] b) 
     		 �Ӵ��������н���� b.length ���ֽڵ����ݶ���һ�� byte ������ 
	 void close() 
      		�رմ��ļ����������ͷ�������йص�����ϵͳ��Դ
```

### java.io.FileOutputStream��

###### public class FileOutputStream extends OutputStream

FileOutputStream���ļ��ֽ�����������ڽ�����д���ļ��У��ͼ��� 

```java
���췽����
	FileOutputStream(File file) 
      		����һ����ָ�� File �����ʾ���ļ���д�����ݵ��ļ������
	FileOutputStream(String name) 
      		����һ�������ָ�����Ƶ��ļ���д�����ݵ�����ļ���
	FileOutputStream(File file, boolean append) 
		 	����һ����ָ�� File �����ʾ���ļ���д�����ݵ��ļ��������׷��
������
	 void write(byte[] b) 
     		�� b.length ���ֽڴ�ָ�� byte ����д����ļ��������
	 void write(byte[] b, int off, int len) 
      		��ָ�� byte �����д�ƫ���� off ��ʼ�� len ���ֽ�д����ļ������
	 void close() 
      		�رմ��ļ���������ͷ�������йص�����ϵͳ��Դ�� 

ע�⣺	����д��Ĭ�ϴ�����FOS�Ǹ���д�Ĳ�����FOS���Ƚ��ļ�����ȫ��ɾ����Ȼ���ڿ�ʼд
	  ׷��д���ڴ���FOSʱ����ָ���ڶ������������Ҹ�ֵΪtrueʱ������׷��д����ô���ݻᱻ׷�ӵ�ĩβ
```



### �ַ������߼�����

�ַ�����ȫ����Reader��Writer�����࣬����������Reader��Writer��β����charΪ��д��λ
�ַ���ʹ�����ֽ�������һ��������ʱ����ȥ��ָ���ı�������鵽���ַ�����

```java
public abstract class Reader  public abstract class Writer
```

ת���������Խ��ֽ���ת��Ϊ�ַ�������
�ص㣺���԰���ָ�����ַ����뼯д���ַ�

### InputStreamReader �ࣺ���ַ�����װ���ֽ���

public class InputStreamReader extends Reader
	���캯����
		InputStreamReader(InputStream in, Charset cs) 
          		����ʹ��ָ���ַ����� InputStreamReader

### OutputStreamWriter ��

public class OutputStreamWriter extends Writer
	���캯����
		OutputStreamWriter(OutputStream out, Charset cs) 
          		����ʹ�ø����ַ����� OutputStreamWriter
		



### ���ݷ�װ�����ݽ� io ��Ϊ���ڵ���(�ͼ���)  +   ���������߼�����

�ڵ�������ʵ�����д���ݵ���

����������װ�ڵ����ģ������������ݵģ����ܶ������ڣ��߼����������������γ����������ӣ����ӵ�Ч��

�����Ĵ�������BufferedInputStream, BufferedOputStream , BufferedReader, BufferedWriter

ע�⣺�ӻ������Ժ��ڽ���д��ʱ��һ��Ҫflush()

### ��������

###  BufferedInputStream :�ڴ��� BufferedInputStream ʱ���ᴴ��һ���ڲ���������ʹ������Լӿ��дЧ��

 public class BufferedInputStream extends FilterInputStream

```java
���췽����
	BufferedInputStream(InputStream in) 
      	 	����һ�� BufferedInputStream ��������������������� in���Ա㽫��ʹ��
������
	int read(byte[] b) 
      		�Ӵ��ֽ��������п�ʼ�����ֽڶ�ȡ��ָ���� byte ������
	void close() 
    		�رմ����������ͷ����������������ϵͳ��Դ
```



###  BufferedOutputStream  :����ʵ�ֻ���������

######  public class BufferedOutputStream extends FilterOutputStream

```java
���췽����
	BufferedOutputStream(OutputStream out) 
    		����һ���µĻ�����������Խ�����д��ָ���ĵײ��������

������
	 void flush() 
      		ˢ�´˻���������ǿ�ƻ�����������һ����д��
	 void write(byte[] b) 
      		��ָ�� byte ������д��˻���������
```

 public class BufferedReader extends Reader

###  BufferedReader	:�ַ����������������ж�ȡ�ַ���

���췽����
	BufferedReader(Reader in)
������
	int read(char[] cbuf) 
		���ַ����������ĳһ����
	String readLine() 
		��ȡһ���ı��У�ȫ����ȡ������String�У�������ΪNULL���ʾ��ȡ��ĩβ

###  BufferedWriter :�ַ����������

 public class BufferedWriter extends Writer

```java
������
	void write(char[] cbuf, int off, int len)
	void write(String s) 
     		д���ַ�����ĳһ����
	void close() 
   	 		�رմ�������Ҫ��ˢ����
	void newLine() 
     		д��һ���зָ���
	void flush() 
          	ˢ�¸����Ļ���
```


 BufferWriter��ǿ��

###  PrintWriter�������Զ�ˢ�£����й��ܵĻ����ַ���������ڲ��ᴴ��BufferWriter��Ϊ���幦�ܵĵ���

```java
���췽����
	PrintWriter(File file) 
      		ʹ��ָ���ļ������������Զ���ˢ�µ��� PrintWriter��
	PrintWriter(String fileName) 
      		��������ָ���ļ������Ҳ����Զ���ˢ�µ��� PrintWriterjava
	PrintWriter(OutputStream out, boolean autoFlush) 
      		���������ֽ����������µ� PrintWriter��ͬ���ڶ���������ȷ���߲�������ˢ��

���������е�println�����Զ���ˢ���ܣ�ÿ������ʱ���Զ�ˢ��  
	void println() 
      		ͨ��д���зָ����ַ�����ֹ��ǰ�� 
	void println(String x) 
      		��ӡ String��Ȼ���У���ֹ��ǰ�У� 
	void close() 
      		�رո������ͷ���֮����������ϵͳ��Դ
```



�������ࣺ
   **1��ByteArrayInputStream��ByteArrayOutputStream**  

?	���� ByteArrayInputStream �� ByteArrayOutputStream ����� close ����û���κ����壬�����������ڴ����
?	 ֻҪ�������������������ܹ��ͷ���Դ����ͬ��������
   **2��ImageIO**



