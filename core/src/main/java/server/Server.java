package server;

import com.badlogic.gdx.Gdx;
import connection.RemoteClientConnection;
import enums.Colour;
import exceptions.GameFullException;
import game.Game;
import game.players.Player;
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
			ListenerThread conn = connections.get(msgProc.getLastMessage().getCol());
			if(conn != null)
				conn.sendError();
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
	 * Sends the dice and each player's respective resource count to the player's socket
	 * @param dice the dice roll to send
	 * @throws IOException
	 */
	private void sendTurns(Board.Roll dice) throws IOException
	{
		int sum = dice.getA() + dice.getB();

		// For each player
		for(Colour c : Colour.values())
		{
			if(game.getPlayers().containsKey(c))
			{
				Player p = game.getPlayers().get(c);

				if(sum == 7 && p.getNumResources() > 7)
				{
					msgProc.addExpectedMove(c, Request.BodyCase.DISCARDRESOURCES);
				}
			}
		}
		sendDice(dice);
	}

	/**
	 * Sends the dice to the player's socket
	 * @param roll the dice roll to send
	 * @throws IOException
	 */
	private void sendDice(Board.Roll roll) throws IOException
	{
		// Set up event
		Event.Builder ev = Event.newBuilder();
		ev.setRolled(roll);

		sendEvents(ev.build());
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
				connections.get(c).sendMessage(msg.build());

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
			connections.get(game.getPlayer(ev.getInstigator().getId()).getColour()).sendMessage(msg.build());
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
				connections.get(c).sendMessage(msg.build());
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
		if(numConnections == Game.NUM_PLAYERS) return;

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
	 * Logs the message depending on whether or not this is a local or remote server
	 * @param tag the tag (for Gdx)
	 * @param msg the msg to log
	 */
	private void log(String tag, String msg)
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
	protected void forwardTradeOffer(Messages.Message msg, Trade.WithPlayer playerTrade) throws IOException
	{
		Colour col = game.getPlayer(playerTrade.getOther().getId()).getColour();

		if(connections.containsKey(col))
			connections.get(col).sendMessage(msg);
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
