import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.io.File;

import javax.swing.*;

/**
 * Main class in the torrent client Calls all other necessary classes to begin
 * the process, maintains a small interface for the user, and closes client when
 * necessary.
 */

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
	public static TrackerThread t;
	public static JButton stopButton;
	public static ArrayList<Thread> allThreads;
	public static JFrame frame;

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		FrontDoor f;
		Scanner sc;
		int i = 0, choice = 0;

		frame = new JFrame("RUBT Client");
		String torrentFile = JOptionPane.showInputDialog(frame,
				"Where's your torrent file?", "project2.torrent");

		RUBTClientUtils.Parse_Torrent_Contact_Tracker(torrentFile);
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
				stopButtonPressed();
			}
		});
		
		stopButton.setEnabled(false);
		JProgressBar pg = new JProgressBar();
		progressPanel.add(stopButton, BorderLayout.EAST);
		progressPanel.add(pg, BorderLayout.CENTER);
		mainPanel.add(progressPanel, BorderLayout.SOUTH);
		
		allThreads = new ArrayList<Thread>();
		
		frame.add(mainPanel);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(720, 320);
		frame.setVisible(true);

		RUBTClientUtils.initializeFields();
		RUBTClientUtils.checkState(filePtr);

		// spawn download threads
		for (i = 0; i < trackerResponse.peers.size(); i++) {
			trackerResponse.peers.get(i).ip = trackerResponse.peers.get(i).ip
					.replaceAll(":", ".");
			Thread thread =new Thread(trackerResponse.peers.get(i));
			allThreads.add(thread);
			thread.start();
		}

		t = new TrackerThread();
		Thread thread = new Thread(t);
		thread.start();
		
		sc = new Scanner(System.in);
		while (true) {
			int done = (int) (((float) downloaded / (float) torrentInfo.file_length) * 100);
			pg.setValue(done);
			trv.update(trackerResponse);
			if (done == 100) {
				stopButton.setEnabled(true);
			}
		}
	}

	public static void stopButtonPressed() {
		// stop threads from running, if any
		for (int i = 0; i < trackerResponse.peers.size(); i++) {
			trackerResponse.peers.get(i).isRunning = false; // this doesn't seem to stop them!
		}
		
		for (int i = 0; i < allThreads.size(); i++)
			allThreads.get(i).interrupt();
		
		if (RUBTClientUtils.check()) {
			RUBTClientUtils.SaveFile();
		} else {
			try {
				trackerResponse.sendEventStopped(torrentInfo);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		t.stopExecution();
		frame.setVisible(false);
		System.exit(0);
	}
}
