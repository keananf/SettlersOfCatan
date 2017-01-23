package exceptions;

import protocol.MessageProtos;
import protocol.MessageProtos.*;

public class UnexpectedMoveTypeException extends Exception
{
    private Message msg;

    public UnexpectedMoveTypeException(Message msg)
    {
        this.msg = msg;
    }

    @Override
    public String getMessage()
    {
        String str = "";
        switch(msg.getTypeCase())
        {
            case EVENT:
                str = "Event";
                break;
            case REQUEST:
                str = "Request: " + msg.getRequest().getTypeCase().name();
                break;
            case RESPONSE:
                str = "Response";
                break;
        }

        return String.format("Unexpected Message Type: %s", str);
    }

    public Message getOriginalMessage()
    {
        return msg;
    }
}