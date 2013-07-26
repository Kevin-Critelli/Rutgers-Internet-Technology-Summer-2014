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

	/*
	 * public static DataOutputStream dout = null; public static DataInputStream
	 * din = null; public static Socket clientSocket = null; public static
	 * OutputStream output = null; public static InputStream input = null;
	 */

	public static TorrentInfo ti = null;

	/**
	 * Fields for handshake Message
	 * */

	public static byte[] protocol_string = new byte[] { 'B', 'i', 't', 'T',
			'o', 'r', 'r', 'e', 'n', 't', ' ', 'p', 'r', 'o', 't', 'o', 'c',
			'o', 'l' };
	public static byte[] peerid = "paukevinsrichschmidt".getBytes(); // generate
																		// random
																		// peerid?
	public static byte[] info_hash = null;

	public static void main(String[] args) {

		byte[] trackerResponse = null;
		ArrayList<Peer> peers = null;
		TrackerResponse tr = null;
		boolean retVal = false;
		boolean found = false;
		int i;

		if (args.length != 2) {
			System.out.println("Incorrect number of arguments");
			System.out
					.println("Correct Usage: java -cp . RUBTClient <Torrent File> <File to Save to>");
			return;
		}

		try {
			ti = getTorrentInfoFrom(args[0]);
		} catch (Exception e) {
			System.out
					.println("There's something wrong with your torrent info file.");
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
			System.out
					.println("There was a problem decoding the tracker response");
			e.printStackTrace();
		}

		/**
		 * Obtain list of peers from Tracker Response Object and find ip
		 * 128.6.171.3 (for this portion of project)
		 * */

		peers = tr.peers;

		for (i = 0; i < peers.size(); i++) {
			if (peers.get(i).toString().contains("128:6:171:3")) {
				// System.out.println("Found proper peer at index " + i +
				// " with port " + peers.get(i).port);
				found = true;
				break;
			}
		}

		String ipString = peers.get(i).ip.replaceAll(":", ".");

		if (found == false) {
			System.out
					.println("Could not find peer @ ip 128.6.171.3 from peer list");
			return;
		}

		info_hash = ti.info_hash.array();

		// create peer with connection variables and handshake variables

		Peer peer = new Peer(peers.get(i).port, ipString, ti, peerid);

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

	// do we need this ?? if not take out please

	private static void print(String s) {
		System.out.println(s);
	}

}
