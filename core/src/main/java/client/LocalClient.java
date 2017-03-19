package client;

import connection.LocalClientConnection;
import connection.LocalServerConnection;
import server.LocalServer;
import server.Server;

/**
 * Class representing a client being run on the same machine as the server
 * @author 140001596
 */
public class LocalClient extends Client
{
    private LocalServerConnection conn;
    private Server server;
    private Thread serverThread;

    public LocalClient()
    {
        setUpConnection();
    }

    @Override
    protected void setUpConnection()
    {
        conn = new LocalServerConnection();
        conn.setConn(new LocalClientConnection(conn));
        server = new LocalServer(conn.getConn());
        serverThread = new Thread(server);
        serverThread.start();


        // TODO eliminate:
        state = new ClientGame();
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
