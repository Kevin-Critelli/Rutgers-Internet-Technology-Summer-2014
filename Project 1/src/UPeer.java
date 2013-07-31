import java.io.*;
import java.net.*;
import java.util.Arrays;

public class UPeer extends RUBTClient implements Runnable {
	byte[] HandshakePrefix = new byte[2];
	// This will most certainly be changed
	byte[] CopiedInfoHash = null;
	public Socket connectionSocket;
	public DataInputStream din = null;
	public InputStream input = null;
	public DataOutputStream dout = null;
	public OutputStream output = null;

	// I will only use the input streams for now to test whether we can get a
	// handshake message

	/**
	 * Constructor for objects of class UPeer
	 */

	public UPeer(Socket connectionSocket) throws Exception {
		input = connectionSocket.getInputStream();
		output = connectionSocket.getOutputStream();
		din = new DataInputStream(input);
		dout = new DataOutputStream(output);

		// again, only using half of this for now
	}

	public boolean ReceiveHandshake(byte[] infoHash) throws Exception {
		byte HandshakePrefix = 19;

		byte[] receivingShake = new byte[68];
		din.readFully(receivingShake);

		byte MessageType = receivingShake[0];

		if (HandshakePrefix != MessageType) {

			return false;
		} else {

			return true;
		}
		
		//this is BitTorrent Protocl
		//Place info_hash in correct spot
		//send back handshake
		//wait for interested message
		//send unchoke
		//wait for requests, send pieces
		
		// This is just to verify that the handshake does not come through in
		// simultaneous fashion
		// with the foreign client attempting to set up a socket.
		// If this doesn't work it might be worth testing if there is anything
		// in the streams.
	}

	public void run() {
		try {
			ReceiveHandshake(CopiedInfoHash);
		} catch (Exception e) {
			System.out.println("Something went wrong receiving the handshake");
		}
	}

}
// For now hardcode the InfoHash of the only file that we have
// Namely- the picture
