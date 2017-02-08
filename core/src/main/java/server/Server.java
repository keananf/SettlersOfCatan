package server;

import board.Board;
import catan.Events.Event;
import catan.Messages.Message;
import catan.Requests.Request;
import enums.Colour;
import exceptions.UnexpectedMoveTypeException;
import game.Game;
import game.players.NetworkPlayer;
import game.players.Player;
import lobby.Lobby;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server implements Runnable
{
	private ServerGame game;
	private int numConnections;;
	private Map<Colour, ListenerThread> connections;
	private ServerSocket serverSocket;
	private static final int PORT = 12345;
	private Logger logger;
	private ConcurrentHashMap<Colour, Queue<Message>> movesToProcess;

	public Server()
	{
		logger = new Logger();
		game = new ServerGame();
		movesToProcess = new ConcurrentHashMap<Colour, Queue<Message>>();
		connections = new HashMap<Colour, ListenerThread>();
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
				game.allocateResources(dice.getA() + dice.getB());
				sendTurns(dice);

				// Read moves from queue and log
				processMessage();
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
	 * Sends the dice and each player's respective resource count to the player's socket
	 * @param dice the dice roll to send
	 * @throws IOException 
	 */
	private void sendTurns(Board.Roll dice) throws IOException
	{
		List<Player> discardList = new ArrayList<Player>();
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
					discardList.add(p);
				}
			}
		}

		// Process discard requests if necessary
		if(discardList.size() > 0)
		{
			processDiscardRequests(discardList);
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

		// TODO deal with ai

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

		// TODO deal with AI

		// For each player
		for(Colour c : Colour.values())
		{
			if(connections.containsKey(c))
			{
				connections.get(c).sendMessage(msg.build());
			}
		}
	}
/*

	*/
/**
	 * Simply forwards the trade offer to the intended recipients
	 * @param msg the original message
	 * @param playerTrade the internal trade request inside the message
	 *//*

	private void forwardTradeOffer(Message msg, PlayerTradeProto playerTrade) throws IOException
	{
		List<ColourProto> options = playerTrade.getRecipientsList();

		// For each player
		for(Colour c : Colour.values())
		{
			// TODO deal with AI
			if(connections.containsKey(c) && options.contains(Colour.toProto(c)))
			{
				connections.get(c).sendMessage(msg);
			}
		}

	}
*/

	/**
	 * Process the next message, and send any responses and events.
	 * @throws IOException
	 */
	private void processMessage() throws IOException
	{
		Message msg = movesToProcess.get(game.getCurrentPlayer()).poll();
		ListenerThread conn = connections.get(game.getCurrentPlayer());
		logger.logReceivedMessage(msg);

		// If not valid
		if(!validateMsg(msg, game.getCurrentPlayer()))
		{
			conn.sendError();
			return;
		}

		// switch on message type
		switch(msg.getTypeCase())
		{
			// User request
			case REQUEST:
				Event ev = processMove(msg, game.getCurrentPlayer());
				sendEvents(ev);
				break;

			default:
				conn.sendError();
		}
	}

	/**
	 * Broadcast the necessary events to all players based upon the type of event.
	 * @param event the event from the last processed move
	 */
	private void sendEvents(Event event) throws IOException
	{
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
	private Event processMove(Message msg, Colour colour)
	{
		Request request = msg.getRequest();
		Event.Builder resp = Event.newBuilder();
		Player copy = ((NetworkPlayer)game.getPlayers().get(game.getCurrentPlayer())).copy();

		try
		{
			// Switch on message type to interpret the move, then process the move
			// and receive the response
			switch (request.getBodyCase())
			{
				case BUILDROAD:
					game.buildRoad(request.getBuildRoad());
					resp.setRoadBuilt(request.getBuildRoad());
					break;
				case BUILDSETTLEMENT:
					game.buildSettlement(request.getBuildSettlement());
					resp.setSettlementBuilt(request.getBuildSettlement());
					break;
				case BUILDCITY:
					game.upgradeSettlement(request.getBuildCity());
					resp.setCityBuilt(request.getBuildCity());
					break;
				case BUYDEVCARD:
					resp.setDevCardBought(game.buyDevelopmentCard());
					break;
				case JOINLOBBY:
					// TODO what event does this get sent as
					Colour col = game.joinGame(request.getJoinLobby());
					break;
				case MOVEROBBER:
					game.moveRobber(request.getMoveRobber());
					resp.setRobberMoved(request.getMoveRobber());
					break;
				case DISCARDRESOURCES:
					game.processDiscard(request.getDiscardResources(), colour);
					resp.setCardsDiscarded(request.getDiscardResources());
					break;
				case ENDTURN:
					resp.setTurnEnded(game.changeTurn());
					break;
				case CHOOSERESOURCE:
					game.chooseResources(request.getChooseResource());
					resp.setResourceChosen(request.getChooseResource());
					break;
				case ROLLDICE:
					resp.setRolled(game.generateDiceRoll());
					break;
				case PLAYDEVCARD:
					game.playDevelopmentCard(request.getPlayDevCard());
					resp.setDevCardPlayed(request.getPlayDevCard());
					break;
				case SUBMITTARGETPLAYER:
					game.takeResource(request.getSubmitTargetPlayer().getId());
					break;

				//TODO INCORPORATE TRADING
			}
		}
		catch(Exception e)
		{
			// Error. Reset player and return exception message
			game.restorePlayerFromCopy(copy);
			// TODO set error response correctly
		}
		
		// Return response to be sent back to clients
		return resp.build();
	}

	/**
	 * Block until all players have submitted valid discard requests
	 * @param discardList the list of players that need to discard resources
	 */
	private void processDiscardRequests(List<Player> discardList) throws IOException
	{
		Request.BodyCase[] allowedTypes = new Request.BodyCase[1];
		allowedTypes[0] = Request.BodyCase.DISCARDRESOURCES;

		// Get moves from the player until they have completed an initial turn
		while(waitingForDiscards(discardList))
		{
			// cycle through players trying to find a move of the correct type
			for(Player p : discardList)
			{
				Colour c = p.getColour();
				Queue<Message> queue = movesToProcess.get(c);

				// See if the most recent received message is of the correct type
				Request req = queue.peek().getRequest();
				if(queue.isEmpty() || !req.getBodyCase().equals(Request.BodyCase.DISCARDRESOURCES))
				{
					// Throw away invalid message
					queue.poll();
					continue;
				}

				// Try to receive a move
				try
				{
					receiveMove(c, allowedTypes, false, false);
				}

				// Move was illegal.
				catch (UnexpectedMoveTypeException e)
				{
					if(connections.containsKey(c))
						connections.get(c).sendError();
				}
			}
		}
	}

	/**
	 * Checks if the given players still need to discard cards or not
	 * @param discardList the list of players who need to discard
	 * @return boolean indicating whether ot not
	 */
	private boolean waitingForDiscards(List<Player> discardList)
	{
		for(Player p : discardList)
		{
			if(p.getNumResources() > 7)
			{
				return true;
			}
		}
		return false;
	}
/*

	*/
/**
	 * Forwards the trade request to the other player and blocks for a response
	 * @param request the trade request
	 * @param msg the original request, received from across the network
	 * @return the status of the trade "accepted, denied, offer"
	 *//*

	private AcceptRejectResponse processTradeType(TradeRequest request, Message msg) throws IllegalPortTradeException,
																						IllegalBankTradeException,
																						CannotAffordException,
																						IOException
	{
		//TODO DOUBLE CHECK
		// Set up response object
		AcceptRejectResponse.Builder resp = AcceptRejectResponse.newBuilder();
		resp.setTrade(request);

		// Switch on trade type
		switch(request.getContentsCase())
		{
			// Simply forward the message
			case PLAYERTRADE:
				forwardTradeOffer(msg, request.getPlayerTrade());
				resp.setAnswer(TradeStatusProto.PENDING);
				break;

			// Process the trade and ensure it is legal
			case PORTTRADE:
				resp.setAnswer(game.processPortTrade(request.getPortTrade()));
				break;

			case BANKTRADE:
				resp.setAnswer(game.processBankTrade(request.getBankTrade()));
				break;
		}

		return resp.build();
	}

*/
	/**
	 * Get initial placements from each of the connections
	 * and send them to the game.
	 */
	private void getInitialSettlementsAndRoads() throws IOException
	{
		Colour current = game.getCurrentPlayer();
		Colour next =  null;

		// Get settlements and roads forwards from the first player
		for(int i = 0; i < Game.NUM_PLAYERS; i++)
		{
			next = Colour.values()[(current.ordinal() + i) % Game.NUM_PLAYERS];
			receiveInitialMoves(next);
		}
		
		// Get second set of settlements and roads in reverse order
		for(int i = 0; i < Game.NUM_PLAYERS; i--)
		{
			receiveInitialMoves(next);
			next = Colour.values()[(current.ordinal() - i) % Game.NUM_PLAYERS];
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
		Request.BodyCase[] allowedTypes = new Request.BodyCase[2];
		allowedTypes[0] = Request.BodyCase.BUILDROAD;
		allowedTypes[1] = Request.BodyCase.BUILDSETTLEMENT;
		int oldRoadAmount = p.getRoads().size(), oldSettlementsAmount = p.getSettlements().size();
		boolean builtSettlement = false, builtRoad = false;
		int amount = 2;

		// Get moves from the player until they have completed an initial turn
		while(p.getRoads().size() - oldRoadAmount < amount && p.getSettlements().size() - oldSettlementsAmount < amount)
		{
			// Try to receive a move
			try
			{
				// Check return value and validity of move
				Request.BodyCase ret = receiveMove(c, allowedTypes, builtRoad, builtSettlement);
				if(ret == Request.BodyCase.BUILDSETTLEMENT)
				{
					builtSettlement = true;
				}
				else if(ret == Request.BodyCase.BUILDROAD)
				{
					builtRoad = true;
				}
			}

			// Move was illegal.
			catch (UnexpectedMoveTypeException e)
			{
				if(connections.containsKey(c))
					connections.get(c).sendError();
			}
		}
	}

	/**
	 * Receive an initial move. Must be of type BuildRoadRequest OR BuildSettlementRequest
	 * @param c the player colour
	 * @param allowedTypes the array of allowed move types
	 * @param builtRoad
	 *@param builtSettlement @throws UnexpectedMoveTypeException if the move is of an expected type
	 */
	private Request.BodyCase receiveMove(Colour c, Request.BodyCase[] allowedTypes, boolean builtRoad, boolean builtSettlement)
			throws UnexpectedMoveTypeException, IOException
	{
		//TODO DOUBLE CHECK
		Request.BodyCase ret = null;

		// Try to parse a move from the player. If it is not of
		// the prescribed types, then an exception is thrown
		if(connections.containsKey(c))
		{
			boolean processed = false;
			game.setCurrentPlayer(c);
			Message msg = movesToProcess.get(c).poll();
			if(validateMsg(msg, c) && msg.getTypeCase().equals(Message.TypeCase.REQUEST))
			{
				Request.BodyCase msgType = msg.getRequest().getBodyCase();

				// Ensure this message is of an allowed type
				for(Request.BodyCase type : allowedTypes)
				{
					// If valid move type
					if(msgType.equals(type))
					{
						// If the player hasn't already done this in the initial move
						if(builtRoad && msgType == Request.BodyCase.BUILDROAD ||
								builtSettlement && msgType == Request.BodyCase.BUILDSETTLEMENT)
						{
							throw new UnexpectedMoveTypeException(msg);
						}

						processMove(msg, c);
						processed = true;
						ret = type;
						break;
					}
				}
				// Move was not of a prescribed type
				if(!processed)
				{
					throw new UnexpectedMoveTypeException(msg);
				}
			}
			else throw new UnexpectedMoveTypeException(msg);
		}

		return ret;
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
		Colour currentPlayerColour = game.getCurrentPlayer();

		// If it is not the player's turn, send error and return false
		if(!playerColour.equals(currentPlayerColour))
		{
			connections.get(playerColour).sendError();
			return false;
		}

		return true;
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

	public void addMessageToProcess(Message msg, Colour colour) throws IOException
	{
		movesToProcess.get(colour).add(msg);
	}
}
