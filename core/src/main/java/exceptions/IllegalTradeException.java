package exceptions;

import enums.Colour;

@SuppressWarnings("serial")
public class IllegalTradeException extends Exception
{
	private final Colour offerer;
	private final Colour recipient;

	public IllegalTradeException(Colour offerer, Colour recipient)
	{
		this.offerer = offerer;
		this.recipient = recipient;
	}

	@Override
	public String getMessage()
	{
		return String.format("Cannot complete the requested trade between players %s and %s", offerer.toString(),
				recipient.toString());
	}
}
