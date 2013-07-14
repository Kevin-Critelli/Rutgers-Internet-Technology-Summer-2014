import java.nio.ByteBuffer;

public class Peer {
	public byte[] peerID;
	public int port;
	public String ip;

	public static final ByteBuffer KEY_IP = ByteBuffer.wrap(new byte[] { 'i',
			'p' });
	public static final ByteBuffer KEY_PORT = ByteBuffer.wrap(new byte[] { 'p',
			'o', 'r', 't' });
	public static final ByteBuffer KEY_PEERID = ByteBuffer.wrap(new byte[] {
			'p', 'e', 'e', 'r', ' ', 'i', 'd' });

	public Peer(byte[] id, int port, String ip) {
		this.peerID = id;
		this.port = port;
		this.ip = ip;
	}
}
