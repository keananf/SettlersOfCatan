package connection;

import com.badlogic.gdx.Gdx;
import intergroup.Events;
import intergroup.Messages;

import java.io.IOException;
import java.net.Socket;

/**
 * Class representing a remote connection with a client
 * @author 140001596
 */
public class RemoteClientConnection implements IClientConnection
{
    private Socket conn;

    public RemoteClientConnection(Socket conn)
    {
        this.conn = conn;
    }

    @Override
    public void sendMessageToClient(Messages.Message message)
    {
        if(conn != null)
        {
            try
            {
                Events.Event ev = message.getEvent();
                message.writeDelimitedTo(conn.getOutputStream());
                Gdx.app.log("Event", String.format("Sent. %s", ev.getTypeCase().name()));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Messages.Message getMessageFromClient()
    {
        if(conn != null)
        {
            try
            {
                return Messages.Message.parseDelimitedFrom(conn.getInputStream());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void shutDown()
    {
        try
        {
            conn.close();
            conn = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
