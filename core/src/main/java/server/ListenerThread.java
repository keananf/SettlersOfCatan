package server;

import connection.IClientConnection;
import enums.Colour;
import intergroup.Events.ErrorCause;
import intergroup.Events.Event;
import intergroup.Messages.Message;

import java.io.IOException;

/**
 * Class which simply listens to a socket. The successfully received message is
 * added to a ConcurrentLinkedQueue. Created by 140001596.
 */
public class ListenerThread implements Runnable
{
	private Thread thread;
	protected IClientConnection conn;
	private Colour colour;
	private Server server;

    public ListenerThread(IClientConnection conn, Colour c, Server server)
    {
        this.conn = conn;
        this.server = server;
        colour = c;
        this.thread = new Thread(this);
        this.thread.start();
    }

	@Override
	public void run()
	{
		// Continually poll for new messages
		try
		{
			receiveMoves();
		}
		catch (IOException e)
		{
			// TODO replace with AI
			e.printStackTrace();
		}
	}

    /**
     * Listens for moves from the current player
     * @return the bytes received from the current player
     * @throws IOException
     */
    private void receiveMoves() throws IOException
    {
        // Receive and process moves until the end one is received
        while(true)
        {
            // Parse message and add to queue
            Message msg = conn.getMessageFromClient();
            server.addMessageToProcess(new ReceivedMessage(colour, msg));
		}
	}

	/**
	 * If an unknown or invalid message is received, then this message sends an
	 * error back
	 */
	protected Event.Error getError() throws IOException
	{
		// Set up result message
		Event.Error.Builder err = Event.Error.newBuilder();
		err.setDescription("Invalid message type");
		err.setCause(ErrorCause.UNRECOGNIZED);

		return err.build();
	}

    /**
     * Sends the message out to the client
     * @param msg the message
     * @throws IOException
     */
    public void sendMessage(Message msg) throws IOException
    {
        conn.sendMessageToClient(msg);
    }

	public void sendError() throws IOException
	{
		Message.Builder msg = Message.newBuilder();
		msg.setEvent(Event.newBuilder().setError(getError()).build());
		sendMessage(msg.build());
	}

	/**
	 * Terminates the underlying connection and this thread
	 */
	public void shutDown()
	{
		conn.shutDown();
		try
		{
			thread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
