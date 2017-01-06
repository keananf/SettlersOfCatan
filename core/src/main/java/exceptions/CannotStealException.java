package main.java.exceptions;

import main.java.enums.Colour;

@SuppressWarnings("serial")
public class CannotStealException extends Exception
{
	private Colour taker, other;

	public CannotStealException(Colour taker, Colour other)
	{
		this.taker = taker;
		this.other = other;
	}
	
	public String getMessage()
	{
		return String.format("Player %s cannot take resource from %s.", 
				taker.toString(), other.toString());
		
	}

}