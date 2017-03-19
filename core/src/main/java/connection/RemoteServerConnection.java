package connection;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
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
        if(conn != null)
        {
            try
            {
                CodedInputStream c = CodedInputStream.newInstance(conn.getInputStream());
                System.out.println("Receiving");
                Messages.Message m = Messages.Message.parseFrom(c.readByteArray());
                System.out.println("Received");
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
                CodedOutputStream c = CodedOutputStream.newInstance(conn.getOutputStream());
                message.writeTo(c);
                c.flush();
                conn.getOutputStream().flush();
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
