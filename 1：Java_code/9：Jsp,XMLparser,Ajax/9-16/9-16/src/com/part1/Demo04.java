package com.part1;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
//������ͼ���ļ���������
public class Demo04 {

	public static void main(String[] args)throws Exception {

        File dir=new File("F:\\ͼ��");
        File[] all=dir.listFiles();
        
        for(File f:all)
        {
        	//���������������ͼ�񻺴�����ͼ��
        	BufferedImage img=ImageIO.read(f);
        	int width=img.getWidth();
        	int height=img.getHeight();
        	//��ͼ��
        	Graphics g=img.getGraphics();
        	g.setColor(Color.red);
        	g.setFont(new Font("΢���ź�", Font.BOLD, 18));
        	g.drawString("����˧!", width-100, height-100);
        	
        	ImageIO.write(img, "jpg", new File(dir,"a"+f.getName()));
        }
        System.out.println("OK");
        
	}

}
