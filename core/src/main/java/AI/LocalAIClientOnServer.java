package AI;

import connection.LocalClientConnection;
import connection.LocalServerConnection;
import enums.Difficulty;

/**
 * Class representing an ai client being run on the same machine as the server
 */
public class LocalAIClientOnServer extends AIClient
{
	private LocalServerConnection conn;

	public LocalAIClientOnServer(Difficulty difficulty)
	{
		super(difficulty);
		setUpConnection();
	}

	public LocalAIClientOnServer()
	{
		super();
		setUpConnection();
	}

	@Override
	protected void setUpConnection()
	{
		conn = new LocalServerConnection();
		conn.setConn(new LocalClientConnection(conn));
		setUp(conn);
	}

	public LocalServerConnection getConn()
	{
		return conn;
	}
}