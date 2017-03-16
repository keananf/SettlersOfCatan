package client;

import java.io.IOException;
import java.net.Socket;

/**
 * Main client class
 * @author 140001596
 */
public class Client
{
    private ClientGame state;
    private Socket socket;
    private EventProcessor eventProcessor;
    private Thread evProcessor;
    private TurnProcessor turnProcessor;
    private MoveProcessor moveProcessor;
    private static final int PORT = 12345;

    public Client()
    {
        // TODO: delete once front-end properly set up with server.
        setUp();
        // Right now, Socket is null
    }

    /**
     * Attempts to establish a connection with the given host
     * @param host the host
     */
    public void setUpConnection(String host)
    {
        try
        {
            socket = new Socket(host, PORT);
            setUp();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the different components for a Client
     */
    private void setUp()
    {
        this.state = new ClientGame();
        this.moveProcessor = new MoveProcessor(state);
        this.eventProcessor = new EventProcessor(socket, state);
        this.turnProcessor = new TurnProcessor(socket, state);

        evProcessor = new Thread(eventProcessor);
        evProcessor.start();
    }

    /**
     * Shuts down a client by terminating the socket and the event processor thread.
     */
    private void shutDown()
    {
        try
        {
            evProcessor.join();
            socket.close();
        }
        catch (IOException | InterruptedException e)
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
        return turnProcessor.getTurn();
    }
}
