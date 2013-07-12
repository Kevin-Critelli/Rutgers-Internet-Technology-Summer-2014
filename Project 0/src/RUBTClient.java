import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class RUBTClient {

	boolean isDevelopment = true;
	
	/*
	 * Open the .torrent file and parse the data inside. You may use the
	 * Bencoder2.java class to decode the data.
	 */

	public static void main(String[] args) {
		/*
		 * Simulated arguments
		 */

		String[] simargs = new String[2];

		simargs[0] = "cs352.png.torrent";
		simargs[1] = "cs352.png";
		
		byte[] torrentFileBytes;
		
		try {
			torrentFileBytes = readFile(simargs[0]);
		} catch (IOException e) {
			System.out.println("The does not exist or is too big.");
			e.printStackTrace();
			return;
		}
		
		Object o;
		
		try {
			o = Bencoder2.getInfoBytes(torrentFileBytes);
		} catch (BencodingException e) {
			System.out.println("Something is wrong with the bencoder.");
			e.printStackTrace();
			return;
		}
		
		ByteBuffer b = (ByteBuffer) o;
		
		System.out.println("Executed.");
		
		for (int i = 0 ; i < b.capacity(); i++) {
			System.out.print((char)b.get(i));
		}
	}

	/*
	 * Send an HTTP GET request to the tracker at the IP address and port
	 * specified by the TorrentFile object. The java.net.URL class is very
	 * useful for this.
	 */

	/*
	 * Capture the response from the tracker and decode it in order to get the
	 * list of peers. From this list of peers, use only the peer at IP address
	 * 128.6.171.3. You must extract this IP from the list, hard-coding it is
	 * not acceptable.
	 */

	/*
	 * Open a TCP socket on the local machine and contact the peer using the BT
	 * peer protocol and request a piece of the file.
	 */

	/*
	 * Download the piece of the file and verify its SHA-1 hash against the hash
	 * stored in the metadata file. The first time you begin the download, you
	 * need to contact the tracker and let it know you are starting to download.
	 */

	/*
	 * After a piece is downloaded and verified, the peer is notified that you
	 * have completed the piece.
	 */

	/*
	 * Repeat steps 5-7 (using the same TCP connection) for the rest of the
	 * file.
	 */

	/*
	 * When the file is finished, you must contact the tracker and send it the
	 * completed event and properly close all TCP connections
	 */

	/*
	 * Save the file to the hard disk according to the second command-line
	 * argument.
	 */

	/**
	 * Convinience method for getting byte array adapted from
	 * 
	 * @see http://stackoverflow.com/a/7591216/1489522
	 * 
	 * @param file is a filename
	 * @return a byte array
	 * @throws IOException if the file doesn't exist or is too big 
	 */
	public static byte[] readFile(String file) throws IOException {
		return readFile(new File(file));
	}

	public static byte[] readFile(File file) throws IOException {
		RandomAccessFile f = new RandomAccessFile(file, "r");
		int length = (int) f.length();
		byte[] data = new byte[length];
		f.readFully(data);
		f.close();
		return data;
	}

}