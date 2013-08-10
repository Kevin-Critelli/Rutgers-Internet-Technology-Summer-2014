import java.nio.ByteBuffer;


public class RUBTClientUtils extends RUBTClient {

	/**
	 * Checks to see if we have all pieces, that way we do not attempt to save the file
	 * before having all pieces
	 * 
	 * @author Kevin Critelli
	 * @return boolean A boolean value representing whether or not we have all the pieces
	 * */

	public static final boolean check() {
		int i;
		for (i = 0; i < have.length; i++) {
			if (have[i] != true) {
				return false;
			}
		}
		return true;
	}
	
	/**
	  * Initializes certain fields in RUBTClient Main Class
	  *
	  * @author Kevin Critelli
	  *
	  */
	
	public static final void initializeFields(){
		int i;
		
		pieces = new ByteBuffer[torrentInfo.piece_hashes.length];
		requests = new boolean[torrentInfo.piece_hashes.length];
		have = new boolean[torrentInfo.piece_hashes.length];
		left = torrentInfo.file_length;

		for (i = 0; i < have.length; i++) {
			have[i] = false;
		}

		for (i = 0; i < requests.length; i++) {
			requests[i] = false;
		}
	}

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
	
	/**
	 * Converts a 4 byte Big-Endian Hex Value to an int
	 * 
	 * @author Kevin Critelli
	 * 
	 * @param bytes
	 *            The byte array representing the number
	 * @return int returns an int representation of the byte array
	 * */

	public static final int byteArrayToInt(byte[] bytes) {
		return java.nio.ByteBuffer.wrap(bytes).getInt();
	}
	
	/**
	 * Converts an integer value to a 4 byte Big-Endian Hex Value
	 * 
	 * @author Kevin Critelli
	 * 
	 * @param value
	 *            The Integer to change into 4 byte Big-Endian Hex
	 * @return byte[] A Byte array of size 4 containing the four Hex values
	 */

	public static byte[] intToByteArray(int value) {
		byte[] retVal = new byte[4];
		retVal[0] = (byte)(value >> 24);
		retVal[1] = (byte)(value >> 16);
		retVal[2] = (byte)(value >> 8);
		retVal[3] = (byte)(value);
		return retVal;
	}
}
