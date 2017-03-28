package exceptions;

public class CannotPlayException extends Exception
{
	@Override
	public String getMessage()
	{
		return String.format("Cannot play Development card that you purchased this turn.");
	}
}
