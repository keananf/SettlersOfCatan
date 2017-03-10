package server;

import intergroup.Messages.Message;

/**
 * Created by 140001596 on 1/10/17.
 */
public class Logger
{
    public void logReceivedMessage(Message msg)
    {
        String str = String.format("RECEIVED: Message of type %s.\n", msg.getTypeCase().name());

        switch(msg.getTypeCase())
        {
            case REQUEST:
                str = String.format(str + "Type of Request: %s\n", msg.getRequest().getBodyCase().name());
                break;

            case EVENT:
                str = String.format(str + "Type of Event: %s. From player: %s\n", msg.getEvent().getTypeCase().name());
                break;
        }

        // TODO uncomment.
        //Gdx.app.debug("Server", str);
    }
}
