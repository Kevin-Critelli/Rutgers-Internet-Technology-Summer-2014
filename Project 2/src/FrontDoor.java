import java.io.*;
import java.net.*;

/**
 * This class represents a server, that waits for peers to contact us for uploads
 * 
 * @author Richie VonderSchmidt
 * */

public class FrontDoor implements Runnable {
        int port = 5110;
        ServerSocket frontDoor;
        
        /**
         * FrontDoor Object Constructor
         * 
         * @author Richie VonderSchmidt
         * @throws Exception An Exception object is thrown if an error occurs
         * */
        
        public FrontDoor(){
			try{this.frontDoor = new ServerSocket(port);}
			catch(Exception e){e.printStackTrace();}
        }
        
        /**
         * This function sits on our server port, and waits for incoming handshakes
         * than spawns threads (upload peer object) to handle the connection
         * with that specific peer
         * 
         * @throws Exception An exception is thrown if an error occurs
         * @author Richie VonderSchmidt
         * */
        
        public void ListenForHandshakes() throws Exception {
            while(true){
                Socket connectionSocket = frontDoor.accept();
                UPeer Leech = new UPeer(connectionSocket);
                new Thread(Leech).start();
            }
        }
        
        /**
         * Run method for this thread, listen for handshakes, and spawn threads to handle each connection
         * 
         * @author Richie VonderSchmidt
         * */
         
        public void run() 
		{
			try 
			{
			   ListenForHandshakes();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

}
