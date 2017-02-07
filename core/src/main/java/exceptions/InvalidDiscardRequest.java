package exceptions;


public class InvalidDiscardRequest extends Exception
{
    int before, after;

    public InvalidDiscardRequest(int oldAmount, int newAmount)
    {
        before = oldAmount;
        after = newAmount;
    }

    @Override
    public String getMessage()
    {
        return String.format("Invalid discard request. Player needs 7 or less resources.\n" +
                "Before: %d. After: %d", before, after);
    }
}
