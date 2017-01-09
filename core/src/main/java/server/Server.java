package server;

import enums.*;
import game.Game;
import game.build.DevelopmentCard;
import game.players.Player;
import protocol.EnumProtos.*;
import protocol.MessageProtos;
import protocol.ResponseProtos.*;
import protocol.RequestProtos.*;
import protocol.ResourceProtos.*;
import protocol.EventProtos.*;
import protocol.BoardProtos.*;
import protocol.MessageProtos.*;
import protocol.TradeProtos;


import java.io.*;
import java.net.*;
import java.util.*;

public class Server
{
	private Game game;
	private int numConnections;
	private Map<Colour, Socket> connections;
	private ServerSocket serverSocket;
	private static final int PORT = 12345;
	
	public Server()
	{
		game = new Game();
		connections = new HashMap<Colour, Socket>();
	}
	
	public static void main(String[] args)
	{
		Server s = new Server();
		try
		{
			// Get players and initial moves
			s.getPlayers();
			s.broadcastBoard();
			s.game.chooseFirstPlayer();
			s.getInitialSettlementsAndRoads();
			
			// Main game loop
			while(!s.game.isOver())
			{			
				// Allocate and send resources and dice out to players
				int dice = s.game.generateDiceRoll();
				Map<Colour, Map<ResourceType, Integer>> resources = s.game.allocateResources(dice);
				s.sendTurns(dice, resources);
				
				s.receiveMoves();
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
			Socket s = connections.get(c);
			
			// Set up message
			GiveBoardResponse board = game.getBoard();

			// Serialise and Send
			board.writeTo(s.getOutputStream());
			s.getOutputStream().flush();
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
		Socket socket = connections.get(colour);

		// Receive and process moves until the end one is received
		while(true)
		{
			Message msg = Message.parseFrom(socket.getInputStream());

			// switch on message type
			switch(msg.getTypeCase())
			{
				case EVENT:
					sendError(socket, msg);
					break;
				case REQUEST:
					Response response = processMove(msg, Colour.fromProto(msg.getPlayerColour()));
					sendResponse(response);
					// TODO sendEvents(response);
					break;
				case RESPONSE:
					sendError(socket, msg);
					break;
			}

			// If end move, stop
			if(msg.getTypeCase().equals(Message.TypeCase.REQUEST) && msg.getRequest().getTypeCase().equals(Request.TypeCase.ENDMOVEREQUEST))
			{
				break;
			}
		}		
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
		response.setOriginalMessage(originalMsg);
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
		DevelopmentCard card = null;

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
					resp.setSuccessFailResponse(game.buildSettlement(request.getBuildSettlementRequest(), playerColour));
					break;
				case UPRADESETTLEMENTREQUEST:
					resp.setSuccessFailResponse(game.upgradeSettlement(request.getUpradeSettlementRequest(), playerColour));
					break;
				case BUYDEVCARDREQUEST:
					resp.setBuyDevCardResponse(game.buyDevelopmentCard(request.getBuyDevCardRequest(), playerColour));
					break;
				case GETBOARDREQUEST:
					resp.setCurrentBoardResponse(game.getBoard());
					break;
				case PLAYROADBUILDINGCARDREQUEST:
					card = new DevelopmentCard();
					card.setType(DevelopmentCardType.RoadBuilding);
					resp.setPlayRoadBuildingCardResponse(game.playBuildRoadsCard(request.getPlayRoadBuildingCardRequest(), playerColour));
					break;
				case PLAYMONOPOLYCARDREQUEST:
					card = new DevelopmentCard();
					card.setType(DevelopmentCardType.Monopoly);
					resp.setPlayMonopolyCardResponse(game.playMonopolyCard(request.getPlayMonopolyCardRequest()));
					break;
				case PLAYYEAROFPLENTYCARDREQUEST:
					card = new DevelopmentCard();
					card.setType(DevelopmentCardType.YearOfPlenty);
					resp.setSuccessFailResponse(game.playYearOfPlentyCard((request.getPlayYearOfPlentyCardRequest())));
					break;
				case PLAYLIBRARYCARDREQUEST:
					card = new DevelopmentCard();
					card.setType(DevelopmentCardType.Library);
					resp.setSuccessFailResponse(game.playLibraryCard());
					break;
				case PLAYUNIVERSITYCARDREQUEST:
					card = new DevelopmentCard();
					card.setType(DevelopmentCardType.University);
					resp.setSuccessFailResponse(game.playUniversityCard());
					break;
				case PLAYKNIGHTCARDREQUEST:
					card = new DevelopmentCard();
					card.setType(DevelopmentCardType.Knight);
					resp.setMoveRobberResponse(game.moveRobber(request.getPlayKnightCardRequest().getRequest(), playerColour));
					break;
				case MOVEROBBERREQUEST:
					resp.setMoveRobberResponse(game.moveRobber(request.getMoveRobberRequest(), playerColour));
					break;
				case ENDMOVEREQUEST:
					resp.setEndMoveResponse(game.changeTurn());
					break;
				//case TRADEREQUEST:
					// TODO HANDLE TRADE
				//	break;
			}
		}
		catch(Exception e)
		{
			// Error. Reset player and return exception message
			game.restorePlayerFromCopy(copy, card != null ? card : null);
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
	private String processTrade(TradeProtos.TradeProto request, Message msg)
	{
		Colour colour = game.getCurrentPlayer().getColour();
		Socket socket = connections.get(colour);

		// Receive and process moves until the end one is received
		while(true)
		{
			Message msg = Message.parseFrom(socket.getInputStream());

			// switch on message type
			switch(msg.getTypeCase())
			{
				case EVENT:
					sendError(socket, msg);
					break;
				case REQUEST:
					Response response = processMove(msg, Colour.fromProto(msg.getPlayerColour()));
					sendResponse(response);
					// TODO sendEvents(response);
					break;
				case RESPONSE:
					sendError(socket, msg);
					break;
			}

			// If end move, stop
			if(msg.getTypeCase().equals(Message.TypeCase.REQUEST) && msg.getRequest().getTypeCase().equals(Request.TypeCase.ENDMOVEREQUEST))
			{
				break;
			}
		}


		try (BufferedReader reader = new BufferedReader(new InputStreamReader(recipient.getInputStream())))
		{
			// Send message to recipient 
			recipient.getOutputStream().write(rawMsg);

			// Receive request and process
			byte[] rec = reader.lines().toString().getBytes(); // TODO fix this
					
			// Overwrite message object with response. Should be the same contents except 
			// with a different status
			request = (TradeMessage) requestDeserialiser.deserialiseMove(rec, MoveType.TradeMove);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
				
		return request.getStatus().toString();
	}
	
	/**
	 * Get initial placements from each of the connections
	 * and send them to the game.
	 */
	public void getInitialSettlementsAndRoads()
	{
		// Get settlements and roads one way
		for(int i = 0; i < Game.NUM_PLAYERS; i++)
		{
			// receiveInitialMoves();
			// Throw exception or something if unexpected move type?
		}
		
		// Get settlements and roads one way
		for(int i = Game.NUM_PLAYERS - 1; i >= 0; i--)
		{
			// game.processMove(s.receiveMove());
			// Throw exception or something if unexpected move type?
		}
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
