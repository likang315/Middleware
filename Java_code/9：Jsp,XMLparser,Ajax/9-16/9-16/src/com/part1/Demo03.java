package com.part1;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
//��ͼ�����ұ��浽ָ��λ��
public class Demo03 {

	public static void main(String[] args)throws Exception
	{
		 Robot robot=new Robot();//����������ȡscreen�Ľ�ͼ
		 BufferedImage img=robot.createScreenCapture(
				 new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));//��ȡ��Ļ�ߴ磬���ҽ�ͼ�����뵽�ڴ���
         
		 ByteArrayOutputStream bao=new ByteArrayOutputStream();
		 ImageIO.write(img, "jpg", bao);
		 byte [] data=bao.toByteArray();
		 
		 //���ֽ�תΪͼƬ
		 
		 ByteArrayInputStream bio=new ByteArrayInputStream(data);
		 
		 BufferedImage rimg=ImageIO.read(bio);
		 
		 
		 FileOutputStream fo=new FileOutputStream("F:\\aaa.jpg");
		 fo.write(data);
		 fo.close();
		 System.out.println("Ok");
		 
	}

}
