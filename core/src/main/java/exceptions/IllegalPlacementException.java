package exceptions;

import grid.*;
import game.build.Settlement;

@SuppressWarnings("serial")
public class IllegalPlacementException extends Exception
{
	private Settlement settlement;

	public IllegalPlacementException(Settlement s)
	{
		settlement = s;
	}

	public String getMessage()
	{
		Node n = settlement.getNode();

		return String.format(
				"%s player cannot build settlement on node (%d,%d) due to it being within two nodes of another settlement",
				settlement.getPlayerColour().toString(), n.getX(), n.getY());
	}
}
