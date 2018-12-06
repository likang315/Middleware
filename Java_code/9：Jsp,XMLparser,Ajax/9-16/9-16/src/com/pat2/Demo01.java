package com.pat2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Demo01 {
/**
 * step1:�Ⱥ�URL�������ӣ���������
 * step2:��ѡ������������������
 * step3:�ٸ���������ʽ��ƥ�����Ҫ��URL
 * @param args
 */
	public static void main(String[] args) 
	{
		String str="http://roll.news.sina.com.cn/s/channel.php?ch=01#col=91&spec=&type=&ch=01&k=&offset_page=0&offset_num=0&num=60&asc=&page=1";
		String re=url2String(str);
		
		String subtxt=subtxt(re,"<div class=\"d_list_txt\" id=\"d_list\">", "<div class=\"pagebox\">");
		List<String> list=parseURL(subtxt);
		
		for(String s:list)
			System.out.println(s);
	}
	
	/**
	 * ���ַ������ҳ������ӵ�url
	 * @param str
	 * @return
	 */
	public static List<String> parseURL(String str)
	{
		List<String> list=new ArrayList<String>();
		Pattern p=Pattern.compile("(<a href=\"(.*?)\" target=\"_blank\">(.*?)</a>)");
		Matcher m=p.matcher(str);
		int index=0;
		while(m.find(index))
		{
			list.add(m.group(2));
			index=m.end();
		}
		return list;
	}
	
	/**
	 * ���˵õ����õ�����
	 * @param start
	 * @param end
	 * @return
	 */
	public static String subtxt(String data,String start,String end)
	{
		String str=null;
		
		int s=data.indexOf(start);
		int e=data.indexOf(end);
		if(e>s)
		{
			str=data.substring(s+start.length(), e);
		}
		return str;
	}
	
	
	/**
	 * ��url�������� ,��url��Ӧ�����ݱ���ַ�������
	 * @param surl   url
	 * @return
	 */
	public static String url2String(String surl)
	{
		StringBuilder sb=new StringBuilder();
		try {
			//ͳһ��Դ��λ��
			URL url=new URL(surl);
		    URLConnection uc=url.openConnection();//�����ӣ��� URL �����õ�Զ�̶�������ӣ�
		    InputStream is=uc.getInputStream();
		    BufferedReader br=new BufferedReader(new InputStreamReader(is));//�ַ���
		    String str=null;
		    while(null!=(str=br.readLine()))
		    {
		    	sb.append(str);
		    }
		   br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	

}
