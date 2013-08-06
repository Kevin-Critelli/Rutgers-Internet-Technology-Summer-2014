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
	 * This function downloads all available pieces that the peer has that we do
	 * not have it starts requesting pieces from 0 up to however many pieces
	 * there are total
	 * 
	 * @author Kevin Critelli
	 * @throws Exception An Exception object is thrown if an error occurs
	 * */

	public synchronized void downloadPiece() throws Exception {
		Message interestedMessage, request;
		int i=0, dif=0, lastPieceSize=0, count=16384, numPieces=0, begin=0;
		
		interestedMessage = new Message(1, (byte) RUBTClientConstants.MESSAGE_TYPE_INTERESTED);
		
		//Read Response from handshake, potential bit field
		try{
			socket.setSoTimeout(6000);
			if(Message.readMessage(din) == 5){}										/*bit field message*/
			else{din.readByte();}													/*no bitfield message*/
		}catch(Exception e){
			//Timed out waiting for bit message, proceeding to send interested message
		}
		
		//send interested message
		dout.write(interestedMessage.message);
		dout.flush();
		socket.setSoTimeout(130000);
		
		//waiting for unchoke message
		if(Message.readMessage(din) ==  1){din.readByte();}											/*unchoked*/
		else{}																						/*no unchoke message*/

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
							
							if(Message.readMessage(din) == 7){}								/*piece message*/
							else{}															/*no piece message*/
							
							pieceSubset = new byte[count];
							
							for (i = 0; i < count; i++) {
								pieceSubset[i] = din.readByte();
							} // save block
							
							if(this.have[numPieces] != true){this.downloaded += count;}
							this.subPieces.add(pieceSubset);

							if (lastPieceSize < 0) {
								updatePieces(numPieces);
								numPieces++;
								
								//SEND EVEN=COMPLETED TRACKER HERE
								//UPDATE DOWNLOADED TOTAL NUMBER OF BYTES THUS FAR	
								this.trackerResponse.sendTrackerFinishedEvent(this.announce_url,this.torrentInfo.info_hash.array(),
																			  this.downloaded,this.uploaded,this.torrentInfo.file_length - this.downloaded);
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
								
								if(Message.readMessage(din) == 7){} 										/*piece message*/
								else{}																		/*no piece message*/
								
								pieceSubset = new byte[16384];

								for (i = 0; i < 16384; i++) {  			
									pieceSubset[i] = din.readByte();
								} // save block
								
								
								if(this.have[numPieces] != true){this.downloaded += count;}
								this.subPieces.add(pieceSubset);

								if (begin + 16384 == this.torrentInfo.piece_length) {
									
									if(numPieces == 0){
										//send event = started to tracker
							
									}
									//UPDATED DOWNLOADED TOTAL NUMBER OF BYTES THUS FAR
									
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
	 * This function updates our main array of pieces, and sets a flag in the have array
	 * notifying other threads that we now have this piece 
	 * 
	 * @author Kevin Critelli
	 * @param index int representing the index of the piece we just downloaded and
	 *              are about to write into our main piece array
	 * */

	public synchronized void updatePieces(int index) throws Exception{
		byte[] fullPiece;
		int size=0, count=0, i=0; 

		this.requests[index] = true;

		for (i = 0; i < this.subPieces.size(); i++) {
			size += this.subPieces.get(i).length;
		}
		fullPiece = new byte[size];

		for (i = 0; i < this.subPieces.size(); i++) {
			System.arraycopy(this.subPieces.get(i),0,fullPiece,count,this.subPieces.get(i).length);
			count += this.subPieces.get(i).length;
		}
		
		//verify hash
		if(verifyHash(fullPiece, this.torrentInfo.piece_hashes[index].array())){
			//hashes match
		}else{
			//hashes do not much
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
		try{
			socket = new Socket(ip, port);
			input = socket.getInputStream();
			output = socket.getOutputStream();
			din = new DataInputStream(input);
			dout = new DataOutputStream(output);
		
			if (!(sendHandshake(this.torrentInfo.info_hash.array()))) {
				System.out.println("Handshake failed");
				return;
			}
			
			downloadPiece();
	
			din.close();
			input.close();
			dout.close();
			output.close();
			socket.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
