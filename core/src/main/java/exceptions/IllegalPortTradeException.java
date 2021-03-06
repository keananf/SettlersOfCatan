package exceptions;

import grid.Port;
import enums.Colour;

@SuppressWarnings("serial")
public class IllegalPortTradeException extends Exception
{
	private final Colour offerer;
	private final Port port;

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
