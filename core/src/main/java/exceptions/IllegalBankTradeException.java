package exceptions;

import enums.Colour;

/**
 * Created by 1400001596 on 1/10/17.
 */
@SuppressWarnings("serial")
public class IllegalBankTradeException extends Exception
{
	private final Colour offerer;

	public IllegalBankTradeException(Colour offerer)
	{
		this.offerer = offerer;
	}

	@Override
	public String getMessage()
	{
		return String.format("Cannot complete the requested trade between player %s and the bank, "
				+ "as resources are inconsistent", offerer.toString());
	}
}
