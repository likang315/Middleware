package com.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.pojo.Admin;
import com.utils.MD5;

/**
 * 增加用户
 * @author likang
 *
 */
@WebServlet("/addadmin")
public class AddAdminServlet extends HttpServlet {

	@Override
	public void service( HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		 req.setCharacterEncoding("utf-8");
		 String uname=null!=req.getParameter("uname")?req.getParameter("uname"):"";
		 String upwd=null!=req.getParameter("upwd")?req.getParameter("upwd"):"";
		 String mipwd=MD5.getInstance().getMD5(upwd);
		 
		 try {
			 Connection con=ConnectionManager.getConnection();
			 QueryRunner run=new QueryRunner();
			 String sql="insert into admin(name,pwd) values(?,?)";
			 run.update(con,sql,uname,mipwd);
			
			 List<Admin> alladmin=new QueryRunner(ConnectionManager.ds).query("select * from admin", new BeanListHandler<Admin>(Admin.class)); 
			 req.setAttribute("alladmin", alladmin);
			 
			 req.getRequestDispatcher("success").forward(req, resp);
			 
		} catch (SQLException e) {
			e.printStackTrace();
		}	 
	}

}
