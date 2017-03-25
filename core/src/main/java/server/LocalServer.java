package server;

import AI.LocalAIClientOnServer;
import connection.LocalClientConnection;
import enums.Colour;
import exceptions.GameFullException;
import game.players.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a locally hosted server
 * 
 * @author 140001596
 */
public class LocalServer extends Server
{
	private Player localPlayer;
	private List<LocalAIClientOnServer> ais;

	public LocalServer(LocalClientConnection connection)
	{
		super();
		ais = new ArrayList<LocalAIClientOnServer>();
		ServerGame.NUM_PLAYERS = 4;
		Colour c = joinGame(connection);
		localPlayer = game.getPlayer(c);
		addAIs(3);
	}

	public LocalServer()
	{
		super();
		ais = new ArrayList<LocalAIClientOnServer>();
		ServerGame.NUM_PLAYERS = 4;
		addAIs(4);
		localPlayer = game.getPlayers().get(Colour.BLUE);
	}

	public static void main(String[] args)
	{
		LocalServer s = new LocalServer();
		Thread t = new Thread(s);
		t.start();
	}

	/**
	 * Adds the given number of local ai players
	 * 
	 * @param num the number of AIs to add
	 */
	private void addAIs(int num)
	{
		// Add 'num' AIs
		for (int i = 0; i < num; i++)
		{
			// Don't add more if game is full
			if (ServerGame.NUM_PLAYERS == numConnections)
			{
				break;
			}

			LocalAIClientOnServer ai = new LocalAIClientOnServer();
			LocalClientConnection conn = ai.getConn().getConn();
			joinGame(conn);
			ais.add(ai);
		}
		log("Server SetUp", String.format("Number of AIs: %d. Connections: %d", num, numConnections));
	}

	/**
	 * Adds the given local connection to the game
	 * 
	 * @param connection
	 */
	private Colour joinGame(LocalClientConnection connection)
	{
		Colour c = null;
		try
		{
			c = game.joinGame();
		}
		catch (GameFullException e)
		{
		}
		connections.put(c, new ListenerThread(connection, c, this));
		log("Server Setup", String.format("Player %d connected", numConnections));
		numConnections++;

		return c;
	}
}
