package com.part01;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * �ļ����������
 * @author Administrator
 *
 */
public class Demo04 extends JFrame
{
	private JButton but=new JButton("��ѡ��Ҫ���ܻ���ܵ��ļ�");
	private JLabel  show=new JLabel("��ѡ��Ҫ����");
	private JButton act=new JButton("����");
    public Demo04()
    {
    	super("�ļ���������ܣ�");
    	this.setSize(400, 300);
    	this.setLocation(200,200);
    	this.setResizable(false);
    	this.setLayout(null);
    	show.setFont(new Font("΢���ź�",Font.BOLD,16));
    	show.setForeground(Color.RED);
    	but.setBounds(100, 50, 200, 40);
    	show.setBounds(30, 100, 200, 40);
    	act.setBounds(140, 180, 100, 30);
    	this.add(but);
    	this.add(show);
    	this.add(act);
    	
    	this.setVisible(true);
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	public static void main(String[] args)
	{
	  new Demo04();

	}

}
