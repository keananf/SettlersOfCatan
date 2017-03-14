package exceptions;

public class GameFullException extends Exception
{
    @Override
    public String getMessage()
    {
        return "Game is full. Cannot join.";
    }
}
