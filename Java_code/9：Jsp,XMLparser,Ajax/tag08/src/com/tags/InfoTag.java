package com.tags;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

/**
 *ֱ�Ӽ̳���ʵ���࣬BodyTagSupport����д��Ҫ�ķ��� ���滻��ǩʵ������
 * @author likang
 *
 */
public class InfoTag extends BodyTagSupport
{

	@Override
	public int doEndTag() throws JspException {
		try {
			this.getPreviousOut().print(MessageFormat.format(bodyContent.getString(), "����",new Date()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Tag.EVAL_PAGE;
	}

}
