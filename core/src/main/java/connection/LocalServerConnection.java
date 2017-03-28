package connection;

import client.Client;
import com.badlogic.gdx.Gdx;
import intergroup.Messages;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class representing a local server connection
 * 
 * @author 140001596
 */
public class LocalServerConnection implements IServerConnection
{
	private final Client client;
	private LocalClientConnection conn;
	protected ConcurrentLinkedQueue<Messages.Message> fromServer;

	public LocalServerConnection(Client client)
	{
		this.client = client;
		fromServer = new ConcurrentLinkedQueue<Messages.Message>();
	}

	public void setConn(LocalClientConnection conn)
	{
		this.conn = conn;
	}

	@Override
	public Messages.Message getMessageFromServer()
	{
		// Block until message is in
		while (fromServer != null && fromServer.isEmpty())
		{
		}

		if (fromServer == null)
		{
			shutDown();
			return null;
		}

		Messages.Message m = fromServer.poll();
		return m;
	}

	@Override
	public void sendMessageToServer(Messages.Message message)
	{
		conn.fromClient.add(message);
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

	/**
	 * Logs the message depending on whether or not this is a local or remote
	 * server
	 * 
	 * @param tag the tag (for Gdx)
	 * @param msg the msg to log
	 */
	public void log(String tag, String msg)
	{
		if (Gdx.app == null)
		{
			System.out.println(msg);
		}
		else
			Gdx.app.log(tag, msg);
	}
}
