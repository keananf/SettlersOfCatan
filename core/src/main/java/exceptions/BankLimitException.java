package exceptions;

/**
 * @author 140001596
 */
public class BankLimitException extends Exception
{
	private String msg = "";

	public BankLimitException(String format)
	{
		msg = format;
	}

	@Override
	public String getMessage()
	{
		return msg;
	}
}
