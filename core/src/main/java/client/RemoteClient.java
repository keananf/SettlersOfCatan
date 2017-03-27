package client;

import catan.SettlersOfCatan;
import connection.RemoteServerConnection;

import java.io.IOException;

/**
 * Main client class
 * 
 * @author 140001596
 */
public class RemoteClient extends Client
{
	private String host;
	private RemoteServerConnection conn;

	public RemoteClient(String host)
	{
		super();
		this.host = host;
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
			setUp(conn);
			conn.connect(host, PORT, this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void shutDown()
	{
		super.shutDown();
	}
}
