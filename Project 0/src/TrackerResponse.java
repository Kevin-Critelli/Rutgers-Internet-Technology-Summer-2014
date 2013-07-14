import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class TrackerResponse {

	public String failureReason;
	public String failureMessage;
	public int interval;
	public int minimumInterval;
	public String trackerID;
	public int complete;
	public int incomplete;
	public ArrayList<Peer> peers;

	public static final ByteBuffer KEY_FAILURE = ByteBuffer.wrap(new byte[] {
			'f', 'a', 'i', 'l', 'u', 'r', 'e', ' ', 'r', 'e', 'a', 's', 'o',
			'n' });
	public static final ByteBuffer KEY_PEERS = ByteBuffer.wrap(new byte[] {
			'p', 'e', 'e', 'r', 's' });
	public static final ByteBuffer KEY_INTERVAL = ByteBuffer.wrap(new byte[] {
			'i', 'n', 't', 'e', 'r', 'v', 'a', 'l' });
	public static final ByteBuffer KEY_MIN_INTERVAL = ByteBuffer.wrap(new byte[] {
			'm', 'i', 'n', ' ', 'i', 'n', 't', 'e', 'r', 'v', 'a', 'l' });
	public static final ByteBuffer KEY_COMPLETE = ByteBuffer.wrap(new byte[] {
			'c', 'o', 'm', 'p', 'l', 'e', 't', 'e' });
	public static final ByteBuffer KEY_INCOMPLETE = ByteBuffer.wrap(new byte[] {
			'i', 'n', 'c', 'o', 'm', 'p', 'l', 'e', 't', 'e' });

	public TrackerResponse(HashMap<ByteBuffer, Object> response)
			throws Exception {
		if (response.containsKey(KEY_FAILURE)) {
			throw new Exception("Tracker failed");
		}
		
		if (response.containsKey(KEY_INTERVAL))
			this.interval = (Integer) response.get(KEY_INTERVAL);
		
		if (response.containsKey(KEY_COMPLETE))
			this.complete = (Integer) response.get(KEY_COMPLETE);
		
		if (response.containsKey(KEY_INCOMPLETE))
			this.incomplete = (Integer) response.get(KEY_INCOMPLETE);
		
		if (response.containsKey(KEY_MIN_INTERVAL))
			this.minimumInterval = (Integer) response.get(KEY_MIN_INTERVAL);
		
		ByteBuffer o = (ByteBuffer) response.get(KEY_PEERS);
		System.out.println(RUBTClient.byteBufferToString(o));
	}
}
