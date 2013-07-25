//***************************************************************************************************************
// This class represents the protocol for uploading a file piece to a given peer
//
//
// Author: The Notorious n00b Richie
//***************************************************************************************************************
// may need to import more stuff than we have previously imported 

public class Seed
{
    // instance variables - here we have to be careful since the directions are reversed from other examples
    public int port;
	public String ip;
	public DataOutputStream dout = null;
	public DataInputStream din = null;
	public Socket socket = null;
	public OutputStream output = null;
	public InputStream input = null;
	public TorrentInfo ti = null;
    //Constructor for objects of class Upload
    
    public Seed(string args[])
    {
        this.bullshit = bullshit;
        this.horseshit = horseshit;
        //Empty constructor
        // When we construct it, we can't give it a port and IP from the tracker
        // Until I/we figure out which port we are supposed to be listening on. 
        // Maybe we create this outside of this class? 
        // We want to have a port we listen on for requests from any peer
        // And then a separate port for each peer for whom we are seeding data
    }

    
    public boolean ReceiveHandshakes()
    {
        // This should end with setting up the socket after we parse the data
        // 
    }
    public byte[] ReceiveMessages()
    {
        // parse it and depending on its type, send a message, or send a message and a piece
        // We may send this somewhere else, we may decide, like the other methods
        // That it ought to be a boolean
    }
    public byte[] SendMessages()
    {
        // what are all the types of messages we can send? 
        // here we are sending choke and unchoke
        // no interested messages
        // maybe a bitfield
        // potentially a cancel? 
        // have? 
    }
    public byte[] SendFilePiece()
    {
        // take as a parameter a piece of the byte array from the receive message
        // where do we access the file pieces? from our completed array? from things we have downloaded?
        // this will always be accompanied by a piece message from above- might we worth hard coding
    }
}
// What port are they going to be sending us the handshake message on? 
// We do not close the socket once we send one piece. We wait for a message for a certain amount of time. 
// If we do not hear anything in that amount of time then we don't do anything.
// Remember we ask for a piece and we get a piece message, and the piece itself. Think in reverse