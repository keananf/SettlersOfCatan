package client;

import java.io.IOException;
import java.net.Socket;

/**
 * Main client class
 * 
 * @author 140001596
 */
public class Client
{
	private ClientGame state;
	private Socket socket;
	private EventProcessor eventProcessor;
	private TurnProcessor turnProcessor;
	private static final int PORT = 12345;

	public Client()
	{
		// TODO: delete once front-end properly set up with server.
		setUp();
		// Right now, Socket is null
	}

	/**
	 * Attempts to establish a connection with the given host
	 * 
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
		this.eventProcessor = new EventProcessor(socket, state);
		this.turnProcessor = new TurnProcessor(socket, state);
	}

	public ClientGame getState()
	{
		return state;
	}

	public Turn getTurn()
	{
		return turnProcessor.getTurn();
	}
}
