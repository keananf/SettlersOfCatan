package exceptions;

import enums.Colour;

@SuppressWarnings("serial")
public class CannotStealException extends Exception
{
	private final Colour taker;
	private final Colour other;

	public CannotStealException(Colour taker, Colour other)
	{
		this.taker = taker;
		this.other = other;
	}

	public String getMessage()
	{
		return String.format("Player %s cannot take resource from %s.", taker.toString(), other.toString());

	}

}
