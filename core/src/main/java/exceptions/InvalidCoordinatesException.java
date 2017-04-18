package exceptions;

@SuppressWarnings("serial")
public class InvalidCoordinatesException extends Exception
{
	private final int x;
	private final int y;

	public InvalidCoordinatesException(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public String getMessage()
	{
		return String.format("Invalid coordinates: (%d, %d)", x, y);
	}
}
