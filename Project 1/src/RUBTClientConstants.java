import java.nio.ByteBuffer;

public class RUBTClientConstants {

	public final static boolean DEVELOP = true;

	/**
	 * Key used to retrieve the info dictionary from the torrent metainfo file.
	 */
	public final static ByteBuffer KEY_INFO = ByteBuffer
			.wrap("info".getBytes());

	/**
	 * Key used to retrieve the length of the torrent.
	 */
	public final static ByteBuffer KEY_LENGTH = ByteBuffer.wrap("length"
			.getBytes());

	/**
	 * Key used to retrieve the piece hashes.
	 */
	public final static ByteBuffer KEY_PIECES = ByteBuffer.wrap("pieces"
			.getBytes());

	/**
	 * Key used to retrieve the file name.
	 */
	public final static ByteBuffer KEY_NAME = ByteBuffer
			.wrap("name".getBytes());

	/**
	 * Key used to retrieve the default piece length.
	 */
	public final static ByteBuffer KEY_PIECE_LENGTH = ByteBuffer
			.wrap("piece length".getBytes());

	/**
	 * ByteBuffer to retrieve the announce URL from the metainfo dictionary.
	 */
	public static final ByteBuffer KEY_ANNOUNCE = ByteBuffer.wrap("announce"
			.getBytes());

	public final static String TR_KEY_INFO_HASH = "info_hash";
	public final static String TR_KEY_PEER_ID = "peer_id";
	public final static String TR_KEY_PORT = "port";
	public final static String TR_KEY_UPLOADED = "uploaded";
	public final static String TR_KEY_DOWNLOADED = "downloaded";
	public final static String TR_KEY_LEFT = "left";

	public static final ByteBuffer TR_KEY_FAILURE = ByteBuffer
			.wrap("failure reason".getBytes());
	public static final ByteBuffer TR_KEY_PEERS = ByteBuffer.wrap("peers"
			.getBytes());
	public static final ByteBuffer TR_KEY_INTERVAL = ByteBuffer.wrap("interval"
			.getBytes());
	public static final ByteBuffer TR_KEY_MIN_INTERVAL = ByteBuffer
			.wrap("min interval".getBytes());
	public static final ByteBuffer TR_KEY_COMPLETE = ByteBuffer.wrap("complete"
			.getBytes());
	public static final ByteBuffer TR_KEY_INCOMPLETE = ByteBuffer
			.wrap("incomplete".getBytes());
}
