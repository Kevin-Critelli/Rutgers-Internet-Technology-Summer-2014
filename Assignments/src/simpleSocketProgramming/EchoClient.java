package simpleSocketProgramming;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class EchoClient {
	public static void main(String[] args) throws Exception {
		Socket s; // this is the principle container/manipulator for this program
		BufferedReader sin; // to read in from t
		PrintWriter sout; // to read out from the socket
		BufferedReader in; // to read form user
		String host, line; // host is for an argument, line is read in
		int port; // the port read in from user, command line
		
		/*
		 * Accept the echo server hostname/IP address as args[0] and the port as
		 * args[1] in the main method. The port should be parsed into an int or
		 * Integer.
		 */

		host = args[0]; // this gets the argument given at 0
		port = Integer.parseInt(args[1]); // parses the integer from argument at 1

		/*
		 * Construct a socket connected to the echo server based on the
		 * hostname/port specified in the command-line arguments.
		 */

		 s = new Socket(host, port); // opens the principle container/mannipulator for this assignment

		/*
		 * Read a single line of text from System.in and send it to the echo
		 * server including any newline characters (CR/LF).
		 */

		sin = new BufferedReader(new InputStreamReader(s.getInputStream())); // to read
		sout = new PrintWriter(s.getOutputStream(), true); // to write
		in = new BufferedReader(new InputStreamReader(System.in)); // from user

		line = in.readLine(); // getting from user
		sout.println(line); // sending out to server
		
		/*
		 * Read a single line response from the echo server and print it to
		 * System.out.
		 */

		line = sin.readLine(); // reading from server
		System.out.println(line); // printing to user
		
		/*
		 * Close the Socket to the echo server and any IOStreams that you may
		 * have opened during the program's lifetime.
		 */
		
		s.close(); // this
		sin.close(); // closes
		sout.close(); // all of my
		in.close(); // sockets and IO streams

		/*
		 * Exit your program. At no point in your application should you call
		 * System.exit(int).
		 */
		
		// program exits.
	}
}
