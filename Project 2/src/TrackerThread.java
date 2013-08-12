import java.io.IOException;
import java.net.MalformedURLException;

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
