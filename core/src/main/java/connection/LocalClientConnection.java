package connection;

import intergroup.Messages;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class representing a local connection with a client
 * 
 * @author 140001596
 */
public class LocalClientConnection implements IClientConnection
{
	private LocalServerConnection conn;
	protected BlockingQueue<Messages.Message> fromClient;

	public LocalClientConnection(LocalServerConnection conn)
	{
		this.conn = conn;
		fromClient = new LinkedBlockingQueue<Messages.Message>();
	}

	@Override
	public void sendMessageToClient(Messages.Message message)
	{
		if (conn == null || conn.fromServer == null)
		{
			shutDown();
			return;
		}

		try
		{
			conn.fromServer.put(message);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Messages.Message getMessageFromClient()
	{
		if (fromClient == null) return null;

		try
		{
			return fromClient.take();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void shutDown()
	{
		conn = null;
		fromClient = null;
	}
}
