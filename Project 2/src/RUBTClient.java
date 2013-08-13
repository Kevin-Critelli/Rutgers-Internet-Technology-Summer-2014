/**
 * @author Kevin Critelli
 * @author Paul Jones
 * @author Richie von der Schmidt
 */

import java.net.UnknownHostException;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.File;

import javax.swing.*;
import javax.swing.table.*;

/**
 * Main class in the torrent client Calls all other necessary classes to begin
 * the process, maintains a small interface for the user, and closes client when
 * necessary.
 * 
 * */
public class RUBTClient {

	// static members belong to class, not specific instances

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
	public static JFrame frame;
	public static JTable peerTable;
	public static JButton stopButton;
	public static String filename;

	public static void main(String[] args) throws InterruptedException {

		frame = new JFrame("RUBT Client");
		String torrentFile = JOptionPane.showInputDialog(frame,
				"Where's your torrent file?", "project2.torrent");
		filename = JOptionPane.showInputDialog(frame,
				"What would you like to call the saved file?");

		TrackerThread t;
		FrontDoor f;
		int i = 0;

		RUBTClientUtils.Parse_Torrent_Contact_Tracker(torrentFile);
		RUBTClientUtils.initializeFields();
		RUBTClientUtils.checkState(filePtr);

		trackerResponse = new TrackerResponse(torrentInfo);
		announce_url = trackerResponse.announceURL;

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		TorrentInfoView tiv = new TorrentInfoView(torrentInfo);
		TrackerResponseView trv = new TrackerResponseView(trackerResponse);

		JPanel headerPanel = new JPanel(new GridLayout(2, 1));
		headerPanel.add(tiv);
		headerPanel.add(trv);
		mainPanel.add(headerPanel, BorderLayout.NORTH);

		JPanel progressPanel = new JPanel(new BorderLayout());
		stopButton = new JButton("Finish and save");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				finishAndSaveButtonClicked();
			}
		});

		stopButton.setEnabled(false);
		JProgressBar pg = new JProgressBar();
		progressPanel.add(stopButton, BorderLayout.EAST);
		progressPanel.add(pg, BorderLayout.CENTER);
		mainPanel.add(progressPanel, BorderLayout.SOUTH);

		frame.add(mainPanel);

		frame.setSize(720, 320);
		frame.setMinimumSize(new Dimension(480, 240));
		frame.setMaximumSize(new Dimension(900, 280));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {

				System.out.println("stopped!");

				try {
					trackerResponse.sendEventStopped(torrentInfo);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				System.exit(0);
			}
		});

		JPanel peerPanel = new JPanel(new BorderLayout());
		peerTable = new JTable(new PeerTableModel(trackerResponse,
				trackerResponse.peerSize()));

		JTableHeader th = peerTable.getTableHeader();
		TableColumnModel tcm = th.getColumnModel();
		TableColumn tc = tcm.getColumn(0);
		tc.setHeaderValue("Peer IPs");
		tc = tcm.getColumn(1);
		tc.setHeaderValue("Peer port");
		tc = tcm.getColumn(2);
		tc.setHeaderValue("Is choked?");
		th.repaint();

		peerPanel.add(peerTable, BorderLayout.CENTER);
		peerPanel.add(peerTable.getTableHeader(), BorderLayout.NORTH);

		mainPanel.add(peerPanel, BorderLayout.CENTER);

		// spawn download threads
		for (i = 0; i < trackerResponse.peers.size(); i++) {
			if (trackerResponse.peers.get(i).ip.startsWith("128:6:171")) {
				System.out.println("kasjnkjnsd found a correct IP");
				trackerResponse.peers.get(i).ip = trackerResponse.peers.get(i).ip
						.replaceAll(":", ".");
				new Thread(trackerResponse.peers.get(i)).start();
			}
		}

		// sets up front door object to listen for peers and spawn upload
		// threads
		System.out
				.println("Spawning Front Door Object to Listen for Incoming Peer Connections");
		f = new FrontDoor();
		new Thread(f).start();

		// spawn tracker thread to send updates during time interval
		t = new TrackerThread();
		new Thread(t).start();

		while (true) {
			int done = (int) (((float) downloaded / (float) torrentInfo.file_length) * 100);
			pg.setValue(done);
			trv.update(trackerResponse, uploaded);

			if (done >= 100) {
				stopButton.setEnabled(true);

				try {
					trackerResponse.sendEventCompleted(torrentInfo);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			PeerTableModel tableModel = (PeerTableModel) peerTable.getModel();
			tableModel.setPeerList(trackerResponse, trackerResponse.peerSize());
			peerTable.setModel(tableModel);
		}
	}

	public static void finishAndSaveButtonClicked() {
		// stop threads from running, if any
		for (int i = 0; i < trackerResponse.peers.size(); i++) {
			trackerResponse.peers.get(i).isRunning = false; // this doesn't seem
															// to stop them!
		}

		if (RUBTClientUtils.check()) {
			RUBTClientUtils.SaveFile(filename);
		}

		frame.setVisible(false);
		System.exit(0);
	}
}

/**
 * This is how the table in the RUBTClient frame gets updated from the tracker
 * response and peers.
 * 
 * @author pauljones
 * 
 */
class PeerTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 6201801201614880087L;
	private TrackerResponse ti;
	private int numberOfPeers = 0;

	public PeerTableModel(TrackerResponse ti, int numberOfPeers) {
		this.ti = ti;
		this.numberOfPeers = numberOfPeers;
	}

	public void setPeerList(TrackerResponse ti, int numberOfPeers) {
		this.ti = ti;
		this.numberOfPeers = numberOfPeers;
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return numberOfPeers;
	}

	public Object getValueAt(int row, int column) {
		if (row >= ti.peers.size()) {
			return null;
		}

		return ti.peers.get(row).getTableInfo()[column];
	}
}
