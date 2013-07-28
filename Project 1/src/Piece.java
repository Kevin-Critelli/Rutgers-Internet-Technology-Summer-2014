/**
 * Class Representing a Piece of data from the file 
 * 
 * @author Kevin Critelli
 * 
 * */
 
 import java.util.ArrayList;
 
 public class Piece{
	
	public int index;
	
	private ArrayList<byte[]> subPieces;
	
	public Piece(int index){
		this.index = index;
		this.subPieces = new ArrayList<byte[]>();
	}
	
	public void addSubPiece(byte[] subPiece){	
		this.subPieces.add(subPiece);
	}
	
	public ArrayList<byte[]> getPiece(){
			return this.subPieces;
	}
}
