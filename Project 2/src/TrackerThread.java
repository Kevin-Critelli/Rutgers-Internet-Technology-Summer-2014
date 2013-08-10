import java.io.IOException;
import java.net.MalformedURLException;

public class TrackerThread extends RUBTClient implements Runnable {
	
	public static boolean running = true;
	
	public void run() {
		while(running){
			try{
				//send the tracker an update of our status, ie event 
				System.out.println("Sending update to tracker because the interval has just started");
				trackerResponse = trackerResponse.getTrackerResponse(announce_url,
						torrentInfo.info_hash.array(), downloaded, uploaded,
						left);

				
				System.out.println("Sleeping for " + trackerResponse.interval + " seconds");
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
