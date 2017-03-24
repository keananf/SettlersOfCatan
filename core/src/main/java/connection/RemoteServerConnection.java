package connection;

import client.Client;
import com.badlogic.gdx.Gdx;
import intergroup.Messages;

import java.io.IOException;
import java.net.Socket;

/**
 * Class representing a remote connection with a client
 * 
 * @author 140001596
 */
public class RemoteServerConnection implements IServerConnection
{
	private Socket conn;
	private Client client;

	public void connect(String host, int port, Client client) throws IOException
	{
		this.client = client;
		conn = new Socket(host, port);
	}

	@Override
	public Messages.Message getMessageFromServer() throws Exception
	{
		if (conn != null)
		{
			Messages.Message m = Messages.Message.parseDelimitedFrom(conn.getInputStream());
			return m;
		}

		return null;
	}

	@Override
	public void sendMessageToServer(Messages.Message message) throws Exception
	{
		if (conn != null)
		{
			message.writeDelimitedTo(conn.getOutputStream());
		}
	}

	@Override
	public void shutDown()
	{
		if (conn != null)
		{
			try
			{
				conn.close();
				conn = null;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
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

	public boolean isInitialised()
	{
		return conn.isConnected();
	}
}
