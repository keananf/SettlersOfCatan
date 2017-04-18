package server;

import AI.AIClient;
import AI.LocalAIClientOnServer;
import com.badlogic.gdx.Gdx;
import connection.LocalClientConnection;
import connection.RemoteClientConnection;
import enums.Colour;
import enums.ResourceType;
import exceptions.BankLimitException;
import exceptions.GameFullException;
import game.CurrentTrade;
import game.Game;
import game.players.Player;
import game.players.ServerPlayer;
import grid.Hex;
import intergroup.EmptyOuterClass;
import intergroup.Events.Event;
import intergroup.Messages.Message;
import intergroup.Requests;
import intergroup.Requests.Request;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Server implements Runnable
{
	private final MessageProcessor msgProc;
	ServerGame game;
	int numConnections;
	final Map<Colour, ListenerThread> connections;
	final Map<Colour, Thread> aiThreads;
	final Map<Colour, Thread> threads;
	final Map<Colour, AIClient> ais;
	private static final int PORT = 7000;
	private boolean active;
	private final Semaphore lock;

	public Server()
	{
		ais = new HashMap<>();
		aiThreads = new HashMap<>();
		threads = new HashMap<>();
		game = new ServerGame();
		Game.NUM_PLAYERS = 4;

		// Set up
		msgProc = new MessageProcessor(game, this);
		connections = new HashMap<>();
		lock = new Semaphore(1);
	}

	public void run()
	{
		try
		{
			active = true;
			getPlayers();
			game.chooseFirstPlayer();
			waitForJoinLobby();
			broadcastBoard();
			getInitialSettlementsAndRoads();
			allocateInitialResources();

			log("\n\nServer Start", "All players Connected. Beginning play.\n");
			while (active && !game.isOver())
			{
				try
				{
					lock.acquire();
					try
					{
						// Read moves from queue and log
						processMessage();
					}
					finally
					{
						lock.release();
					}
					sleep();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			if (active)
			{
				sendEvents(Event.newBuilder().setGameWon(msgProc.getGameWon()).build());
			}
		}
		catch (IOException e)
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

	public void terminate()
	{
		try
		{
			lock.acquire();
			try
			{
				if (active)
				{
					active = false;
				}
			}
			finally
			{
				lock.release();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Shuts down all individual connections, and then shut down
	 */
	private void shutDown()
	{
		log("Shutdown Server", "Shutting down.");
		int i = 1;

		// Shut down all individual connections
		for (Colour c : connections.keySet())
		{
			log("Shutdown Server", String.format("Thread %d / %d shutdown.", i++, connections.size()));
			try
			{
				// Instruct ListenerThread to terminate, then wait
				threads.get(c).interrupt();
				connections.get(c).shutDown();
				threads.get(c).join();

				// Instruct AI thread to terminate, then wait
				if (ais.containsKey(c) && aiThreads.containsKey(c))
				{
					aiThreads.get(c).interrupt();
					ais.get(c).shutDown();
					aiThreads.get(c).join();
				}
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
		Event ev = null;
		try
		{
			ev = msgProc.processMessage();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

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
	private void sendEvents(Event event) {
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
		case PLAYERTRADEACCEPTED:
		case LOBBYUPDATE:
		case CHATMESSAGE:
		case RESOURCESTOLEN:
		case RESOURCECHOSEN:
		case MONOPOLYRESOLUTION:
		case CARDSDISCARDED:
		case INITIALALLOCATION:
		case PLAYERTRADEREJECTED:
		case PLAYERTRADEINITIATED:
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
	private void broadcastBoard()
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
	private void broadcastEvent(Event ev)
	{
		Message.Builder msg = Message.newBuilder();
		msg.setEvent(ev);
		List<Colour> sent = new ArrayList<>(2);

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
		if (numConnections == Game.NUM_PLAYERS && active)
		{
			log("Server Setup", "All Players connected. Starting game...\n");
			return;
		}

		ServerSocket serverSocket = new ServerSocket(PORT);

		log("Server Setup",
				String.format("Server started. Waiting for client(s)...%s\n", serverSocket.getInetAddress()));

		// Loop until all players found
		while (active && numConnections < Game.NUM_PLAYERS)
		{
			Socket connection = serverSocket.accept();

			Colour c = null;
			try
			{
				c = game.joinGame();
			}
			catch (GameFullException ignored)
			{}
			ListenerThread l = new ListenerThread(new RemoteClientConnection(connection), c, this);
			connections.put(c, l);
			Thread t = new Thread(l);
			t.start();
			threads.put(c, t);
			log("Server Setup", String.format("Player %d connected", numConnections));
			numConnections++;
		}
		if (active && numConnections == Game.NUM_PLAYERS)
		{
			log("Server Setup", "All Players connected. Starting game...\n");
		}
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

		while (active && !checkJoinedLobby())
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
					if (!getExpectedMoves(c).contains(Request.BodyCase.JOINLOBBY) && ev != null)
						sendMessage(Message.newBuilder().setEvent(ev).build(), c);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (active)
		{
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
		for (int i = 0; active && i < Game.NUM_PLAYERS; i++)
		{
			log("Server Initial Phase", String.format("Player %s receive initial moves", current.name()));
			receiveInitialMoves(game.getPlayer(current).getColour());

			if (i + 1 < Game.NUM_PLAYERS)
			{
				current = Board.Player.Id.values()[i + 1];
			}
		}

		// Get second set of settlements and roads in reverse order
		for (int i = Game.NUM_PLAYERS - 1; active && i >= 0; i--)
		{
			log("Server Initial Phase", String.format("Player %s receive initial moves", current.name()));
			receiveInitialMoves(game.getPlayer(current).getColour());

			if (i > 0)
			{
				current = Board.Player.Id.values()[i - 1];
			}
		}

		// Add roll dice to start the game off
		game.setCurrentPlayer(game.getPlayer(Board.Player.Id.PLAYER_1).getColour());
		getExpectedMoves(game.getCurrentPlayer()).add(Requests.Request.BodyCase.ROLLDICE);
		msgProc.initialPhase = true;
	}

	/**
	 * Give the player one of each resource which pertains to the second built
	 * settlement
	 */
	private void allocateInitialResources() throws IOException
	{
		Board.InitialResourceAllocation.Builder allocs = Board.InitialResourceAllocation.newBuilder();
		for (Player p : game.getPlayers().values())
		{
			Map<ResourceType, Integer> resources = new HashMap<>();
			for (Hex h : ((ServerPlayer) p).getSettlementForInitialResources().getNode().getHexes())
			{
				if (h.getResource().equals(ResourceType.Generic)) continue;
				int existing = resources.getOrDefault(h.getResource(), 0);
				resources.put(h.getResource(), existing + 1);
			}

			try
			{
				p.grantResources(resources, game.getBank());
			}
			catch (BankLimitException e)
			{
				e.printStackTrace();
			}
			Board.ResourceAllocation.Builder alloc = Board.ResourceAllocation.newBuilder();
			alloc.setPlayer(Board.Player.newBuilder().setId(p.getId()).build());
			alloc.setResources(game.processResources(resources));
			allocs.addResourceAllocation(alloc.build());
		}

		sendEvents(Event.newBuilder().setInitialAllocation(allocs.build()).build());
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
		while (active && p.getSettlements().size() == oldSettlementsAmount)
		{
			game.setCurrentPlayer(c);

			processMessage();
			sleep();
		}

		// Loop until player sends valid new road
		getExpectedMoves(c).add(Requests.Request.BodyCase.BUILDROAD);
		while (active && p.getRoads().size() == oldRoadAmount)
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
	private void replacePlayer(Colour c)
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
	private void replacePlayerWithAI(Colour col)
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
	 * Simply forwards the trade offer to the intended recipient
	 *
	 * @param playerTrade the internal trade request inside the message
	 * @param instigator the player who requested the trade that was rejected
	 */
	void forwardTradeOffer(Trade.WithPlayer playerTrade, Board.Player instigator)
	{
		Event ev = Event.newBuilder().setPlayerTradeInitiated(playerTrade).setInstigator(instigator).build();
		sendEvents(ev);

		/*
		 * // Send messages if (connections.containsKey(recipient)) {
		 * sendMessage(msg, recipient); } if (connections.containsKey(player)) {
		 * sendMessage(msg, player); }
		 */
	}

	/**
	 * Simply forwards the reject to the participants
	 *
	 * @param playerTrade the internal trade request inside the message
	 * @param instigator the player who requested the trade that was rejected
	 */
	void forwardTradeReject(Trade.WithPlayer playerTrade, Board.Player instigator)
	{
		// Extract player info, and set up the reject event
		Event ev = Event.newBuilder().setInstigator(instigator)
				.setPlayerTradeRejected(EmptyOuterClass.Empty.getDefaultInstance()).build();
		sendEvents(ev);
	}

	/**
	 * Sends the message out to the client
	 * 
	 * @param msg the message
	 * @param col
	 */
	private void sendMessage(Message msg, Colour col)
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
	private void sendError(Event event, Colour c)
	{
		Message msg = Message.newBuilder().setEvent(event).build();
		sendMessage(msg, c);
	}

	public void addMessageToProcess(ReceivedMessage msg)
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

	private void sleep()
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

	public CurrentTrade getCurrentTrade()
	{
		return msgProc.getCurrentTrade();
	}
}