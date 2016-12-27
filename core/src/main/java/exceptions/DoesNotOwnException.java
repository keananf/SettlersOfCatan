package main.java.exceptions;

import main.java.game.build.*;

@SuppressWarnings("serial")
public class DoesNotOwnException extends Exception
{
	private DevelopmentCard card;
	
	public DoesNotOwnException(DevelopmentCard d)
	{
		card = d;
	}

	public String getMessage()
	{
		return String.format("Cannot play %s", 
				card.toString(), card.getColour().toString());
	}
}
