package connection;

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

    public RemoteServerConnection(Socket conn)
    {
        this.conn = conn;
    }

    @Override
    public Messages.Message getMessageFromServer()
    {
        try
        {
            return Messages.Message.parseFrom(conn.getInputStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void sendMessageToServer(Messages.Message message)
    {
        try
        {
            message.writeTo(conn.getOutputStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
