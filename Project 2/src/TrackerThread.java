import java.io.IOException;
import java.net.MalformedURLException;

public class TrackerThread extends RUBTClient implements Runnable {
	
	public static boolean running = true;
	
	public void run() {
		while(running){
			try{
				//send the tracker an update of our status, ie event 
				trackerResponse = trackerResponse.getTrackerResponse(announce_url,
						torrentInfo.info_hash.array(), downloaded, uploaded,
						left);
				//Sleep for the interval, than re-announce with updated information
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
