package catan.game.exceptions;

import catan.board.*;
import catan.game.build.Road;

@SuppressWarnings("serial")
public class CannotBuildRoadException extends Exception
{
	private Road road;
	
	public CannotBuildRoadException(Road r)
	{
		road = r;
	}

	public String getMessage()
	{
		Node n1 = road.getEdge().getX(), n2 = road.getEdge().getY();
		
		return String.format("Cannot build road from node (%d,%d) to node (%d,%d)\n"
				+ "due to it not being connected to another road of colour %s", 
				n1.getX(), n1.getY(), n2.getX(), n2.getY(), road.getPlayerColour().toString());
	}
}
