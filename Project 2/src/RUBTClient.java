import java.net.MalformedURLException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.io.File;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class RUBTClient implements ActionListener {

	public static ByteBuffer[] pieces = null;
	public static boolean[] requests = null;
	public static boolean[] have = null;
	public static int downloaded = 0;
	public static int uploaded = 0;
	public static int left = 0;
	public static boolean isStarted = false; // first request must contain
												// started event
	public static boolean isStopped = false; // used when client is shut down
												// gracefully
	public static boolean isCompleted = false;
	public static TorrentInfo torrentInfo = null;
	public static TrackerResponse trackerResponse = null;
	public static String announce_url = "";

	/**
	 * @param args
	 * @throws InterruptedException
	 */

	public static void main(String[] args) throws InterruptedException {
		TrackerThread t;
		int i = 0;

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
		mainPanel.add(tiv, BorderLayout.NORTH);
		mainPanel.add(trv, BorderLayout.SOUTH);
		
		JProgressBar pg = new JProgressBar();
		mainPanel.add(pg, BorderLayout.CENTER);
		
		frame.add(mainPanel);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 320);
        frame.setVisible(true);
		
		RUBTClientUtils.initializeFields();

		for (i = 0; i < trackerResponse.peers.size(); i++) {
			trackerResponse.peers.get(i).ip = trackerResponse.peers.get(i).ip.replaceAll(":",".");
			new Thread(trackerResponse.peers.get(i)).start();
		}
		
		t = new TrackerThread();
		new Thread(t).start();
		
		while (true) {
			int done = (int)(((float)downloaded / (float)torrentInfo.file_length) * 100);
			pg.setValue(done);
			trv.update(trackerResponse);
		}

		// spawn tracker thread to send updates during time interval
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		FileOutputStream fileoutput;
		
		try {
			// save file
			fileoutput = new FileOutputStream(new File(torrentInfo.file_name));

			for (int i = 0; i < pieces.length; i++) {
				fileoutput.write(pieces[i].array());
			}

			fileoutput.close();

			System.out.println("End Download " + downloaded);
			System.out.println("End Left " + left);

			// send stopped event to tracker
			trackerResponse.sendTrackerFinishedStopped(announce_url,
					torrentInfo.info_hash.array(), downloaded, uploaded, 0);
			//t.stopExecution();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
