## �ļ����ϴ�������

upload:����RFC1867Э��
     RFC1867Э����Ҫ��**��HTTPЭ��Ļ�����Ϊinput��ǩ������type="file"���ԣ�ͬʱ�޶���Form��method="post"
     enctype="multipart/form-data"**

### 1��Http �ļ��ϴ��ͻ���Ҫ��

1���ñ��ύ���� <from></form>
2�����󷽷�����Ϊ post   <form method="post">
3���Զ��������ύ���ݸ�������

```html
<form action="upload" method="post" enctype="multipart/form-data">
    ������<input type="text" name="dis"/><br/>
    �ļ�:<input type="file" name="pic"/><br/>
    <input type="submit" value="�ϴ�"/>
</form>
```

enctype Ĭ��ֵΪ application/x-www-form-urlencoded�������ύ�ļ�����
enctype ֵ��Ϊ multipart/form-data ʱ�ύ�ļ����ݸ�������

### 2��Servlet 3.0  �ļ��ϴ�

���ã�@MultipartConfig 

HttpServletRequest �����˶��ļ��ϴ���֧��
	Part getPart(String name)
		������������ȡ�ļ��ϴ���part)
	Collection<Part> getParts()
		��ȡ���е��ļ��ϴ���

### 3��part (InterFace)��ÿ�� Part �����װһ���ļ��ϴ��򣬸ö����ṩ�����ϴ��ļ����ļ����͡���С���������ȷ�����

###### ���ṩ��һ�� write(String file)�������ϴ��ļ�д����������̣��ϴ���ָ type="file" ��input

?	java.lang.String   getContentType() 
?		�ļ������ͣ�mimetype:����ǰ�����ֽڣ��ж��ļ����ͣ�

 	java.util.Collection<java.lang.String>	getHeaderNames() 
         	�õ���ͷkey �ļ���
	java.lang.String   getHeader(java.lang.String name) 
          	�õ�ָ����ͷ��ֵ
 	java.lang.String	getName() 
        	�õ��ı����name����
 	long	getSize() 
         	�õ��ļ��Ĵ�С���ֽڣ�

######  	void	write(java.lang.String fileName) 	

���ݾ���·�������ϴ��ļ�д��server���̵�ָ��·��λ��

```java
PrintWriter out=resp.getWriter();
//�õ��ļ��ϴ���
Part part=req.getPart("file1");
//�õ�����·��
String realpath=this.getServletContext().getRealPath("ups");
String fname=getFileName(part);
String newname=newName()+getExtName(fname);
part.write(realpath+"/"+newname);
```



### 4�� Apache Commons  fileupload �ļ��ϴ���������� jar ��

���˼���ǽ��û�������ת��Ϊһ�� List<FileItem> �ļ��ϣ����� FileItem ����һ��part���ļ��ϴ���

###### FileItem(Interface ):

String	getContentType()
	�õ��ļ�����(mimetype)
String	getName()
	�õ��ϴ����ļ���
String	getFieldName()
	�õ�����name����

boolean	isFormField()
	�ж�form��type�Ƿ�����ͨ����(���ϴ��ļ��ֶ�)��������ϴ��ļ�
long    getSize()
	�õ��ļ���С	
void	write(File file)
	���ϴ��ļ���ָ�����ļ�

```java
@Override
protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    req.setCharacterEncoding("utf-8");
    resp.setContentType("text/html;charset=utf-8");
    PrintWriter out=resp.getWriter();

    out.println("<ul>");
    try {
        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletContext sc = this.getServletContext();
        File repository = (File) sc.getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(repository);
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        List<FileItem> items = upload.parseRequest(req);
        if(null!=items && items.size()>0)
        {
            for(FileItem it:items)
            {
                if(it.isFormField())
                {
                    if("dis".equals(it.getFieldName()))
                        out.println("<li>������"+it.getString("utf-8")+"</li>"); 		
                }else
                {
                    String extname=this.getExtName(it.getName());
                    String newname=this.getRandName()+extname;	   
                    it.write(new File(sc.getRealPath("ups")+"/"+newname));
                }
            }
        }
    } catch (Exception e) {

    }
}
```



### 5���ļ�������

##### 	download�����֣�

##### 	   1��ֱ�ӽ�������ָ��Ҫ���ص��ļ��������������ʶ��� MIME ���ͻ�������ضԻ���ѹ������ �������ַ�ʽ�����޷�ͨ���û�Ȩ������������

##### 	   2��ͨ��������Ӧ��ͷContent-Disposition�������ļ�

��Servlet ��bit��������Ӧ(д)���ͻ��ˣ����Ը����ɵؿ����û�����Ȩ��

```java
Content-Disposition�������������ͣ�inline �� attachment����Ϊ�����ļ��ֶ�
 	 inline�����ļ�����ֱ����ʾ��ҳ��
	 attachment�������Ի������û�����

Content-Disposition:inline;filename=�ļ���;	
Content-Disposition:attachment;filename=�ļ���;

resp.setHeader("Content-Disposition", "attachment;filename="+showcname);
//�õ��ļ���������
ServletContext sc=this.getServletContext();
String path=sc.getRealPath("res");
File file=new File(path,fname);
//�ѷ����������ϵ��ļ�����
FileInputStream fi=new FileInputStream(file);

ServletOutputStream  out=resp.getOutputStream();
byte [] buffer=new byte[1024];
int len=-1;
while((len=fi.read(buffer))>0)
{
    out.write(buffer, 0, len);
}
fi.close();
out.close();
```




### 6��ͼƬ��֤��

��֤��ԭ��
	�ɳ������������ĸ�����ֻ��ֵ�ͼƬ������ͼƬ�ϵ�������ݼ�**�� session �У���Ϊ��ͷ**�����û�����ͼƬ�����ֵ��д�ڱ�����]�ύ��������������˳����û���д����Ϣ�� Session �е���Ϣ���жԱȣ���֪���ǲ����û���д�ģ��ؼ����ڲ����ܳ����Զ�ʶ���ͼƬ�ϵ���Ϣ������һ�����Ƕ��Ӹ����ߡ��㣬����ת�����ŵ�




