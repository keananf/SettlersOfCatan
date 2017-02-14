package AI;

import board.BoardElement;
import board.Edge;
import board.Node;
import enums.Move;

public class MoveEntry 
{
	private Node node;
	private Edge edge;
	private Move move;
	private int rank;
	
	public MoveEntry(Node node, Move move)
	{
		this.node = node;
		this.move = move;
	}
	
	public MoveEntry(Edge edge, Move move)
	{
		this.edge = edge;
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
	
	public Node getNode()
	{
		Node retNode = null;
		
		if(move == Move.BUILD_SETTLEMENT || move == Move.UPGRADE_SETTLEMENT)
		{
			retNode = node;
		}
		
		return retNode;
	
	}
	
	public Edge getEdge()
	{
		Edge retEdge = null;
		
		if(move == Move.BUILD_ROAD)
		{
			retEdge = edge;
		}
		
		return retEdge;
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
