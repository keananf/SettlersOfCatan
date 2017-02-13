package exceptions;

import board.Port;
import enums.Colour;

public class IllegalPortTradeException extends Exception
{
	private Colour offerer;
	private Port port;

	public IllegalPortTradeException(Colour offerer, Port port)
	{
		this.offerer = offerer;
		this.port = port;
	}

	@Override
	public String getMessage()
	{
		return String.format("Cannot complete the requested trade between player %s and port %s", offerer.toString(),
				port.toString());
	}
}
