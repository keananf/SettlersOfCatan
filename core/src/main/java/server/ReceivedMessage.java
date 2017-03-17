package server;

import enums.Colour;
import intergroup.Messages;

/**
 * @author 140001596
 */
public class ReceivedMessage
{
    private Colour col;
    private Messages.Message msg;

    public ReceivedMessage(Colour col, Messages.Message msg)
    {
        this.col = col;
        this.msg = msg;
    }

    public Messages.Message getMsg()
    {
        return msg;
    }

    public Colour getCol()
    {
        return col;
    }
}
