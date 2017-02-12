package client;

import enums.Move;
import board.Edge;
import board.Node;
import enums.ClickObject;
import enums.ResourceType;

public class InProgressTurn 
{
	public ClickObject initialClickObject = null;
	public Move[] possibilities = {null, null};
	public Move chosenMove = null;
	
	public Node chosenNode;
	public Edge chosenEdge;
	public Edge[] chosenEdges = new Edge[2];
	public ResourceType chosenResource;
	public ResourceType[] chosenResources;
}
