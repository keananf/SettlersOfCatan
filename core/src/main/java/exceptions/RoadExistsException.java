package exceptions;

import grid.*;
import game.build.Road;

@SuppressWarnings("serial")
public class RoadExistsException extends Exception
{
	private Road road;

	public RoadExistsException(Road r)
	{
		road = r;
	}

	public String getMessage()
	{
		Node n1 = road.getEdge().getX(), n2 = road.getEdge().getY();

		return String.format("Road from node (%d,%d) to node (%d,%d) already exists\n", n1.getX(), n1.getY(), n2.getX(),
				n2.getY());
	}
}
