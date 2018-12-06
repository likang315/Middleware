package com.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bean.Student;

@WebServlet("/show")
public class ShowServlet extends HttpServlet {
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		List<Student> stus=new ArrayList<Student>();
		stus.add(new Student(1,23,"����","��"));
		stus.add(new Student(2,24,"����","��"));
		stus.add(new Student(3,22,"С��","Ů"));
		stus.add(new Student(4,26,"��ǿ","��"));
		stus.add(new Student(5,21,"����","Ů"));
		
		req.setAttribute("stus", stus);
		req.getRequestDispatcher("show.jsp").forward(req, resp);
		
	}
}


