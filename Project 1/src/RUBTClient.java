/**
 * @author pauljones
 * 
 *         Do not put any error checking in this class.
 * 
 *         This is for high level abstraction, delegating error handling and
 *         hard work to classes.
 * 
 */
public class RUBTClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/**
		 * 1. Take as a command-line argument the name of the .torrent file to
		 * be loaded and the name of the file to save the data to. Your main
		 * class MUST be called "RUBTClient.java", but may reside in any
		 * package. For example:
		 * 
		 * java RUBTClient somefile.torrent picture.jpg
		 */

		String[] simargs = new String[2];

		simargs[0] = "cs352.png.torrent";
		simargs[1] = "cs352.png";

		String torrentFile = simargs[0];
		String outputFile = simargs[1];

		/**
		 * 2. Open the .torrent file and parse the data inside. You may use the
		 * Bencoder2 or TorrentInfo classes to decode the data.
		 */

		TorrentInfo torrentInfo = TorrentInfo.getTorrentInfoFrom(torrentFile);

		/**
		 * 3. Contact the tracker via the announce URL, including all of the
		 * necessary key/value pairs in the request. The java.net.URL class is a
		 * convenient way to accomplish this.
		 * 
		 */
		
		
	}

}
