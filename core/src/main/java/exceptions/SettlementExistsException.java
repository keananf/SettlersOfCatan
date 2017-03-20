package exceptions;

import grid.*;
import game.build.Settlement;

@SuppressWarnings("serial")
public class SettlementExistsException extends Exception
{
	private Settlement settlement;

	public SettlementExistsException(Settlement settlement)
	{
		this.settlement = settlement;
	}

	public String getMessage()
	{
		Node n = settlement.getNode();

		return String.format("Settlement at node (%d,%d) already exists\n", n.getX(), n.getY());
	}
}
