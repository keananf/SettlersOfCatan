package server;

import enums.Colour;
import intergroup.Messages.*;
import intergroup.Events.*;

import java.io.IOException;
import java.net.Socket;

/**
 * Class which simply listens to a socket. The successfully received message is
 * added to a ConcurrentLinkedQueue. Created by 140001596.
 */
public class ListenerThread implements Runnable
{
	private Thread thread;
	protected Socket socket;
	private Colour colour;
	private Server server;

    public ListenerThread(Socket socket, Colour c, Server server)
    {
        this.socket = socket;
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
            Message msg = Message.parseFrom(socket.getInputStream());
            server.addMessageToProcess(new ReceivedMessage(colour, msg));
		}
	}

    /**
     * If an unknown or invalid message is received, then this message sends an error back
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
        // Serialise and Send
        msg.writeTo(socket.getOutputStream());
        socket.getOutputStream().flush();
    }

    public void sendError() throws IOException
    {
        Message.Builder msg = Message.newBuilder();
        msg.setEvent(Event.newBuilder().setError(getError()).build());
        sendMessage(msg.build());
    }
}
