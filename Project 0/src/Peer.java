/**
 * Group Members (CS 352 Internet Technology 2013 Summer Session Project 0)
 * 
 * Kevin Critelli
 * Ritchie Vonderschmidt
 * Paul Jones
 * */

import java.nio.ByteBuffer;

/**
 * Class representing a peer for the download
 **/ 

public class Peer {
	
	/**
	 * Field representing the port for the peer
	 * */
	
	public int port;
	
	/**
	 * Field representing ip for the peer
	 **/ 
	
	public String ip;
	
	/**
	 * Keys for ip and port
	 **/ 
	
	public static final ByteBuffer KEY_IP = ByteBuffer.wrap(new byte[] { 'i','p' });
	public static final ByteBuffer KEY_PORT = ByteBuffer.wrap(new byte[] { 'p','o', 'r', 't' });

	public Peer(int port, String ip) {
		this.port = port;
		this.ip = ip;
	}

	public String toString() {
		return "" + ip + ":" + port;
	}
}

