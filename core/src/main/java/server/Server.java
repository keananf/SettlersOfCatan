package server;

import enums.*;
import game.Game;
import game.moves.*;
import comm.*;
import comm.messages.*;

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
		Request req = requestDeserialiser.deserialiseRequest(bytes);
		Response resp = new Response();
		byte[] rawMsg = req.getMsg().getBytes();
		Move move = requestDeserialiser.deserialiseMove(rawMsg, req.getType());
		
		// Switch on message type to interpret the move, then process the move
		// and receive the response
		switch(req.getType())
		{
			// Development Cards. Need extra processing step to extract internal move
			case PlayDevelopmentCard:
				PlayDevelopmentCardMove devMove = requestDeserialiser.getPlayDevelopmentCardMove(rawMsg);
				response = game.processDevelopmentCard(devMove, requestDeserialiser.getInternalDevCardMove(devMove));
				break;
			
			case TradeMove:
				response = processTrade((TradeMessage)move, rawMsg);
				
				// if response is denied, simply break. Otherwise proceed into default behaviour
				if(response.equals(TradeStatus.Denied.toString())) break;
				
			// Other moves
			default:
				response = game.processMove(move, req.getType());
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
	 * Forwards the trade request to the other player and blocks for a response
	 * @param move the trade move
	 * @param rawMsg the serialised move, received from across the network
	 * @return the status of the trade "accepted, denied, offer"
	 */
	private String processTrade(TradeMessage move, byte[] rawMsg) 
	{
		Socket recipient = connections.get(move.getRecipient());
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(recipient.getInputStream())))
		{
			// Send message to recipient 
			recipient.getOutputStream().write(rawMsg);

			// Receive move and process
			byte[] rec = reader.lines().toString().getBytes(); // TODO fix this
					
			// Overwrite message object with response. Should be the same contents except 
			// with a different status
			move = (TradeMessage) requestDeserialiser.deserialiseMove(rec, MoveType.TradeMove);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
				
		return move.getStatus().toString();
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
