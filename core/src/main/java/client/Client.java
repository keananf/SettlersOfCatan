package client;

import connection.IServerConnection;

/**
 * Abstract notion of a client
 * @author 140001596
 */
public abstract class Client
{
    protected ClientGame state;
    protected EventProcessor eventProcessor;
    protected Thread evProcessor;
    protected TurnProcessor turnProcessor;
    protected MoveProcessor moveProcessor;
    protected static final int PORT = 12345;
    private TurnInProgress turn;
    private IServerConnection conn;


    /**
     * Attempts to set up a connection with the server
     */
    protected abstract void setUpConnection();

    public void sendTurn()
    {
        turnProcessor.sendMove();
    }

    /**
     * Sets up the different components for a RemoteClient
     */
    protected void setUp(IServerConnection conn)
    {
        this.conn = conn;
        this.turn = new TurnInProgress();
        this.turnProcessor = new TurnProcessor(conn, this);
        this.moveProcessor = new MoveProcessor(this);
        this.eventProcessor = new EventProcessor(conn, this);

        evProcessor = new Thread(eventProcessor);
        evProcessor.start();
    }


    /**
     * Shuts down a client by terminating the socket and the event processor thread.
     */
    public void shutDown()
    {
        conn.shutDown();
        try
        {
            evProcessor.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public ClientGame getState()
    {
        return state;
    }

    public TurnInProgress getTurn()
    {
        return turn;
    }

    public MoveProcessor getMoveProcessor()
    {
        return moveProcessor;
    }
}
