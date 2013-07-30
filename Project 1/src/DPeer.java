/**
 * Class representing a connection/stream to a peer in which we are downloading from
 * @author Kevin Critelli
 * 
 **/

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;

public class DPeer extends RUBTClient implements Runnable {

	public int port;
	public String ip;
	public DataOutputStream dout = null;
	public DataInputStream din = null;
	public Socket socket = null;
	public OutputStream output = null;
	public InputStream input = null;
	ArrayList<byte[]> subPieces = new ArrayList<byte[]>(); // arraylist holding
															// the sub pieces
															// within the piece
															// requested
	byte[] pieceSubset = null; // byte array representing a sub piece from
								// within the piece requested

	/**
	 * Constructor for a DPeer object
	 * 
	 * @author Kevin Critelli
	 * @param ip
	 *            String representation of the ip of the peer to connect to
	 * @param port
	 *            int value of the port to connect to for this peer
	 * */
	 
	public DPeer(String ip, int port) {
		try {
			this.port = port;
			this.ip = ip;
			socket = new Socket(ip, port);
			input = socket.getInputStream();
			output = socket.getOutputStream();
			din = new DataInputStream(input);
			dout = new DataOutputStream(output);
		} catch (Exception e) {
			System.out.println("exception thrown in DPeer thread");
		}
	}

	/**
	 * This function sends a handshake to the peer at ip and port passed to
	 * constructor
	 * 
	 * @author Kevin Critelli
	 * @param info_hash
	 *            20 byte info_hash from the meta file
	 * @throws Exception
	 * @return true if handshake accepted, false otherwise
	 * */

	public boolean sendHandshake(byte[] info_hash) throws Exception {

		Message handshake = new Message(info_hash, RUBTClientConstants.peerid);
		dout.write(handshake.message);
		dout.flush();
		socket.setSoTimeout(130000);

		byte[] receivingShake = new byte[68];
		din.readFully(receivingShake);

		byte[] peerInfoHash = Arrays.copyOfRange(receivingShake, 28, 48);
		if (!Arrays.equals(peerInfoHash, info_hash)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * This function downloads all available pieces that the peer has that we do
	 * not have it starts requesting pieces from 0 up to however many pieces
	 * there are total
	 * 
	 * @author Kevin Critelli
	 * @throws Exception
	 * */

	public void downloadPiece() throws Exception {
		Message interestedMessage = new Message(1, (byte) RUBTClientConstants.MESSAGE_TYPE_INTERESTED);
		Message request = null;
		int i;
		int dif;
		int lastPieceSize;
		int count = 16384;
		int numPieces = 0;
		int begin = 0;
		byte[] buf = null;
		
		//Read Response from handshake, potential bit field
		if(readMessage() == 5){}										/*bit field message*/
		else{ din.readByte();}											/*no bitfield message*/
		
		//send interested message
		dout.write(interestedMessage.message);
		dout.flush();
		socket.setSoTimeout(130000);
		
		if(readMessage() ==  1){ din.readByte();}						/*unchoked*/
		else{}															/*no unchoke message*/

		dout.write(interestedMessage.message);
		dout.flush();
		socket.setSoTimeout(130000);

		dif = this.torrentInfo.piece_hashes.length - 1;
		lastPieceSize = this.torrentInfo.file_length - (dif * this.torrentInfo.piece_length);

		// READY TO EXCHANGE DATA
		// loop until we have each piece
		while (numPieces != this.torrentInfo.piece_hashes.length) {
			// loop until we have each subset of data to make up the piece
			while (true) {
				//this block is for the last piece which is variable size
				if (numPieces + 1 == this.torrentInfo.piece_hashes.length) {

					if (this.requests[numPieces] == true
							&& this.have[numPieces] == true) {
						// already have last piece
						numPieces++;
						break;
					}else{
						while (true) {

							this.requests[numPieces] = true;
							request = new Message(13, (byte) 6);

							if (lastPieceSize < 16384) {
								count = lastPieceSize;
							} else {
								count = 16384;
							}

							lastPieceSize = lastPieceSize - 16384;
							request.setPayload(null, -1, -1, count, begin,numPieces, -1);
							dout.write(request.message);
							dout.flush();
							socket.setSoTimeout(130000);
							buf = new byte[4];
							
							if(readMessage() == 7){						/*piece message*/}
							else{										/*no piece message*/}
							
							pieceSubset = new byte[count];
							
							for (i = 0; i < count; i++) {
								pieceSubset[i] = din.readByte();
							} // save block

							this.subPieces.add(pieceSubset);

							if (lastPieceSize < 0) {
								updatePieces(numPieces);
								numPieces++;
								break;
							}
							begin += count;
						}
						break;
					}
				} else {
						//this else block is for pieces that are fixed size ie everything but the last piece
						if(this.requests[numPieces] == true && this.have[numPieces] == true){
							//we have the piece already
							numPieces++;
							break;
						}else{
							while(true){
								this.requests[numPieces] = true;
								request = new Message(13, (byte) 6);
								request.setPayload(null, -1, -1, 16384, begin, numPieces,-1);
								dout.write(request.message);
								dout.flush();
								socket.setSoTimeout(1300000);
								buf = new byte[4];
								
								if(readMessage() == 7){ 				/*piece message*/}
								else{									/*no piece message*/}
								
								pieceSubset = new byte[16384];

								for (i = 0; i < 16384; i++) {  			
									pieceSubset[i] = din.readByte();
								} // save block

								this.subPieces.add(pieceSubset);

								if (begin + 16384 == this.torrentInfo.piece_length) {
									updatePieces(numPieces);
									numPieces++;
									begin = 0;
									break;
								} else {
									begin += 16384;
								}
							}
						}
					break;
				}
			}
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
			case 4: //have message.
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
	 * This funtion verifys the hash of the piece against the hash from the torrent file
	 * 
	 * @author Kevin Critelli
	 * @param piece A byte[] array representation of the piece
	 * @param hash A byte[] array of the hash from the torrent file
	 * */
	
	public boolean verifyHash(byte[] piece, byte[]hash)throws Exception{
		byte temp[];
		
		MessageDigest x = MessageDigest.getInstance("SHA-1");
		temp = x.digest(piece);
		x.update(temp);
		
		if(Arrays.equals(temp,hash)){ return true;}
		else{ return false;}
	}
	
	/**
	 * This function updates our main array of pieces, and sets a flag in the have array
	 * notifying other threads that we now have this piece 
	 * This function updates our main array of pieces, and sets a flag in the
	 * have array notifying other threads that we now have this piece
	 * 
	 * @author Kevin Critelli
	 * @param index
	 *            int representing the index of the piece we just downloaded and
	 *            are about to write into our main piece array
	 * */

	public synchronized void updatePieces(int index) {
		byte[] fullPiece;
		int size = 0;
		int count = 0;
		int i;

		this.requests[index] = true;

		for (i = 0; i < this.subPieces.size(); i++) {
			size += this.subPieces.get(i).length;
		}
		fullPiece = new byte[size];

		for (i = 0; i < this.subPieces.size(); i++) {
			System.arraycopy(this.subPieces.get(i), 0, fullPiece, count,
					this.subPieces.get(i).length);
			count += this.subPieces.get(i).length;
		}

		// wraps full piece and puts into main piece array
		ByteBuffer buffer = ByteBuffer.wrap(fullPiece);
		this.pieces[index] = buffer;
		this.have[index] = true;
		this.subPieces = new ArrayList<byte[]>();
	}

	public String toString() {
		return "" + ip + ":" + port;
	}

	/**
	 * Run function for this thread, send a handshake, than request pieces from
	 * the peer
	 * 
	 * @author Kevin Critelli
	 * */

	public void run() {
		try {
			if (!(sendHandshake(this.torrentInfo.info_hash.array()))) {
				System.out.println("Handshake failed");
				return;
			}
	
			downloadPiece();
		} catch (Exception e) {
			System.out.println("Exception thrown in run method");
			e.printStackTrace();
		}
	}
}
