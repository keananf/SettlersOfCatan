package exceptions;

@SuppressWarnings("serial")
public class CannotUpgradeException extends Exception
{
	private final int x;
	private final int y;

	public CannotUpgradeException(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public String getMessage()
	{
		return String.format("Cannot build City. No settlement located at (%d, %d)", x, y);
	}

}
