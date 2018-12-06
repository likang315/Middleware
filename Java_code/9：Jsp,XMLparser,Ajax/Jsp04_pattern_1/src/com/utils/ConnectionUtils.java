package com.utils;

import java.sql.Connection;
import java.sql.DriverManager;

import javax.servlet.ServletContext;
import javax.servlet.jsp.PageContext;

/**
 * ���ڵõ�Connection�Ķ���
 * @author Administrator
 *
 */
public class ConnectionUtils
{
	//��web.xml �е����ʼ��ֵ
	public static Connection getConnection(PageContext pc)
	{
		Connection con=null;
		try {
			ServletContext sc=pc.getServletContext();
			Class.forName(sc.getInitParameter("driver"));
			String url=sc.getInitParameter("url");
			String user=sc.getInitParameter("user");
			String upwd=sc.getInitParameter("pwd");
			con=DriverManager.getConnection(url,user,upwd);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return con;
	}
	
}
