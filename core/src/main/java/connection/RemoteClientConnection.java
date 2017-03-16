package connection;

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
        try
        {
            message.writeTo(conn.getOutputStream());
            conn.getOutputStream().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Messages.Message getMessageFromClient()
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
}
