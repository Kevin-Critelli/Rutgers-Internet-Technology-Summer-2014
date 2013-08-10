

import java.awt.*;
import javax.swing.*;

public class TorrentInfoView extends JPanel {
	private static final long serialVersionUID = -693263233518741146L;
	
	JLabel pieceLengthLabel;
	JLabel filenameLabel;
	JLabel fileLengthLabel;
	
	public TorrentInfoView(TorrentInfo info) {
		this.setLayout(new GridLayout(0,3));
		
		filenameLabel = new JLabel();
		filenameLabel.setText("Filename: " + info.file_name);
		filenameLabel.setHorizontalAlignment( SwingConstants.CENTER );
		this.add(filenameLabel);
		
		pieceLengthLabel = new JLabel();
		pieceLengthLabel.setText("Piece length: " + info.piece_length + " bytes");
		pieceLengthLabel.setHorizontalAlignment( SwingConstants.CENTER );
		this.add(pieceLengthLabel);
		
		fileLengthLabel = new JLabel();
		fileLengthLabel.setText("File length: " + info.file_length + " bytes");
		fileLengthLabel.setHorizontalAlignment( SwingConstants.CENTER );
		this.add(fileLengthLabel);
		
	}
}