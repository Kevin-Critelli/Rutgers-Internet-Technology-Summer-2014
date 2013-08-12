import java.net.MalformedURLException;
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
	public static JButton stopButton;

	/**
	 * @param args
	 * @throws InterruptedException
	 */

	public static void main(String[] args) throws InterruptedException {
		TrackerThread t;
		FrontDoor f;
		Scanner sc;
		int i = 0, choice = 0;

		JFrame frame = new JFrame("RUBT Client");
		String torrentFile = JOptionPane.showInputDialog(frame,
				"Where's your torrent file?", "project2.torrent");

		torrentInfo = TorrentInfo.getTorrentInfoFrom(torrentFile);
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
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopButtonPressed();
			}
		});
		JProgressBar pg = new JProgressBar();
		progressPanel.add(stopButton, BorderLayout.EAST);
		progressPanel.add(pg, BorderLayout.CENTER);
		mainPanel.add(progressPanel, BorderLayout.SOUTH);
		
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
			new Thread(trackerResponse.peers.get(i)).start();
		}

		t = new TrackerThread();
		new Thread(t).start();

		sc = new Scanner(System.in);
		while (true) {
			int done = (int) (((float) downloaded / (float) torrentInfo.file_length) * 100);
			pg.setValue(done);
			trv.update(trackerResponse);
		}
	}

	public static void stopButtonPressed() {
		System.out.println("Exiting Program and Current State...");

		// stop threads from running, if any
		for (int i = 0; i < trackerResponse.peers.size(); i++) {
			trackerResponse.peers.get(i).isRunning = false;
		}

		if (RUBTClientUtils.check()) {
			System.out.println();
			System.out
					.println("File Finished Downloading...Saving File Now...");
			RUBTClientUtils.SaveFile();
		} else {
			System.out.println();
			System.out.println("File Download Still In Progress ");
			System.out.println("Total Bytes Downloaded: " + downloaded);
			System.out.println("Total Bytes Left To Download: " + left);
			// **ADD**SEND EVENT STOPPED TO TRACKER
		}
	}
}
