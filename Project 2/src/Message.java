/**
 * Group Members (CS 352 Internet Technology 2013 Summer Session Project 0)
 * 
 * Kevin Critelli
 * Ritchie Vonderschmidt
 * Paul Jones
 * */

import java.io.*;
import java.nio.*;

/**
 * Class representing a message (a stream of bytes) between that is sent between
 * two peers
 * 
 * @author Kevin Critelli
 * 
 * */

public class Message {

	/** non-keep alive message are of size 0 and disregarded **/
	/**
	 * All non-keepalive messages start with a single byte which gives their
	 * type.
	 **/

	/*************************************************************************************************************
	 * \
	 ** 
	 ** After the handshake, messages between peers take the form of <length
	 * prefix><message ID><payload> where length prefix is a 4-byte big-endian
	 * value and message ID is a single byte. The payload depends on the message
	 ** 
	 ** 
	 ** keep-alive: <length prefix> is 0. There is no message ID and no payload.
	 * These should be sent around once every 2 minutes to prevent peers from
	 * closing connections. These only need to be sent if no other messages are
	 * sent within a 2-minute interval.
	 ** 
	 ** choke: <length prefix> is 1 and message ID is 0. There is no payload.
	 ** 
	 ** unchoke: <length prefix> is 1 and the message ID is 1. There is no
	 * payload.
	 ** 
	 ** interested: <length prefix> is 1 and message ID is 2. There is no
	 * payload.
	 ** 
	 ** uninterested: <length prefix> is 1 and message ID is 3. There is no
	 * payload.
	 ** 
	 ** have: <length prefix> is 5 and message ID is 4. The payload is a
	 * zero-based index of the piece that has just been downloaded and verified.
	 ** 
	 ** request: <length prefix> is 13 and message ID is 6. The payload is as
	 * follows: <index><begin><length> Where <index> is an integer specifying
	 * the zero-based piece index <begin> is an integer specifying the
	 * zero-based byte offset within the piece, and <length> is the integer
	 * specifying the requested length.<length> is typically 2^14 (16384) bytes.
	 * A smaller piece should only be used if the piece length is not divisible
	 * by 16384. A peer may close the connection if a block larger than 2^14
	 * bytes is requested.
	 ** 
	 ** piece: <length prefix> is 9+X and message ID is 7. The payload is as
	 * follows: <index><begin><block> Where <index> is an integer specifying the
	 * zero-based piece index <begin> is an integer specifying the zero-based
	 * byte offset within the piece <block> which is a block of data, and is a
	 * subset of the piece specified by <index>
	 ** 
	 *******************************************************************************************************/

	public final int lengthPrefix;
	public final byte id;
	public byte[] message = null;
	public byte[] info_hash = null; // used for handshake message
	public byte[] peerid = null; // used for handshake message
	public byte[] piece = null; // block of data in a piece messsage

	// constructor for handshake message
	public Message(byte[] info_hash, byte[] peerid) {

		this.lengthPrefix = 0;
		this.id = RUBTClientConstants.MESSAGE_TYPE_HANDSHAKE;
		this.message = new byte[68];
		this.message[0] = (byte) 19;
		this.info_hash = info_hash;
		this.peerid = peerid;
		System.arraycopy(RUBTClientConstants.BIT_TORRENT_PROTOCOL, 0, this.message,1, 19);
		System.arraycopy(info_hash, 0, this.message, 28, 20);
		System.arraycopy(peerid, 0, this.message, 48, 20);
	}

	// constructor for rest of message types
	// when constructing keep-alive message, messageID = -1
	public Message(int lengthPre, byte messageID) {
		this.id = messageID;
		this.lengthPrefix = lengthPre;
		this.message = new byte[this.lengthPrefix + 4];

		if (id == RUBTClientConstants.MESSAGE_TYPE_CHOKE) {
			System.arraycopy(RUBTClientUtils.intToByteArray(1), 0,
					this.message, 0, 4);
			this.message[4] = (byte) 0;
		} else if (id == RUBTClientConstants.MESSAGE_TYPE_UNCHOKE) {
			System.arraycopy(RUBTClientUtils.intToByteArray(1), 0,
					this.message, 0, 4);
			this.message[4] = (byte) 1;
		} else if (id == RUBTClientConstants.MESSAGE_TYPE_INTERESTED) {
			System.arraycopy(RUBTClientUtils.intToByteArray(1), 0,
					this.message, 0, 4);
			this.message[4] = (byte) 2;
		} else if (id == RUBTClientConstants.MESSAGE_TYPE_NOT_INTERESTED) {
			System.arraycopy(RUBTClientUtils.intToByteArray(1), 0,
					this.message, 0, 4);
			this.message[4] = (byte) 3;
		} else if (id == RUBTClientConstants.MESSAGE_TYPE_HAVE) {
			System.arraycopy(RUBTClientUtils.intToByteArray(5), 0,
					this.message, 0, 4);
			this.message[4] = (byte) 4;
		} else if (id == RUBTClientConstants.MESSAGE_TYPE_REQUEST) {
			System.arraycopy(RUBTClientUtils.intToByteArray(13), 0,
					this.message, 0, 4);
			this.message[4] = (byte) 6;
		} else if (id == RUBTClientConstants.MESSAGE_TYPE_PIECE) {
			System.arraycopy(RUBTClientUtils.intToByteArray(this.lengthPrefix),
					0, this.message, 0, 4);
			this.message[4] = (byte) 7;
		} else if (id == RUBTClientConstants.MESSAGE_TYPE_KEEP_ALIVE) {
			// no id
		}
	}

	/**
	 * This function returns the payload of this Message Object
	 * 
	 * @author Kevin Critelli
	 * @throws Exception
	 *             An Exception Object is thrown if this message does not
	 *             contain a payload
	 * @return byte[] A Byte array of the payload field of the message
	 * */

	public byte[] getPayload() throws Exception {
		byte[] payload = null;
		if (id == RUBTClientConstants.MESSAGE_TYPE_HAVE
				|| id == RUBTClientConstants.MESSAGE_TYPE_REQUEST
				|| id == RUBTClientConstants.MESSAGE_TYPE_PIECE) {
			if (id == RUBTClientConstants.MESSAGE_TYPE_HAVE) {
				payload = new byte[4];
				System.arraycopy(this.message, 5, payload, 0, 4);
				return payload;

			} else if (id == RUBTClientConstants.MESSAGE_TYPE_PIECE) {
				payload = new byte[this.lengthPrefix - 1];
				System.arraycopy(this.message, 5, payload, 0,
						this.lengthPrefix - 1);
				return payload;

			} else {
				// request message
				payload = new byte[12];
				System.arraycopy(this.message, 5, payload, 0, 12);
				return payload;
			}
		} else {
			throw new Exception("This message contains no payload");
		}
	}

	/**
	 * This function sets the payload of this Message Object
	 * 
	 * A -1 value of any int parameter indicates that it is not related to the
	 * payload of this message object
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
			int requestLength, int requestBegin, int requestIndex,
			int havePayload) {
		if (id == RUBTClientConstants.MESSAGE_TYPE_HAVE) {
			System.arraycopy(RUBTClientUtils.intToByteArray(havePayload), 0,
					this.message, 5, 4);
		} else if (id == RUBTClientConstants.MESSAGE_TYPE_PIECE) {
			this.piece = pieceBlock;
			System.arraycopy(RUBTClientUtils.intToByteArray(pieceIndex), 0,
					this.message, 5, 4); // set
			// index
			// payload
			System.arraycopy(RUBTClientUtils.intToByteArray(pieceBegin), 0,
					this.message, 9, 4); // set
			// begin
			// payload
			System.arraycopy(pieceBlock, 0, this.message, 13,
					this.lengthPrefix - 9); // set bock payload

		} else if (id == RUBTClientConstants.MESSAGE_TYPE_REQUEST) {
			System.arraycopy(RUBTClientUtils.intToByteArray(requestIndex), 0,
					this.message, 5, 4); // set index payload
			System.arraycopy(RUBTClientUtils.intToByteArray(requestBegin), 0,
					this.message, 9, 4); // set begin payload
			System.arraycopy(RUBTClientUtils.intToByteArray(requestLength), 0,
					this.message, 13, 4); // set length payload

		} else {
			System.out
					.println("Not a valid Message Object to call this method on");
			return;
		}
		return;
	}

	/**
	 * @override toString
	 * 
	 * */

	public String toString() {
		String result = null;
		switch (this.id) {
		case RUBTClientConstants.MESSAGE_TYPE_BITFIELD:
			result = "BitField Message";
			break;
		case RUBTClientConstants.MESSAGE_TYPE_CANCEL:
			result = "Cancel Message";
			break;
		case RUBTClientConstants.MESSAGE_TYPE_CHOKE:
			result = "Choke Message";
			break;
		case RUBTClientConstants.MESSAGE_TYPE_HANDSHAKE:
			result = "Handshake Mesesage";
			System.out.print("Protocol ");
			for (int i = 0; i < 20; i++) {
				System.out.print(this.message[i]);
			}
			System.out.println();

			System.out.print("Info_hash ");
			for (int i = 0; i < this.info_hash.length; i++) {
				System.out.print(this.info_hash[i] + " ");
			}
			System.out.println();

			System.out.print("peerid ");
			for (int i = 0; i < this.peerid.length; i++) {
				System.out.print(this.peerid[i]);
			}
			System.out.println();
			break;
		case RUBTClientConstants.MESSAGE_TYPE_HAVE:
			result = "Have Message";
			break;
		case RUBTClientConstants.MESSAGE_TYPE_INTERESTED:
			result = "Interested Message";
			break;
		case RUBTClientConstants.MESSAGE_TYPE_KEEP_ALIVE:
			result = "Keep-Alive Message";
			break;
		case RUBTClientConstants.MESSAGE_TYPE_NOT_INTERESTED:
			result = "Not Interested Message";
			break;
		case RUBTClientConstants.MESSAGE_TYPE_PIECE:
			result = "Piece Message";
			System.out.print("block ");
			for (int i = 0; i < this.piece.length; i++) {
				System.out.print(this.piece[i]);
			}
			System.out.println();
			break;
		case RUBTClientConstants.MESSAGE_TYPE_REQUEST:
			result = "Request Message";
			break;
		case RUBTClientConstants.MESSAGE_TYPE_UNCHOKE:
			result = "Unchoke Message";
			break;
		}
		return result;
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
	
	public static final byte readMessage(DataInputStream din)throws Exception{
		int length = din.readInt(); 
		
		if(length == 0){return -1;}
		
		byte id = din.readByte();
		
		switch(id){
			case 0: //choke message
				//System.out.println("choke");
					return id;
			case 1: //unchoke message
					//System.out.println("unchoked");
					return id;
			case 2: //interested message
					return id;
			case 3: //not interested message
					return id;
			case 4: //have message
					return id;
			case 5: //bitfield message 
					//System.out.println("bit field");
					for(int i =0;i<length-1;i++){
						din.readByte();
					}
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
		System.out.println("waht");
		return 0;
	}
}
