package com.servlet.upload;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * enctype 属性没有修改，没有上传内容
 * @author likang
 *
 */
@WebServlet("/upload")
public class UploadServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		 resp.setContentType("text/html;charset=utf-8");
		 PrintWriter out=resp.getWriter();
		 
		 InputStream is=req.getInputStream();
		 byte [] buffer=new byte[1024];
		 int len=-1;
		 FileOutputStream fo=new FileOutputStream("e:\\up.txt");
		 while((len=is.read(buffer))>0)
		 {
			fo.write(buffer, 0, len); 
		 }
		 is.close();
		 fo.close();
		 
		 out.println("<h1>OK</h1>");
		 out.close();
	}

}
