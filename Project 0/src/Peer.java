/**
 * Group Members (CS 352 Internet Technology 2013 Summer Session Project 0)
 * 
 * Kevin Critelli
 * Ritchie Vonderschmidt
 * Paul Jones
 * */

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * Class representing a peer for the download Thread will support
 * upload/download, run method will process uploads?
 **/

// *************************************************************
// Richie wuz here.
// We may want to use synchronized instead of runnable?
// More to come soon.
// **************************************************************

public class Peer implements Runnable {

	public int port;
	public String ip;
	public DataOutputStream dout = null;
	public DataInputStream din = null;
	public Socket socket = null;
	public OutputStream output = null;
	public InputStream input = null;
	public TorrentInfo ti = null;
	ArrayList<byte[]> pieces = new ArrayList<byte[]>();
	FileOutputStream fileoutput = null;

	// placeholder constructor for trackerresponse class

	public Peer(int port, String ip) {
		this.port = port;
		this.ip = ip;
	}

	/**
	 * Constructor Peer object
	 * 
	 * @author Kevin Critelli
	 * 
	 * @param port
	 *            The port for this peer connection
	 * @param ip
	 *            The string representation of the ip of this peer
	 * @param ti
	 *            The torrent info object
	 * @param peerid
	 *            The peer id for this peer object
	 * 
	 * */

	public Peer(int port, String ip, TorrentInfo ti, byte[] peerid) {
		this.port = port;
		this.ip = ip;
		this.ti = ti;

		try {
			socket = new Socket(ip, port);
			input = socket.getInputStream();
			output = socket.getOutputStream();

			din = new DataInputStream(input);
			dout = new DataOutputStream(output);
		} catch (Exception e) {
			System.out.println("Exception thrown during connection setup");
		}

		if (!(sendHandshake(ti.info_hash.array(), peerid))) {
			System.out.println("Handshake failed");
			return;
		}

		try {
			downloadFile();
		} catch (Exception e) {
			System.out.println("exception thrown during download");
		}

		try {
			completeConnection();
		} catch (Exception e) {
			System.out.println("Exception thrown during download");
		}
	}

	/**
	 * Sends a handshake message to a peer
	 * 
	 * @author Kevin Critelli
	 * 
	 * @param info_hash
	 *            The byte array representation of the info_hash for this
	 *            torrent
	 * @param peerid
	 *            The byte array represention of peerid
	 * @return true if this handshake was accepted, false if not
	 * 
	 * */

	public boolean sendHandshake(byte[] info_hash, byte[] peerid) {
		Message handshake = new Message(info_hash, peerid);

		try {
			dout.write(handshake.message);
			dout.flush();
			socket.setSoTimeout(130000);

			byte[] receivingShake = new byte[68];
			din.readFully(receivingShake);

			byte[] peerInfoHash = Arrays.copyOfRange(receivingShake, 28, 48);
			if (!Arrays.equals(peerInfoHash, info_hash)) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			System.out.println("Exception thrown during handshake");
			return false;
		}
	}

	/**
	 * Downloads the file from the peer
	 * 
	 * @author Kevin Critelli
	 * @throws Exception
	 *             An Exception is thrown an error occurs during the process
	 * 
	 * */

	public boolean downloadFile() throws Exception {
		Message interestedMessage = new Message(1, (byte) 2);
		Message request = null;
		byte[] pieceSubset = null;
		byte[] buf = null;
		int lastPieceSize;
		int count = 16384;
		int numPieces = 0;
		int begin = 0;
		int dif;
		int i;

		for (i = 0; i < 6; i++) {
			din.readByte();
		} // bypass bytes

		dout.write(interestedMessage.message);
		dout.flush();
		socket.setSoTimeout(130000);

		// grab response from interest
		for (i = 0; i < 5; i++) {
			if (i == 4) {
				if (din.readByte() == 1) {
					// unchoked
					break;
				}
			}
			din.readByte();
		}

		dif = ti.piece_hashes.length - 1;
		lastPieceSize = ti.file_length - (dif * ti.piece_length);
		fileoutput = new FileOutputStream(new File("picture.jpg"));

		// READY TO EXCHANGE DATA
		// loop until we have each piece
		while (numPieces != ti.piece_hashes.length) {
			// loop until we have each subset of data to make up the piece
			while (true) {
				if (numPieces + 1 == ti.piece_hashes.length) {
					request = new Message(13, (byte) 6);

					if (lastPieceSize < 16384) {
						count = lastPieceSize;
					} else {
						count = 16384;
					}

					lastPieceSize = lastPieceSize - 16384;
					request.setPayload(null, -1, -1, count, begin, numPieces,
							-1);
					dout.write(request.message);
					dout.flush();
					socket.setSoTimeout(130000);
					buf = new byte[4];

					for (i = 0; i < 4; i++) {
						buf[i] = din.readByte();
					} // bypass bytes

					pieceSubset = new byte[count];

					for (i = 0; i < 9; i++) {
						din.readByte();
					} // bypass bytes

					for (i = 0; i < count; i++) {
						pieceSubset[i] = din.readByte();
					} // save block

					this.pieces.add(pieceSubset);
					fileoutput.write(pieceSubset);

					if (lastPieceSize < 0) {
						numPieces++;
						break;
					}
					begin += count;
				} else {
					request = new Message(13, (byte) 6);
					request.setPayload(null, -1, -1, 16384, begin, numPieces,
							-1);
					dout.write(request.message);
					dout.flush();
					socket.setSoTimeout(1300000);

					buf = new byte[4];
					for (i = 0; i < 4; i++) {
						buf[i] = din.readByte();
					}

					pieceSubset = new byte[16384];

					for (i = 0; i < 9; i++) {
						din.readByte();
					} // bypass bytes

					for (i = 0; i < 16384; i++) {
						pieceSubset[i] = din.readByte();
					} // save data

					this.pieces.add(pieceSubset);
					fileoutput.write(pieceSubset);

					if (begin + 16384 == ti.piece_length) {
						numPieces++;
						begin = 0;
						break;
					} else {
						begin += 16384;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Closes all sockets/streams
	 * 
	 * @author Kevin Critelli
	 * @throws Exception
	 *             Throws a general Exception when an error occurs
	 * */

	public void completeConnection() throws Exception {
		socket.close();
		din.close();
		dout.close();
		fileoutput.close();
	}

	public String toString() {
		return "" + ip + ":" + port;
	}

	public void run() {
		System.out.println("run");
	}
}
