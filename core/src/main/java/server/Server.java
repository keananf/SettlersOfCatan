package server;

import AI.AIClient;
import AI.LocalAIClientOnServer;
import com.badlogic.gdx.Gdx;
import connection.LocalClientConnection;
import connection.RemoteClientConnection;
import enums.Colour;
import exceptions.GameFullException;
import game.Game;
import game.players.Player;
import intergroup.EmptyOuterClass;
import intergroup.Events;
import intergroup.Events.Event;
import intergroup.Messages;
import intergroup.Messages.Message;
import intergroup.Requests;
import intergroup.Requests.Request;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server implements Runnable
{
	protected MessageProcessor msgProc;
	protected ServerGame game;
	protected int numConnections;
	protected Map<Colour, ListenerThread> connections;
	protected ServerSocket serverSocket;
	protected Map<Colour, Thread> aiThreads, threads;
	protected Map<Colour, AIClient> ais;
	protected static final int PORT = 12345;

	public Server()
	{
		ais = new HashMap<Colour, AIClient>();
		aiThreads = new HashMap<Colour, Thread>();
		threads = new HashMap<Colour, Thread>();
		game = new ServerGame();
		Game.NUM_PLAYERS = 2;

		// Set up
		msgProc = new MessageProcessor(game, this);
		connections = new HashMap<Colour, ListenerThread>();
	}

	public void run()
	{
		try
		{
			getPlayers();
			game.chooseFirstPlayer();
			waitForJoinLobby();
			broadcastBoard();
			getInitialSettlementsAndRoads();

			Thread.sleep(500);
			log("Server Start", "\n\nAll players Connected. Beginning play.\n");
			while (!game.isOver())
			{
				// Read moves from queue and log
				processMessage();
				sleep();
			}
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
			log("Server Setup", "Error connecting players");
		}

		shutDown();
	}

	public static void main(String[] args)
	{
		Server s = new Server();
		Thread t = new Thread(s);
		t.start();
	}

	/**
	 * Shuts down all individual connections, and then shut down
	 */
	public void shutDown()
	{
		// Shut down all individual connections
		for (Colour c : connections.keySet())
		{
			try
			{
				// Instruct AI thread to terminate, then wait
				if (ais.containsKey(c) && aiThreads.containsKey(c))
				{
					ais.get(c).shutDown();
					aiThreads.get(c).join();
				}

				// Instruct ListenerThread to terminate, then wait
				connections.get(c).shutDown();
				threads.get(c).join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Process the next message, and send any responses and events.
	 * 
	 * @throws IOException
	 */
	public void processMessage() throws IOException
	{
		Event ev = msgProc.processMessage();

		if ((ev == null || !ev.isInitialized()) && msgProc.getLastMessage() != null)
		{
			replacePlayerWithAI(msgProc.getLastMessage().getCol());
		}
		else
		{
			sendEvents(ev);
		}
	}

	/**
	 * Broadcast the necessary events to all players based upon the type of
	 * event.
	 * 
	 * @param event the event from the last processed move
	 */
	protected void sendEvents(Event event) throws IOException
	{
		if (event == null) return;

		// Switch on message type to interpret which event(s) need to be sent
		// out
		switch (event.getTypeCase())
		{
		// These events need to be propagated to everyone
		case DEVCARDPLAYED:
		case DEVCARDBOUGHT:
		case SETTLEMENTBUILT:
		case TURNENDED:
		case ROLLED:
		case GAMEWON:
		case ROBBERMOVED:
		case CITYBUILT:
		case ROADBUILT:
		case BANKTRADE:
		case PLAYERTRADE:
		case LOBBYUPDATE:
		case CHATMESSAGE:
		case RESOURCESTOLEN:
		case RESOURCECHOSEN:
		case MONOPOLYRESOLUTION:
		case CARDSDISCARDED:
			broadcastEvent(event);
			break;

		// Sent individually, so ignore
		case BEGINGAME:
			break;

		// Send back to original player only
		case ERROR:
			sendError(event, game.getPlayer(event.getInstigator().getId()).getColour());
			break;
		}
	}

	/**
	 * Serialises and Broadcasts the board to each connected player
	 * 
	 * @throws IOException
	 */
	private void broadcastBoard() throws IOException
	{
		// Set up message
		Message.Builder msg = Message.newBuilder();
		Event.Builder ev = Event.newBuilder();

		// For each player
		for (Colour c : Colour.values())
		{
			// Set up the board and the info indicating which player
			if (connections.containsKey(c))
			{
				Lobby.GameSetup board = game.getGameSettings(c);
				ev.setBeginGame(board);
				ev.setInstigator(Board.Player.newBuilder().setId(game.getPlayer(c).getId()));
				msg.setEvent(ev.build());

				sendMessage(msg.build(), c);
			}
		}
	}

	/**
	 * Serialises and Broadcasts the event to each connected player
	 * 
	 * @throws IOException
	 */
	private void broadcastEvent(Event ev) throws IOException
	{
		Message.Builder msg = Message.newBuilder();
		msg.setEvent(ev);
		List<Colour> sent = new ArrayList<Colour>(2);

		// Modify event before sending to other players
		if (ev.getTypeCase().equals(Event.TypeCase.RESOURCESTOLEN)
				|| ev.getTypeCase().equals(Event.TypeCase.DEVCARDBOUGHT))
		{
			// Send original to player
			sent.add(game.getPlayer(ev.getInstigator().getId()).getColour());
			sendMessage(msg.build(), sent.get(0));

			// Obscure important info from other players
			if (ev.getTypeCase().equals(Event.TypeCase.RESOURCESTOLEN))
			{
				// Send to victim player and then obscure
				sent.add(game.getPlayer(ev.getResourceStolen().getVictim().getId()).getColour());
				sendMessage(msg.build(), sent.get(1));
				msg.setEvent(
						ev.toBuilder()
								.setResourceStolen(
										ev.getResourceStolen().toBuilder().setResource(Resource.Kind.GENERIC).build())
								.build());
			}
			else
				msg.setEvent(ev.toBuilder()
						.setDevCardBought(
								Board.DevCard.newBuilder().setUnknown(Board.Empty.getDefaultInstance()).build())
						.build());
		}

		// For each player
		for (Colour c : Colour.values())
		{
			// Skip sending message if already sent
			if (sent.contains(c))
			{
				continue;
			}

			if (connections.containsKey(c))
			{
				sendMessage(msg.build(), c);
			}
		}
	}

	/**
	 * Loops until four players have been found.
	 * 
	 * @throws IOException
	 */
	private void getPlayers() throws IOException
	{
		// If no remote players
		if (numConnections == Game.NUM_PLAYERS)
		{
			log("Server Setup", "All Players connected. Starting game...\n");
			return;
		}

		serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("localhost", PORT));
		log("Server Setup",
				String.format("Server started. Waiting for client(s)...%s\n", serverSocket.getInetAddress()));

		// Loop until all players found
		while (numConnections < Game.NUM_PLAYERS)
		{
			Socket connection = serverSocket.accept();

			if (connection != null)
			{
				Colour c = null;
				try
				{
					c = game.joinGame();
				}
				catch (GameFullException e)
				{
				}
				ListenerThread l = new ListenerThread(new RemoteClientConnection(connection), c, this);
				connections.put(c, l);
				Thread t = new Thread(l);
				t.start();
				threads.put(c, t);
				log("Server Setup", String.format("Player %d connected", numConnections));
				numConnections++;
			}
		}

		log("Server Setup", "All Players connected. Starting game...\n");
	}

	/**
	 * Blocks until all players have sent a join lobby request
	 */
	private void waitForJoinLobby()
	{
		for (Colour c : connections.keySet())
		{
			getExpectedMoves(c).add(Request.BodyCase.JOINLOBBY);
		}

		while (!checkJoinedLobby())
		{
			try
			{
				Event ev = msgProc.processMessage();
				if ((ev == null || !ev.isInitialized()) && msgProc.getLastMessage() != null)
				{
					replacePlayerWithAI(msgProc.getLastMessage().getCol());
					continue;
				}

				for (Colour c : connections.keySet())
				{
					if (getExpectedMoves(c).contains(Request.BodyCase.JOINLOBBY))
					{
						continue;
					}
					else if (ev != null) sendMessage(Message.newBuilder().setEvent(ev).build(), c);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		log("Server play", "All players connected\n\n");
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return if everyone has joined the lobby
	 */
	private boolean checkJoinedLobby()
	{
		for (Colour c : connections.keySet())
		{
			// If the player still has an expected move, then this phase is NOT
			// done yet.
			if (!getExpectedMoves(c).isEmpty()) return false;
		}
		return true;
	}

	/**
	 * Get initial placements from each of the connections and send them to the
	 * game.
	 */
	private void getInitialSettlementsAndRoads() throws IOException
	{
		Board.Player.Id current = game.getPlayer(game.getCurrentPlayer()).getId();

		// Get settlements and roads forwards from the first player
		for (int i = 0; i < Game.NUM_PLAYERS; i++)
		{
			log("Server Initial Phase", String.format("Player %s receive initial moves", current.name()));
			receiveInitialMoves(game.getPlayer(current).getColour());

			if (i + 1 < Game.NUM_PLAYERS)
			{
				sendEvents(Events.Event.newBuilder().setTurnEnded(EmptyOuterClass.Empty.getDefaultInstance()).build());
				current = Board.Player.Id.values()[i + 1];
			}
		}

		// Get second set of settlements and roads in reverse order
		for (int i = Game.NUM_PLAYERS - 1; i >= 0; i--)
		{
			log("Server Initial Phase", String.format("Player %s receive initial moves", current.name()));
			receiveInitialMoves(game.getPlayer(current).getColour());
			sendEvents(Events.Event.newBuilder().setTurnEnded(EmptyOuterClass.Empty.getDefaultInstance()).build());

			if (i > 0) current = Board.Player.Id.values()[i - 1];
		}

		// Add roll dice to start the game off
		game.setCurrentPlayer(game.getPlayer(Board.Player.Id.PLAYER_1).getColour());
		getExpectedMoves(game.getCurrentPlayer()).add(Requests.Request.BodyCase.ROLLDICE);
	}

	/**
	 * Receives the initial moves for each player in the appropriate order
	 * 
	 * @param c the player to receive the initial moves from
	 * @throws IOException
	 */
	private void receiveInitialMoves(Colour c) throws IOException
	{
		Player p = game.getPlayers().get(c);
		int oldRoadAmount = p.getRoads().size(), oldSettlementsAmount = p.getSettlements().size();

		// Loop until player sends valid new settlement
		getExpectedMoves(c).add(Requests.Request.BodyCase.BUILDSETTLEMENT);
		while (p.getSettlements().size() == oldSettlementsAmount)
		{
			game.setCurrentPlayer(c);

			processMessage();
			sleep();
		}

		// Loop until player sends valid new road
		getExpectedMoves(c).add(Requests.Request.BodyCase.BUILDROAD);
		while (p.getRoads().size() == oldRoadAmount)
		{
			processMessage();
			sleep();
		}
	}

	/**
	 * Adds the given local connection to the game
	 * 
	 * @param c the colour of the player
	 */
	protected void replacePlayer(Colour c)
	{
		// Replace connection with a new ai
		LocalAIClientOnServer ai = new LocalAIClientOnServer();
		LocalClientConnection conn = ai.getConn().getConn();
		ais.put(c, ai);

		ListenerThread l = new ListenerThread(conn, c, this);
		connections.put(c, l);
		Thread t = new Thread(l);
		t.start();
		threads.put(c, t);

		t = new Thread(ai);
		t.start();
		aiThreads.put(c, t);
		try
		{
			Thread.sleep(300);
			sendGameInfo(c);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		log("Server Error",
				String.format("Replaced Player %s with an ai due to error", game.getPlayer(c).getId().name()));
	}

	/**
	 * Sends the entire game state to the given colour
	 * 
	 * @param c the colour to send the info to
	 */
	private void sendGameInfo(Colour c)
	{
		Message.Builder msg = Message.newBuilder().setEvent(Event.newBuilder().setGameInfo(game.getGameInfo(c)));
		sendMessage(msg.build(), c);
	}

	/**
	 * Adds the given number of local ai players
	 * 
	 * @param col the colour of the connection to overwrite
	 */
	protected void replacePlayerWithAI(Colour col)
	{
		if (connections.containsKey(col) && connections.get(col).getConnection() instanceof RemoteClientConnection)
		{
			try
			{
				// Instruct ListenerThread to shutdown and wait until it joins
				connections.get(col).shutDown();
				threads.get(col).join();

				replacePlayer(col);
			}
			catch (InterruptedException e)
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
			System.out.println(tag + ": " + msg);
		}
		else
			Gdx.app.log(tag, msg);
	}

	/**
	 * Simply forwards the trade offer to the intended recipients
	 * 
	 * @param msg the original message
	 * @param playerTrade the internal trade request inside the message
	 */
	protected void forwardTradeOffer(Messages.Message msg, Trade.WithPlayer playerTrade)
	{
		Colour col = game.getPlayer(playerTrade.getOther().getId()).getColour();

		if (connections.containsKey(col)) sendMessage(msg, col);
	}

	/**
	 * Sends the message out to the client
	 * 
	 * @param msg the message
	 * @param col
	 * @throws IOException
	 */
	public void sendMessage(Message msg, Colour col)
	{
		if (col != null)
		{
			try
			{
				connections.get(col).sendMessage(msg);
			}
			catch (Exception e)
			{
				replacePlayerWithAI(col);
			}
		}
	}

	/**
	 * If an unknown or invalid message is received, then this message sends an
	 * error back
	 * 
	 * @param event
	 * @param c the colour of the player to send the error to
	 */
	public void sendError(Event event, Colour c)
	{
		Message msg = Message.newBuilder().setEvent(event).build();
		sendMessage(msg, c);
	}

	public void addMessageToProcess(ReceivedMessage msg) throws IOException
	{
		msgProc.addMoveToProcess(msg);
	}

	public void setGame(ServerGame game)
	{
		this.game = game;
		msgProc.setGame(game);
	}

	public List<Request.BodyCase> getExpectedMoves(Colour colour)
	{
		return msgProc.getExpectedMoves(colour);
	}

	public boolean isTradePhase()
	{
		return msgProc.isTradePhase();
	}

	public void sleep()
	{
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
