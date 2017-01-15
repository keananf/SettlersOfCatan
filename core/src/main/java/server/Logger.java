package server;

import com.badlogic.gdx.Gdx;

import enums.Colour;
import protocol.MessageProtos.Message;

/**
 * Created by 140001596 on 1/10/17.
 */
public class Logger
{
    public void logReceivedMessage(Message msg)
    {
        Colour col = Colour.fromProto(msg.getPlayerColour());
        String str = String.format("RECEIVED: Message of type %s from player %s.\n", msg.getTypeCase().name(), col.toString());

        switch(msg.getTypeCase())
        {
            case REQUEST:
                str = String.format(str + "Type of Request:  %s", msg.getRequest().getTypeCase().name());
                break;

            case EVENT:
                str = String.format(str + "Type of Event:  %s", msg.getEvent().getTypeCase().name());
                break;

            case RESPONSE:
                str = String.format(str + "Type of Response:  %s", msg.getResponse().getTypeCase().name());
                break;
        }

        Gdx.app.debug("Server", str);
    }

}
