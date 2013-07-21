/**
 * Group Members (CS 352 Internet Technology 2013 Summer Session Project 0)
 * 
 * Kevin Critelli
 * Ritchie Vonderschmidt
 * Paul Jones
 * */

import java.nio.ByteBuffer;
import java.util.*;
import java.io.*;
import java.net.*;

public class RUBTClient {

/**
 * Fields used for Tracker Request
 * */
 
private final static String KEY_INFO_HASH = "info_hash";
private final static String KEY_PEER_ID = "peer_id";
private final static String KEY_PORT = "port";
private final static String KEY_UPLOADED = "uploaded";
private final static String KEY_DOWNLOADED = "downloaded";
private final static String KEY_LEFT = "left";
private final static String KEY_EVENT = "event";
private final static String KEY_EVENT_STARTED = "started";
private final static String KEY_EVENT_STOPPED = "stopped";
private final static String KEY_EVENT_COMPLETELED = "completed";
private final static String KEY_IP = "ip"; // optional!

/**
 * Streams and socket to read send information to/from peer
 * */

public static DataOutputStream dout = null;
public static DataInputStream din = null;
public static Socket clientSocket = null;
public static OutputStream output = null;
public static InputStream input = null;

public static TorrentInfo ti = null;

/**
 * Fields for handshake Message
 * */

public static byte[] protocol_string = new byte [] { 'B', 'i', 't', 'T', 'o', 'r', 'r', 'e', 'n', 't', ' ', 'p', 'r', 'o', 't', 'o', 'c', 'o', 'l'};
public static byte[] peerid = "paukevinsrichschmidt".getBytes(); //generate random peerid?
public static byte[] info_hash = null;

public static void main(String[] args) {
	
	byte[] trackerResponse = null;
	ArrayList<Peer> peers = null;
	TrackerResponse tr = null;
	boolean retVal = false;
	boolean found = false;
	int i;

	if(args.length != 2){
		System.out.println("Incorrect number of arguments");
		System.out.println("Correct Usage: java -cp . RUBTClient <Torrent File> <File to Save to>");
		return;
	}
	
	try {
		ti = getTorrentInfoFrom(args[0]);
	} catch (Exception e) {
		System.out.println("There's something wrong with your torrent info file.");
		return;
	}

	try {
		trackerResponse = getTrackerResponse(ti);
	} catch (Exception e) {
		System.out.println("There was a problem with a GET request.");
		e.printStackTrace();
	return;
	}
	
	try {
		tr = decodeTrackerResponse(trackerResponse);
	} catch (Exception e) {
		System.out.println("There was a problem decoding the tracker response");
		e.printStackTrace();
	}
	
	/**
	 * Obtain list of peers from Tracker Response Object and find ip 128.6.171.3 (for this portion of project)
	 * */
	
	peers = tr.peers;		
	
    for(i=0;i<peers.size();i++){
      if(peers.get(i).toString().contains("128:6:171:3")) {
        //System.out.println("Found proper peer at index " + i + " with port " + peers.get(i).port);
        found = true;
        break;
      }
	}	
	
	String ipString = peers.get(i).ip.replaceAll(":",".");
	
	if(found == false){
		System.out.println("Could not find peer @ ip 128.6.171.3 from peer list");
		return;
	}
	
	/**
	 * grab info_hash from torrent object
	 * */
	
	info_hash = ti.info_hash.array();
	
	/**
	 * send handshake to peer @ ip 128.6.171.3 (for this assignment)
	 * */
	
	retVal = sendHandshake(ipString, peers.get(i).port);
	
	if(retVal == false){
		System.out.println("Handshake Failed severing connection");
		try{
			completeConnection();
		}catch(Exception e){
			e.printStackTrace();
		}
		return;
	}else{
		System.out.println("Successful Handshake");
	}

	/**
	 * Handshake set up, now request pieces of file and download
	 * Method not finished yet
	 * */
	
	//downloadFile();
	
	/**
	 * close connections/streams and exit program
	 * */
	
	try{
		completeConnection();
	}catch(Exception e){
		e.printStackTrace();
	}*/
}
/**
/**
 * Sends a handshake message to the peer obtained from the tracker response
 * 
 * @author Kevin Critelli
 * 
 * @param ip The ip of the peer to handshake with
 * @param port The port of the peer to handshake with
 * @return True if handshake succeeds, False if fails
 * */

public static boolean sendHandshake(String ip, int port){
	try{	
		clientSocket = new Socket(ip,port);
		input = clientSocket.getInputStream();
		output = clientSocket.getOutputStream();
		
		din = new DataInputStream(input);	
		dout = new DataOutputStream(output);
		
		/**
		 * write handshake information into byte array to send into socket to peer
		 * */
		
		byte [] sendingShake = new byte [68];
		sendingShake[0] = 19;
		System.arraycopy(protocol_string,0,sendingShake,1,19);
		System.arraycopy(info_hash,0,sendingShake,28,20);
		System.arraycopy(peerid,0,sendingShake,48,20);
		
		//System.out.println("Sending handshake Message...");
		dout.write(sendingShake);
		dout.flush();
		clientSocket.setSoTimeout(130000);
		
		/**
		 * Capture the receiving message from the handshake
		 * */
		
		//read response
		byte[] receivingShake = new byte[68];
		din.readFully(receivingShake);
		
		/**
		 * Verify that our info_hash matches with the peer
		 * */
		
		byte[] peerInfoHash = Arrays.copyOfRange(receivingShake, 28, 48);
		if(!Arrays.equals(peerInfoHash, info_hash)){
			System.out.println("Info_hash values do not match....");
			return false;
		}
		
		/**
		 * sends an interested message to peer
		 * */
		
		byte[] bigEnd = intToByteArray(1);
		byte msgId = 2;
		byte[] interestMsg = new byte[5];
		interestMsg[0] = bigEnd[0];
		interestMsg[1] = bigEnd[1];
		interestMsg[2] = bigEnd[2];
		interestMsg[3] = bigEnd[3];
		interestMsg[4] = msgId;
		dout.write(interestMsg);
		dout.flush();
		clientSocket.setSoTimeout(130000);
		
		/**
		 * Grabs response from peer 
		 * */
		 
		byte[] receiveMsg = new byte[10];
		din.readFully(receiveMsg);
		/*for(int k =0;k<receiveMsg.length;k++){
			System.out.println("byte " + receiveMsg[k]);
		}*
		//System.out.println("received?");
		
		//begin piece1
		
		//4 bytes <length prefix> 1 byte ID
		//4 byte integer <index> 0
		//4 byte integer <begin> 0 
		//4 byte integer <length> public final int piece_length;
		
		/**
		 * request first piece message
		 * all hardcoded due to starting too late on project
		 * *
		
		byte[] requestMsg1 = new byte[17];
		byte[] lengthPrefix = intToByteArray(13);
		byte msg1ID = 6;
		requestMsg1[0] = lengthPrefix[0];
		requestMsg1[1] = lengthPrefix[1];
		requestMsg1[2] = lengthPrefix[2];
		requestMsg1[3] = lengthPrefix[3];
		requestMsg1[4] = msg1ID;
		
		requestMsg1[5] = 0;
		requestMsg1[6] = 0;
		requestMsg1[7] = 0;
		requestMsg1[8] = 0;
		
		requestMsg1[9] = 0;
		requestMsg1[10] = 0;
		requestMsg1[11] = 0;
		requestMsg1[12] = 0;
		
		byte[] length = intToByteArray(ti.piece_length);
		requestMsg1[13] = length[0];
		requestMsg1[14] = length[1];
		requestMsg1[15] = length[2];
		requestMsg1[16] = length[3];
		
		dout.write(requestMsg1);
		dout.flush();
		clientSocket.setSoTimeout(130000);
		
		byte[] piece1 = new byte[ti.piece_length];
		din.readFully(piece1);
		//System.out.println("Piece 1 Received?");
		
		/*for(int k =0;k<piece1.length;k++){
			System.out.println("piece? " + piece1[k]);
		}*
		
		//end piece 1
		//System.out.println("ti " + ti.piece_length);
		//begin piece 2
		//4 bytes <length prefix> 1 byte ID
		//4 byte integer <index> 8
		//4 byte integer <begin> 0 
		//4 byte integer <length> public final int piece_length;
		/*
		byte[] requestMsg2 = new byte[17];
		byte[] lengthPrefix2 = intToByteArray(13);
		byte msg2ID = 6;
		requestMsg2[0] = lengthPrefix2[0];
		requestMsg2[1] = lengthPrefix2[1];
		requestMsg2[2] = lengthPrefix2[2];
		requestMsg2[3] = lengthPrefix2[3];
		requestMsg2[4] = msg2ID;
		
		//index = 1
		byte[] index1 = intToByteArray(1);
		requestMsg2[5] = index1[0];
		requestMsg2[6] = index1[1];
		requestMsg2[7] = index1[2];
		requestMsg2[8] = index1[3];
		
		byte[] begin1 = intToByteArray(ti.piece_length);
		requestMsg2[9] = begin1[0];
		requestMsg2[10] = begin1[1];
		requestMsg2[11] = begin1[2];
		requestMsg2[12] = begin1[3];
		
		byte[] length2 = intToByteArray(16384);
		requestMsg2[13] = length2[0];
		requestMsg2[14] = length2[1];
		requestMsg2[15] = length2[2];
		requestMsg2[16] = length2[3];
		
		dout.write(requestMsg2);
		dout.flush();
		clientSocket.setSoTimeout(130000);
		
		byte[] piece2 = new byte[ti.piece_length];
		//din.readFully(piece2);
		/*for(int k = 0;k<piece2.length;k++){
			try{
				System.out.println("piece " + din.readByte());
			}catch(Exception e){
				break;
			}
		}*
		/*
		System.out.println("received piece 2?");
		
		/*for(int k =0;k<piece2.length;k++){
			System.out.println("piece? " + piece2[k]);
		}*
		//end piece 2
		
		//begin piece 3
		//4 bytes <length prefix> 1 byte ID
		//4 byte integer <index> 8
		//4 byte integer <begin> 0 
		//4 byte integer <length> public final int piece_length;
		/*
		byte[] requestMsg3 = new byte[17];
		byte[] lengthPrefix3 = intToByteArray(13);
		byte msg3ID = 6;
		requestMsg3[0] = lengthPrefix3[0];
		requestMsg3[1] = lengthPrefix3[1];
		requestMsg3[2] = lengthPrefix3[2];
		requestMsg3[3] = lengthPrefix3[3];
		requestMsg3[4] = msg3ID;
		
		//index = 2
		byte[] index2 = intToByteArray(2);
		requestMsg3[5] = index2[0];
		requestMsg3[6] = index2[1];
		requestMsg3[7] = index2[2];
		requestMsg3[8] = index2[3];
		
		byte[] begin2 = intToByteArray(ti.piece_length*2);
		requestMsg3[9] = begin2[0];
		requestMsg3[10] = begin2[1];
		requestMsg3[11] = begin2[2];
		requestMsg3[12] = begin2[3];
		
		byte[] length3 = intToByteArray(ti.piece_length);
		requestMsg3[13] = length3[0];
		requestMsg3[14] = length3[1];
		requestMsg3[15] = length3[2];
		requestMsg3[16] = length3[3];
		
		dout.write(requestMsg3);
		dout.flush();
		clientSocket.setSoTimeout(130000);
		
		byte[] piece3 = new byte[ti.piece_length];
		din.readFully(piece3);
		System.out.println("received piece 3?");
		
		/*for(int k =0;k<piece3.length;k++){
			System.out.println("piece? " + piece3[k]);
		}*
		//end piece 3
		
		//begin piece 4
		//4 bytes <length prefix> 1 byte ID
		//4 byte integer <index> 8
		//4 byte integer <begin> 0 
		//4 byte integer <length> public final int piece_length;
		/*
		byte[] requestMsg4 = new byte[17];
		byte[] lengthPrefix4 = intToByteArray(13);
		byte msg4ID = 6;
		requestMsg4[0] = lengthPrefix4[0];
		requestMsg4[1] = lengthPrefix4[1];
		requestMsg4[2] = lengthPrefix4[2];
		requestMsg4[3] = lengthPrefix4[3];
		requestMsg4[4] = msg4ID;
		
		//index = 3
		byte[] index3 = intToByteArray(3);
		requestMsg4[5] = index3[0];
		requestMsg4[6] = index3[1];
		requestMsg4[7] = index3[2];
		requestMsg4[8] = index3[3];
		
		byte[] begin3 = intToByteArray(ti.piece_length*3);
		requestMsg4[9] = begin3[0];
		requestMsg4[10] = begin3[1];
		requestMsg4[11] = begin3[2];
		requestMsg4[12] = begin3[3];
		
		byte[] length4 = intToByteArray(ti.piece_length);
		requestMsg4[13] = length4[0];
		requestMsg4[14] = length4[1];
		requestMsg4[15] = length4[2];
		requestMsg4[16] = length4[3];
		
		dout.write(requestMsg4);
		dout.flush();
		clientSocket.setSoTimeout(130000);
		
		byte[] piece4 = new byte[ti.piece_length];
		din.readFully(piece4);
		System.out.println("received piece 4?");
		
		/*for(int k =0;k<piece4.length;k++){
			System.out.println("piece? " + piece4[k]);
		}*
		//end piece 4
		
		//begin piece 5
		//4 bytes <length prefix> 1 byte ID
		//4 byte integer <index> 8
		//4 byte integer <begin> 0 
		//4 byte integer <length> public final int piece_length;
		
		//calculate size
		/*
		int piece5Length = (ti.file_length - (ti.piece_length * 4));
		System.out.println("piece 5 " + piece5Length);
	
		byte[] requestMsg5 = new byte[17];
		byte[] lengthPrefix5 = intToByteArray(13);
		byte msg5ID = 6;
		requestMsg5[0] = lengthPrefix5[0];
		requestMsg5[1] = lengthPrefix5[1];
		requestMsg5[2] = lengthPrefix5[2];
		requestMsg5[3] = lengthPrefix5[3];
		requestMsg5[4] = msg5ID;
		
		//index = 4
		byte[] index4 = intToByteArray(4);
		requestMsg5[5] = index4[0];
		requestMsg5[6] = index4[1];
		requestMsg5[7] = index4[2];
		requestMsg5[8] = index4[3];
		
		byte[] begin4 = intToByteArray(ti.piece_length*4);
		requestMsg5[9] = begin4[0];
		requestMsg5[10] = begin4[1];
		requestMsg5[11] = begin4[2];
		requestMsg5[12] = begin4[3];
		
		byte[] length5 = intToByteArray(piece5Length);
		requestMsg5[13] = length5[0];
		requestMsg5[14] = length5[1];
		requestMsg5[15] = length5[2];
		requestMsg5[16] = length5[3];
		
		dout.write(requestMsg5);
		dout.flush();
		clientSocket.setSoTimeout(130000);
		
		byte[] piece5 = new byte[piece5Length];
		//din.readFully(piece5);
		System.out.println("received piece 5?");
		
		for(int k =0;k<piece5.length;k++){
			try{
				piece5[k] = din.readByte();
				//System.out.println("piece? " + piece5[k]);
			}catch(Exception e){
				System.out.println("stream is empty, piece extracted");
			}
		}
		//end piece 5
		
		//save each piece to the file
		
		/*FileOutputStream fileoutput = new FileOutputStream(new File("picture.jpg"));
		fileoutput.write(piece1);
		fileoutput.write(piece2);
		fileoutput.write(piece3);
		fileoutput.write(piece4);
		fileoutput.write(piece5);*/
			
		return true;
		
	} catch(Exception e){
		System.out.println("Exception thrown during handshake...");
		e.printStackTrace();
		return false;
	}
}

/**
* Convinience wrapper for the first step of this assignment, accepting the
* torrent info filename and returning the torrent info object.
*
* @param file
* the path to the file
* @return
* @throws IOException
* if the file doesn't exist
* @throws BencodingException
* if the file isn't b-encoded properly
*/
private static TorrentInfo getTorrentInfoFrom(String file)
throws IOException, BencodingException {
	byte[] torrentFileBytes;
	TorrentInfo ti;

	try {
		torrentFileBytes = readFile(file);
	} catch (IOException ioe) {
		ioe.printStackTrace();
		throw ioe;
	}

	try {
		ti = new TorrentInfo(torrentFileBytes);
	} catch (BencodingException be) {
		be.printStackTrace();
		throw be;
	}

	return ti;
}

/**
* Send an HTTP GET request to the tracker at the IP address and port
* specified by the TorrentFile object. The java.net.URL class is very
* useful for this.
*
* @author Paul Jones
*
* @param ti
* any torrent info object
* @throws UnknownHostException
* @throws IOException
*/
private static byte[] getTrackerResponse(TorrentInfo ti)
throws UnknownHostException, IOException {
	
	String info_hash = toHexString(ti.info_hash.array()); // info_hash
	String peer_id = toHexString("paukevinsrichschmidt".getBytes()); // peer_id
	String port = "" + 6883; // port
	String downloaded = "" + 0;
	String uploaded = "" + 0;
	String left = "" + ti.file_length;
	String announceURL = ti.announce_url.toString();

	String newURL = announceURL.toString();

	newURL += "?" + KEY_INFO_HASH + "=" + info_hash + "&" + KEY_PEER_ID
	+ "=" + peer_id + "&" + KEY_PORT + "=" + port + "&"
	+ KEY_UPLOADED + "=" + uploaded + "&" + KEY_DOWNLOADED + "="
	+ downloaded + "&" + KEY_LEFT + "=" + left;

	HttpURLConnection huc = (HttpURLConnection) new URL(newURL)
	.openConnection();
	DataInputStream dis = new DataInputStream(huc.getInputStream());

	int dataSize = huc.getContentLength();
	byte[] retArray = new byte[dataSize];

	dis.readFully(retArray);
	dis.close();

	return retArray;

}

public static String toHexString(byte[] bytes) {
	StringBuilder sb = new StringBuilder(bytes.length * 3);
	char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
	'B', 'C', 'D', 'E', 'F' };

	for (int i = 0; i < bytes.length; i++) {
		byte b = bytes[i];
		byte hi = (byte) ((b >> 4) & 0x0f);
		byte lo = (byte) (b & 0x0f);
		sb.append('%').append(hex[hi]).append(hex[lo]);
	}

	return sb.toString();
}

/**
* Capture the response from the tracker and decode it in order to get the
* list of peers. From this list of peers, use only the peer at IP address
* 128.6.171.3. You must extract this IP from the list, hard-coding it is
* not acceptable.
*/

public static TrackerResponse decodeTrackerResponse(byte[] trackerResponse)
throws BencodingException {

	Object o = Bencoder2.decode(trackerResponse);

	HashMap<ByteBuffer, Object> response = (HashMap<ByteBuffer, Object>) o;

	TrackerResponse tr = null;

	try {
		tr = new TrackerResponse(response);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	return tr;
}

/**
 * Function used to request pieces and commence download from the peer
 * Not Finished
 * *

public static void downloadFile(String ip, int port) {}

/**
 * Contacts Tracker, closes all connections/streams
 * Not Finished
 * *

public static void completeConnection() throws Exception{
	clientSocket.close();
	din.close();
	dout.close();
}

/**
 * Used to save the data to a file on local machine
 * Not Finished
 * *

public static void saveFile() {}

/**
* Convinience method for getting byte array adapted from
*
* @see http://stackoverflow.com/a/7591216/1489522
*
* @param file
* is a filename
* @return a byte array
* @throws IOException
* if the file doesn't exist or is too big
*

private static byte[] readFile(String file) throws IOException {
	RandomAccessFile f = new RandomAccessFile(new File(file), "r");
	int length = (int) f.length();
	byte[] data = new byte[length];
	f.readFully(data);
	f.close();
	return data;
}

/**
* This returns a string from a byte array, where every byte is cast to a
* char. This is used for debugging and curiosity purposes.
*
* @author Paul Jones
*
* @param b
* @return
*
public static String byteBufferToString(ByteBuffer b) {
	String s = new String();

	for (int i = 0; i < b.array().length; i++) {
		s += b.array()[i];
	}

	return s;
}

private static void print(String s) {
	System.out.println(s);
}**/



}
