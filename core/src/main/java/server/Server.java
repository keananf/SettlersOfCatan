package main.java.server;

import main.java.enums.Colour;
import main.java.enums.MoveType;
import main.java.game.Game;
import main.java.comm.Request;
import main.java.comm.Response;
import main.java.comm.ResponseSerialiser;
import main.java.comm.RequestDeserialiser;

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
	private RequestDeserialiser requestDeserialiser;
	
	public Server()
	{
		game = new Game();
		connections = new HashMap<Colour, Socket>();
		responseSerialiser = new ResponseSerialiser();
		requestDeserialiser = new RequestDeserialiser();
	}
	
	public static void main(String[] args)
	{
		Server s = new Server();
		try
		{
			s.getPlayers();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("Error connecting players");
			return;
		} 
		
		s.game.chooseFirstPlayer();
		s.getInitialSettlementsAndRoads();
		
		// Main game loop
		while(!s.game.isOver())
		{
			int dice = s.game.generateDiceRoll();
			s.game.allocateResources(dice);
			//TODO send resources and dice out to players
			
			s.receiveMoves();
		}
	}

	/**
	 * Listens for moves from the current player
	 * @return the bytes received from the current player
	 */
	private void receiveMoves()
	{
		Colour colour = game.getCurrentPlayer().getColour();
		Socket socket = connections.get(colour);
		Response response = null;
		
		// Try to read this players moves 
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())))
		{	
			// Receive and process moves until the end one is received
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
		catch (IOException e)
		{
			e.printStackTrace();
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
		
		// Switch on message type to interpret the move, then process the move
		// and receive the response
		switch(req.getType())
		{
			case BuildRoad:
				response = game.buildRoad(requestDeserialiser.getBuildRoadMove(bytes));
				break;
			case BuildSettlement:
				response = game.buildSettlement(requestDeserialiser.getBuildSettlementMove(bytes));
				break;
			case MoveRobber:
				response = game.moveRobber(requestDeserialiser.getMoveRobberMove(bytes));
				break;
			case UpgradeSettlement:
				response = game.upgradeSettlement(requestDeserialiser.getUpgradeSettlementMove(bytes));
				break;
			case BuyDevelopmentCard:
				response = game.buyDevelopmentCard(requestDeserialiser.getBuyDevelopmentCardMove(bytes));
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
		resp.setMsg(bytes.toString());
		
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
