import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.io.File;

public class RUBTClientUtils extends RUBTClient {

	/**
	 * Checks to see if we have all pieces before closing the program
	 * 
	 * @author Kevin Critelli
	 * @return boolean A boolean value representing whether or not we have all the pieces
	 * */

	public static final boolean check() {
		int i;
		for (i = 0; i < have.length; i++) {
			if (have[i] != true) {
				return false;
			}
		}
		return true;
	}
	
	/**
	  * Initializes fields in RUBTClient Main Class
	  *
	  * @author Kevin Critelli
	  *
	  */
	
	public static final void initializeFields(){
		int i;
		
		pieces = new ByteBuffer[torrentInfo.piece_hashes.length];
		requests = new boolean[torrentInfo.piece_hashes.length];
		have = new boolean[torrentInfo.piece_hashes.length];
		left = torrentInfo.file_length;
		filePtr = new File("sav.dat");

		for (i = 0; i < have.length; i++) {
			have[i] = false;
		}

		for (i = 0; i < requests.length; i++) {
			requests[i] = false;
		}
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
	 * This returns a string from a byte array, where every byte is cast to a
	 * char. This is used for debugging and curiosity purposes.
	 * 
	 * @author Paul Jones
	 * 
	 * @param b
	 * @return
	 */

	public static String byteBufferToString(ByteBuffer b) {
		String s = new String();

		for (int i = 0; i < b.array().length; i++) {
			s += b.array()[i];
		}

		return s;
	}
	
	/**
	 * Converts a 4 byte Big-Endian Hex Value to an int
	 * 
	 * @author Kevin Critelli
	 * 
	 * @param bytes
	 *            The byte array representing the number
	 * @return int returns an int representation of the byte array
	 * */

	public static final int byteArrayToInt(byte[] bytes) {
		return java.nio.ByteBuffer.wrap(bytes).getInt();
	}
	
	/**
	 * Converts an integer value to a 4 byte Big-Endian Hex Value
	 * 
	 * @author Kevin Critelli
	 * 
	 * @param value
	 *            The Integer to change into 4 byte Big-Endian Hex
	 * @return byte[] A Byte array of size 4 containing the four Hex values
	 */

	public static byte[] intToByteArray(int value) {
		byte[] retVal = ByteBuffer.allocate(4).putInt(value).array();
		
		return retVal;
	}
	
	/**
	 * This function checks to see if we are starting a fresh session, or resuming from a previous one
	 * As we download pieces of the file, we write them out to a file called 'sav.dat' that is kept in the local directory
	 * where ever the program is being stored. Upon calling this function, it will first check if sav.dat exists, if it does not exist
	 * we know that we are starting a fresh session (or in the previous session we completed downloading the whole file)
	 * If it does exist, we know we are resuming from a previous session, so it will read the bytes in the file and store 
	 * them into our pieces array, also updating have/requests array. The bytes are written to the file in this format:
	 * <index><length><block> where index and length are 4-byte big-endian values, and block is the piece of data. Index
	 * is the index of the piece, length is the size of the block.
	 * 
	 * @author Kevin Critelli
	 * 
	 * */
	
	public static final void checkState(File file){
		//Before we start downloading see if were resuming from another session, ie see if SAV.DAT exists
		if(file.exists()){
			try{
				//resume from another session, read it in
				System.out.println("Resuming from a previous session");
				FileInputStream fin = new FileInputStream(new File("sav.dat"));				//open file that contains our data from previous session
				
				int ind = 0;
				int len = 0;
				byte[] buffer = new byte[4];
				byte[] data = null;
				
				while(true){
					int retVal = 0;										//retval used to determine if we reached the end of reading from the file
					retVal = fin.read(buffer); 				
					if(retVal == -1){break;}							
					ind = RUBTClientUtils.byteArrayToInt(buffer); 		//grab 4-byte big-endian index value from file
					fin.read(buffer); 								
					len = RUBTClientUtils.byteArrayToInt(buffer); 		//grab 4-byte big-endian length value from file
					
					//grab the block, and update all our main information back in RUBT
					data = new byte[len];					
					fin.read(data);
					requests[ind] = true;								
					have[ind] = true;
					pieces[ind] = ByteBuffer.wrap(data);
					downloaded += data.length;
					left = left - data.length;
					numPieces++;
				}
				System.out.println("Total Bytes downloaded from previous session: " + downloaded);
				System.out.println("Total Bytes left to download from previous session: " + left);
			}catch(Exception e){
				//error
			}
		}else{
			//new session, continue as regular
			System.out.println("Starting new session");
		}
	}
	
	/**
	 * This function is called when we have completed downloaded the file
	 * It simply saves the bytes we've downloaded out to a file, the name of the file
	 * is determined from the torrent info, it also deletes the 'sav.dat' because it is 
	 * only used for resuming state, and at this point we have already downloaded the whole file
	 * so there is no reason to still have the file.
	 * 
	 * @author Kevin Critelli
	 * */
	
	public static final void SaveFile(){
		int i;
		
		try {					
			FileOutputStream fileoutput = new FileOutputStream(new File(torrentInfo.file_name));
									
			for (i = 0; i < pieces.length; i++) {
				byte[] array = pieces[i].array();
				fileoutput.write(pieces[i].array());
			}
							
			fileoutput.close();
								
			//send stopped event to tracker
			trackerResponse.sendTrackerFinishedStopped(announce_url,torrentInfo.info_hash.array(), downloaded, uploaded, 0);
			//t.stopExecution();
			
			//DELETE SAV.DAT - Don't need it they finished the entire file
			filePtr.delete();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This function writes a block of data into our main piece storage in RUBT.
	 * Synchronized function so that no more than one Peer object/thread can access
	 * it at at time. It is declared 'static synchronized' so that it synchronizes
	 * on RUBTClientUtils.class, and not a specific instance. It also writes the 
	 * block out to a file so that we can save our progress. It writes the pieces 
	 * out to the file in the format:
	 * 
	 * <index><length><block> 
	 * index - 4-byte big-endian integer value - index of the piece
	 * length - 4-byte big-endian integer value - length in bytes of the piece
	 * block - the block of data
	 * 
	 * @author Kevin Critelli
	 * */
	
	public static synchronized void update(int index, byte[]fullPiece) throws Exception{
		downloaded += fullPiece.length;
		left = left - fullPiece.length;
		ByteBuffer buffer = ByteBuffer.wrap(fullPiece);
		pieces[index] = buffer;
		have[index] = true;
		numPieces++;
			
		FileOutputStream fos = new FileOutputStream(filePtr,true);
		fos.write(RUBTClientUtils.intToByteArray(index));
		fos.write(RUBTClientUtils.intToByteArray(fullPiece.length));
		fos.write(fullPiece);
		fos.flush();
		fos.close();	
	}
	
	/**
	 * This function determines what piece we need to request from the peer
	 * It is synchronized so that no more than one Peer thread will access
	 * it at a time. It is declared 'static synchronized' so that it synchronizes
	 * on RUBTClientUtils.class, and not a specific instance
	 * 
	 * @author Kevin Critelli
	 * @return int Returns an int value representing the index of the piece that this thread should request from peer
	 * 
	 * */
	
	public static synchronized int get_piece_to_request(){
		for(int i=0;i<requests.length;i++){
			if(requests[i] == false){
				requests[i] = true;
				return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * This method calls the necessary functions to parse the information
	 * from the torrent file, and contact the tracker getting the list of peers,
	 * and also announcing ourselves. This method was done simply to take code
	 * out of main.
	 * 
	 * @author Kevin Critelli
	 * */
	
	public static void Parse_Torrent_Contact_Tracker(String torrentFile){
		//parse torrent file
		torrentInfo = TorrentInfo.getTorrentInfoFrom(torrentFile);
		
		if (RUBTClientConstants.DEVELOP)
			System.out.println(torrentInfo);

		//send HTTP Request to tracker
		trackerResponse = new TrackerResponse(torrentInfo);

		if (RUBTClientConstants.DEVELOP) 
			System.out.println(trackerResponse);
			
		System.out.println("File Size: " + torrentInfo.file_length);
		
		announce_url = trackerResponse.announceURL;
	}
}
