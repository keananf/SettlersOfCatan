package server;

import AI.LocalAIClientOnServer;
import com.badlogic.gdx.Gdx;
import connection.LocalClientConnection;
import connection.RemoteClientConnection;
import enums.Colour;
import exceptions.GameFullException;
import game.Game;
import intergroup.Events;
import intergroup.Events.Event;
import intergroup.Messages;
import intergroup.Messages.Message;
import intergroup.Requests.Request;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.resource.Resource;
import intergroup.trade.Trade;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
	protected static final int PORT = 12345;

	public Server()
	{
		game = new ServerGame();
		Game.NUM_PLAYERS = 1;

		// Set up
		msgProc = new MessageProcessor(game, this);
		connections = new HashMap<Colour, ListenerThread>();
	}

	public void run()
	{
		try
		{
			getPlayers();
			broadcastBoard();
			game.chooseFirstPlayer();
			msgProc.getInitialSettlementsAndRoads();

			while (!game.isOver())
			{
				// Read moves from queue and log
				processMessage();
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

	/**
	 * Shuts down all individual connections, and then shut down
	 */
	public void shutDown()
	{
		// Shut down all individual connections
		for(ListenerThread conn : connections.values())
		{
			conn.shutDown();
		}
	}

	/**
	 * Process the next message, and send any responses and events.
	 * @throws IOException
	 */
	public void processMessage() throws IOException
	{
		Event ev = msgProc.processMessage();

		if((ev == null || !ev.isInitialized()) && msgProc.getLastMessage() != null)
		{
			sendError(msgProc.getLastMessage().getCol());
		}
	}

	/**
	 * Broadcast the necessary events to all players based upon the type of event.
	 * @param event the event from the last processed move
	 */
	private void sendEvents(Event event) throws IOException
	{
		if(event == null) return;

		// Switch on message type to interpret which event(s) need to be sent out
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
				connections.get(game.getPlayer(event.getInstigator().getId()));
				break;
		}
	}

	/**
	 * Serialises and Broadcasts the board to each connected player
	 * @throws IOException
	 */
	private void broadcastBoard() throws IOException
	{
		// Set up message
		Message.Builder msg = Message.newBuilder();
		Event.Builder ev = Event.newBuilder();

		// For each player
		for(Colour c : Colour.values())
		{
			// Set up the board and the info indicating which player
			Lobby.GameSetup board = game.getGameSettings(c);
			ev.setBeginGame(board);
			msg.setEvent(ev.build());

			if(connections.containsKey(c))
			{
				sendMessage(msg.build(), c);
			}
		}
	}

	/**
	 * Serialises and Broadcasts the event to each connected player
	 * @throws IOException
	 */
	private void broadcastEvent(Event ev) throws IOException
	{
		boolean foo = false;
		Message.Builder msg = Message.newBuilder();
		msg.setEvent(ev);

		// Modify event before sending to other players
		if(ev.getTypeCase().equals(Event.TypeCase.RESOURCESTOLEN) || ev.getTypeCase().equals(Event.TypeCase.DEVCARDBOUGHT))
		{
			// Send original to player
			sendMessage(msg.build(), game.getPlayer(ev.getInstigator().getId()).getColour());
			foo = true;

			// Obscure important info from other players
			if(ev.getTypeCase().equals(Event.TypeCase.RESOURCESTOLEN))
				msg.setEvent(ev.toBuilder().setResourceStolen(ev.getResourceStolen().toBuilder().setResource(Resource.Kind.GENERIC).build()).build());
			else
				msg.setEvent(ev.toBuilder().setDevCardBought(Board.DevCard.newBuilder().setPlayableDevCard(Board.PlayableDevCard.UNRECOGNIZED).build()).build());
		}

		// For each player
		for(Colour c : Colour.values())
		{
			// Skip sending message if already sent
			if(foo && c.equals(game.getPlayer(ev.getInstigator().getId()).getColour()))
			{
				continue;
			}

			if(connections.containsKey(c))
			{
				sendMessage(msg.build(), c);
			}
		}
	}

	/**
	 * Loops until four players have been found.
	 * @throws IOException 
	 */
	private void getPlayers() throws IOException
	{
		// If no remote players
		if(numConnections == Game.NUM_PLAYERS)
		{
			log("Server Setup", "All Players connected. Starting game...\n");
			return;
		}

		serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress("localhost", PORT));
		log("Server Setup", String.format("Server started. Waiting for client(s)...%s\n", serverSocket.getInetAddress()));

		// Loop until all players found
		while(numConnections < Game.NUM_PLAYERS)
		{
			Socket connection = serverSocket.accept();
			
			if (connection != null)
			{
				Colour c = null;
				try
				{
					c = game.joinGame();
				}
				catch (GameFullException e) {}
				connections.put(c, new ListenerThread(new RemoteClientConnection(connection), c,  this));
				log("Server Setup", String.format("Player %d connected", numConnections));
				numConnections++;
			}
		}

		log("Server Setup", "All Players connected. Starting game...\n");
	}

	/**
	 * Adds the given local connection to the game
	 * @param c the colour of the player
	 */
	protected void replacePlayer(Colour c)
	{
		// Replace connection with a new AI
		LocalAIClientOnServer ai = new LocalAIClientOnServer();
		LocalClientConnection conn = ai.getConn().getConn();
		connections.put(c, new ListenerThread(conn, c,  this));
		sendGameInfo(c);

		log("Server Error", String.format("Replaced Player %s with an AI due to error", game.getPlayer(c).getId().name()));
	}

	/**
	 * Sends the entire game state to the given colour
	 * @param c the colour to send the info to
	 */
	private void sendGameInfo(Colour c)
	{
		Message.Builder msg = Message.newBuilder().setEvent(Event.newBuilder().setGameInfo(game.getGameInfo(c)));
		sendMessage(msg.build(), c);
	}

	/**
	 * Adds the given number of local AI players
	 * @param col the colour of the connection to overwrite
	 */
	protected void replacePlayerWithAI(Colour col)
	{;
		connections.get(col).shutDown();
		replacePlayer(col);
	}

	/**
	 * Logs the message depending on whether or not this is a local or remote server
	 * @param tag the tag (for Gdx)
	 * @param msg the msg to log
	 */
	public void log(String tag, String msg)
	{
		if(Gdx.app == null)
		{
			System.out.println(msg);
		}
		else Gdx.app.log(tag, msg);
	}

	/**
	 * Simply forwards the trade offer to the intended recipients
	 * @param msg the original message
	 * @param playerTrade the internal trade request inside the message
	 */
	protected void forwardTradeOffer(Messages.Message msg, Trade.WithPlayer playerTrade)
	{
		Colour col = game.getPlayer(playerTrade.getOther().getId()).getColour();

		if(connections.containsKey(col))
			sendMessage(msg, col);
	}

	/**
	 * Sends the message out to the client
	 * @param msg the message
	 * @param col
	 * @throws IOException
	 */
	public void sendMessage(Message msg, Colour col)
	{
		if(col != null)
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
	 * @param c the colour of the player to send the error to
	 */
	public void sendError(Colour c)
	{
		Message.Builder msg = Message.newBuilder();
		Event.Error.Builder err = Event.Error.newBuilder();

		// Set up err message
		err.setDescription("Invalid message type");
		err.setCause(Events.ErrorCause.UNKNOWN);

		msg.setEvent(Event.newBuilder().setError(err.build()).build());
		sendMessage(msg.build(), c);
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

	public boolean isTradePhase() {
		return msgProc.isTradePhase();
	}
}
