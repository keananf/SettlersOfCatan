package connection;

import client.Client;
import intergroup.Messages;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class representing a local server connection
 * 
 * @author 140001596
 */
public class LocalServerConnection implements IServerConnection
{
	private final Client client;
	private LocalClientConnection conn;
	protected BlockingQueue<Messages.Message> fromServer;

	public LocalServerConnection(Client client)
	{
		this.client = client;
		fromServer = new LinkedBlockingQueue<Messages.Message>();
	}

	public void setConn(LocalClientConnection conn)
	{
		this.conn = conn;
	}

	@Override
	public Messages.Message getMessageFromServer()
	{
		if (fromServer == null)
		{
			shutDown();
			return null;
		}

		try
		{
			return fromServer.take();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void sendMessageToServer(Messages.Message message)
	{
		if (conn == null || conn.fromClient == null)
		{
			shutDown();
			return;
		}

		try
		{
			conn.fromClient.put(message);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void shutDown()
	{
		conn = null;
		fromServer = null;
	}

	public LocalClientConnection getConn()
	{
		return conn;
	}
}
