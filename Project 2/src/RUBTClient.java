import java.net.MalformedURLException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.io.File;

/**
 * Main class in the torrent client
 * Calls all other necessary classes to begin the process, maintains a small
 * interface for the user, and closes client when necessary.
 * 
 * */

public class RUBTClient {
	
	//static members belong to class, not specific instances
	
	public static ByteBuffer[] pieces = null;
	public static boolean[] requests = null;
	public static boolean[] have = null;
	public static int downloaded = 0;
	public static int uploaded = 0;
	public static int left = 0;
	public static boolean isStarted = false;							
	public static boolean isStopped = false;							
	public static boolean isCompleted = false;				
	public static TorrentInfo torrentInfo = null;
	public static TrackerResponse trackerResponse = null;
	public static String announce_url = "";
	public static File filePtr = null;
	public static int numPieces = 0;

	/**
	 * @param args
	 * @throws InterruptedException
	 */

	public static void main(String[] args) throws InterruptedException {
		TrackerThread t;
		FrontDoor f;
		Scanner sc;
		int i=0, choice=0;
		
		RUBTClientUtils.Parse_Torrent_Contact_Tracker(args[0]);
	
		RUBTClientUtils.initializeFields();		
		
		RUBTClientUtils.checkState(filePtr);
		
		//spawn download threads
		for (i = 0; i < trackerResponse.peers.size(); i++) {
			trackerResponse.peers.get(i).ip = trackerResponse.peers.get(i).ip.replaceAll(":",".");
			new Thread(trackerResponse.peers.get(i)).start();
		}
		
		//sets up front door object to listen for peers and spawn upload threads
		/*System.out.println("Spawning Front Door");
		f = new FrontDoor();
		new Thread(f).start();*/

		/*
		//spawn tracker thread to send updates during time interval
		t = new TrackerThread();
		new Thread(t).start();*/
		
		//Small Interface for user
		sc = new Scanner(System.in);
		while (true) {
			System.out.println("1) Exit");	
			System.out.println();
			choice = sc.nextInt();

			if (choice == 1) {
				System.out.println("Exiting Program and Current State...");
				
				//stop threads from running, if any
				for (i = 0; i < trackerResponse.peers.size(); i++) {
					trackerResponse.peers.get(i).isRunning = false;
				}
				
				if(RUBTClientUtils.check()) {
					System.out.println();
					System.out.println("File Finished Downloading...Saving File Now...");
					RUBTClientUtils.SaveFile();
					break;
				}else{
					System.out.println();
					System.out.println("File Download Still In Progress ");
					System.out.println("Total Bytes Downloaded: " + downloaded);
					System.out.println("Total Bytes Left To Download: " + left);
					//**ADD**SEND EVENT STOPPED TO TRACKER
					break;
				}
			}else {
				System.out.println("Invalid option, please enter 1 to quit");
			}
		}
		System.exit(0);
	}
}
