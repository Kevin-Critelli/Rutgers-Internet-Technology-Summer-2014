import java.io.IOException;
import java.net.MalformedURLException;

public class TrackerThread extends RUBTClient implements Runnable {

	boolean firstTime = true;

	@Override
	public void run() {
		try {
			if (!firstTime) {
				trackerResponse = trackerResponse.getTrackerResponse(announce_url,
						torrentInfo.info_hash.array(), downloaded, uploaded,
						torrentInfo.file_length - downloaded);

				System.out.println(trackerResponse);
			} else
				firstTime = false;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
