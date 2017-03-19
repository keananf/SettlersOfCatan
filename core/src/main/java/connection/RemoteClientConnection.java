package connection;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
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
                CodedOutputStream c = CodedOutputStream.newInstance(conn.getOutputStream());
                message.writeTo(c);

                c.flush();
                conn.getOutputStream().flush();
                System.out.println(String.format("Sent. %s", ev.getTypeCase().name()));
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
                CodedInputStream c = CodedInputStream.newInstance(conn.getInputStream());
                return Messages.Message.parseFrom(c);
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
