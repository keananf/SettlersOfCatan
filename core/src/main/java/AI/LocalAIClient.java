package AI;

import catan.SettlersOfCatan;
import connection.LocalClientConnection;
import connection.LocalServerConnection;
import enums.Difficulty;
import server.LocalServer;
import server.Server;

/**
 * Class representing an ai client being run on the same machine as the server
 */
public class LocalAIClient extends AIClient
{
	private final int numAis;
	private final Difficulty opponentDifficulty;
	private Server server;
	private Thread serverThread;

	public LocalAIClient(Difficulty difficulty, Difficulty opponentDifficulty, SettlersOfCatan game, String userName,
			int numAIs)
	{
		super(difficulty, userName, game);
		this.opponentDifficulty = opponentDifficulty;
		this.numAis = numAIs;
		setUpConnection();
	}

	@Override
	protected void setUpConnection()
	{
		LocalServerConnection conn = new LocalServerConnection();
		conn.setConn(new LocalClientConnection(conn));
		setUp(conn);
		server = new LocalServer(conn.getConn(), numAis, opponentDifficulty);
		serverThread = new Thread(server);
		serverThread.start();
	}

	@Override
	public void shutDown()
	{
		super.shutDown();
		server.terminate();
		try
		{
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public Server getServer()
	{
		return server;
	}
}