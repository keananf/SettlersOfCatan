package AI;

import catan.SettlersOfCatan;
import connection.RemoteServerConnection;
import enums.Difficulty;

import java.io.IOException;

/**
 * Client for an ai connection up to a remote server
 */
public class RemoteAIClient extends AIClient
{
	private String host;
	private RemoteServerConnection conn;

	public RemoteAIClient(String host, Difficulty difficulty, String userName, SettlersOfCatan game)
	{
		super(difficulty, userName, game);
		this.host = host;
		setUpConnection();
	}

	/**
	 * Attempts to connect to the given string
	 * 
	 * @param host the host
	 * @return if connected or not
	 */
	public boolean tryAgain(String host)
	{
		this.host = host;
		setUpConnection();

		return isInitialised();
	}

	/**
	 * @return if this client is connected or not
	 */
	public boolean isInitialised()
	{
		return conn != null && conn.isInitialised();
	}

	/**
	 * Attempts to establish a connection with the given host
	 */
	protected void setUpConnection()
	{
		try
		{
			conn = new RemoteServerConnection();
			conn.connect(host, PORT);
			if (conn.isInitialised()) log("Client Set-Up", String.format("Connected to: %s", host));
			setUp(conn);
		}
		catch (IOException e)
		{
			log("Client Set-Up", String.format("Could not connect to: %s", host));
		}
	}

}
