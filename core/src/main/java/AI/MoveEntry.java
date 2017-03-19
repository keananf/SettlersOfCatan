package AI;

import grid.BoardElement;
import enums.DevelopmentCardType;
import enums.Move;

public class MoveEntry 
{
	private BoardElement element;
	private DevelopmentCardType cardType;
	private Requests.Request.BodyCase move;
	private int rank;
	
	public MoveEntry(BoardElement element, Requests.Request.BodyCase move)
	{
		this.element = element;
		this.move = move;
	}
		
	public MoveEntry(Requests.Request.BodyCase move)
	{
		this.move = move;
	}
	
	public MoveEntry(DevelopmentCardType cardType, Requests.Request.BodyCase move)
	{
		this.cardType = cardType;
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
	
	public void setCardType(DevelopmentCardType cardType)
	{
		this.cardType = cardType;
	}
	
	public DevelopmentCardType getCardType()
	{
		return cardType;
	}
	
}
