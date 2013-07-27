import java.nio.ByteBuffer;


public class RUBTClientUtils {
	public static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 3);
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
				'B', 'C', 'D', 'E', 'F' };

		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			byte hi = (byte) ((b >> 4) & 0x0f);
			byte lo = (byte) (b & 0x0f);
			sb.append('%').append(hex[hi]).append(hex[lo]);
		}

		return sb.toString();
	}
	
	/**
	 * This returns a string from a byte array, where every byte is cast to a
	 * char. This is used for debugging and curiosity purposes.
	 * 
	 * @author Paul Jones
	 * 
	 * @param b
	 * @return
	 */

	public static String byteBufferToString(ByteBuffer b) {
		String s = new String();

		for (int i = 0; i < b.array().length; i++) {
			s += b.array()[i];
		}

		return s;
	}
}
