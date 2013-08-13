/**
 * This class represents a connection that handles uploading from a peer
 * Written by Richie / Kevin
 * 
 * */

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;

public class UPeer extends Peer {

	/**
	 * Constructor for objects of class UPeer
	 * 
	 * @author Kevin Critelli, Richie Vonder Schmidt
	 */

	public UPeer(Socket connectionSocket) {
		super(-1, "-1"); // must invoke superclass, these fields are not used
							// those for upeer, so just pass -1 for both
		try {
			initConnection(connectionSocket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * receives a handshake, verifies information and sends appropriate message
	 * for upload to begin
	 * 
	 * @author Kevin Critelli, Richie Vonder Schmidt
	 * @throws Exception
	 *             An Exception is thrown if method encounters an error
	 * @return boolean A boolean value is returned, true if this function
	 *         succeeds, false otherwise
	 * */

	public boolean ReceiveHandshake(byte[] info_hash) throws Exception {
		byte[] receivingShake = new byte[68];
		din.readFully(receivingShake);

		Message returnhandshake = new Message(info_hash,
				RUBTClientConstants.peerid);

		if (receivingShake[0] != (byte) 19) {
			// NOT BIT TORRENT PROTOCOL
			return false;
		} else {
			// place info_hash in correct spot
			// send back handshake
			dout.write(returnhandshake.message);
			dout.flush();

			// read message see if interested
			if (Message.readMessage(din) == 2) {
				// Interested
				// System.out.println("there interested");
			} else {
				// Not Interested
			}

			// if interested send unchoke message
			Message unchoke = new Message(1, (byte) 1);
			dout.write(unchoke.message);
			dout.flush();

			return true;
		}
	}

	/**
	 * This function handles the piece/request portion of the upload It will
	 * wait for a request message, and respond to them sending correct pieces if
	 * we have them
	 * 
	 * @author Kevin Critelli
	 * @throws Exception
	 *             An Exception object is thrown if an error occurs
	 * */

	public void upload() throws Exception {
		Message pieceMsg;
		byte[] block;
		int index, begin, length;

		// wait for requests
		while (true) {

			byte k = Message.readMessage(din);
			if (k == RUBTClientConstants.MESSAGE_TYPE_REQUEST
					|| k == RUBTClientConstants.MESSAGE_TYPE_HAVE) {

				if (k == RUBTClientConstants.MESSAGE_TYPE_HAVE) {

					try {
						k = Message.readMessage(din); // read the next request
					} catch (Exception e) {
						// catch EOF ie no more messages
						return;
					}
				} else {
					// RECEIVED SOME OTHER MESSAGE BESIDES A REQUEST
				}

				index = din.readInt();
				begin = din.readInt();
				length = din.readInt();

				if (this.have[index] == true) {
					block = new byte[length];
					System.arraycopy(this.pieces[index].array(), begin, block,
							0, length);
					pieceMsg = new Message(9 + length, (byte) 7);
					pieceMsg.setPayload(block, begin, index, -1, -1, -1, -1);
					dout.write(pieceMsg.message);
					dout.flush();
					this.uploaded += length;
				} else {
					System.out.println("no I don't have this piece " + index);
				}
			} else {
				// RECEIVED SOME OTHER MESSAGE BESIDES REQUEST OR HAVE
			}
		}
	}

	/**
	 * Run method for this thread object, process handshake, upload to peer,
	 * close all connections/streams
	 * 
	 * @author Kevin Critelli, Richi Vonder Schmidt
	 * */

	public void run() {
		System.out.println("new thread - uploading to some peer");
		try {
			if (ReceiveHandshake(this.torrentInfo.info_hash.array())) {
				// call upload function
				upload();
			}
			// at this point, upload is complete, close all streams, exit thread
			closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
