/**
* Abstract Class representing a connection to a peer, upload or download
**/

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.InputStream;

public abstract class Peer extends RUBTClient implements Runnable {

	public int port;
	public String ip;
	public DataOutputStream dout = null;
	public DataInputStream din = null;
	public OutputStream output = null;
	public InputStream input = null;
	boolean isChoked = false;

	public Peer(int port, String ip) {
		this.port = port;
		this.ip = ip;
	}

	public String toString() {
		return "" + ip + ":" + port;
	}

}
