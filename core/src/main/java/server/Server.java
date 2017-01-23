package server;

import enums.*;
import exceptions.CannotAffordException;
import exceptions.IllegalBankTradeException;
import exceptions.IllegalPortTradeException;
import exceptions.UnexpectedMoveTypeException;
import game.Game;
import game.players.Player;
import protocol.EnumProtos.*;
import protocol.MessageProtos;
import protocol.MessageProtos.*;
import protocol.ResponseProtos.*;
import protocol.RequestProtos.*;
import protocol.ResourceProtos.*;
import protocol.EventProtos.*;
import protocol.TradeProtos.*;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements Runnable
{
	private Game game;
	private int numConnections;
	private Map<Colour, ListenerThread> connections;
	private ServerSocket serverSocket;
	private static final int PORT = 12345;
	private Logger logger;
	private ConcurrentLinkedQueue<Message> movesToProcess;

	public Server()
	{
		logger = new Logger();
		game = new Game();
		movesToProcess = new ConcurrentLinkedQueue<Message>();
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
				int dice = game.generateDiceRoll();
				Map<Colour, Map<ResourceType, Integer>> resources = game.allocateResources(dice);
				sendTurns(dice, resources);

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
	 * @param resources the map of each 
	 * @throws IOException 
	 */
	private void sendTurns(int dice, Map<Colour, Map<ResourceType, Integer>> resources) throws IOException
	{		
		// For each player
		for(Colour c : Colour.values())
		{
			sendDice(c, dice);
			sendResources(c, resources.get(c));
		}
	}


	/**
	 * Sends the dice to the player's socket
	 * @param num the dice roll to send
	 * @param c the colour of the player
	 * @throws IOException
	 */
	private void sendDice(Colour c, int num) throws IOException
	{
		// Set up event
		Event.Builder ev = Event.newBuilder();
		DiceRoll.Builder dice = DiceRoll.newBuilder();
		dice.setDice(num);
		ev.setDiceRoll(dice.build());

		// Set up message
		Message.Builder msg = Message.newBuilder();
		msg.setEvent(ev.build());

		broadcastEvent(ev.build());
	}

	/**
	 * Sends the player's resource count to the player's socket
	 * @param c the colour of the player
	 * @param resources the map of resources 
	 * @throws IOException 
	 */
	private void sendResources(Colour c, Map<ResourceType, Integer> resources) throws IOException
	{
		Message.Builder msg = Message.newBuilder();
		Response.Builder resp = Response.newBuilder();

		// Set up resourcess
		ResourceAllocation.Builder alloc = ResourceAllocation.newBuilder();
		ResourceCount.Builder builder = ResourceCount.newBuilder();
		builder.setBrick(resources.get(ResourceType.Brick));
		builder.setGrain(resources.get(ResourceType.Grain));
		builder.setOre(resources.get(ResourceType.Ore));
		builder.setLumber(resources.get(ResourceType.Lumber));
		builder.setWool(resources.get(ResourceType.Wool));
		alloc.setResources(builder.build());
		alloc.setPlayerColour(Colour.toProto(c));

		// Set up and send message
		resp.setResourceAllocation(alloc);
		connections.get(c).sendMessage(msg.build());
	}
	
	/**
	 * Serialises and Broadcasts the board to each connected player
	 * @throws IOException
	 */
	private void broadcastBoard() throws IOException
	{
		// Set up message
		GiveBoardResponse board = game.getBoard();
		Message.Builder msg = Message.newBuilder();
		Response.Builder resp = Response.newBuilder();
		resp.setCurrentBoardResponse(board);
		msg.setResponse(resp.build());

		// TODO deal with ai

		// For each player
		for(Colour c : Colour.values())
		{
			if(connections.containsKey(c))
			{
				msg.setPlayerColour(Colour.toProto(c));
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
		msg.setPlayerColour(Colour.toProto(game.getCurrentPlayer().getColour()));

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

	/**
	 * Simply forwards the trade offer to the intended recipients
	 * @param msg the original message
	 * @param playerTrade the internal trade request inside the message
	 */
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

	/**
	 * Process the next message, and send any responses and events.
	 * @throws IOException
	 */
	private void processMessage() throws IOException
	{
		Message msg = movesToProcess.poll();
		ListenerThread conn = connections.get(Colour.fromProto(msg.getPlayerColour()));
		logger.logReceivedMessage(msg);

		// switch on message type
		switch(msg.getTypeCase())
		{
			// User request
			case REQUEST:
				Response response = processMove(msg, Colour.fromProto(msg.getPlayerColour()));
				conn.sendResponse(response);
				sendEvents(response);
				break;

			case RESPONSE:
				processResponse(msg, Colour.fromProto(msg.getPlayerColour()));
				break;

			default:
				conn.sendError(msg);
		}
	}

	/**
	 * Broadcast the necessary events to all players based upon the type of response.
	 * @param response the response from the last processed move
	 */
	private void sendEvents(Response response) throws IOException
	{
		Event.Builder event = Event.newBuilder();

		// Switch on message type to interpret which event(s) need to be sent out
		switch (response.getTypeCase())
		{
			case BUILDROADRESPONSE:
				event.setNewRoad(response.getBuildRoadResponse().getNewRoad());
				break;
			case BUILDSETTLEMENTRESPONSE:
				event.setNewBuilding(response.getBuildSettlementResponse().getNewBuilding());
				break;
			case UPGRADESETTLEMENTRESPONSE:
				event.setNewBuilding(response.getUpgradeSettlementResponse().getNewBuilding());
				break;
			case BUYDEVCARDRESPONSE:
				event.setBoughtDevCard(Colour.toProto(game.getCurrentPlayer().getColour()));
				break;
			case ENDMOVERESPONSE:
				event.setNewTurn(response.getEndMoveResponse().getNewTurn());
				break;
			case PLAYKNIGHTCARDRESPONSE:
				event.setRobberMove(response.getPlayKnightCardResponse().getMoveRobberResponse().getRobberLocation());
				break;
			case MOVEROBBERRESPONSE:
				event.setRobberMove(response.getMoveRobberResponse().getRobberLocation());
				break;
			case PLAYMONOPOLYCARDRESPONSE:
				event.setPlayedDevCard(setUpDevCardEvent(DevelopmentCardProto.MONOPOLY));
				break;
			case PLAYROADBUILDINGCARDRESPONSE:
				event.setPlayedDevCard(setUpDevCardEvent(DevelopmentCardProto.ROAD_BUILDING));
				break;
			case PLAYYEAROFPLENTYCARDRESPONSE:
				event.setPlayedDevCard(setUpDevCardEvent(DevelopmentCardProto.YEAR_OF_PLENTY));
				break;
			case ACCEPTREJECTRESPONSE:
				event.setTransaction(response.getAcceptRejectResponse().getTrade());
				break;
		}

		broadcastEvent(event.build());
	}

	/**
	 * Creates a PlayDevCardEvent  for the given type and current player
	 * @param type the type that was played
	 * @return the protobuf-compatible PlayDevCardEvent to send to all players
	 */
	private PlayDevCardEvent setUpDevCardEvent(DevelopmentCardProto type)
	{
		PlayDevCardEvent.Builder ev = PlayDevCardEvent.newBuilder();
		ev.setType(type);
		ev.setPlayerColour(Colour.toProto(game.getCurrentPlayer().getColour()));

		return ev.build();
	}

	/**
	 * This method interprets the response received from a client
	 * @param msg the message received from across the network
	 * @param playerColour the colour of the client who sent the response
	 */
	private void processResponse(Message msg, Colour playerColour) throws IOException
	{
		Response response = msg.getResponse();
		Response.Builder resp = Response.newBuilder();

		// Swtich on response type
		switch(response.getTypeCase())
		{
			case ACCEPTREJECTRESPONSE:
				AcceptRejectResponse ans = response.getAcceptRejectResponse();

				// If valid trade type
				if(ans.getTrade().hasPlayerTrade())
				{
					// Send response to offerer
					Colour offerer = Colour.fromProto(ans.getTrade().getPlayerTrade().getOfferer());
					connections.get(offerer).sendMessage(msg);

					// If offer was accepted
					if(ans.getAnswer().equals(TradeStatusProto.ACCEPT))
					{
						// Process player trade
						Colour recipient = Colour.fromProto(msg.getPlayerColour());
						resp.setSuccessFailResponse(game.processPlayerTrade(ans.getTrade().getPlayerTrade(), offerer, recipient));
						sendEvents(resp.build());
					}
				}
				break;

			default:
				connections.get(playerColour).sendError(msg);
		}

	}

		/**
         * This method interprets the move sent across the network and attempts
         * to process it
         * @param msg the message received from across the network
         * @return the response message
         */
	private Response processMove(Message msg, Colour playerColour)
	{
		Request request = msg.getRequest();
		Response.Builder resp = Response.newBuilder();
		Player copy = game.getCurrentPlayer().copy();
		DevelopmentCardType card = null;

		try
		{
			// Switch on message type to interpret the move, then process the move
			// and receive the response
			switch (request.getTypeCase())
			{
				case BUILDROADREQUEST:
					resp.setBuildRoadResponse(game.buildRoad(request.getBuildRoadRequest(), playerColour));
					break;
				case BUILDSETTLEMENTREQUEST:
					resp.setBuildSettlementResponse(game.buildSettlement(request.getBuildSettlementRequest(), playerColour));
					break;
				case UPRADESETTLEMENTREQUEST:
					resp.setUpgradeSettlementResponse(game.upgradeSettlement(request.getUpradeSettlementRequest(), playerColour));
					break;
				case BUYDEVCARDREQUEST:
					resp.setBuyDevCardResponse(game.buyDevelopmentCard(request.getBuyDevCardRequest(), playerColour));
					break;
				case GETBOARDREQUEST:
					resp.setCurrentBoardResponse(game.getBoard());
					break;
				case PLAYROADBUILDINGCARDREQUEST:
					card = DevelopmentCardType.RoadBuilding;
					resp.setPlayRoadBuildingCardResponse(game.playBuildRoadsCard(request.getPlayRoadBuildingCardRequest(), playerColour));
					break;
				case PLAYMONOPOLYCARDREQUEST:
					card = DevelopmentCardType.Monopoly;
					resp.setPlayMonopolyCardResponse(game.playMonopolyCard(request.getPlayMonopolyCardRequest()));
					break;
				case PLAYYEAROFPLENTYCARDREQUEST:
					card = DevelopmentCardType.YearOfPlenty;
					resp.setPlayYearOfPlentyCardResponse(game.playYearOfPlentyCard((request.getPlayYearOfPlentyCardRequest())));
					break;
				case PLAYLIBRARYCARDREQUEST:
					card = DevelopmentCardType.Library;
					resp.setSuccessFailResponse(game.playLibraryCard());
					break;
				case PLAYUNIVERSITYCARDREQUEST:
					card = DevelopmentCardType.University;
					resp.setSuccessFailResponse(game.playUniversityCard());
					break;
				case PLAYKNIGHTCARDREQUEST:
					card = DevelopmentCardType.Knight;
					resp.setPlayKnightCardResponse(game.playKnightCard(request.getPlayKnightCardRequest(), playerColour));
					break;
				case MOVEROBBERREQUEST:
					resp.setMoveRobberResponse(game.moveRobber(request.getMoveRobberRequest(), playerColour));
					break;
				case ENDMOVEREQUEST:
					resp.setEndMoveResponse(game.changeTurn());
					break;
				case TRADEREQUEST:
					resp.setAcceptRejectResponse(processTradeType(request.getTradeRequest(), msg));
					break;
			}
		}
		catch(Exception e)
		{
			// Error. Reset player and return exception message
			game.restorePlayerFromCopy(copy, card != null ? card : card);
			// TODO set error response correctly
		}
		
		// Return response to be sent back to clients
		return resp.build();
	}
	

	/**
	 * Forwards the trade request to the other player and blocks for a response
	 * @param request the trade request
	 * @param msg the original request, received from across the network
	 * @return the status of the trade "accepted, denied, offer"
	 */
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

	/**
	 * Get initial placements from each of the connections
	 * and send them to the game.
	 */
	private void getInitialSettlementsAndRoads() throws IOException
	{
		Colour current = game.getCurrentPlayer().getColour();
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
		//TODO DOUBLE CHECK
		Player p = game.getPlayers().get(c);
		Request.TypeCase[] allowedTypes = new Request.TypeCase[2];
		allowedTypes[0] = Request.TypeCase.BUILDROADREQUEST;
		allowedTypes[1] = Request.TypeCase.BUILDSETTLEMENTREQUEST;
		int oldRoadAmount = p.getRoads().size(), oldSettlementsAmount = p.getSettlements().size();
		boolean builtSettlement = false, builtRoad = false;

		// Get moves from the player until they have completed an initial turn
		while(p.getRoads().size() < oldRoadAmount && p.getSettlements().size() < oldSettlementsAmount)
		{
			// Try to receive a move
			try
			{
				// Check return value and validity of move
				Request.TypeCase ret = receiveMove(c, allowedTypes, builtRoad, builtSettlement);
				if(ret == Request.TypeCase.BUILDSETTLEMENTREQUEST)
				{
					builtSettlement = true;
				}
				else if(ret == Request.TypeCase.BUILDROADREQUEST)
				{
					builtRoad = true;
				}
			}

			// Move was illegal.
			catch (UnexpectedMoveTypeException e)
			{
				if(connections.containsKey(c))
					connections.get(c).sendError(e.getOriginalMessage());
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
	private Request.TypeCase receiveMove(Colour c, Request.TypeCase[] allowedTypes, boolean builtRoad, boolean builtSettlement)
			throws UnexpectedMoveTypeException, IOException
	{
		//TODO DOUBLE CHECK
		Request.TypeCase ret = null;

		// Try to parse a move from the player. If it is not of
		// the prescribed types, then an exception is thrown
		if(connections.containsKey(c))
		{
			boolean processed = false;
			Message msg = movesToProcess.poll();
			if(validateMsg(msg) && msg.hasRequest())
			{
				Request.TypeCase msgType = msg.getRequest().getTypeCase();

				// Ensure this message is of an allowed type
				for(Request.TypeCase type : allowedTypes)
				{
					// If valid move type
					if(msgType.equals(type))
					{
						// If the player hasn't already done this in the initial move
						if(builtRoad && msgType == Request.TypeCase.BUILDROADREQUEST ||
								builtSettlement && msgType == Request.TypeCase.BUILDSETTLEMENTREQUEST)
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
	private boolean validateMsg(Message msg) throws IOException
	{
		Colour playerColour = Colour.fromProto(msg.getPlayerColour());
		Colour currentPlayerColour = game.getCurrentPlayer().getColour();

		// If it is not the player's turn, send error and return false
		if(!playerColour.equals(currentPlayerColour))
		{
			connections.get(playerColour).sendError(msg);
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
				Colour c = game.addNetworkPlayer(connection.getInetAddress());
				
				connections.put(c, new ListenerThread(connection, c, this));
				System.out.println(String.format("Player %d connected", numConnections));
			}
		}
		
		System.out.println("All Players connected. Starting game...\n");
	}

	public void addMessageToProcess(Message msg)
	{
		movesToProcess.add(msg);
	}
}
