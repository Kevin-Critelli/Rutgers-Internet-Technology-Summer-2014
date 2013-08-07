/**
 * This class represents a connection that handles uploading from a peer
 * Written by Richie / Kevin
 * 
 * */

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.InputStream; 
import java.net.Socket;
 
public class UPeer extends Peer{

	/**
	 * Constructor for objects of class UPeer
	 * 
	 * @author Kevin Critelli, Richie Vonder Schmidt
	 */

	public UPeer(Socket connectionSocket){
		super(-1,"-1");														//must invoke superclass, these fields are not used those for upeer, so just pass -1 for both
		try{
			System.out.println("new upeer");
			input = connectionSocket.getInputStream();
			output = connectionSocket.getOutputStream();
			din = new DataInputStream(input);
			dout = new DataOutputStream(output);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * receives a handshake, verifies information and sends appropriate message for upload to begin
	 * 
	 * @author Kevin Critelli, Richie Vonder Schmidt
	 * @throws Exception An Exception is thrown if method encounters an error
	 * @return boolean A boolean value is returned, true if this function succeeds, false otherwise
	 * */

	public boolean ReceiveHandshake(byte[] info_hash) throws Exception {
		byte[] receivingShake = new byte[68];
		din.readFully(receivingShake);
		
		Message returnhandshake = new Message(info_hash, RUBTClientConstants.peerid);

		if (receivingShake[0] != (byte)19) {
			//NOT BIT TORRENT PROTOCOL
			return false;
		} else {
			//place info_hash in correct spot
				
			//send back handshake
			dout.write(returnhandshake.message);
			dout.flush();
		
			//read message see if interested
			if(Message.readMessage(din) == 2){
				//Interested
			}else{
				//Not Interested
			}
			
			//if interested send unchoke message
			Message unchoke = new Message(1,(byte)1);
			dout.write(unchoke.message);
			
			//call upload function
			upload();
			return true;
		}
	}
	
	/**
	 * This function handles the piece/request portion of the upload
	 * It will wait for a request message, and respond to them sending correct pieces if we have them
	 * 
	 * @author Kevin Critelli
	 * @throws Exception An Exception object is thrown if an error occurs
	 * */
	
	public void upload()throws Exception{
		Message pieceMsg;
		byte[] block;
		int index, begin, length;
	
		//wait for requests
		while(true){
			if(Message.readMessage(din) == 6){
				index = din.readInt();
				begin = din.readInt();
				length = din.readInt();
					
				if(this.have[index] == true){
					block = new byte[length];
					System.arraycopy(this.pieces[index].array(),begin,block,0,length);
					pieceMsg = new Message(9+length,(byte)7);
					pieceMsg.setPayload(block,begin,index,-1,-1,-1,-1);
					dout.write(pieceMsg.message);
					this.uploaded += length;
				}
			}
		}
	}
	
	/**
	 * Run method for this thread object, process handshake, upload to peer, close all connections/streams
	 * 
	 * @author Kevin Critelli, Richi Vonder Schmidt
	 * */
	
	public void run() {
		try{
			ReceiveHandshake(this.torrentInfo.info_hash.array());
			//at this point, upload is complete, close all streams, exit thread
			din.close();
			input.close();
			dout.close();
			output.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
