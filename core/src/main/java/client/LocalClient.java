package client;

import catan.SettlersOfCatan;
import connection.LocalClientConnection;
import connection.LocalServerConnection;
import server.LocalServer;
import server.Server;

/**
 * Class representing a client being run on the same machine as the server
 * 
 * @author 140001596
 */
public class LocalClient extends Client
{
	private final int numAis;
	private Server server;
	private Thread serverThread;

	public LocalClient(SettlersOfCatan game, String userName, int numAIs)
	{
		super(game, userName);
		this.numAis = numAIs;
		setUpConnection();
	}

	protected void setUpConnection()
	{
		LocalServerConnection conn = new LocalServerConnection();
		conn.setConn(new LocalClientConnection(conn));
		setUp(conn);
		server = new LocalServer(conn.getConn(), numAis);
		serverThread = new Thread(server);
		serverThread.start();
	}

	@Override
	public void shutDown()
	{
		server.terminate();
		try
		{
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		super.shutDown();
	}

	public Server getServer()
	{
		return server;
	}
}
