package connection;

import com.badlogic.gdx.Gdx;
import intergroup.Messages;

import java.io.IOException;
import java.net.Socket;

/**
 * Class representing a remote connection with a client
 * @author 140001596
 */
public class RemoteServerConnection implements IServerConnection
{
    private Socket conn;

    public void connect(String host, int port) throws IOException
    {
        conn = new Socket(host, port);
    }

    @Override
    public Messages.Message getMessageFromServer()
    {
        if(conn != null)
        {
            try
            {
                Messages.Message m = Messages.Message.parseDelimitedFrom(conn.getInputStream());
                Gdx.app.log("Event", String.format("Received %s", m.getEvent().getTypeCase().name()));
                return m;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void sendMessageToServer(Messages.Message message)
    {
        if(conn != null)
        {
            try
            {
                message.writeDelimitedTo(conn.getOutputStream());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
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

    public boolean isInitialised()
    {
        return conn.isConnected();
    }
}
