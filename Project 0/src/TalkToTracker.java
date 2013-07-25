//***************************************************************************************************************
// This class represents the protocol for uploading a file piece to a given peer
//
//
// Author: The Notorious n00b Richie
//***************************************************************************************************************
// may need to import more stuff than we have previously imported... just like Seed Class

public class TalkToTracker
{
    // instance variables - replace the example below with your own
    private int progress;
    
    // Constructor 
    
    public TalkToTracker()
    {
        // initialise instance variables
        progress = 0;
        // presumably we are going to need the URL class somewhere here
    }

    // one of the many methods we will use here
    public int UpdateTracker()
    {
        // put your code here
        return progress;
    }
}
//*******************************************************************************************
// Here is my proposed plan of attack
// 1) Finish Peer Class
// 2) Try to get a successful thread with one peer seeding from us, while we download
// 3) After that is successful, try to get one peer downloading, seed for someone else, incorporate
// communication with the tracker
// 4) Then, repeat 3, but this time thread with multiple peers
// 5) Then, repeat 4, but this time thread with seeding for multiple peers. 
// Many Design Questions abound
// A) We obviously thread between three key things, seed, download, and talk to tracker
// B) But how do we handle the child threads of each of these three things? 
// C) The main question becomes, do we thread by piece? or do we thread by each peer? 
// Presumably we will know more as we screw up along the way. 
// Signing off for the night
// But it wouldn't be complete without a little rhyme...right?
// Turn your tux red, I'm far from broke, got enough bread
// And mad hoes, ask Beavis I get nothing Butthead
// Big L
//********************************************************************************************