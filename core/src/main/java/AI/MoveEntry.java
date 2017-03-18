package AI;

import grid.BoardElement;
import enums.Move;

public class MoveEntry 
{
	private BoardElement element;
	private Move move;
	private int rank;
	
	public MoveEntry(BoardElement element, Move move)
	{
		this.element = element;
		this.move = move;
	}
		
	public MoveEntry(Move move)
	{
		this.move = move;
	}
	
	public Move getMove()
	{
		return move;
	}
	
	public BoardElement getElement()
	{
		return element;
	}
	
	public int getRank()
	{
		return rank;
	}
	
	public void setRank(int rank)
	{
		this.rank = rank;
	}
	
	
}
