package AI;

import connection.LocalClientConnection;
import connection.LocalServerConnection;
import enums.Difficulty;

/**
 * Class representing an AI client being run on the same machine as the server
 */
public class LocalAIClientOnServer extends AIClient implements Runnable
{
    private LocalServerConnection conn;
    private Thread thread;

    public LocalAIClientOnServer(Difficulty difficulty)
    {
        super(difficulty);
        setUpConnection();
        thread = new Thread(this);
        thread.start();
    }

    public LocalAIClientOnServer()
    {
        super();
        setUpConnection();
        thread = new Thread(this);
        thread.start();
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

    @Override
    public void run()
    {

    }
}