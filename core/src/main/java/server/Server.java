package main.java.server;

import main.java.enums.Colour;
import main.java.enums.MoveType;
import main.java.enums.ResourceType;
import main.java.game.Game;
import main.java.game.build.DevelopmentCard;
import main.java.game.moves.PlayDevelopmentCardMove;
import main.java.game.players.Player;
import main.java.comm.BoardSerialiser;
import main.java.comm.DevelopmentCardSerialiser;
import main.java.comm.ResponseSerialiser;
import main.java.comm.RequestDeserialiser;
import main.java.comm.TurnSerialiser;
import main.java.comm.messages.BoardMessage;
import main.java.comm.messages.Request;
import main.java.comm.messages.Response;
import main.java.comm.messages.TurnUpdateMessage;

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
	private ResponseSerialiser responseSerialiser;
	private TurnSerialiser turnSerialiser;
	private BoardSerialiser boardSerialiser;
	private RequestDeserialiser requestDeserialiser;
	
	public Server()
	{
		game = new Game();
		connections = new HashMap<Colour, Socket>();
		responseSerialiser = new ResponseSerialiser();
		requestDeserialiser = new RequestDeserialiser();
		boardSerialiser = new BoardSerialiser();
		turnSerialiser = new TurnSerialiser();
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
			sendTurn(dice, c, resources.get(c));
		}
	}

	/**
	 * Sends the dice and the player's resource count to the player's socket
	 * @param dice the dice roll to send
	 * @param c the colour of the player
	 * @param resources the map of resources 
	 * @throws IOException 
	 */
	private void sendTurn(int dice, Colour c, Map<ResourceType, Integer> resources) throws IOException
	{		
		Socket s = connections.get(c);
		
		// Set up message
		TurnUpdateMessage msg = new TurnUpdateMessage();
		msg.setDice(dice);
		msg.setPlayer(c);
		msg.setResources(resources);
		
		// Serialise and Send
		byte[] bytes = turnSerialiser.serialise(msg);
		s.getOutputStream().write(bytes);
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
			BoardMessage msg = new BoardMessage();
			msg.setEdges(game.getGrid().edges);
			msg.setNodes(game.getGrid().nodes);
			msg.setPorts(game.getGrid().ports);
			msg.setHexes(game.getGrid().grid);
			
			// Serialise and Send
			byte[] bytes = boardSerialiser.serialise(msg);
			s.getOutputStream().write(bytes);
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
		Response response = null;
		 
		// Receive and process moves until the end one is received
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));	
		while(true)
		{				
			byte[] move = reader.lines().toString().getBytes(); // TODO fix this
			response = processMove(move);
			sendResponse(response);

			// If end move, stop
			if(response.getType() == MoveType.EndMove)
			{
				break;
			}
		}		
	}

	/**
	 * This method interprets the move sent across the network and attempts
	 * to process it 
	 * @param bytes the move received from across the network
	 * @return the response message
	 */
	private Response processMove(byte[] bytes)
	{
		String response = null;	
		Request req = requestDeserialiser.deserialise(bytes);
		Response resp = new Response();
		byte[] rawMsg = req.getMsg().getBytes();
		
		// Switch on message type to interpret the move, then process the move
		// and receive the response
		switch(req.getType())
		{
			case BuildRoad:
				response = game.buildRoad(requestDeserialiser.getBuildRoadMove(rawMsg));
				break;
			case BuildSettlement:
				response = game.buildSettlement(requestDeserialiser.getBuildSettlementMove(rawMsg));
				break;
			case MoveRobber:
				response = game.moveRobber(requestDeserialiser.getMoveRobberMove(rawMsg));
				break;
			case UpgradeSettlement:
				response = game.upgradeSettlement(requestDeserialiser.getUpgradeSettlementMove(rawMsg));
				break;
			case BuyDevelopmentCard:
				DevelopmentCard c = null;
				response = game.buyDevelopmentCard(requestDeserialiser.getBuyDevelopmentCardMove(rawMsg), c);
				rawMsg = DevelopmentCardSerialiser.serialise(c).getBytes();
				break;
			case PlayDevelopmentCard:
				response = processDevelopmentCard(requestDeserialiser.getPlayDevelopmentCardMove(rawMsg));
				break;
			case EndMove:
				response = game.changeTurn();			
				break;
				
			default:
				break;
			
		}

		// Set up response object
		resp.setType(req.getType());
		resp.setResponse(response);
		resp.setMsg(rawMsg.toString());
		
		// Return response to be sent back to clients
		return resp;
	}
	
	/**
	 * Processes the type of development card being played
	 * @param move the move
	 * @return the response
	 */
	private String processDevelopmentCard(PlayDevelopmentCardMove move)
	{
		//TODO rollback if individual component in overall move fails
		
		// Update player's inventory and ensure card can be played
		String response = game.playDevelopmentCard(move);
		if(response.equals("ok"))
		{
			// If valid, process internal message 
			byte[] bytes = move.getMoveAsJson().getBytes();
			switch(move.getCard().getType())
			{
				case Knight:
					response = game.moveRobber(requestDeserialiser.getMoveRobberMove(bytes));
					break;
				case Library:
					response = game.playLibraryCard();
					break;
				case University:
					response = game.playUniversityCard();
					break;
				case Monopoly:
					response = game.playMonopolyCard(requestDeserialiser.getPlayMonopolyCardMove(bytes));
					break;
				case RoadBuilding:
					response = game.playBuildRoadsCard(requestDeserialiser.getPlayRoadBuildingCardMove(bytes));
					break;
				case YearOfPlenty:
					response =  game.playYearOfPlentyCard(requestDeserialiser.getPlayYearOfPlentyCardMove(bytes));
					break;
				default:
					break;
				
			}
		}
		
		return response;
	}

	/**
	 * Sends response out to each client so that they may
	 * update their boards
	 * @param response the response message from the last action
	 * @throws IOException 
	 */
	private void sendResponse(Response response) throws IOException
	{
		// Serialise response to be sent back to clients
		byte[] bytes = responseSerialiser.serialise(response);
		
		// If ok, propogate out to everyone. Otherwise just send
		// error response to the current player.
		if(response.getResponse().equals("ok"))
		{
			// For each socket, write the response
			for(Socket conn : connections.values())
			{
				conn.getOutputStream().write(bytes);
				conn.getOutputStream().flush();
			}
		}
		else
		{
			Colour colour = game.getCurrentPlayer().getColour();
			Socket socket = connections.get(colour);
			socket.getOutputStream().write(bytes);
		}
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
			// game.processMove(s.receiveMove());
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
