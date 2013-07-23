/**
 * Group Members (CS 352 Internet Technology 2013 Summer Session Project 0)
 * 
 * Kevin Critelli
 * Ritchie Vonderschmidt
 * Paul Jones
 * */

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * Class representing a peer for the download
 **/ 

public class Peer implements Runnable{
	
	public int port;
	public String ip;
	public DataOutputStream dout = null;
	public DataInputStream din = null;
	public Socket socket = null;
	public OutputStream output = null;
	public InputStream input = null;
	public TorrentInfo ti = null;
	ByteBuffer[] pieces = null;
	byte[] piece = null;
	int numPieces = 0;
	byte [] buffer = null;

	//placeholder constructor for trackerresponse class
	
	public Peer(int port, String ip){
		this.port = port;
		this.ip = ip;
	}		
			
	public Peer(int port, String ip, TorrentInfo ti, byte[] peerid) {
		this.port = port;
		this.ip = ip;
		this.ti = ti;
		
		try{
			socket = new Socket(ip,port);
			input = socket.getInputStream();
			output = socket.getOutputStream();
				
			din = new DataInputStream(input);	
			dout = new DataOutputStream(output);
		}catch(Exception e){
			System.out.println("Exception thrown during connection setup");
		}
		
		if(sendHandshake(ti.info_hash.array(),peerid)){
			System.out.println("handshake accepted");
		}else{
			System.out.println("handshake denied");
		}
		
		try{
			downloadFile();
		}catch(Exception e){
			System.out.println("exception thrown during download");
		}
		
		try{
			completeConnection();
		}catch(Exception e){
			System.out.println("Exception thrown during download");
		}
	}
	
	public boolean sendHandshake(byte[] info_hash, byte[]peerid){
		Message handshake = new Message(info_hash,peerid);
	
		try{	
			dout.write(handshake.message);
			dout.flush();
			socket.setSoTimeout(130000);
			
			byte[] receivingShake = new byte[68];
			din.readFully(receivingShake);
						
			byte[] peerInfoHash = Arrays.copyOfRange(receivingShake, 28, 48);
			if(!Arrays.equals(peerInfoHash, info_hash)){
				return false;
			}else{
				return true;
			}
		}catch(Exception e){
			System.out.println("Exception thrown during handshake");
			return false;
		}
	}
	
	public boolean downloadFile()throws Exception{
		//public final int file_length;
		//public final int piece_length;
		//public final ByteBuffer[] piece_hashes;
		
		byte[] buf = new byte[17000];
	
		System.out.println("File size " + ti.file_length);
		System.out.println("Num Pieces " + ti.piece_hashes.length);
		System.out.println("Piece Size " + ti.piece_length);
		
		for(int i=0;i<6;i++){
			if(i == 5){
				String s1 = String.format("%8s", Integer.toBinaryString(din.readByte() & 0xFF)).replace(' ', '0');
				System.out.println("bit string = " + s1);
				break;
			}
			buf[i] = din.readByte();
		}
		
		//interested message	
		Message interestedMessage = new Message(1,(byte)2);
		System.out.println(interestedMessage);
		
		dout.write(interestedMessage.message);
		dout.flush();
		socket.setSoTimeout(130000);
		
		
		//grab response from interest
		for(int i=0;i<5;i++){
			if(i == 4){
				if(din.readByte() == 1){
					System.out.println("unchoked");
					break;
				}
			}
			din.readByte();
		}
		
		//request first piece
		Message request = new Message(13,(byte)6);
		request.setPayload(null,-1,-1,16384,0,0,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
		
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece0 sub 1 message " + Message.byteArrayToInt(buf));
		
		byte[] piece0Sub1 = new byte[16384];
		
		//bypass bytes
		for(int i=0;i<9;i++){
			din.readByte();
			//System.out.println(din.readByte());
		}
		
		for(int i=0;i<16384;i++){
			piece0Sub1[i] = din.readByte();
		}
		
		System.out.println("got piece 0 sub 1");
		//end piece1sub1
		
		//begin request piece1sub2
		
		request.setPayload(null,-1,-1,16384,16384,0,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
		
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 0 sub 2message " + Message.byteArrayToInt(buf));
		byte [] piece0Sub2 = new byte[16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<16384;i++){
			piece0Sub2[i] = din.readByte();
		}
		
		System.out.println("got piece 0 sub 2");
		
		
		//begin request piece1sub1
		
		request.setPayload(null,-1,-1,16384,0,1,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
	
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 1 sub 1 message " + Message.byteArrayToInt(buf));
		byte [] piece1Sub1 = new byte[16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<16384;i++){
			piece1Sub1[i] = din.readByte();
		}
		
		System.out.println("got piece 1 sub 1");
		
		//end request piece1sub1
		
		//begin request piece1sub2
		
		request.setPayload(null,-1,-1,16384,16384,1,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
	
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 1 sub 2 message " + Message.byteArrayToInt(buf));
		byte [] piece1Sub2 = new byte[16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<16384;i++){
			piece1Sub2[i] = din.readByte();
		}
		
		System.out.println("got piece 1 sub 2");
		
		//end request piece1sub2
		
		//begin request piece2sub1
		
		request.setPayload(null,-1,-1,16384,0,2,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
	
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 2 sub 1 message " + Message.byteArrayToInt(buf));
		byte [] piece2Sub1 = new byte[16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<16384;i++){
			piece2Sub1[i] = din.readByte();
		}
		
		System.out.println("got piece 2 sub 1");
		
		//end request piece2sub1
		
		//begin request piece2sub2
		
		request.setPayload(null,-1,-1,16384,16384,2,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
	
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 2 sub 2 message " + Message.byteArrayToInt(buf));
		byte [] piece2Sub2 = new byte[16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<16384;i++){
			piece2Sub2[i] = din.readByte();
		}
		
		System.out.println("got piece 2 sub 2");
		
		//end request piece2sub2
		
		//begin request piece3sub1
		
		request.setPayload(null,-1,-1,16384,0,3,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
	
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 3 sub 1 message " + Message.byteArrayToInt(buf));
		byte [] piece3Sub1 = new byte[16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<16384;i++){
			piece3Sub1[i] = din.readByte();
		}
		
		System.out.println("got piece 3 sub 1");
		
		//end request piece3sub1
		
		//begin request piece3sub2
		
		request.setPayload(null,-1,-1,16384,16384,3,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
	
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 3 sub 2 message " + Message.byteArrayToInt(buf));
		byte [] piece3Sub2 = new byte[16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<16384;i++){
			piece3Sub2[i] = din.readByte();
		}
		
		System.out.println("got piece 3 sub 2");
		
		//end request piece3sub2
		
		System.out.println(ti.file_length - (ti.piece_length * 4));
		
		//begin request piece4sub1
		
		request.setPayload(null,-1,-1,16384,0,4,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
	
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 4 sub 1 message " + Message.byteArrayToInt(buf));
		byte [] piece4Sub1 = new byte[16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<16384;i++){
			piece4Sub1[i] = din.readByte();
		}
		
		System.out.println("got piece 4 sub 1");
		
		//end request piece4sub1
		
		//begin request piece4sub2
		
		request.setPayload(null,-1,-1,20637-16384,16384,4,-1);
		dout.write(request.message);
		dout.flush();
		socket.setSoTimeout(1300000);
	
		buf = new byte[4];
		for(int i=0;i<4;i++){
			buf[i] = din.readByte();
		}
		
		System.out.println("the length of piece 4 sub 2 message " + Message.byteArrayToInt(buf));
		byte [] piece4Sub2 = new byte[20637-16384];
		
		for(int i=0;i<9;i++){
			din.readByte();
		}
		
		for(int i=0;i<piece4Sub2.length;i++){
			piece4Sub2[i] = din.readByte();
		}
		
		System.out.println("got piece 4 sub 2");
		
		//end request piece4sub2
		
		
		FileOutputStream fileoutput = new FileOutputStream(new File("picture.jpg"));
		fileoutput.write(piece0Sub1);
		fileoutput.write(piece0Sub2);
		fileoutput.write(piece1Sub1);
		fileoutput.write(piece1Sub2);
		fileoutput.write(piece2Sub1);
		fileoutput.write(piece2Sub2);
		fileoutput.write(piece3Sub1);
		fileoutput.write(piece3Sub2);
		fileoutput.write(piece4Sub1);
		fileoutput.write(piece4Sub2);
	
		return true;
	}
	
	public void completeConnection()throws Exception{
		socket.close();
		din.close();
		dout.close();
	}
	
	public String toString() {
		return "" + ip + ":" + port;
	}
	
	public void run(){
		System.out.println("run");
	}
}

