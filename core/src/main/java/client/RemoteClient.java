package client;

import connection.RemoteServerConnection;

import java.io.IOException;
import java.net.Socket;

/**
 * Main client class
 * @author 140001596
 */
public class RemoteClient extends Client
{
    private Socket socket;
    private String host;

    public RemoteClient(String host)
    {
        this.host = host;
        setUpConnection();
    }

    /**
     * Attempts to connect to the given string
     * @param host the host
     * @return if connected or not
     */
    public boolean tryAgain(String host)
    {
        this.host = host;
        setUpConnection();

        return isInitialised();
    }

    /**
     * @return if this client is connected or not
     */
    public boolean isInitialised()
    {
        return socket != null && socket.isConnected();
    }

    /**
     * Attempts to establish a connection with the given host
     */
    protected void setUpConnection()
    {
        try
        {
            socket = new Socket(host, PORT);
            setUp(new RemoteServerConnection(socket));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void shutDown()
    {
        super.shutDown();
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
