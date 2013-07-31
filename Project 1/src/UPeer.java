import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * This class represents a connection that handles uploading from a peer
 * Written by Richie / Kevin
 * 
 * */

public class UPeer extends RUBTClient implements Runnable {
	byte[] HandshakePrefix = new byte[2];
	public Socket connectionSocket;
	public DataInputStream din = null;
	public InputStream input = null;
	public DataOutputStream dout = null;
	public OutputStream output = null;


	/**
	 * Constructor for objects of class UPeer
	 * 
	 * @author Richie Vonder Schmidt
	 */

	public UPeer(Socket connectionSocket) throws Exception {
		input = connectionSocket.getInputStream();
		output = connectionSocket.getOutputStream();
		din = new DataInputStream(input);
		dout = new DataOutputStream(output);
	}
	
	/**
	 * receives a handshake, verifies information and sends appropriate message for upload to begin
	 * 
	 * @author Kevin Critelli, Richie Vonder Schmidt
	 * @throws Exception An Exception is thrown if method encounters an error
	 * @return boolean A boolean value is returned, true if this function succeeds, false otherwise
	 * */

	public boolean ReceiveHandshake() throws Exception {
		byte HandshakePrefix = 19;

		byte[] receivingShake = new byte[68];
		din.readFully(receivingShake);

		byte MessageType = receivingShake[0];

		if (HandshakePrefix != MessageType) {
			return false;
		} else {
			//place info_hash in correct spot
			System.arraycopy(this.torrentInfo.info_hash,0,receivingShake,28,20);
			
			//send back handshake
			dout.write(receivingShake);
		
			//read message see if interested
			if(readMessage() == 2){
				//System.out.println("Interested");
			}else{
				//System.out.println("not interested");
			}
			
			//if interested send unchoke message
			//System.out.println("sending choke message");
			Message unchoke = new Message(1,(byte)1);
			dout.write(unchoke.message);
			
			//call upload function
			upload();
			return true;
		}
	}
	
	/**
	 * Reads a message from the data input stream and determines
	 * what type of message it is, it returns the byte id corresponding
	 * to the type of message it is
	 * 
	 * @author Kevin Critelli
	 * 
	 * @param din The datainputstream object
	 * @return byte The byte id of the message
	 * */
	
	public byte readMessage()throws Exception{
		int length = din.readInt();
		byte id = din.readByte();
		
		if(length == 0){ System.out.println("Keep-Alive"); return -1;}
	
		switch(id){
			case 0: //choke message
					return id;
			case 1: //unchoke message
					return id;
			case 2: //interested message
					return id;
			case 3: //not interested message
					return id;
			case 4: //have message
					return id;
			case 5: //bitfield message
					return id;
			case 6: //request message
					return id;
			case 7: //piece message
					int index = din.readInt();
					int begin = din.readInt();
			case 8: //cancel message
					return id;
			default: break;
		}
		return 0;
	}
	
	/**
	 * This function handles the piece/request portion of the upload
	 * It will wait for a request message, and respond to them sending correct pieces if we have them
	 * 
	 * @author Kevin Critelli
	 * @throws Exception An Exception object is thrown if an error occurs
	 * */
	
	public void upload()throws Exception{
		//wait for requests
		while(true){
			if(readMessage() == 6){
				//System.out.println("got a request message");
				int index = din.readInt();
				int begin = din.readInt();
				int length = din.readInt();
				
				//System.out.println("index " + index);
				//System.out.println("begin " + begin);
				//System.out.println("length " + length);
				
				if(this.have[index] == true){
					byte [] block = new byte[length];
					System.arraycopy(this.pieces[index].array(),begin,block,0,length);
					Message pieceMsg = new Message(9+length,(byte)7);
					pieceMsg.setPayload(block,begin,index,-1,-1,-1,-1);
					//System.out.println("sending piece message");
					dout.write(pieceMsg.message);
					this.uploaded += length;
				}
			}
			//System.out.println("waiting for request");
		}
	}
	
	/**
	 * Run method for this thread object, process handshake, than upload to peer
	 * 
	 * @author Kevin Critelli, Richi Vonder Schmidt
	 * */
	
	public void run() {
		try {
			ReceiveHandshake();
		} catch (Exception e) {
			//something went wrong during handshake or upload 
			e.printStackTrace(e);
		}
		
		try{
			connectionSocket.close();
			din.close();
			input.close();
			dout.close();
			output.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
