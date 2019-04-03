package com.part1;

import java.util.Arrays;
//����ת��Ϊ�ֽ�����

public class Demo01 {

	public static byte[] int2byte(int data)
	{
		byte [] re=new byte[4];
		//00000100110   010010100  0101010 00110101 
		//00000000000   000000000  0000000 11111111
		//   0            1           2       3
		//ÿ���ֽڴ洢һ���ֽڵ�����
		re[3]=(byte)(data&0xFF);
		re[2]=(byte)((data>>8)&0xFF);
		re[1]=(byte)((data>>16)&0xFF);
		re[0]=(byte)((data>>24)&0xFF);
		return re;
	}
	//�ֽ�����ת��Ϊ����
	public static int byte2int(byte[]data)
	{
		int re=-1;
		if(null!=data&&data.length==4)
		{
			//���������ƴ��
			re=data[3]&0xff|(data[2]<<8)&0xff|(data[1]<<16)&0xff|(data[0]<<24)&0xff;
		}
		return re;
	}

	public static void main(String[] args) {
		 int i=23345;
		 byte [] re=int2byte(i);
		 System.out.println(Arrays.toString(re));
		 System.out.println(byte2int(re));
	}

}
