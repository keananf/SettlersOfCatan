package client;

import enums.Move;
import board.Edge;
import board.Node;
import enums.ClickObject;

public class InProgressTurn 
{
	public ClickObject initialClickObject = null;
	public Move[] possibilities = {null, null};
	public Move chosenMove = null;
	
	public Node chosenNode;
	public Edge chosenEdge;
}
