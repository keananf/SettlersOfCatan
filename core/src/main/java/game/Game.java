package main.java.game;

import main.java.game.build.*;
import main.java.game.enums.*;
import main.java.game.exceptions.*;
import main.java.game.moves.*;
import main.java.game.players.*;

import java.awt.Point;
import java.net.InetAddress;
import java.util.*;

import main.java.board.*;

public class Game
{
	private HexGrid grid;
	private Map<Colour, Player> players;
	Random dice;
	Player currentPlayer;
	private int current; // index of current player
	private Player p;
	private Player playerWithLongestRoad;
	private int longestRoad;
	private int numPlayers;
	public static final int NUM_PLAYERS = 4;
	
	public Game()
	{
		grid = new HexGrid();
		players = new HashMap<Colour, Player>(); 
		dice = new Random();
	}

	/**
	 * Chooses first player.
	 */
	public void chooseFirstPlayer()
	{
		int dice = this.dice.nextInt(NUM_PLAYERS);
		
		current = dice;
		currentPlayer = getPlayers()[dice];
	}

	/**
	 * Assigns resources to each player based upon their settlements and the dice
	 * @param dice the dice roll
	 */
	public void allocateResources(int dice)
	{
		// for each player
		for(Player player : getPlayers())
		{
			Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
			
			// for each of the players settlements
			for(Building building : player.getSettlements().values())
			{
				int amount = building instanceof City ? 2 : 1;
				List<Hex> hexes = ((Building)building).getNode().getHexes();
				
				// for each hex on this settlement
				for(Hex hex : hexes)
				{
					// If the hex's chit is equal to the dice roll
					if(hex.getChit() == dice && !hex.hasRobber())
					{
						grant.put(hex.getResource(), amount);
					}
				}
			}
			player.grantResources(grant); // Will be overriden in each type of player's implementation
		}
	}

	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 */
	public byte[] buildCity(BuildCityMove move)
	{
		Player p = players.get(move.getPlayerColour());
		Node n = grid.nodes.get(new Point(move.getX(), move.getY()));
		
		try
		{
			p.upgradeSettlement(n);
		}
		catch (CannotUpgradeException e)
		{
			// TODO construct error message to give to the server
			// to be sent to the client
			e.printStackTrace();
		}
		catch (CannotAffordException e2)
		{
			// TODO construct error message to give to the server
			// to be sent to the client
			e2.printStackTrace();
		}
		
		// return success message
		return new byte[]{};
	}
	
	/**
	 * Checks that the player can build a settlement at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 */
	public byte[] buildSettlement(BuildSettlementMove move)
	{
		Player p = players.get(move.getPlayerColour());
		Node n = grid.nodes.get(new Point(move.getX(), move.getY()));
		
		try
		{
			p.buildSettlement(n);
		}
		catch (CannotAffordException e)
		{
			// TODO construct error message to give to the server
			// to be sent to the client
			e.printStackTrace();
		}
		
		// return success message
		return new byte[]{};
	}
	
	/**
	 * Moves the robber and takes a card from the player
	 * who has a settlement on the hex
	 * @param move the move
	 * @return success message
	 */
	public byte[] moveRobber(MoveRobberMove move)
	{
		Hex hexWithRobber = grid.swapRobbers(move.getX(), move.getY());
		List<Node> nodes = hexWithRobber.getNodes();
		
		
		// return success message
		return new byte[]{};
	}
	
	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 */
	public byte[] buildRoad(BuildRoadMove move)
	{
		Player p = players.get(move.getPlayerColour());
		Node n = grid.nodes.get(new Point(move.getX1(), move.getY1()));
		Node n2 = grid.nodes.get(new Point(move.getX2(), move.getY2()));
		Edge edge  = null;
		
		// Find edge
		for(Edge e : n.getEdges())
		{
			if(e.getX().equals(n2) || e.getY().equals(n2))
			{
				edge = e;
				break;
			}
		}
		
		try
		{
			p.buildRoad(edge);
		}
		catch (CannotBuildRoadException e1)
		{
			// TODO construct error message to give to the server
			// to be sent to the client
			e1.printStackTrace();
		}
		catch (CannotAffordException e2)
		{
			// TODO construct error message to give to the server
			// to be sent to the client
			e2.printStackTrace();
		}
		
		checkLongestRoad();
		
		// return success message
		return new byte[]{};
	}
	
	/**
	 * Checks who has the longest road
	 */
	private void checkLongestRoad()
	{
		Player current = players.get(currentPlayer);
		Player longestRoadPlayer = players.get(playerWithLongestRoad);
		
		int length = current.calcRoadLength();
		if(length > longestRoad)
		{
			// Update victory points
			if(longestRoad >= 5)
			{
				longestRoadPlayer.setVp(longestRoadPlayer.getVp() - 2);
			}
			if (length >= 5) current.setVp(current.getVp() + 2);
			
			longestRoad = length;
			longestRoadPlayer.setHasLongestRoad(false);
			playerWithLongestRoad = currentPlayer;
			current.setHasLongestRoad(true);
		}
	}

	/**
	 * Toggles a player's turn
	 */
	public void changeTurn()
	{
		currentPlayer = getPlayers()[++current % NUM_PLAYERS];
	}

	/**
	 * Generates a random roll between 1 and 12
	 */
	public int generateDiceRoll()
	{
		return dice.nextInt(12) + 1;
	}

	/**
	 * Looks to see if any player has won
	 * @return true if a player has won
	 */
	public boolean isOver()
	{
		for(Player p : getPlayers())
		{
			if(p.hasWon()) return true;
		}
		
		return false;
	}

	/**
	 * @return the grid
	 */
	public HexGrid getGrid()
	{
		return grid;
	}
	
	public Player[] getPlayers()
	{
		return players.values().toArray(new Player[]{});
	}

	public void addNetworkPlayer(InetAddress inetAddress)
	{
		NetworkPlayer p = new NetworkPlayer(Colour.values()[numPlayers++]);
		p.setInetAddress(inetAddress);
		
		players.put(p.getColour(), p);
	}

	public void addPlayer(Player p)
	{
		players.put(p.getColour(), p);
	}
}
