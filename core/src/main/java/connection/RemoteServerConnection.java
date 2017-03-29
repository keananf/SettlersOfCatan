package connection;

import client.Client;
import com.badlogic.gdx.Gdx;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
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
			CodedInputStream c = CodedInputStream.newInstance(conn.getInputStream());
			ByteString b = c.readBytes();
			Messages.Message m = Messages.Message.parseFrom(b);
			return m;
		}

		return null;
	}

	@Override
	public void sendMessageToServer(Messages.Message message) throws Exception
	{
		if (conn != null)
		{
			CodedOutputStream c = CodedOutputStream.newInstance(conn.getOutputStream());
			int size = message.getSerializedSize();
			c.writeUInt64NoTag(size);
			message.writeTo(c);
			c.flush();
			conn.getOutputStream().flush();
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
		return conn != null && conn.isConnected();
	}
}
