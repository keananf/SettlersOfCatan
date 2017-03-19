package AI;

import java.util.ArrayList;

public interface IAI 
{
	public ArrayList<MoveEntry> getMoves();
	public ArrayList<MoveEntry>rankMoves(ArrayList<MoveEntry> moves);
	public MoveEntry selectMove(ArrayList<MoveEntry> moves);
}
