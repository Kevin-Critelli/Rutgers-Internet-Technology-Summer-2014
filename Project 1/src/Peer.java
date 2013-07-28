/**
* Class representing a peer for the download Thread will support
* upload/download, run method will process uploads?
**/

public class Peer {

public int port;
public String ip;

public Peer(int port, String ip) {
this.port = port;
this.ip = ip;
}

public String toString() {
return "" + ip + ":" + port;
}

}
