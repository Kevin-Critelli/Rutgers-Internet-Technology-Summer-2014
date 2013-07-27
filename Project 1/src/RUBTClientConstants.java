import java.nio.ByteBuffer;

public class RUBTClientConstants {
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
}
