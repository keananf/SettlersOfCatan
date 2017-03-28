package connection;

import com.badlogic.gdx.Gdx;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
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
			CodedOutputStream c = CodedOutputStream.newInstance(conn.getOutputStream());
			int size = message.getSerializedSize();
			c.writeUInt64NoTag(size);
			message.writeTo(c);
			c.flush();
			conn.getOutputStream().flush();
		}
	}

	@Override
	public Messages.Message getMessageFromClient() throws Exception
	{
		if (conn != null && (conn.isClosed() || !conn.isConnected()))
		{
			CodedInputStream c = CodedInputStream.newInstance(conn.getInputStream());
			ByteString b = c.readBytes();
			Messages.Message m = Messages.Message.parseFrom(b);
			return m;
		}

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
