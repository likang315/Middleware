package com.part01;

import javax.swing.JFrame;

/**
 * �ļ����������
 * @author Administrator
 *
 */
public class Demo03 extends JFrame
{
    public Demo03()
    {
    	super("�ļ���������ܣ�");
    	this.setSize(400, 300);
    	this.setLocation(200,200);
    	
    	this.setVisible(true);
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	public static void main(String[] args)
	{
	  new Demo03();

	}

}
