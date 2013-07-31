import java.io.*;
import java.net.*;
import java.util.Arrays;

public class UPeer extends RUBTClient implements Runnable {
	byte[] HandshakePrefix = new byte[2];
	public Socket connectionSocket;
	public DataInputStream din = null;
	public InputStream input = null;
	public DataOutputStream dout = null;
	public OutputStream output = null;


	/**
	 * Constructor for objects of class UPeer
	 */

	public UPeer(Socket connectionSocket) throws Exception {
		input = connectionSocket.getInputStream();
		output = connectionSocket.getOutputStream();
		din = new DataInputStream(input);
		dout = new DataOutputStream(output);
	}

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
	
	public void run() {
		try {
			ReceiveHandshake();
		} catch (Exception e) {
			System.out.println("Something went wrong receiving the handshake");
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
