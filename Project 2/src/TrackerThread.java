import java.io.IOException;
import java.net.MalformedURLException;

/**
 * This class is responsible for updating TrackerReponse in RUBTClient.
 * It sleeps on the interval returned from the tracker.
 * 
 * It does not send any events to the tracker.
 * 
 * @author pauljones
 *
 */
public class TrackerThread extends RUBTClient implements Runnable {
	
	public static boolean running = true;
	
	public void run() {
		while(running){
			try{
				trackerResponse = trackerResponse.getTrackerResponse(announce_url,
						torrentInfo.info_hash.array(), downloaded, uploaded,
						left);
				Thread.sleep(trackerResponse.interval);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void stopExecution(){
		this.running = false;
	}
}
