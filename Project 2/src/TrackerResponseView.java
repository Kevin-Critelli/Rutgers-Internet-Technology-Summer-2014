import java.awt.*;

import javax.swing.*;

/**
 * This class has to labels and gets the infromation
 * from a tracker response object. Will be updated
 * along with the tracker response.
 * 
 * @author pauljones
 *
 */
public class TrackerResponseView extends JPanel {
	private static final long serialVersionUID = 821753837489416698L;
	
	JLabel trackerIntervalLabel;
	JLabel numberOfPeersLabel;
	
	public TrackerResponseView(TrackerResponse tr) {
		this.setLayout(new GridLayout(1,2));
		
		trackerIntervalLabel = new JLabel();
		trackerIntervalLabel.setText("Tracker update interval: " + tr.interval);
		trackerIntervalLabel.setHorizontalAlignment( SwingConstants.CENTER );
		this.add(trackerIntervalLabel);
		
		numberOfPeersLabel = new JLabel();
		numberOfPeersLabel.setText("Number of peers: " + tr.peerSize() + " (" + tr.complete + " seeders/" + tr.incomplete + " leechers)");
		numberOfPeersLabel.setHorizontalAlignment( SwingConstants.CENTER );
		this.add(numberOfPeersLabel);

	}
	
	public void update(TrackerResponse tr) {
		trackerIntervalLabel.setText("Tracker update interval: " + tr.interval);
		numberOfPeersLabel.setText("Number of peers: " + tr.peerSize() + " (" + tr.complete + " seeders/" + tr.incomplete + " leechers)");
	}
}
