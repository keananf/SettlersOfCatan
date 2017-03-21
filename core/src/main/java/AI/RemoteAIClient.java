package AI;

import connection.RemoteServerConnection;
import enums.Difficulty;

import java.io.IOException;

/**
 * Client for an AI connection up to a remote server
 */
public class RemoteAIClient extends AIClient
{
    private String host;
    private RemoteServerConnection conn;

    public RemoteAIClient(Difficulty difficulty)
    {
        super(difficulty);
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
        return conn != null && conn.isInitialised();
    }

    /**
     * Attempts to establish a connection with the given host
     */
    protected void setUpConnection()
    {
        try
        {
            conn = new RemoteServerConnection();
            setUp(conn);
            conn.connect(host, PORT, this);
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
    }
}
