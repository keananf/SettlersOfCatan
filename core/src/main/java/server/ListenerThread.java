package server;

import connection.IClientConnection;
import enums.Colour;
import intergroup.Messages.Message;

import java.io.IOException;

/**
 * Class which simply listens to a socket. The successfully received message is
 * added to a ConcurrentLinkedQueue. Created by 140001596.
 */
public class ListenerThread implements Runnable
{
	protected IClientConnection conn;
	private Colour colour;
	private Server server;

	public ListenerThread(IClientConnection conn, Colour c, Server server)
	{
		this.conn = conn;
		this.server = server;
		colour = c;
	}

	@Override
	public void run()
	{
		// Continually poll for new messages
		try
		{
			receiveMoves();
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
			// TODO replace 'conn' with a LocalClientConnection to a
			// LocalAIClient
			conn = null;
			// e.printStackTrace();
		}
	}

	/**
	 * Listens for moves from the current player
	 * 
	 * @return the bytes received from the current player
	 * @throws IOException
	 */
	private void receiveMoves() throws Exception
	{
		// Receive and process moves until the end one is received
		while (true)
		{
			if (conn == null)
			{
				break;
			}

			// Parse message and add to queue
			Message msg = conn.getMessageFromClient();
			server.addMessageToProcess(new ReceivedMessage(colour, msg));
		}
	}

	/**
	 * Sends the message out to the client
	 * 
	 * @param msg the message
	 * @throws IOException
	 */
	public void sendMessage(Message msg) throws Exception
	{
		if (conn != null)
		{
			conn.sendMessageToClient(msg);
		}
	}

	/**
	 * Terminates the underlying connection and this thread
	 */
	public void shutDown()
	{
		conn.shutDown();
	}

	public Colour getColour()
	{
		return colour;
	}
}
