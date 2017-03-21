package client;

import com.badlogic.gdx.Gdx;
import connection.IServerConnection;

import java.util.concurrent.Semaphore;

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
    private Turn turn;
    private IServerConnection conn;
    private Semaphore stateLock, turnLock;


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
        this.stateLock = new Semaphore(1);
        this.turnLock = new Semaphore(1);
        this.turn = new Turn();
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

    /**
     * Logs the given message
     * @param tag the tag (for Gdx)
     * @param msg the message to log
     */
    public void log(String tag, String msg)
    {
        if(Gdx.app != null)
        {
            Gdx.app.log(tag, msg);
        }
        else System.out.println(msg);
    }

    public ClientGame getState()
    {
        return state;
    }

    public Turn getTurn()
    {
        return turn;
    }

    public MoveProcessor getMoveProcessor()
    {
        return moveProcessor;
    }

    public void setGame(ClientGame game)
    {
        this.state = game;
    }

    public Semaphore getStateLock()
    {
        return stateLock;
    }

    public Semaphore getTurnLock()
    {
        return turnLock;
    }
}
