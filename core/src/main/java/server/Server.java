package server;

import enums.Colour;
import exceptions.CannotAffordException;
import exceptions.IllegalBankTradeException;
import exceptions.IllegalPortTradeException;
import game.Game;
import game.players.NetworkPlayer;
import game.players.Player;
import intergroup.Events.Event;
import intergroup.Messages.Message;
import intergroup.Requests.Request;
import intergroup.board.Board;
import intergroup.lobby.Lobby;
import intergroup.trade.Trade;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements Runnable
{
	private ServerGame game;
	private int numConnections;
	private Map<Colour, ListenerThread> connections;
	private ServerSocket serverSocket;
	private static final int PORT = 12345;
	private Logger logger;
	private ConcurrentLinkedQueue<ReceivedMessage> movesToProcess;
	private HashMap<Colour, List<Request.BodyCase>> expectedMoves;
	private Trade.WithPlayer currentTrade;
	private boolean tradePhase;

	public Server()
	{
		logger = new Logger();
		game = new ServerGame();

		// Set up
		movesToProcess = new ConcurrentLinkedQueue<ReceivedMessage>();
		expectedMoves = new HashMap<Colour, List<Request.BodyCase>>();
		connections = new HashMap<Colour, ListenerThread>();
		for(Colour c : Colour.values())
		{
			expectedMoves.put(c, new ArrayList<>());
		}
	}

	public void run()
	{
		try
		{
			getPlayers();
			broadcastBoard();
			game.chooseFirstPlayer();
			getInitialSettlementsAndRoads();

			while (!game.isOver())
			{
				Board.Roll dice = game.generateDiceRoll();

				// TODO Adjust so turn message has resources
				game.allocateResources(dice.getA() + dice.getB());
				sendTurns(dice);

				// Read moves from queue and log
				ReceivedMessage msg = movesToProcess.poll();
				processMessage(msg.getMsg(), msg.getCol());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Error connecting players");
			return;
		}
	}

	/**
	 * Process the next message, and send any responses and events.
	 * @throws IOException
	 */
	public void processMessage(Message msg, Colour col) throws IOException
	{
		ListenerThread conn = connections.get(col);
		logger.logReceivedMessage(msg);

		// If not valid
		if(!validateMsg(msg, col))
		{
			if(conn != null)
			{
				conn.sendError();
			}
			return;
		}

		// switch on message type
		switch(msg.getTypeCase())
		{
			// User request
			case REQUEST:
				Event ev = processMove(msg, col);
				sendEvents(ev);
				break;

			default:
				if(conn != null)
				{
					conn.sendError();
				}
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
				broadcastEvent(event);
				break;

			// Sent individually, so ignore
			case BEGINGAME:
				break;

			// Send back to original player only
			case ERROR:
				connections.get(game.getPlayer(event.getInstigator().getId()));
				break;

			//TODO complete
		}
	}

	/**
	 * This method interprets the move sent across the network and attempts
	 * to process it
	 * @param msg the message received from across the network
	 * @return the response message
	 */
	private Event processMove(Message msg, Colour colour) throws IOException
	{
		Request request = msg.getRequest();
		Event.Builder ev = Event.newBuilder();
		Player copy = ((NetworkPlayer)game.getPlayers().get(colour)).copy();
		boolean invalid = false;

		try
		{
			// Switch on message type to interpret the move, then process the move
			// and receive the response
			switch (request.getBodyCase())
			{
				case BUILDROAD:
					game.buildRoad(request.getBuildRoad());
					ev.setRoadBuilt(request.getBuildRoad());
					break;
				case BUILDSETTLEMENT:
					game.buildSettlement(request.getBuildSettlement());
					ev.setSettlementBuilt(request.getBuildSettlement());
					break;
				case BUILDCITY:
					game.upgradeSettlement(request.getBuildCity());
					ev.setCityBuilt(request.getBuildCity());
					break;
				case BUYDEVCARD:
					ev.setDevCardBought(game.buyDevelopmentCard());
					break;
				case JOINLOBBY:
					ev.setLobbyUpdate(game.joinGame(request.getJoinLobby()));
					break;
				case MOVEROBBER:
					game.moveRobber(request.getMoveRobber());
					ev.setRobberMoved(request.getMoveRobber());
					break;
				case DISCARDRESOURCES:
					game.processDiscard(request.getDiscardResources(), colour);
					ev.setCardsDiscarded(request.getDiscardResources());
					break;
				case ENDTURN:
					if(canEndTurn())
					{
						ev.setTurnEnded(game.changeTurn());
						tradePhase = false;
						currentTrade = null;
					}
					else ev.setError(Event.Error.newBuilder().setDescription("Cannot end turn yet."));
					break;
				case CHOOSERESOURCE:
					game.chooseResources(request.getChooseResource());
					ev.setResourceChosen(request.getChooseResource());
					break;
				case ROLLDICE:
					ev.setRolled(game.generateDiceRoll());
					break;
				case PLAYDEVCARD:
					game.playDevelopmentCard(request.getPlayDevCard());
					ev.setDevCardPlayed(request.getPlayDevCard());
					break;
				case SUBMITTARGETPLAYER:
					game.takeResource(request.getSubmitTargetPlayer().getId());
					break;
				case INITIATETRADE:
					processTradeType(request.getInitiateTrade(), msg);
					break;
				case SUBMITTRADERESPONSE:
					// TODO timeouts?
					if(currentTrade != null && request.getSubmitTradeResponse().equals(Trade.Response.ACCEPT))
					{
						ev.setPlayerTrade(currentTrade);
						game.processPlayerTrade(currentTrade);
						currentTrade = null;
						break;
					}
					else
					{
						currentTrade = null; // TODO send rejection?
						return null;
					}
				case CHATMESSAGE:
					ev.setChatMessage(request.getChatMessage());
					break;

			}
		}
		catch(Exception e)
		{
			// Error. Reset player and return exception message
			game.restorePlayerFromCopy(copy);
			invalid = true;

			if(connections.containsKey(colour))
				ev.setError(connections.get(colour).getError());
		}

		// Add expected trade response for other player
		if(request.getBodyCase().equals(Request.BodyCase.INITIATETRADE)
				&& request.getInitiateTrade().getTradeCase().equals(Trade.Kind.TradeCase.PLAYER) && !invalid)
		{
			updateExpectedMoves(request, game.getPlayer(request.getInitiateTrade().getPlayer().getOther().getId()).getColour());
		}

		// Update expected moves if no error from the previously processed one
		else if(!ev.getTypeCase().equals(Event.TypeCase.ERROR) && !invalid)
		{
			// Remove move from expected list, and add any new ones
			if(expectedMoves.get(colour).contains(request.getBodyCase()))
			{
				expectedMoves.get(colour).remove(request.getBodyCase());
			}
			updateExpectedMoves(request, colour);
		}

		return ev.build();
	}

	/**
	 * Updates the expected moves for the given player
	 * @param req the request that just succeeded
	 * @param colour the player's colour
	 */
	private void updateExpectedMoves(Request req, Colour colour)
	{
		List<Request.BodyCase> moves = expectedMoves.get(colour);

		// Switch on request type
		switch(req.getBodyCase())
		{
			// Expect for the player to send a steal request next
			case MOVEROBBER:
				moves.add(Request.BodyCase.SUBMITTARGETPLAYER);
				break;

			// Add that a response is expected from this player
			case INITIATETRADE:
				moves.add(Request.BodyCase.SUBMITTRADERESPONSE);
				break;

			// Add expected moves based on recently played dev card
			case PLAYDEVCARD:
			{
				switch(req.getPlayDevCard())
				{
					case KNIGHT:
						moves.add(Request.BodyCase.MOVEROBBER);
						break;
					case YEAR_OF_PLENTY:
						moves.add(Request.BodyCase.CHOOSERESOURCE);
						moves.add(Request.BodyCase.CHOOSERESOURCE);
						break;
					case ROAD_BUILDING:
						moves.add(Request.BodyCase.BUILDROAD);
						moves.add(Request.BodyCase.BUILDROAD);
						break;

					// TODO
					case MONOPOLY:
						break;
				}
				break;
			}

			// No new expected moves
			default:
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
				sendDice(dice);

				if(sum == 7 && p.getNumResources() > 7)
				{
					expectedMoves.get(c).add(Request.BodyCase.DISCARDRESOURCES);
				}
			}
		}
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
		Message.Builder msg = Message.newBuilder();
		msg.setEvent(ev);

		// For each player
		for(Colour c : Colour.values())
		{
			if(connections.containsKey(c))
			{
				connections.get(c).sendMessage(msg.build());
			}
		}
	}

	/**
	 * Get initial placements from each of the connections
	 * and send them to the game.
	 */
	private void getInitialSettlementsAndRoads() throws IOException
	{
		Board.Player.Id current = game.getPlayer(game.getCurrentPlayer()).getId();
		Board.Player.Id next = null;

		// Get settlements and roads forwards from the first player
		for(int i = 0; i < Game.NUM_PLAYERS; i++)
		{
			next = Board.Player.Id.values()[(current.ordinal() + i) % Game.NUM_PLAYERS];
			receiveInitialMoves(game.getPlayer(next).getColour());
		}
		
		// Get second set of settlements and roads in reverse order
		for(int i = Game.NUM_PLAYERS - 1; i >= 0; i--)
		{
			receiveInitialMoves(game.getPlayer(next).getColour());
			next = Board.Player.Id.values()[(current.ordinal() + i) % Game.NUM_PLAYERS];
		}
	}

	/**
	 * Receives the initial moves for each player in the appropriate order
	 * @param c the player to receive the initial moves from
	 * @throws IOException
	 */
	private void receiveInitialMoves(Colour c) throws IOException
	{
		Player p = game.getPlayers().get(c);
		int oldRoadAmount = p.getRoads().size(), oldSettlementsAmount = p.getSettlements().size();

		// Loop until player sends valid new settlement
		while(p.getSettlements().size() < oldSettlementsAmount)
		{
			expectedMoves.get(c).add(Request.BodyCase.BUILDSETTLEMENT);
			game.setCurrentPlayer(c);

			ReceivedMessage msg = movesToProcess.poll();
			if(!msg.getCol().equals(c)) continue;

			processMessage(msg.getMsg(), msg.getCol());
		}

		// Loop until player sends valid new road
		while(p.getRoads().size() < oldRoadAmount)
		{
			expectedMoves.get(c).add(Request.BodyCase.BUILDROAD);
			game.setCurrentPlayer(c);

			ReceivedMessage msg = movesToProcess.poll();
			if(!msg.getCol().equals(c)) continue;

			processMessage(msg.getMsg(), msg.getCol());
		}
	}

	/**
	 * Checks that the given player is currently able to make a move of the given type
	 * @param msg the received message
	 * @param col the current turn
	 * @return true / false depending on legality of move
	 */
	private boolean isExpected(Message msg, Colour col)
	{
		Request.BodyCase type = msg.getTypeCase().equals(Message.TypeCase.REQUEST) ? msg.getRequest().getBodyCase() : null;
		List<Request.BodyCase> expected = expectedMoves.get(col);

		// If in trade phase and the given message isn't a trade
		if(tradePhase && (!msg.getRequest().getBodyCase().equals(Request.BodyCase.INITIATETRADE) && game.getCurrentPlayer().equals(col))
				|| (!msg.getRequest().getBodyCase().equals(Request.BodyCase.SUBMITTRADERESPONSE) && !game.getCurrentPlayer().equals(col)) )
		{
			return false;
		}

		// If it's not your turn and there are no expected moves from you
		if(expected.size() == 0 && !game.getCurrentPlayer().equals(col))
		{
			return false;
		}

		// If not a request or the move is not expected
		if(type == null || (!expected.contains(type) && expected.size() > 0))
		{
			return false;
		}

		return true;
	}

	/**
	 * Ensures the message in the queue pertains to the current player
	 * @param msg the message polled from the queue
	 * @return a boolean indicating success or not
	 * @throws IOException
	 */
	private boolean validateMsg(Message msg, Colour col) throws IOException
	{
		Colour playerColour = col;

		// If it is not the player's turn, the message type is unknown OR the given request is NOT expected
		// send error and return false
		if(!isExpected(msg, col))
		{
			return false;
		}

		return true;
	}

	/**
	 * Checks to see if there are any expected moves
	 * @return if the turn can be ended
	 */
	private boolean canEndTurn()
	{
		boolean valid = true;

		// Check if any expected moves first
		for(List<Request.BodyCase> expected : expectedMoves.values())
		{
			if(expected.size() > 0)
			{
				valid = false;
			}
		}

		return valid;
	}

	/**
	 * Loops until four players have been found.
	 * TODO incorporate AI
	 * @throws IOException 
	 */
	private void getPlayers() throws IOException
	{
		serverSocket = new ServerSocket(PORT);
		System.out.println("Server started. Waiting for client(s)...\n");

		while(numConnections++ < Game.NUM_PLAYERS)
		{
			Socket connection = serverSocket.accept();
			
			if (connection != null)
			{
				Colour c = game.joinGame();
				connections.put(c, new ListenerThread(connection, c,  this));
				System.out.println(String.format("Player %d connected", numConnections));
			}
		}
		
		System.out.println("All Players connected. Starting game...\n");
	}

	public void addMessageToProcess(ReceivedMessage msg) throws IOException
	{
		movesToProcess.add(msg);
	}

	public void setGame(ServerGame game)
	{
		this.game = game;
	}

	public List<Request.BodyCase> getExpectedMoves(Colour colour)
	{
		return expectedMoves.get(colour);
	}

	/**
	 * Simply forwards the trade offer to the intended recipients
	 * @param msg the original message
	 * @param playerTrade the internal trade request inside the message
	 */
	private void forwardTradeOffer(Message msg, Trade.WithPlayer playerTrade) throws IOException
	{
		Colour col = game.getPlayer(playerTrade.getOther().getId()).getColour();

		if(connections.containsKey(col))
			connections.get(col).sendMessage(msg);
	}

	/**
	 * Forwards the trade request to the other player and blocks for a response
	 * @param request the trade request
	 * @param msg the original request, received from across the network
	 * @return the status of the trade "accepted, denied, offer"
	 */
	private Trade.WithBank processTradeType(Trade.Kind request, Message msg) throws IllegalPortTradeException,
			IllegalBankTradeException, CannotAffordException, IOException
	{
		tradePhase = true;

		// Switch on trade type
		switch(request.getTradeCase())
		{
			// Simply forward the message
			case PLAYER:
				currentTrade = request.getPlayer();
				forwardTradeOffer(msg, request.getPlayer());
				return null;

			case BANK:
				game.determineTradeType(request.getBank());
				break;
		}

		return request.getBank();
	}

	public boolean isTradePhase() {
		return tradePhase;
	}
}
