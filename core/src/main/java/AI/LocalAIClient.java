package AI;

import connection.LocalClientConnection;
import connection.LocalServerConnection;
import enums.Difficulty;
import server.LocalServer;
import server.Server;

/**
 * Class representing an AI client being run on the same machine as the server
 */
public class LocalAIClient extends AIClient
{
    private LocalServerConnection conn;
    private Server server;
    private Thread serverThread;

    public LocalAIClient(Difficulty difficulty)
    {
        super(difficulty);
        setUpConnection();
    }
    public LocalAIClient()
    {
        super();
        setUpConnection();
    }

    @Override
    protected void setUpConnection()
    {
        conn = new LocalServerConnection(this);
        conn.setConn(new LocalClientConnection(conn));
        setUp(conn);
        server = new LocalServer(conn.getConn());
        serverThread = new Thread(server);
        serverThread.start();
    }

    @Override
    public void shutDown()
    {
        super.shutDown();
        server.shutDown();
        try
        {
            serverThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public Server getServer()
    {
        return server;
    }
}