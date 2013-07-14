import java.nio.ByteBuffer;

public class Peer {
	public int port;
	public String ip;

	public static final ByteBuffer KEY_IP = ByteBuffer.wrap(new byte[] { 'i',
			'p' });
	public static final ByteBuffer KEY_PORT = ByteBuffer.wrap(new byte[] { 'p',
			'o', 'r', 't' });

	public Peer(int port, String ip) {
		this.port = port;
		this.ip = ip;
	}
	
	public String toString() {
		return "" + ip + ":" + port;
	}
}
