package exceptions;

import enums.Colour;
import enums.DevelopmentCardType;

@SuppressWarnings("serial")
public class DoesNotOwnException extends Exception
{
	private final DevelopmentCardType card;
	private final Colour player;

	public DoesNotOwnException(DevelopmentCardType d, Colour c)
	{
		player = c;
		card = d;
	}

	public String getMessage()
	{
		return String.format("Player %s cannot play %s", player.toString(), card.toString());
	}
}
