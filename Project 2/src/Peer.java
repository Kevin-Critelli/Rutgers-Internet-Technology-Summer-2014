/**
* Abstract Class representing a connection to a peer, upload or download
**/

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;

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
		this.ip = ip.replaceAll(":", ".");
	}

	public String toString() {
		return "" + ip + ":" + port;
	}
	
	/**
	 * Closes all streams for this Peer object
	 * 
	 * @author Kevin Critelli
	 * @throws Exception An exception object is thrown if an error occurs
	 * */
	
	public void closeConnection() throws Exception{
		din.close();
		input.close();
		dout.close();
		output.close();
	}
	
	/**
	 * Initializes all streams for a connection to the peer
	 * 
	 * @author Kevin Critelli
	 * @throws Exception An Exception object is thrown if an error occurs
	 * */
	
	public void initConnection(Socket socket) throws Exception{
		input = socket.getInputStream();
		output = socket.getOutputStream();
		din = new DataInputStream(input);
		dout = new DataOutputStream(output);
	}
}
