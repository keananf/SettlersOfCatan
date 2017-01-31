package exceptions;


public class InvalidCoordinatesException extends Exception
{
    private int x, y;

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
