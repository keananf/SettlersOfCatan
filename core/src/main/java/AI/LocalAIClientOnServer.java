package AI;

import connection.LocalClientConnection;
import connection.LocalServerConnection;
import enums.Difficulty;

/**
 * Class representing an ai client being run on the same machine as the server
 */
public class LocalAIClientOnServer extends AIClient
{
    private LocalServerConnection conn;

    public LocalAIClientOnServer(Difficulty difficulty)
    {
        super(difficulty);
    }

    public LocalAIClientOnServer()
    {
        super();
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