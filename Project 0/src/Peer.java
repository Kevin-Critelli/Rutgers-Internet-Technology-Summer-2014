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
/**
 * Class representing a peer for the download
 **/ 

public class Peer implements Runnable{
	
	public int port;
	public String ip;
	public static DataOutputStream dout = null;
	public static DataInputStream din = null;
	public static Socket socket = null;
	public static OutputStream output = null;
	public static InputStream input = null;

	//placeholder constructor for trackerresponse class
	
	public Peer(int port, String ip){
		this.port = port;
		this.ip = ip;
	}		
			
	public Peer(int port, String ip, byte[] info_hash, byte[] peerid) {
		this.port = port;
		this.ip = ip;
		
		try{
			socket = new Socket(ip,port);
			input = socket.getInputStream();
			output = socket.getOutputStream();
				
			din = new DataInputStream(input);	
			dout = new DataOutputStream(output);
		}catch(Exception e){
			System.out.println("Exception thrown during connection setup");
		}
		
		if(sendHandshake(info_hash,peerid)){
			System.out.println("handshake accepted");
		}else{
			System.out.println("handshake denied");
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

