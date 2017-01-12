package server;

import java.io.*;
import java.util.*;

import enums.Colour;
import protocol.RequestProtos.*;
import protocol.MessageProtos.*;

/**
 * Created by 140001596 on 1/10/17.
 */
public class Logger
{
    private static final String logFile = "log/log.txt";
    private BufferedWriter writer;

    public Logger()
    {
        try
        {
            writer = new BufferedWriter((new FileWriter(logFile)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


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

        try
        {
            writer.write(str);
            writer.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}
