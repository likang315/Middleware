package com.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
/**
 * ��ǩ��������������ʵ��JspTag �����ӽӿڣ�˳�����������
 * @author likang
 *
 */
public class HelloTag implements Tag 
{
	 private  PageContext pc; 	//���� 
	 private  Tag parent;  		//����ǩ
	 private int count;			//Tag����
	     
     public HelloTag()
     {
    	 System.out.println("ʵ������HelloTag..");
     }
     

	@Override
	public int doEndTag() throws JspException {
		System.out.println("doEndTag..............");
		
		try {
			pc.getOut().println("<h1>Hello Tag "+count+"</h1>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Tag.EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException {
		System.out.println("doStartTag..............");
		return Tag.SKIP_BODY;
	}

	@Override
	public Tag getParent() {
		return parent;
	}

	@Override
	public void release() {
		System.out.println("release.............");
	}

	@Override
	public void setPageContext(PageContext arg0) {
		 System.out.println("setPageContext.............");
		 this.pc=arg0;
	}

	@Override
	public void setParent(Tag arg0) {
	   System.out.println("setpaternt.............");
       this.parent=arg0;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		System.out.println("setXXX.....");
		this.count = count;
	}

}
