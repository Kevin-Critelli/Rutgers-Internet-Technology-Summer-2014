/**
 * @author Kevin Critelli
 * @author Paul Jones
 * @author Richie von der Schmidt
 */

import java.nio.ByteBuffer;

public class RUBTClientConstants {

	public final static boolean DEVELOP = true;

	public final static ByteBuffer TRACKER_INFO_KEY_INFO = ByteBuffer
			.wrap("info".getBytes());
	public final static ByteBuffer TRACKER_INFO_KEY_LENGTH = ByteBuffer
			.wrap("length".getBytes());
	public final static ByteBuffer TRACKER_INFO_KEY_PIECES = ByteBuffer
			.wrap("pieces".getBytes());
	public final static ByteBuffer TRACKER_INFO_KEY_NAME = ByteBuffer
			.wrap("name".getBytes());
	public final static ByteBuffer TRACKER_INFO_KEY_PIECE_LENGTH = ByteBuffer
			.wrap("piece length".getBytes());
	public static final ByteBuffer TRACKER_INFO_KEY_ANNOUNCE = ByteBuffer
			.wrap("announce".getBytes());

	public final static String TRACKER_RESPONSE_KEY_INFO_HASH = "info_hash";
	public final static String TRACKER_RESPONSE_KEY_PEER_ID = "peer_id";
	public final static String TRACKER_RESPONSE_KEY_PORT = "port";
	public final static String TRACKER_RESPONSE_KEY_UPLOADED = "uploaded";
	public final static String TRACKER_RESPONSE_KEY_DOWNLOADED = "downloaded";
	public final static String TRACKER_RESPONSE_KEY_LEFT = "left";
	public final static String TRACKER_RESPONSE_KEY_EVENT = "event";
	public final static String TRACKER_RESPONSE_KEY_STARTED = "started";
	public final static String TRACKER_RESPONSE_KEY_COMPLETED = "completed";
	public static final String TRACKER_RESPONSE_KEY_INCOMPLETE = "incomplete";
	public static final String TRACKER_RESPONSE_KEY_STOPPED = "stopped";

	public static final ByteBuffer TRACKER_RESPONSE_KEY_FAILURE = ByteBuffer
			.wrap("failure reason".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_PEERS = ByteBuffer
			.wrap("peers".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_INTERVAL = ByteBuffer
			.wrap("interval".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_MIN_INTERVAL = ByteBuffer
			.wrap("min interval".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_COMPLETE = ByteBuffer
			.wrap("complete".getBytes());

	public final static String ACCEPTABLE_PEER_1 = "128:6:171:3";
	public final static String ACCEPTABLE_PEER_2 = "128:6:171:4";
	public final static byte[] peerid = { 'p', 'a', 'u', 'l', 'k', 'e', 'v',
			'i', 'n', 'r', 'i', 't', 'c', 'h', 'i', 'e', 'c', 's', '$', '$' };

	public static final byte MESSAGE_TYPE_KEEP_ALIVE = -1;
	public static final byte MESSAGE_TYPE_CHOKE = 0;
	public static final byte MESSAGE_TYPE_UNCHOKE = 1;
	public static final byte MESSAGE_TYPE_INTERESTED = 2;
	public static final byte MESSAGE_TYPE_NOT_INTERESTED = 3;
	public static final byte MESSAGE_TYPE_HAVE = 4;
	public static final byte MESSAGE_TYPE_BITFIELD = 5;
	public static final byte MESSAGE_TYPE_REQUEST = 6;
	public static final byte MESSAGE_TYPE_PIECE = 7;
	public static final byte MESSAGE_TYPE_CANCEL = 8;
	public static final byte MESSAGE_TYPE_HANDSHAKE = 9;

	public static byte[] BIT_TORRENT_PROTOCOL = new String(
			"BitTorrent protocol").getBytes();

}
