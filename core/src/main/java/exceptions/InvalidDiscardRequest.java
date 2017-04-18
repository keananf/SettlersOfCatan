package exceptions;

@SuppressWarnings("serial")
public class InvalidDiscardRequest extends Exception
{
	private final int before;
	private final int after;

	public InvalidDiscardRequest(int oldAmount, int newAmount)
	{
		before = oldAmount;
		after = newAmount;
	}

	@Override
	public String getMessage()
	{
		return String.format("Invalid discard request. Player needs to eliminate half of their resources.%n"
				+ "Before: %d. After: %d", before, after);
	}
}
