package AI;

import connection.LocalClientConnection;
import connection.LocalServerConnection;

/**
 * Class representing an AI client being run on the same machine as the server
 */
public class LocalAIClientOnServer extends LocalAIClient
{
    private LocalServerConnection conn;

    public LocalAIClientOnServer()
    {
        setUpConnection();
    }

    @Override
    protected void setUpConnection()
    {
        conn = new LocalServerConnection(this);
        conn.setConn(new LocalClientConnection(conn));
        setUp(conn);
    }

    @Override
    public void shutDown()
    {
        super.shutDown();
    }

    public LocalServerConnection getConn()
    {
        return conn;
    }
}