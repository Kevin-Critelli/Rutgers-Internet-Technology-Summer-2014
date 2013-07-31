/**
 * Group Members (CS 352 Internet Technology 2013 Summer Session Project 0)
 * 
 * Kevin Critelli
 * Ritchie Vonderschmidt
 * Paul Jones
 * */

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class used to contain information from a TrackerResponse
 * 
 * @author Paul Jones
 * */

public class TrackerResponse {

	public String failureReason;
	public String failureMessage;
	public String announceURL;
	public int interval;
	public int minimumInterval;
	public String trackerID;
	public int complete;
	public int incomplete;
	private ArrayList<Peer> peers;

	public TrackerResponse() {
		
	}
	
	public String toString() {
		String ret = "TrackerResponse: \n";

		ret += "\tTracker ID:\t" + trackerID + "\n";
		ret += "\tInterval:\t" + interval + "\n";
		ret += "\tComplete:\t" + complete + "\n";
		ret += "\tIncomplete:\t" + incomplete + "\n";
		ret += "\tPeers:\n";

		for (int i = 0; i < peers.size(); i++) {
			ret += "\t\t" + peers.get(i);
			if (peers.get(i).ip.contains(RUBTClientConstants.ACCEPTABLE_PEER_1) ||
					peers.get(i).ip.contains(RUBTClientConstants.ACCEPTABLE_PEER_2)) {
				ret += "\tacceptable peer";
			}
			
			ret += "\n";
		}

		return ret;
	}

	public TrackerResponse(TorrentInfo torrentInfo) {

		byte[] encodedResponse = null;
		try {
			encodedResponse = getTrackerResponseWithEventStarted(torrentInfo);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		Object o = null;
		try {
			o = Bencoder2.decode(encodedResponse);
		} catch (BencodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HashMap<ByteBuffer, Object> response = (HashMap<ByteBuffer, Object>) o;

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_FAILURE)) {
			// throw new Exception("Tracker failed");
		}

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_INTERVAL))
			this.interval = (Integer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_INTERVAL);
		else {
			System.out.println("Warning: no interval, setting to zero");
			this.interval = 0;
		}

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_COMPLETE))
			this.complete = (Integer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_COMPLETE);
		else {
			System.out.println("Warning: no complete, setting to zero");
			this.complete = 0;
		}

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_INCOMPLETE))
			this.incomplete = (Integer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_INCOMPLETE);
		else {
			System.out.println("Warning: no incomplete, setting to zero");
			this.incomplete = 0;
		}

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_MIN_INTERVAL))
			this.minimumInterval = (Integer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_MIN_INTERVAL);
		else {
			System.out.println("Warning: no minimum interval, setting to zero");
			this.minimumInterval = 0;
		}
		
		// System.out.println(Bencoder2.getInfoBytes(o.array()));

		ByteBuffer peersResponse = (ByteBuffer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_PEERS);
		this.peers = new ArrayList<Peer>();

		for (int i = 0; i < 33; i++) {
			try {
				String peerIP = "";

				peerIP += peersResponse.get() & 0xff;
				peerIP += ":";
				peerIP += peersResponse.get() & 0xff;
				peerIP += ":";
				peerIP += peersResponse.get() & 0xff;
				peerIP += ":";
				peerIP += peersResponse.get() & 0xff;

				int peerPort = peersResponse.get() * 256 + peersResponse.get();

				this.peers.add(new Peer(peerPort, peerIP));
			} catch (Exception e) {
				// I made the number 33 because that's the recommened number of
				// peers.
				// This exception exists because there are obviously not always
				// going
				// to be 33 peers. Sometimes there could be more. But for right
				// now,
				// I'm hacking and slashing and just going with it.
				// It works. (TM)
				// Also I'm sorry. This sucks.
			}
		}
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
	private byte[] getTrackerResponseWithEventStarted(TorrentInfo ti)
			throws UnknownHostException, IOException {

		String info_hash = RUBTClientUtils.toHexString(ti.info_hash.array()); // info_hash
		String peer_id = RUBTClientUtils.toHexString(RUBTClientConstants.peerid); // peer_id
		
		String port = "" + 6883; // port
		String downloaded = "" + 0;
		String uploaded = "" + 0;
		String left = "" + ti.file_length;
		String announceURL = ti.announce_url.toString();

		String newURL = announceURL.toString();
		this.announceURL = newURL;

		newURL += "?" + RUBTClientConstants.TRACKER_RESPONSE_KEY_INFO_HASH + "=" + info_hash
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_PEER_ID + "=" + peer_id
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_PORT + "=" + port + "&"
				+ RUBTClientConstants.TRACKER_RESPONSE_KEY_UPLOADED + "=" + uploaded + "&"
				+ RUBTClientConstants.TRACKER_RESPONSE_KEY_DOWNLOADED + "=" + downloaded
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_LEFT + "=" + left
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_EVENT + "=" + RUBTClientConstants.TRACKER_RESPONSE_KEY_STARTED;
		
		HttpURLConnection huc = (HttpURLConnection) new URL(newURL)
				.openConnection();
		DataInputStream dis = new DataInputStream(huc.getInputStream());

		int dataSize = huc.getContentLength();
		byte[] retArray = new byte[dataSize];

		dis.readFully(retArray);
		dis.close();

		return retArray;
	}
	
	public TrackerResponse getTrackerResponse(String base_url, byte[] info_hash_input, int downloaded, int uploaded, int left) throws MalformedURLException, IOException {

		String info_hash = RUBTClientUtils.toHexString(info_hash_input); // info_hash
		String peer_id = RUBTClientUtils.toHexString(RUBTClientConstants.peerid); // peer_id
		
		String port = "" + 6883; // port

		String newURL = base_url;
		
		newURL += "?" + RUBTClientConstants.TRACKER_RESPONSE_KEY_INFO_HASH + "=" + info_hash
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_PEER_ID + "=" + peer_id
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_PORT + "=" + port + "&"
				+ RUBTClientConstants.TRACKER_RESPONSE_KEY_UPLOADED + "=" + uploaded + "&"
				+ RUBTClientConstants.TRACKER_RESPONSE_KEY_DOWNLOADED + "=" + downloaded
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_LEFT + "=" + left;

		HttpURLConnection huc = (HttpURLConnection) new URL(newURL)
				.openConnection();
		DataInputStream dis = new DataInputStream(huc.getInputStream());

		int dataSize = huc.getContentLength();
		byte[] retArray = new byte[dataSize];

		dis.readFully(retArray);
		dis.close();

		Object o = null;
		try {
			o = Bencoder2.decode(retArray);
		} catch (BencodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HashMap<ByteBuffer, Object> response = (HashMap<ByteBuffer, Object>) o;
		
		TrackerResponse returnResponse = new TrackerResponse();
		
		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_FAILURE)) {
			// throw new Exception("Tracker failed");
		}

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_INTERVAL))
			returnResponse.interval = (Integer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_INTERVAL);
		else {
			System.out.println("Warning: no interval, setting to zero");
			returnResponse.interval = 0;
		}

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_COMPLETE))
			returnResponse.complete = (Integer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_COMPLETE);
		else {
			System.out.println("Warning: no complete, setting to zero");
			returnResponse.complete = 0;
		}

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_INCOMPLETE))
			returnResponse.incomplete = (Integer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_INCOMPLETE);
		else {
			System.out.println("Warning: no incomplete, setting to zero");
			returnResponse.incomplete = 0;
		}

		if (response.containsKey(RUBTClientConstants.TRACKER_RESPONSE_KEY_MIN_INTERVAL))
			returnResponse.minimumInterval = (Integer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_MIN_INTERVAL);
		else {
			System.out.println("Warning: no minimum interval, setting to zero");
			returnResponse.minimumInterval = 0;
		}
		
		// System.out.println(Bencoder2.getInfoBytes(o.array()));

		ByteBuffer peersResponse = (ByteBuffer) response.get(RUBTClientConstants.TRACKER_RESPONSE_KEY_PEERS);
		returnResponse.peers = new ArrayList<Peer>();

		for (int i = 0; i < 33; i++) {
			try {
				String peerIP = "";

				peerIP += peersResponse.get() & 0xff;
				peerIP += ":";
				peerIP += peersResponse.get() & 0xff;
				peerIP += ":";
				peerIP += peersResponse.get() & 0xff;
				peerIP += ":";
				peerIP += peersResponse.get() & 0xff;

				int peerPort = peersResponse.get() * 256 + peersResponse.get();

				returnResponse.peers.add(new Peer(peerPort, peerIP));
			} catch (Exception e) {
				
			}
		}
		
		return returnResponse;
	}
	
	public void sendTrackerFinishedEvent(String base_url, byte[] info_hash_input, int downloaded, int uploaded, int left) throws MalformedURLException, IOException {

		String info_hash = RUBTClientUtils.toHexString(info_hash_input); // info_hash
		String peer_id = RUBTClientUtils.toHexString(RUBTClientConstants.peerid); // peer_id
		
		String port = "" + 6883; // port

		String newURL = base_url;
		
		newURL += "?" + RUBTClientConstants.TRACKER_RESPONSE_KEY_INFO_HASH + "=" + info_hash
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_PEER_ID + "=" + peer_id
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_PORT + "=" + port + "&"
				+ RUBTClientConstants.TRACKER_RESPONSE_KEY_UPLOADED + "=" + uploaded + "&"
				+ RUBTClientConstants.TRACKER_RESPONSE_KEY_DOWNLOADED + "=" + downloaded
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_LEFT + "=" + left
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_EVENT + "=" + RUBTClientConstants.TRACKER_RESPONSE_KEY_COMPLETED;

		HttpURLConnection huc = (HttpURLConnection) new URL(newURL)
				.openConnection();
		DataInputStream dis = new DataInputStream(huc.getInputStream());

		int dataSize = huc.getContentLength();
		byte[] retArray = new byte[dataSize];

		dis.readFully(retArray);
		dis.close();
	}
	
	
	public void sendTrackerFinishedStopped(String base_url, byte[] info_hash_input, int downloaded, int uploaded, int left) throws MalformedURLException, IOException {

		String info_hash = RUBTClientUtils.toHexString(info_hash_input); // info_hash
		String peer_id = RUBTClientUtils.toHexString(RUBTClientConstants.peerid); // peer_id
		
		String port = "" + 6883; // port

		String newURL = base_url;
		
		newURL += "?" + RUBTClientConstants.TRACKER_RESPONSE_KEY_INFO_HASH + "=" + info_hash
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_PEER_ID + "=" + peer_id
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_PORT + "=" + port + "&"
				+ RUBTClientConstants.TRACKER_RESPONSE_KEY_UPLOADED + "=" + uploaded + "&"
				+ RUBTClientConstants.TRACKER_RESPONSE_KEY_DOWNLOADED + "=" + downloaded
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_LEFT + "=" + left
				+ "&" + RUBTClientConstants.TRACKER_RESPONSE_KEY_EVENT + "=" + RUBTClientConstants.TRACKER_RESPONSE_KEY_STOPPED;

		HttpURLConnection huc = (HttpURLConnection) new URL(newURL)
				.openConnection();
		DataInputStream dis = new DataInputStream(huc.getInputStream());

		int dataSize = huc.getContentLength();
		byte[] retArray = new byte[dataSize];

		dis.readFully(retArray);
		dis.close();
	}
	public boolean containsPeer(String peerIP) {
		for (int i = 0; i < peers.size(); i++) {
			if (peers.get(i).ip.contains(peerIP)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Peer getPeerAtIndex(int i) {
		return peers.get(i);
	}
	
	public int peerSize () {
		return peers.size();
	}
	
	public ArrayList<Peer> getAcceptablePeers() {
		ArrayList<Peer> acceptablePeers = new ArrayList<Peer>(2);
		
		for (int i = 0; i < peerSize(); i++) {
			if (getPeerAtIndex(i).ip.contains(RUBTClientConstants.ACCEPTABLE_PEER_1) || 
					getPeerAtIndex(i).ip.contains(RUBTClientConstants.ACCEPTABLE_PEER_2)) {
				acceptablePeers.add(getPeerAtIndex(i));
			}
		}
		
		return acceptablePeers;
	}

}
