/**
 * Group Members (CS 352 Internet Technology 2013 Summer Session Project 0)
 * 
 * Kevin Critelli
 * Ritchie Vonderschmidt
 * Paul Jones
 * */

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
	public int interval;
	public int minimumInterval;
	public String trackerID;
	public int complete;
	public int incomplete;
	public ArrayList<Peer> peers;

	public static final ByteBuffer KEY_FAILURE = ByteBuffer.wrap(new byte[] {
			'f', 'a', 'i', 'l', 'u', 'r', 'e', ' ', 'r', 'e', 'a', 's', 'o',
			'n' });
	public static final ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[] {
			'p', 'e', 'e', 'r', 's' });
	public static final ByteBuffer KEY_INTERVAL = ByteBuffer.wrap(new byte[] {
			'i', 'n', 't', 'e', 'r', 'v', 'a', 'l' });
	public static final ByteBuffer KEY_MIN_INTERVAL = ByteBuffer
			.wrap(new byte[] { 'm', 'i', 'n', ' ', 'i', 'n', 't', 'e', 'r',
					'v', 'a', 'l' });
	public static final ByteBuffer KEY_COMPLETE = ByteBuffer.wrap(new byte[] {
			'c', 'o', 'm', 'p', 'l', 'e', 't', 'e' });
	public static final ByteBuffer KEY_INCOMPLETE = ByteBuffer.wrap(new byte[] {
			'i', 'n', 'c', 'o', 'm', 'p', 'l', 'e', 't', 'e' });

	public TrackerResponse(HashMap<ByteBuffer, Object> response)
			throws Exception {
		if (response.containsKey(KEY_FAILURE)) {
			throw new Exception("Tracker failed");
		}

		if (response.containsKey(KEY_INTERVAL))
			this.interval = (Integer) response.get(KEY_INTERVAL);
		else {
			System.out.println("Warning: no interval, setting to zero");
			this.interval = 0;
		}

		if (response.containsKey(KEY_COMPLETE))
			this.complete = (Integer) response.get(KEY_COMPLETE);
		else {
			System.out.println("Warning: no complete, setting to zero");
			this.complete = 0;
		}

		if (response.containsKey(KEY_INCOMPLETE))
			this.incomplete = (Integer) response.get(KEY_INCOMPLETE);
		else {
			System.out.println("Warning: no incomplete, setting to zero");
			this.incomplete = 0;
		}

		if (response.containsKey(KEY_MIN_INTERVAL))
			this.minimumInterval = (Integer) response.get(KEY_MIN_INTERVAL);
		else {
			System.out.println("Warning: no minimum interval, setting to zero");
			this.minimumInterval = 0;
		}

		// System.out.println(Bencoder2.getInfoBytes(o.array()));

		ByteBuffer peersResponse = (ByteBuffer) response.get(KEY_PEERS);
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

}
