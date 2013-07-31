import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

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
	
	public static ByteBuffer[] pieces = null;
	public static boolean[] requests = null;
	public static boolean[] have = null;
	public static int downloaded = 0;
	public static int uploaded = 0;
	public static TorrentInfo torrentInfo = null;
	public static TrackerResponse trackerResponse = null;
	public static String announce_url = "";

	/**
	 * @param args
	 * @throws InterruptedException 
	 */

	public static void main(String[] args) throws InterruptedException {

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

		String ipString;
		int i;

		/**
		 * 2. Open the .torrent file and parse the data inside. You may use the
		 * Bencoder2 or TorrentInfo classes to decode the data.
		 */

		torrentInfo = TorrentInfo.getTorrentInfoFrom(torrentFile);

		if (RUBTClientConstants.DEVELOP)
			System.out.println(torrentInfo);

		/**
		 * 3. Contact the tracker via the announce URL, including all of the
		 * necessary key/value pairs in the request. The java.net.URL class is a
		 * convenient way to accomplish this.
		 * 
		 */

		trackerResponse = new TrackerResponse(torrentInfo);

		if (RUBTClientConstants.DEVELOP) {
			System.out.println(trackerResponse);
		}
		
		announce_url = trackerResponse.announceURL;

		/**
		 * 6. Capture the response from the tracker and decode it in order to
		 * get the list of peers. From this list of peers, use only the peers
		 * located at 128.6.171.3 and 128.6.171.4 . You must extract these IP
		 * addresses from the list, hard-coding it is not acceptable, except the
		 * comparison itself.
		 */

		ArrayList<Peer> peers = trackerResponse.getAcceptablePeers();

		if (RUBTClientConstants.DEVELOP) {
			System.out.println("Acceptable peers:");

			for (i = 0; i < peers.size(); i++) {
				System.out.println("\t" + peers.get(i));
			}
		}

		/** Initalized variables **/

		pieces = new ByteBuffer[torrentInfo.piece_hashes.length];
		requests = new boolean[torrentInfo.piece_hashes.length];
		have = new boolean[torrentInfo.piece_hashes.length];

		for (i = 0; i < have.length; i++) {
			have[i] = false;
		}

		for (i = 0; i < requests.length; i++) {
			requests[i] = false;
		}

		/**
		 * Create download peer objects (threads) for each valid peer to
		 * download from our list
		 **/

		// add richies ip to peers @ip 192.168.1.3 @port 5100
		// peers.add(new Peer(5100, "192.168.1.3"));

		// create x peers
		System.out.println("Spawning download threads");
		for (i = 0; i < peers.size(); i++) {
			ipString = peers.get(i).ip.replaceAll(":", ".");
			DPeer p = new DPeer(ipString, peers.get(i).port);
			new Thread(p).start();
		}
		
		try{
			System.out.println("creating front door object to listen for uploads");
			FrontDoor f = new FrontDoor();
			new Thread(f).start();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("spawning tracker thread to send announce messages during the interval");
		TrackerThread t = new TrackerThread();
		Thread tt = new Thread(t);
		tt.run();
		
		int choice = 0;
		Scanner sc = new Scanner(System.in);
		while(true){
			System.out.println("1) Suspend Program (Save state)");
			System.out.println("2) Quit");
			choice = sc.nextInt();
			
			if(choice == 1){
				System.out.println("exiting and saving state");
				break;
			}else if(choice == 2){
				System.out.println("Exiting");
				break;
			}else{
				System.out.println("Invalid option, please enter 1 or 2");
			}
		}
		
		while (!check()) {} 											// makes sure we have all pieces before writing to file
		/** Writes data to output file **/

		try {
			//save file
			FileOutputStream fileoutput = new FileOutputStream(new File(simargs[1]));
			for (i = 0; i < pieces.length; i++) {
				byte[] array = pieces[i].array();
				fileoutput.write(pieces[i].array());
			}
		}catch (Exception e) {
			System.out.println("exception thrown writing to file");
		}
		System.exit(0);
	}

	public static boolean check() {
		int i;
		for (i = 0; i < have.length; i++) {
			if (have[i] != true) {
				return false;
			}
		}
		return true;
	}
}
