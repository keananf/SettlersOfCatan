package server;

import com.sun.org.apache.regexp.internal.RE;
import enums.*;
import exceptions.CannotAffordException;
import exceptions.IllegalBankTradeException;
import exceptions.IllegalPortTradeException;
import exceptions.UnexpectedMoveTypeException;
import game.Game;
import game.players.Player;
import protocol.EnumProtos.*;
import protocol.MessageProtos.*;
import protocol.ResponseProtos.*;
import protocol.RequestProtos.*;
import protocol.ResourceProtos.*;
import protocol.EventProtos.*;
import protocol.TradeProtos.*;


import java.io.*;
import java.net.*;
import java.util.*;

public class Server implements Runnable
{
	private Game game;
	private int numConnections;
	private Map<Colour, Socket> connections;
	private ServerSocket serverSocket;
	private static final int PORT = 12345;
	private Logger logger;

	public Server()
	{
		logger = new Logger();
		game = new Game();
		connections = new HashMap<Colour, Socket>();
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
				
				receiveMoves();
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
	 * @param dice the dice roll to send
	 * @param c the colour of the player
	 * @throws IOException
	 */
	private void sendDice(Colour c, int dice) throws IOException
	{
		Socket s = connections.get(c);

		// Set up message
		DiceRoll.Builder builder = DiceRoll.newBuilder();
		builder.setDice(dice);

		// Serialise and Send
		builder.build().writeTo(s.getOutputStream());
		s.getOutputStream().flush();
	}

	/**
	 * Sends the player's resource count to the player's socket
	 * @param c the colour of the player
	 * @param resources the map of resources 
	 * @throws IOException 
	 */
	private void sendResources(Colour c, Map<ResourceType, Integer> resources) throws IOException
	{		
		Socket s = connections.get(c);

		// Set up message
		ResourceCount.Builder builder = ResourceCount.newBuilder();
		builder.setBrick(resources.get(ResourceType.Brick));
		builder.setGrain(resources.get(ResourceType.Grain));
		builder.setOre(resources.get(ResourceType.Ore));
		builder.setLumber(resources.get(ResourceType.Lumber));
		builder.setWool(resources.get(ResourceType.Wool));

		// Serialise and Send
		builder.build().writeTo(s.getOutputStream());
		s.getOutputStream().flush();
	}
	
	/**
	 * Serialises and Broadcasts the board to each connected player
	 * @throws IOException
	 */
	private void broadcastBoard() throws IOException
	{
		// For each player
		for(Colour c : Colour.values())
		{
			if(connections.containsKey(c))
			{
				Socket s = connections.get(c);

				// Set up message
				GiveBoardResponse board = game.getBoard();

				// Serialise and Send
				board.writeTo(s.getOutputStream());
				s.getOutputStream().flush();
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

		// For each player
		for(Colour c : Colour.values())
		{
			if(connections.containsKey(c))
			{
				Socket s = connections.get(c);

				// Serialise and Send
				msg.build().writeTo(s.getOutputStream());
				s.getOutputStream().flush();
			}
		}
	}
	
	/**
	 * Listens for moves from the current player
	 * @return the bytes received from the current player
	 * @throws IOException 
	 */
	private void receiveMoves() throws IOException
	{
		Colour colour = game.getCurrentPlayer().getColour();
		Socket socket = connections.get(colour); //TODO this should be effectively listening to all players

		// Receive and process moves until the end one is received
		while(true)
		{
			Message msg = Message.parseFrom(socket.getInputStream());
			logger.logReceivedMessage(msg);

			processMessage(msg, socket);

			// If end move, stop
			if(msg.getTypeCase().equals(Message.TypeCase.REQUEST) && msg.getRequest().getTypeCase().equals(Request.TypeCase.ENDMOVEREQUEST))
			{
				break;
			}
		}		
	}

	/**
	 * Process the received message, and send any responses and events.
	 * @param msg the recieved message
	 * @param socket the socket which sent the message
	 * @throws IOException
	 */
	private void processMessage(Message msg, Socket socket) throws IOException
	{
		// switch on message type
		switch(msg.getTypeCase())
		{
			case EVENT:
				sendError(socket, msg);
				break;
			case REQUEST:
				Response response = processMove(msg, Colour.fromProto(msg.getPlayerColour()));
				sendResponse(response);
				sendEvents(response);
				break;
			case RESPONSE:
					/*//TODO finish processing
					Response resp = msg.getResponse();
					resp = processResponse(resp, socket, msg);
*/
				sendError(socket, msg);
				break;
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
	 * Processes the response message received from the client
	 * @param resp the response message
	 * @param socket the socket of the sender
	 * @param msg the original received message
	 * @return the response from processing this response
	 * @throws IOException
	 */
	private Response processResponse(Response resp, Socket socket, Message msg) throws IOException
	{
		//TODO
/*
		// If not a trade response, illegal
		if(!resp.hasAcceptRejectResponse())
			sendError(socket, msg);

			// Perform actual trade
		else if(resp.getAcceptRejectResponse().getAnswer().equals(TradeStatusProto.ACCEPT))
			resp = game.processPlayerTrade(resp.getAcceptRejectResponse());

			// Forward denial or counter response to original
		else
			forwardTrade(resp.getAcceptRejectResponse());*/

		return resp;
	}

	/**
	 * If an unknown or invalid message is received, then this message sends an error back
	 * @param originalMsg the original message
	 * @param socket the socket to send the error to
	 */
	private void sendError(Socket socket, Message originalMsg) throws IOException
	{
		// Set up result message
		Response.Builder response = Response.newBuilder();
		SuccessFailResponse.Builder result = SuccessFailResponse.newBuilder();
		result.setResult(ResultProto.FAILURE);
		result.setReason("Invalid message type");

		// Set up wrapper response object
		response.setSuccessFailResponse(result);
		response.build().writeTo(socket.getOutputStream());

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
					resp.setAcceptRejectResponse(processTrade(request.getTradeRequest(), msg));
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
	 * Sends response out to each client so that they may
	 * update their boards
	 * @param response the response message from the last action
	 * @throws IOException
	 */
	private void sendResponse(Response response) throws IOException
	{
		// If ok, propogate out to everyone. Otherwise just send
		// error response to the current player.
		if(response.hasSuccessFailResponse() && response.getSuccessFailResponse().getResult().equals(ResultProto.SUCCESS)
			|| response.hasAcceptRejectResponse() && response.getAcceptRejectResponse().getAnswer().equals(TradeStatusProto.ACCEPT))
		{
			// For each socket, write the response
			for(Socket conn : connections.values())
			{
				response.writeTo(conn.getOutputStream());
				conn.getOutputStream().flush();
			}
		}
		else
		{
			Colour colour = game.getCurrentPlayer().getColour();
			Socket conn = connections.get(colour);
			response.writeTo(conn.getOutputStream());
			conn.getOutputStream().flush();
		}
	}
	

	/**
	 * Forwards the trade request to the other player and blocks for a response
	 * @param request the trade request
	 * @param msg the original request, received from across the network
	 * @return the status of the trade "accepted, denied, offer"
	 */
	private AcceptRejectResponse processTrade(TradeRequest request, Message msg) throws IllegalPortTradeException,
																						IllegalBankTradeException,
																						CannotAffordException,
																						IOException
	{
		// Set up response object
		AcceptRejectResponse.Builder resp = AcceptRejectResponse.newBuilder();
		resp.setTrade(request);

		// Switch on trade type
		switch(request.getContentsCase())
		{
			// Simply forward the message
			case PLAYERTRADE:
				Colour colour = Colour.fromProto(request.getPlayerTrade().getRecipient());
				Socket recipient = connections.get(colour);

				msg.writeTo(recipient.getOutputStream());
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
					sendError(connections.get(c), e.getOriginalMessage());
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
	private Request.TypeCase receiveMove(Colour c, Request.TypeCase[] allowedTypes, boolean builtRoad, boolean builtSettlement) throws UnexpectedMoveTypeException
	{
		Request.TypeCase ret = null;

		try
		{
			// Try to parse a move from the player. If it is not of
			// the prescribed types, then an exception is thrown
			if(connections.containsKey(c))
			{
				boolean processed = false;
				Socket socket = connections.get(c);
				Message msg = Message.parseFrom(socket.getInputStream());
				if(msg.hasRequest())
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
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return ret;
	}

	/**
	 * Loops until four players have been found
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
				
				connections.put(c, connection);
				System.out.println(String.format("Player %d connected", numConnections));
			}
		}
		
		System.out.println("All Players connected. Starting game...\n");
	}
}
