import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class RUBTClient {

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

		TorrentInfo ti;

		/*
		 * Step 1
		 */

		try {
			ti = getTorrentInfoFrom(simargs[0]);
		} catch (Exception e) {
			System.out
					.println("There's something wrong with your torrent info file.");
			return;
		}

		/*
		 * Step 2
		 */

		byte[] trackerResponse;

		try {
			trackerResponse = getTrackerResponse(ti);
		} catch (Exception e) {
			System.out.println("There was a problem with a GET request.");
			e.printStackTrace();
			return;
		}

		/*
		 * Step 3
		 */

		TrackerResponse tr = null;
		
		try {
			tr = decodeTrackerResponse(trackerResponse);
		} catch (Exception e) {
			System.out
					.println("There was a problem decoding the tracker response");
			e.printStackTrace();
		}
		
		/*
		 * Steps 5-7 and 8
		 */
		downloadFile();

		/*
		 * Step 9
		 */

		completeConnection();

		/*
		 * Step 10
		 */

		saveFile();

	}

	/**
	 * Convinience wrapper for the first step of this assignment, accepting the
	 * torrent info filename and returning the torrent info object.
	 * 
	 * @param file
	 *            the path to the file
	 * @return
	 * @throws IOException
	 *             if the file doesn't exist
	 * @throws BencodingException
	 *             if the file isn't b-encoded properly
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
	 *            any torrent info object
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

	/*
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

	public static void downloadFile() {

	}

	/*
	 * When the file is finished, you must contact the tracker and send it the
	 * completed event and properly close all TCP connections
	 */

	public static void completeConnection() {

	}

	/*
	 * Save the file to the hard disk according to the second command-line
	 * argument.
	 */

	public static void saveFile() {

	}

	/**
	 * Convinience method for getting byte array adapted from
	 * 
	 * @see http://stackoverflow.com/a/7591216/1489522
	 * 
	 * @param file
	 *            is a filename
	 * @return a byte array
	 * @throws IOException
	 *             if the file doesn't exist or is too big
	 */
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
	 */
	public static String byteBufferToString(ByteBuffer b) {
		String s = new String();

		for (int i = 0; i < b.array().length; i++) {
			s += b.array()[i];
		}

		return s;
	}

	/**
	 * I'm lazy.
	 */
	private static void print(String s) {
		System.out.println(s);
	}

}