package main.java.game;

import main.java.enums.*;
import main.java.exceptions.*;
import main.java.game.build.*;
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
	private Player currentPlayer;
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
		setCurrentPlayer(getPlayers()[dice]);
	}

	/**
	 * Assigns resources to each player based upon their settlements and the dice
	 * @param dice the dice roll
	 */
	public void allocateResources(int dice)
	{
		int resourceLimit = 7;
		
		// for each player
		for(Player player : getPlayers())
		{
			Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
			
			// If 7, check that no one is above the resource limit
			if(dice == resourceLimit)
			{
				checkResources(player);
				continue;
			}
			
			// for each of this player's settlements
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
	 * Checks if a player has more than 7 resource cards.
	 * 
	 * If so, cards are randomly removed until the player has 7 again.
	 * @param player the player
	 */
	private void checkResources(Player player)
	{
		Random rand = new Random();
		int resourceLimit = 7;
		int numResources = player.getNumResources();
		Map<ResourceType, Integer> resources = player.getResources();
		
		// Remove resources until the cap is reached
		while(numResources > resourceLimit)
		{
			ResourceType key = (ResourceType) resources.keySet().toArray()[rand.nextInt(resources.size())];
			
			if(resources.get(key) > 0)
			{
				resources.put(key, resources.get(key) - 1);
				numResources--;
			}
		}
		
	}

	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 */
	public String upgradeSettlement(UpgradeSettlementMove move)
	{
		Player p = players.get(move.getPlayerColour());
		Node n = grid.nodes.get(new Point(move.getX(), move.getY()));
		
		// Try to upgrade settlement
		try
		{
			p.upgradeSettlement(n);
		}
		catch (CannotUpgradeException | CannotAffordException e)
		{
			// Error
			return e.getMessage();
		}
		
		// Return success message
		return "ok";
	}
	
	/**
	 * Checks that the player can build a settlement at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 */
	public String buildSettlement(BuildSettlementMove move)
	{
		Player p = players.get(move.getPlayerColour());
		Node n = grid.nodes.get(new Point(move.getX(), move.getY()));
		
		// Try to build settlement
		try
		{
			p.buildSettlement(n);
		}
		catch (IllegalPlacementException | CannotAffordException e) 
		{
			// Error
			return e.getMessage();
		}
		
		// Return success message
		return "ok";
	}
	
	/**
	 * Checks that the player can buy a development card
	 * @param move
	 * @return the response message to the client
	 */
	public String playDevelopmentCard(PlayDevelopmentCardMove move)
	{
		Player p = players.get(move.getPlayerColour());
		
		// Try to play card
		try
		{
			p.playDevelopmentCard(move.getCard());
		}
		catch (DoesNotOwnException e) 
		{
			// Error
			return e.getMessage();
		}
		
		// Return success message
		return "ok";
	}
	
	/**
	 * Checks that the player can buy a development card
	 * @param move
	 * @return the response message to the client
	 */
	public String buyDevelopmentCard(BuyDevelopmentCardMove move, DevelopmentCard card)
	{
		Player p = players.get(move.getPlayerColour());
		DevelopmentCard c = null;
		
		// Try to buy card
		try
		{
			c = p.buyDevelopmentCard();
		}
		catch (CannotAffordException e) 
		{
			// Error
			return e.getMessage();
		}
		
		// Return with "ok" status and set the card parameter
		card = c;
		return "ok";
	}
	
	/**
	 * Moves the robber and takes a card from the player
	 * who has a settlement on the hex
	 * @param move the move
	 * @return success message
	 */
	public String moveRobber(MoveRobberMove move)
	{
		// Swap robber and retrieve the new hex it's been moved to.
		Hex hexWithRobber = grid.swapRobbers(move.getX(), move.getY());
		List<Node> nodes = hexWithRobber.getNodes();
		
		//TODO swap cards with player or something. Verify this player
		// can take from the specified one
		// else throw exception?
		
		// return success message
		return "ok";
	}
	
	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 */
	public String buildRoad(BuildRoadMove move)
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
		catch (CannotBuildRoadException | RoadExistsException | CannotAffordException e)
		{
			return e.getMessage();
		}
		
		checkLongestRoad();
		
		// return success message
		return "ok";
	}
	
	/**
	 * Checks who has the longest road
	 */
	private void checkLongestRoad()
	{
		Player current = players.get(getCurrentPlayer());
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
			playerWithLongestRoad = getCurrentPlayer();
			current.setHasLongestRoad(true);
		}
	}

	/**
	 * Toggles a player's turn
	 * @return 
	 */
	public String changeTurn()
	{
		setCurrentPlayer(getPlayers()[++current % NUM_PLAYERS]);
		return "ok";
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

	public Colour addNetworkPlayer(InetAddress inetAddress)
	{
		NetworkPlayer p = new NetworkPlayer(Colour.values()[numPlayers++]);
		p.setInetAddress(inetAddress);
		
		players.put(p.getColour(), p);
		
		return p.getColour();
	}

	public void addPlayer(Player p)
	{
		players.put(p.getColour(), p);
	}

	/**
	 * @return the currentPlayer
	 */
	public Player getCurrentPlayer()
	{
		return currentPlayer;
	}

	/**
	 * @param currentPlayer the currentPlayer to set
	 */
	public void setCurrentPlayer(Player currentPlayer)
	{
		this.currentPlayer = currentPlayer;
	}
}
