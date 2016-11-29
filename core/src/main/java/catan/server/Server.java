package catan.server;

import catan.game.Game;
import catan.game.players.NetworkPlayer;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Server
{
	private Game game;
	private int numConnections;
	private List<Socket> connections;
	private ServerSocket serverSocket;
	private static final int PORT = 12345;
	
	public Server()
	{
		game = new Game();
		connections = new ArrayList<Socket>(Game.NUM_PLAYERS);
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
			
			/*
			 * while((game.processMove(s.receiveMove()) != END)
			 * {
			 * 
			 * }
			 */
			//game.changeTurn();
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
				game.addNetworkPlayer(connection.getInetAddress());
				
				connections.add(connection);
				System.out.println(String.format("Player %d connected", numConnections));
			}
		}
		
		System.out.println("All Players connected. Starting game...\n");
	}
}
