/**
 * Group Members (CS 352 Internet Technology 2013 Summer Session Project 0)
 * 
 * Kevin Critelli
 * Ritchie Vonderschmidt
 * Paul Jones
 * */

import java.io.*;

/**
 * Abstract Class representing a message between that is sent between two peers
 * @author Kevin Critelli
 * 
 * */

public class Message{	
	
	/** non-keep alive message are of size 0 and disregarded **/
	/** All non-keepalive messages start with a single byte which gives their type. **/
 
    /*************************************************************************************************************\
     **
     ** After the handshake, messages between peers take the form of <length prefix><message ID><payload> 
     ** where length prefix is a 4-byte big-endian value and message ID is a single byte. The payload depends on the message
     ** 
     **
	 ** keep-alive: <length prefix> is 0. 	There is no message ID and no payload. 
	 **										These should be sent around once 
	 ** 									every 2 minutes to prevent peers from closing connections. 
	 **										These only need to be sent if no other messages are sent within 
	 **										a 2-minute interval.
	 **
	 ** choke: <length prefix> is 1 and message ID is 0. There is no payload.
	 ** 
	 ** unchoke: <length prefix> is 1 and the message ID is 1. There is no payload.
	 ** 
	 ** interested: <length prefix> is 1 and message ID is 2. There is no payload.
	 **
	 ** uninterested: <length prefix> is 1 and message ID is 3. There is no payload.
	 ** 
	 ** have: <length prefix> is 5 and message ID is 4. The payload is a zero-based index of the piece that 
	 **													has just been downloaded and verified.
	 ** 
	 ** request: <length prefix> is 13 and message ID is 6. The payload is as follows:
	 ** 													<index><begin><length> 
	 ** 													Where <index> is an integer specifying the zero-based piece index 
	 **														<begin> is an integer specifying the zero-based byte offset within 
	 **														the piece, and <length> is the integer specifying the requested 
	 **														length.<length> is typically 2^14 (16384) bytes. A smaller piece 
	 **														should only be used if the piece length is not divisible by 16384. 
	 **														A peer may close the connection if a block larger than 2^14 bytes is requested.
	 ** 
	 ** piece: <length prefix> is 9+X and message ID is 7. The payload is as follows:
	 ** 													<index><begin><block> 
	 **														Where <index> is an integer specifying the zero-based piece index 
	 **														<begin> is an integer specifying the zero-based byte offset within the piece 
	 **														<block> which is a block of data, and is a subset of the piece specified by <index>
	 **
     *******************************************************************************************************/
	
	/** Keep-Alive message ID Field **/
	
	public static final byte MSG_TYPE_KEEP_ALIVE = -1;
	
	/** Choke Message ID Field **/
	
	public static final byte MSG_TYPE_CHOKE = 0;
	
	/** Unchoke Message ID Field **/
	
	public static final byte MSG_TYPE_UNCHOKE = 1;
	
	/** Interested Message ID Field **/
	
	public static final byte MSG_TYPE_INTERESTED = 2;
	
	/** Not Interested Message ID Field **/
	
	public static final byte MSG_TYPE_NOT_INTERESTED = 3;
	
	/** Have Message ID Field **/
	
	public static final byte MSG_TYPE_HAVE = 4;
	
	/** BitField Message ID Field **/
	
	public static final byte MSG_TYPE_BITFIELD = 5;
	
	/** Request Message ID Field **/
	
	public static final byte MSG_TYPE_REQUEST = 6;
	
	/** Piece Message ID Field **/
	
	public static final byte MSG_TYPE_PIECE = 7;
	
	/** Cancel Message ID Field **/
	
	public static final byte MSG_TYPE_CANCEL = 8;
	
	/** Handshake Message ID Field **/

	public static final byte MSG_TYPE_HANDSHAKE = 9; 
	
	/** Protocol String for Handshake Message **/
	
	/**Make some of these variables private ??? **/
	
	public static byte[] PROTOCOL_STRING = new byte [] { 'B', 'i', 't', 'T', 'o', 'r', 'r', 'e', 'n', 't', ' ', 'p', 'r', 'o', 't', 'o', 'c', 'o', 'l'};
	
	public final int lengthPrefix;
	public final byte id;
	public byte[] message = null;
			
	//constructor for keep-alive message
	
	public Message(){
		this.lengthPrefix = 0;
		this.id = MSG_TYPE_KEEP_ALIVE;
		this.message = new byte[] {0,0,0,0};
	}		
			
	//constructor for handshake message		
			
	public Message(byte[] info_hash, byte[] peerid){
		
		this.lengthPrefix = 0;
		this.id = MSG_TYPE_HANDSHAKE;
		this.message = new byte[68];
		this.message[0] = (byte)19;
		System.arraycopy(PROTOCOL_STRING,0,this.message,1,19);
		System.arraycopy(info_hash,0,this.message,28,20);
		System.arraycopy(peerid,0,this.message,48,20);
	}	
	
	//constructor for rest of message types
	
	public Message(int lengthPre, byte messageID){
		this.id = messageID;
		this.lengthPrefix = lengthPre;
		this.message = new byte[this.lengthPrefix + 4];
			
		if(id == MSG_TYPE_CHOKE){								
			System.arraycopy(intToByteArray(1),0,this.message,0,4);				
			this.message[4] = (byte)0;							
		}else if(id == MSG_TYPE_UNCHOKE){
			System.arraycopy(intToByteArray(1),0,this.message,0,4);
			this.message[4] = (byte)1;
		}else if(id == MSG_TYPE_INTERESTED){
			System.arraycopy(intToByteArray(1),0,this.message,0,4);
			this.message[4] = (byte)2;
		}else if(id == MSG_TYPE_NOT_INTERESTED){
			System.arraycopy(intToByteArray(1),0,this.message,0,4);
			this.message[4] = (byte)3;
		}else if(id == MSG_TYPE_HAVE){
			System.arraycopy(intToByteArray(5),0,this.message,0,4);
			this.message[4] = (byte)4;
			//The payload is a zero-based index of the piece that 
			//has just been downloaded and verified
			
		}else if(id == MSG_TYPE_REQUEST){
			System.arraycopy(intToByteArray(13),0,this.message,0,4);
			this.message[4] = (byte)6;
			//<index> is an integer specifying the zero-based piece index 
			//<begin> is an integer specifying the zero-based byte offset within the piece
			//<length> is the integer specifying the requested length
		}else if(id == MSG_TYPE_PIECE){
			System.arraycopy(intToByteArray(this.lengthPrefix),0,this.message,0,4);
			this.message[4] = (byte)7;
		}
	}
	
	/**
	 * This function returns the payload of this Message Object
	 * 
	 * @author Kevin Critelli
	 * @throws Exception An Exception Object is thrown if this message 
	 * 					 does not contain a payload
	 * @return byte[] A Byte array of the payload field of the message
	 * */
	
	public byte[] getPayload() throws Exception{
		byte[] payload = null;
		if(id == MSG_TYPE_HAVE || id == MSG_TYPE_REQUEST || id == MSG_TYPE_PIECE){
			if(id == MSG_TYPE_HAVE){
				payload = new byte[4];
				System.arraycopy(this.message,5,payload,0,4);
				return payload;
				
			}else if(id == MSG_TYPE_PIECE){
				payload = new byte[this.lengthPrefix-1];
				System.arraycopy(this.message,5,payload,0,this.lengthPrefix-1);
				return payload;
				
			}else{
				//request message
				payload = new byte[12];
				System.arraycopy(this.message,5,payload,0,12);
				return payload;
			}
		}else{
			throw new Exception("This message contains no payload");
		}
	}
	
	/**
	 * This function sets the payload of this Message Object
	 * 
	 * A -1 value of any int parameter indicates that it is 
	 * not related to the payload of this message object
	 * 
	 * @param int Have Payload Field
	 * @param int Request Index Payload Field
	 * @param int Request Begin Payload Field
	 * @param int Request Length Payload Field
	 * @param int Piece Index Payload Field
	 * @param int Piece Begin Payload Field
	 * @param byte[] Piece Block Payload Field
	 * 
	 * @author Kevin Critelli
	 * 
	 * */
	
	public void setPayload(byte[] pieceBlock, int pieceBegin, int pieceIndex, 
		int requestLength, int requestBegin, int requestIndex, int havePayload){
			if(id == MSG_TYPE_HAVE){
				System.arraycopy(intToByteArray(havePayload),0,this.message,5,4);
				
			}
			else if(id == MSG_TYPE_PIECE){
				System.arraycopy(intToByteArray(pieceIndex),0,this.message,5,4);		//set index payload
				System.arraycopy(intToByteArray(pieceBegin),0,this.message,9,4);		//set begin payload
				System.arraycopy(pieceBlock,0,this.message,13,this.lengthPrefix-8);			//set bock payload
				
			}else if(id == MSG_TYPE_REQUEST){
				System.arraycopy(intToByteArray(requestIndex),0,this.message,5,4);		//set index payload
				System.arraycopy(intToByteArray(requestBegin),0,this.message,9,4);		//set begin payload
				System.arraycopy(intToByteArray(requestLength),0,this.message,13,4);	//set length payload
				
			}else{
				System.out.println("Not a valid Message Object to call this method on");
				return;
			}
			return;
	}
	
	/**
	 * Converts an integer value to a 4 byte Big-Endian Hex Value
	 * @author Kevin Critelli
	 * 
	 * @param value The Integer to change into 4 byte Big-Endian Hex
	 * @return byte[] A Byte array of size 4 containing the four Hex values
	 */ 

	public static final byte[] intToByteArray(int value) {
		byte[] retVal = new byte[4];
		retVal[0] = (byte) (value >> 24);
		retVal[1] = (byte) (value >> 16);
		retVal[2] = (byte) (value >> 8);
		retVal[3] = (byte) (value);
		return retVal;
	}
	
	/**
	 * @Override toString
	 * 
	 * */
	
	public String toString(){
		String result = null;
		switch(this.id){
			case MSG_TYPE_BITFIELD: result = "BitField Message";
									break;
			case MSG_TYPE_CANCEL: result = "Cancel Message";
									break;
			case MSG_TYPE_CHOKE: result = "Choke Message";
									break;
			case MSG_TYPE_HANDSHAKE: result = "Handshake Mesesage";
									break;
			case MSG_TYPE_HAVE: result = "Have Message";
									break;
			case MSG_TYPE_INTERESTED: result = "Interested Message";
									break;
			case MSG_TYPE_KEEP_ALIVE: result = "Keep-Alive Message";
									break;
			case MSG_TYPE_NOT_INTERESTED: result = "Not Interested Message";
									break;
			case MSG_TYPE_PIECE: result = "Piece Message";
									break;
			case MSG_TYPE_REQUEST: result = "Request Message";
									break;
			case MSG_TYPE_UNCHOKE: result = "Unchoke Message";
									break;
		}			
		return result;
	}
}
