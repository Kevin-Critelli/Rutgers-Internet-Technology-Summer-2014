import java.io.*;
import java.nio.ByteBuffer;

public class test{
	public final static byte[] peerid = { 'p', 'a', 'u', 'l', 'k', 'e', 'v',
			'i', 'n', 'r', 'i', 't', 'c', 'h', 'i', 'e', 'c', 's', '$', '$' };
			
	
	public test(ByteBuffer pieces){
		
	}		 
			
	public static void main(String [] args){
	
		try{
			FileInputStream fin = new FileInputStream(new File("sav.dat"));
			
			byte [] index1 = new byte[4];
			index1[0] = (byte)fin.read();
			index1[1] = (byte)fin.read();
			index1[2] = (byte)fin.read();
			index1[3] = (byte)fin.read();
			
			int index = RUBTClientUtils.byteArrayToInt(index1);
			System.out.println("index " + index);
			
			index1[0] = (byte)fin.read();
			index1[1] = (byte)fin.read();
			index1[2] = (byte)fin.read();
			index1[3] = (byte)fin.read();
			
			index = RUBTClientUtils.byteArrayToInt(index1);
			System.out.println("length " + index);
			
			for(int i=0;i<index;i++){
				fin.read();
			}
			
			index1[0] = (byte)fin.read();
			index1[1] = (byte)fin.read();
			index1[2] = (byte)fin.read();
			index1[3] = (byte)fin.read();
			index = RUBTClientUtils.byteArrayToInt(index1);
			System.out.println("index " + index);
			
			index1[0] = (byte)fin.read();
			index1[1] = (byte)fin.read();
			index1[2] = (byte)fin.read();
			index1[3] = (byte)fin.read();
			
			index = RUBTClientUtils.byteArrayToInt(index1);
			System.out.println("length " + index);
			
			
			
			
			
			
		}catch(Exception e){
			System.out.println("caught an exception");
		}
	}
}
