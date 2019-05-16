package com.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@WebServlet("/show")
public class ShowServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	   
		
		Student stu=new Student("����",23);
        req.setAttribute("stu",stu);
        req.setAttribute("msg", "<h1>alert('hello!');</h1>");
        req.setAttribute("sex", "F");
        
        
        List<Student> stus=new ArrayList<Student>();
        stus.add(new Student("����",22));
        stus.add(new Student("����",21));
        stus.add(new Student("����",24));
        stus.add(new Student("С��",20));
        
        req.setAttribute("stus", stus);
		req.getRequestDispatcher("show.jsp").forward(req, resp);
		
	}

}
