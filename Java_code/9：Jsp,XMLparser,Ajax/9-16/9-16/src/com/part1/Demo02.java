package com.part1;

import java.util.Arrays;
//����Э������ֽ��ļ��ļ�
public class Demo02 {
/**
 * ����Э���
 * @param types  Э������
 * @param length   ���ݳ���
 * @param data   ����
 * @return
 */
	//��������Э��
	public static byte[] data2byte(byte types,int length,byte[] data)
	{
		byte[] allData=null;
		allData=new byte[data.length+4+1];
		allData[0]=types;//��������
		byte[] len=intToByte(length);
		
		System.arraycopy(len, 0, allData, 1, 4);
		System.arraycopy(data, 0, allData, 5, data.length);
		return allData;
	}
	
	//��������Э��
	public static void byte2data(byte[] re) 
	{
		int value,length;
		byte[] len = new byte[100];
		byte[] data= new byte[100];
		if(re[0]==1)//��������
		{
			System.arraycopy(re, 1, len, 0, 3);
			length=byteToInt(len);
			System.arraycopy(re, 4, data, 0, re.length-4);
			byte[] data4=new byte[100];
			for(int i=0;i<data.length;i++)
			{
				for(int j=0;j<4;j++)
				{
					data4[j]=data[i];				
				}
				value=byteToInt(data4);
				System.out.println(value);
			}
		}
	}
	
	//����תΪ�ֽ�����
	public static byte[] intToByte(int data)
	{
		byte [] re=new byte[4];
		re[3]=(byte)(data&0xFF);
		re[2]=(byte)((data>>8)&0xFF);
		re[1]=(byte)((data>>16)&0xFF);
		re[0]=(byte)((data>>24)&0xFF);
		return re;
	}
	
	//�ֽ�����ת��Ϊ����
	public static int byteToInt(byte[]data)
	{
		int re=-1;
		if(null!=data&&data.length==4)
		{
			re=data[3]&0xff|(data[2]<<8)&0xff|(data[1]<<16)&0xff|(data[0]<<24)&0xff;
		}
		return re;
	}
	
	public static void main(String[] args) throws Exception
	{
		/**
		 *  String str="��������������?";
		 *  byte[] r1=str.getBytes("utf-8");
		 *  System.out.println(Arrays.toString(r1));
		 */
		byte[] data=new byte[100];
		byte[] alldata=new byte[100];
		
		data=intToByte(56);
		alldata=data2byte((byte)1,4,data);
		byte2data(alldata);
	}

}
