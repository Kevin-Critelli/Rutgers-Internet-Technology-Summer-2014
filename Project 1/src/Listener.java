import java.io.*;
import java.net.*;

public class Listener {
	public static void main(String args[]) throws Exception

	{
		int newPort = Integer.parseInt(args[0]);
		String clientSentence;
		String myResponse;

		ServerSocket Listener = new ServerSocket(newPort);
		System.out.println("Listening on port" + " " + args[0]);

		while (true) {
			Socket connectionSocket = Listener.accept();

			BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(
					connectionSocket.getOutputStream());

			clientSentence = inFromClient.readLine();

			System.out.println(clientSentence);
			// connectionSocket.close();

			myResponse = "Well hello there";
			outToClient.writeBytes(myResponse + "\n");
		}

	}
}

// See if this changes with threading
