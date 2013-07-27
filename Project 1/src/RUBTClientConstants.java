import java.nio.ByteBuffer;

public class RUBTClientConstants {

	public final static boolean DEVELOP = true;

	public final static ByteBuffer TRACKER_INFO_KEY_INFO = ByteBuffer.wrap("info".getBytes());
	public final static ByteBuffer TRACKER_INFO_KEY_LENGTH = ByteBuffer.wrap("length".getBytes());
	public final static ByteBuffer TRACKER_INFO_KEY_PIECES = ByteBuffer.wrap("pieces".getBytes());
	public final static ByteBuffer TRACKER_INFO_KEY_NAME = ByteBuffer.wrap("name".getBytes());
	public final static ByteBuffer TRACKER_INFO_KEY_PIECE_LENGTH = ByteBuffer.wrap("piece length".getBytes());
	public static final ByteBuffer TRACKER_INFO_KEY_ANNOUNCE = ByteBuffer.wrap("announce".getBytes());

	public final static String TRACKER_RESPONSE_KEY_INFO_HASH = "info_hash";
	public final static String TRACKER_RESPONSE_KEY_PEER_ID = "peer_id";
	public final static String TRACKER_RESPONSE_KEY_PORT = "port";
	public final static String TRACKER_RESPONSE_KEY_UPLOADED = "uploaded";
	public final static String TRACKER_RESPONSE_KEY_DOWNLOADED = "downloaded";
	public final static String TRACKER_RESPONSE_KEY_LEFT = "left";

	public static final ByteBuffer TRACKER_RESPONSE_KEY_FAILURE = ByteBuffer.wrap("failure reason".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_PEERS = ByteBuffer.wrap("peers".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_INTERVAL = ByteBuffer.wrap("interval".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_MIN_INTERVAL = ByteBuffer.wrap("min interval".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_COMPLETE = ByteBuffer.wrap("complete".getBytes());
	public static final ByteBuffer TRACKER_RESPONSE_KEY_INCOMPLETE = ByteBuffer.wrap("incomplete".getBytes());
	
	public final static String ACCEPTABLE_PEER_1 = "128:6:171:3";
	public final static String ACCEPTABLE_PEER_2 = "128:6:171:4";
}
