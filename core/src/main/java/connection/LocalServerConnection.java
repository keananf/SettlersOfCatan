package connection;

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
	private LocalClientConnection conn;
	protected BlockingQueue<Messages.Message> fromServer;

	public LocalServerConnection()
	{
		fromServer = new LinkedBlockingQueue<>();
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
		catch (InterruptedException ignored)
		{
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
		catch (InterruptedException ignored)
		{
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
