/**
 * Class representing a connection/stream to a peer in which we are downloading from
 * @author Kevin Critelli
 * 
 **/

import java.security.MessageDigest;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.net.Socket;
import java.io.*;
import java.net.*;
 
public class DPeer extends Peer{

	public Socket socket = null;
	ArrayList<byte[]> subPieces = new ArrayList<byte[]>(); 							// arraylist holding the sub pieces within the piece requested
	byte[] pieceSubset = null; 														// byte array representing a sub piece from within the piece requested

	/**
	 * Constructor for a DPeer object
	 * 
	 * @author Kevin Critelli
	 * @param ip String representation of the ip of the peer to connect to
	 * @param port int value of the port to connect to for this peer
	 *
	 * */
	 
	public DPeer(int port, String ip){
		super(port,ip);
	}

	/**
	 * This function sends a handshake to the peer at ip and port passed to
	 * constructor
	 * 
	 * @author Kevin Critelli
	 * @param info_hash	20 byte info_hash from the meta file
	 * @throws Exception An Exception object is thrown if an error occurs
	 * @return true if handshake accepted, false otherwise
	 * */

	public boolean sendHandshake(byte[] info_hash) throws Exception {
		Message handshake; 
		byte[] receivingShake, peerInfoHash;
		
		handshake = new Message(info_hash, RUBTClientConstants.peerid);
		dout.write(handshake.message);
		dout.flush();
		socket.setSoTimeout(130000);
		receivingShake = new byte[68];
		din.readFully(receivingShake);
		peerInfoHash = Arrays.copyOfRange(receivingShake, 28, 48);
		
		if(!Arrays.equals(peerInfoHash, info_hash)){ return false;} 
		else 									   { return true;}
	}

	/**
	 * This function attemps to request and download a piece from the peer that
	 * we do not have or aren't already currently downloading, it calls get_piece_to_request
	 * in order to find out what piece to request, it than calls get_piece_from_peer with the
	 * return value from the previous call in order to actually download the piece
	 * 
	 * @author Kevin Critelli
	 * @throws Exception An Exception object is thrown if an error occurs
	 * */

	public void downloadPiece() throws Exception {
		Message interestedMessage, request;
		int i=0, count=16384, numPieces=0, begin=0, messageID =0;
		
		interestedMessage = new Message(1, (byte) RUBTClientConstants.MESSAGE_TYPE_INTERESTED);
		
		if(getResponse() == (int)RUBTClientConstants.MESSAGE_TYPE_BITFIELD){				//Add timing out for no bit fields
			System.out.println("hey we got a bit field!");
		}
		
		//send interested message
		dout.write(interestedMessage.message);
		dout.flush();
		socket.setSoTimeout(130000);
		
		messageID = getResponse();
		if(messageID == 1){
			System.out.println("were unchoked!");
		}else if(messageID == -1){
			return;
		}																						
						
		while(true){
			//call synchronized function to check what piece we need
			int piece_index = get_piece_to_request();
			
			//we have all pieces, so just return and exit thread
			if(piece_index == -1){ 
				break;
			};
			
			int retVal = get_piece_from_peer(piece_index);
			
			if(retVal == -1){
				System.out.println("exiting");
				return;
			}else{
				updatePieces(piece_index);
			}
		}
	}
	
	/**
	 * This function gets the actual piece from the peer, it has two main portions
	 * requesting the last piece, and requesting any other piece besides the last
	 * Last piece is variable size, rest of the pieces are fixed
	 * This function just sends requests until we receive the whole block
	 * 
	 * @author Kevin Critelli
	 * @param index int representing the index of the piece we want to get from peer
	 * @throws Exception Throws an exception if an error occurs
	 * @return int Returns 1 if successful, or -1 if some error occurred
	 * */
	
	public int get_piece_from_peer(int index)throws Exception{
		int totalRequested = 0;
		Message requestMessage, haveMsg;
		int i=0, r=0, dif=0, lastPieceSize=0;
		int messageID = 0;

		//check what piece were requesting
		if(index < this.torrentInfo.piece_hashes.length-1){
			//regular piece
			do{
				//send request message with appropriate size, offset, and index
				requestMessage = new Message(13,(byte)6);
				requestMessage.setPayload(null,-1,-1,16384,totalRequested,index,-1);
				totalRequested += 16384;
				dout.write(requestMessage.message);
				dout.flush();
				socket.setSoTimeout(1300000);
				
				messageID = getResponse();
				if(messageID == (int)RUBTClientConstants.MESSAGE_TYPE_PIECE){
					//piece message
					
					pieceSubset = new byte[16384];

					for (i = 0; i < 16384; i++) {  			
						pieceSubset[i] = din.readByte();
					} // save block
					
					this.subPieces.add(pieceSubset);
				}else if(messageID == -1){
					return -1;
				}
				//POTENTIAL CUT END*/																						
			}while(totalRequested != this.torrentInfo.piece_length);							//keep going until we have requesting the total amount of the piece
			//downloaded the whole piece
			
			haveMsg = new Message(5, (byte) RUBTClientConstants.MESSAGE_TYPE_HAVE);
			haveMsg.setPayload(null,-1,-1,-1,-1,-1,index);
			dout.write(haveMsg.message);
			dout.flush();
			
		}else if(index == this.torrentInfo.piece_hashes.length-1){
			
			//last piece
			dif = this.torrentInfo.piece_hashes.length - 1;
			lastPieceSize = this.torrentInfo.file_length - (dif * this.torrentInfo.piece_length);
			
			do{
				if((totalRequested + 16384) < lastPieceSize){
					//request 16384, send request
					requestMessage = new Message(13,(byte)6);
					requestMessage.setPayload(null,-1,-1,16384,totalRequested,index,-1);
					totalRequested += 16384;
					dout.write(requestMessage.message);
					dout.flush();
					socket.setSoTimeout(1300000);
					
					messageID = getResponse();
					if(messageID == (int)RUBTClientConstants.MESSAGE_TYPE_PIECE){
						//piece message
						
						pieceSubset = new byte[16384];

						for (i = 0; i < 16384; i++) {  			
							pieceSubset[i] = din.readByte();
						} // save block
						
						this.subPieces.add(pieceSubset);
					}else if(messageID == -1){
						return -1;
					}
				}else{
					//request variable amount of whats left, send request
					requestMessage = new Message(13,(byte)6);
					requestMessage.setPayload(null,-1,-1,lastPieceSize-totalRequested,totalRequested,index,-1);
					r = lastPieceSize-totalRequested;
					totalRequested += lastPieceSize-totalRequested;
					dout.write(requestMessage.message);
					dout.flush();
					socket.setSoTimeout(1300000);
					
					messageID = getResponse();
					if(messageID == (int)RUBTClientConstants.MESSAGE_TYPE_PIECE){
						//piece message
						
						pieceSubset = new byte[r];

						for (i = 0; i < r; i++) {  			
							pieceSubset[i] = din.readByte();
						} // save block
						
						this.subPieces.add(pieceSubset);
					}else if(messageID == -1){
						return -1;
					}
				}
			}while(totalRequested != lastPieceSize);					//keep going until the total amount we requested is equal to the size of the piece
			//done downloading piece
			
			haveMsg = new Message(5, (byte) RUBTClientConstants.MESSAGE_TYPE_HAVE);
			haveMsg.setPayload(null,-1,-1,-1,-1,-1,index);
			dout.write(haveMsg.message);
			dout.flush();
			
		}
		return 1;
	}
	
	/**
	 * This function determines what piece we need to request from the peer
	 * It is synchronized so that no threads access the 'Requested' array at the same time,
	 * therefore avoiding two threads requesting the same piece
	 * 
	 * @author Kevin Critelli
	 * @return int Returns an int value representing the index of the piece that this thread should request from peer
	 * 
	 * */
	
	public synchronized int get_piece_to_request(){
		for(int i=0;i<this.requests.length;i++){
			if(this.requests[i] == false){
				this.requests[i] = true;
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * This function verifies the hash of the piece against the hash from the torrent file
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
	 * Synchronized function that updates our main array of pieces, and sets a flag in the have array
	 * notifying other threads that we now have this piece. This function is synchronized
	 * to make sure no two threads write into our main array of pieces at the same time, and no two threads
	 * are updating the 'Have' array at the same time
	 * 
	 * @author Kevin Critelli
	 * @param index int representing the index of the piece we just downloaded and
	 *              are about to write into our main piece array
	 * @throws Exception Throws an exception if an error occurs
	 * */

	public synchronized void updatePieces(int index) throws Exception{
		byte[] fullPiece;
		int size=0, count=0, i=0; 
		
		//Get the size of the full piece
		for (i = 0; i < this.subPieces.size(); i++) {
			size += this.subPieces.get(i).length;
		}
		
		fullPiece = new byte[size];
		
		//copy the pieces into fullPiece array
		for (i = 0; i < this.subPieces.size(); i++) {
			System.arraycopy(this.subPieces.get(i),0,fullPiece,count,this.subPieces.get(i).length);
			count += this.subPieces.get(i).length;
		}
		
		//verify hash
		if(verifyHash(fullPiece, this.torrentInfo.piece_hashes[index].array())){
			//hashes match
			//System.out.println("HASH MATCH");
		}else{
			//hashes do not much, do something close streams?
			//System.out.println("HASH DO NOT MATCH");
		}
	
		// wraps full piece and puts into main piece array
		this.downloaded += fullPiece.length;
		this.left = this.left - fullPiece.length;
		ByteBuffer buffer = ByteBuffer.wrap(fullPiece);
		this.pieces[index] = buffer;
		this.have[index] = true;
		this.subPieces = new ArrayList<byte[]>();
		//System.out.println("downloaded - " + this.downloaded + " left - " + this.left + " Thread " + ip + " " + port);
	}

	/**
	 * Run function for this thread, send a handshake, than request pieces from
	 * the peer
	 * 
	 * @author Kevin Critelli
	 * */

	public void run() {
		try{
			//open socket and streams to peer
			socket = new Socket(ip, port);
			initConnection(socket);
			
			System.out.println("successfull connection to ip " + ip + " port " + port);
			
			//send the handshake to the peer
			if (!(sendHandshake(this.torrentInfo.info_hash.array()))) {
				System.out.println("Handshake failed");
				return;
			}
			
			//handshake accepted, start requesting pieces from peer
			downloadPiece();
	
			//finished downloading all pieces, close all streams and exit
			closeConnection();
			socket.close();
		}catch(UnknownHostException e){
			System.out.println("Unknownhost with Thread " + ip + " port " + port);
		}catch(IOException e){
			System.out.println("IOException with Thread " + ip + " port " + port);
		}catch(SecurityException e){
			System.out.println("SecurityException with Thread " + ip + " port " + port);
		}catch(IllegalArgumentException e){
			System.out.println("IllegalArgumentException " + ip + " port " + port);
		}
		catch(Exception e){
			System.out.println("Exception with Thread " + ip + " port " + port);
		}
	}
	
	/**
	 * This function captures the response from a peer after sending messages to it
	 * It behaves differently according to what it receives, if it recevies a regular message that we
	 * expect it simply returns the id, but if it receives a choke, keep-alive, or cancel it acts differently
	 * If its a choke message we set a time out of 2 mins and wait to be unchoked, if were not unchoked by than we sever connection
	 * If its a keep-alive we simply keep going and read in the next message
	 * If cancel message, it severs the connection
	 * 
	 * @author Kevin Critelli
	 * @throws Exception An Exception object is thrown if an error occurs
	 * @return int The int representation of the id
	 * */
	
	public int getResponse()throws Exception{
		byte messageID = Message.readMessage(din);
		
		while(true){
			switch(messageID){
				case RUBTClientConstants.MESSAGE_TYPE_KEEP_ALIVE : 
					//keep alive keep going
					System.out.println("got a keep-alive " + ip + " " + port);
					break;
				case RUBTClientConstants.MESSAGE_TYPE_CHOKE :
					//choke message
					System.out.println("I got choked " + ip + " " + port);
					this.isChoked = true;
					socket.setSoTimeout(000120);						//wait up to two minutes to get unchoked, if not sever connection
					
					try{
						if(Message.readMessage(din) == 1){
							//unchoked
							System.out.println("I got unchoked before the interval ended");
							this.isChoked = false;
						}
					}catch(Exception e){
						System.out.println("Timed out waiting to be unchoked! Severing Connection");
						return -1;
					}
				case RUBTClientConstants.MESSAGE_TYPE_UNCHOKE :
					return (int)RUBTClientConstants.MESSAGE_TYPE_UNCHOKE;
				case RUBTClientConstants.MESSAGE_TYPE_INTERESTED :
					return (int)RUBTClientConstants.MESSAGE_TYPE_INTERESTED;
				case RUBTClientConstants.MESSAGE_TYPE_NOT_INTERESTED :
					return (int)RUBTClientConstants.MESSAGE_TYPE_NOT_INTERESTED;
				case RUBTClientConstants.MESSAGE_TYPE_HAVE :
					return (int)RUBTClientConstants.MESSAGE_TYPE_HAVE;
				case RUBTClientConstants.MESSAGE_TYPE_BITFIELD :
					return (int)RUBTClientConstants.MESSAGE_TYPE_BITFIELD;
				case RUBTClientConstants.MESSAGE_TYPE_REQUEST :
					return (int)RUBTClientConstants.MESSAGE_TYPE_REQUEST;
				case RUBTClientConstants.MESSAGE_TYPE_PIECE :
					return (int)RUBTClientConstants.MESSAGE_TYPE_PIECE;
				case RUBTClientConstants.MESSAGE_TYPE_CANCEL :
					return (int)RUBTClientConstants.MESSAGE_TYPE_CANCEL;
				case RUBTClientConstants.MESSAGE_TYPE_HANDSHAKE :
					return (int)RUBTClientConstants.MESSAGE_TYPE_HANDSHAKE;
			}
			messageID = Message.readMessage(din);	
		}
	}

	public String toString() {
		return "" + ip + ":" + port;
	}
}
