### �Զ����ǩ�����ñ�ǩ�Ľṹ������Ԥ����ı�ǩ��Ҳ�ɶ��庯��

<%@ taglib prefix="my" uri=" http://www.xxx.com/tags "  %>
ʹ�ô˷�ʽ��  .tld  ����ǩ�����ļ�) ����

��ǩ���������� java�࣬ʵ���˹淶

javax.servlet.jsp.tagext 

### 1��JspTag(Interface):�����Զ����ǩ����ʵ�ֵĽӿ�

?	

### 2��Tag(Interface):JspTag ���ӽӿ�

   Field
	static int	EVAL_BODY_INCLUDE 
          	����ʵ����Ҫ����.
	static int	SKIP_BODY 
         	û��ʵ�崦��
	static int	EVAL_PAGE 
          	��������ҳ��
	static int	SKIP_PAGE 
         	���ڴ���ҳ��

Method
void	setPageContext(PageContext pc) 
		���õ�ǰ��ҳ�滷��
int	doStartTag()
		��ʼ�����ǩ 

int	doEndTag() 
     	���������ǩ
 	
Tag	getParent() 
	�õ�����ǩ
void	setParent(Tag t)
	���ø���ǩ

void	release() 
		�ͷ�������Դ 
         

### 3��SimpleTag��JSPTag���ӽӿڣ��������

### 4��IterationTag(Interface)��Tag���ӽӿڣ���������ʵ������

static int	EVAL_BODY_AGAIN
	��������ʵ������

int	doAfterBody() 
	��������ʵ�����ݣ�����Tag�����ԣ��в�ͬ������

### 5��BodyTag(Interface): ʵ���ǩ�ӿڣ� extends IterationTag 

?    Filed
?	static int	EVAL_BODY_BUFFERED 
?		����,����BodyContent
?    Method 
?	void	setBodyContent(BodyContent b) 
?          	  ����BodyContent��ʵ������
?	void	doInitBody() 
?	          ��ʼ��ʵ���ǩ

### 6��BodyContent(class) :��װ��ʵ������

abstract  java.lang.String	getString() 
      	�õ�ʵ������

JspWriter	getEnclosingWriter() 
         �õ�JspWriter

### 7��TagSupport(class): ʵ����IterationTag �ӿ�

���ԣ�
	protected  PageContext	pageContext 
������
 	int	doStartTag() 

?	int	doAfterBody() 

?	int	doEndTag() 
?     	   	return EVAL_PAGE

?	Tag	getParent()

 	void	setParent(Tag t) 

### 8��BodyTagSupport(Class):ʵ����BodyTag�ӿں��䷽����д��ǩ������ʱ,ֱ�Ӽ̳���,��д��Ҫ�ķ���

Filed
protected  BodyContent	bodyContent 
	��װ�Զ����ǩʵ������
Method

 int	doStartTag() 
     	Returns:EVAL_BODY_BUFFERED.
 int	doAfterBody() 
   		Returns:SKIP_BODY
 int	doEndTag() 
      	Returns: EVAL_PAGE.

void	doInitBody() 
          	evaluation: no action

 BodyContent	getBodyContent() 
     	
 JspWriter	getPreviousOut() 
     	
 void	setBodyContent(BodyContent b) 
	
 void	release() 

### 9��SimpleTagSupport(class):ʵ��SimpleTag �����ʵ������ʱ��ʹ����Ϊ����

?	void	doTag() 
?		�����ǩ
?	protected  JspContext	getJspContext() 
?		�õ���JspContext










