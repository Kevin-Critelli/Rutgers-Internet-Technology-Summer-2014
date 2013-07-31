import java.io.*;
import java.net.*;


public class FrontDoor implements Runnable {
        int port = 5100;
        ServerSocket frontDoor;
        
        public FrontDoor() throws Exception
        {
            this.frontDoor = new ServerSocket(port);
        }
            
        public void ListenForHandshakes() throws Exception {
			System.out.println("Spawning upload threads");
            while(true)
            {
                Socket connectionSocket = frontDoor.accept();
                UPeer Leech = new UPeer(connectionSocket);
                new Thread(Leech).start();
            }
        }
        public void run() 
		{
			try 
			{
			   ListenForHandshakes();
			}
			catch (Exception e) {
				System.out.println("Something went wrong listening for handshakes");
			}
		}

}
// Create the front door once
// Listen for a "doorbell" in the thread
// Cater to each of your "houseguests" in subsequent threads- this is done by
// calling ListenForHandshakes
// in a Thread

// Test to see if we get upload requests after Paul Submits 5100 to the tracker!

// UPeers do not have a port and IP that is accessible to us because of the

// nature of the serverSocket connection returned via the accept method. That information is encapsulated.
// This might need a run method too just to listen to the front door, while also dealing with clients? 

// nature of the serverSocket connection returned via the accept method. That
// information is encapsulated.
// This might need a run method too just to listen to the front door, while also
// dealing with clients
