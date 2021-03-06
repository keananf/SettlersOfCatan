package connection;

import com.badlogic.gdx.Gdx;
import intergroup.Messages;

import java.io.IOException;
import java.net.Socket;

/**
 * Class representing a remote connection with a server
 * 
 * @author 140001596
 */
public class RemoteClientConnection implements IClientConnection
{
	private Socket conn;

	public RemoteClientConnection(Socket conn)
	{
		this.conn = conn;
	}

	@Override
	public void sendMessageToClient(Messages.Message message) throws Exception
	{
		if (conn != null)
		{
			message.writeDelimitedTo(conn.getOutputStream());
		}
	}

	@Override
	public Messages.Message getMessageFromClient() throws Exception
	{
		if (conn != null && (!conn.isClosed() && conn.isConnected())) { return Messages.Message
				.parseDelimitedFrom(conn.getInputStream()); }

		return null;
	}

	@Override
	public void shutDown()
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
