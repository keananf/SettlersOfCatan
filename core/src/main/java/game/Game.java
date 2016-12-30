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
		setCurrentPlayer(getPlayersAsList()[dice]);
	}

	/**
	 * Assigns resources to each player based upon their settlements and the dice
	 * @param dice the dice roll
	 * @return 
	 */
	public Map<Colour, Map<ResourceType, Integer>> allocateResources(int dice)
	{
		int resourceLimit = 7;
		Map<Colour, Map<ResourceType, Integer>> playerResources = new HashMap<Colour, Map<ResourceType, Integer>>();
		
		// for each player
		for(Player player : getPlayersAsList())
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
			player.grantResources(grant);
			playerResources.put(player.getColour(), grant);
		}
		
		return playerResources;
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
		
		// Randomly remove resources until the cap is reached
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
	 * @param move the move
	 * @param card the object to hold the new development card. 
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
	 * @return return message
	 */
	public String moveRobber(MoveRobberMove move)
	{
		// Retrieve the new hex the robber will move to.
		Hex newHex = grid.swapRobbers(move.getX(), move.getY());
		Player other = players.get(move.getColourToTakeFrom());
		boolean valid = false;

		// Verify this player can take from the specified one
		for(Node n : newHex.getNodes())
		{
			// If node has a settlement and it is of the specified colour, then the player can take a card
			// Randomly remove resource
			if(n.getSettlement() != null && n.getSettlement().getPlayerColour().equals(move.getColourToTakeFrom()))
			{
				currentPlayer.takeResource(other);
				valid = true;
			}
		}
		
		// Cannot take from this player
		if(!valid) return String.format("Player %s cannot take resource from %s.", 
							currentPlayer.getColour().toString(), other.getColour().toString());
		
		// return success message
		return "ok";
	}

	/**
	 * Process the playing of the 'Build Roads' development card.
	 * @param move the move to process
	 * @return return message
	 */
	public String playBuildRoadsCard(PlayRoadBuildingCardMove move)
	{
		buildRoad(move.getMove1());
		buildRoad(move.getMove2());
		// TODO MAKE ATOMIC
		
		return "ok";
	}
	
	/**
	 * Process the playing of the 'University' development card.
	 * @param move the move to process
	 * @return return message
	 */
	public String playUniversityCard()
	{
		grantVpPoint();
		return "ok";
	}
	
	/**
	 * Process the playing of the 'Library' development card.
	 * @param move the move to process
	 * @return return message
	 */
	public String playLibraryCard()
	{
		grantVpPoint();
		return "ok";
	}
	
	/**
	 * Process the playing of the 'Year of Plenty' development card.
	 * @param move the move to process
	 * @return return message
	 */
	public String playYearOfPlentyCard(PlayYearOfPlentyCardMove move)
	{
		// Set up grant
		ResourceType r1 = move.getResource1(), r2 = move.getResource2(); 
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		grant.put(r1, 1);
		grant.put(r2, 1);
		currentPlayer.grantResources(grant);
		
		// In receiving 'ok,' the client knows the request has been granted 
		return "ok";
	}
	
	/**
	 * Process the playing of the 'Monopoly' development card.
	 * @param move the move to process
	 * @return return message
	 */
	public String playMonopolyCard(PlayMonopolyCardMove move)
	{
		ResourceType r = move.getResource(); 
		Map<ResourceType, Integer> grant = new HashMap<ResourceType, Integer>();
		int sum = 0;
		
		// for each player
		for(Player p : players.values())
		{
			if(p.equals(currentPlayer)) continue;
			
			// Give p's resources of type 'r' to currentPlayer
			try
			{
				int num = p.getResources().get(r);
				grant.put(r, num);
				p.spendResources(grant);
				sum += num;
			} 
			catch (CannotAffordException e) { /* Will never happen */ }
			currentPlayer.grantResources(grant);
		}
		
		// Return message is string showing number of resources taken
		return String.format("%d", sum);
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
		Player current = getCurrentPlayer();
		
		int length = current.calcRoadLength();
		if(length > longestRoad)
		{
			// Update victory points
			if(longestRoad >= 5)
			{
				playerWithLongestRoad.setVp(playerWithLongestRoad.getVp() - 2);
			}
			if (length >= 5) current.setVp(current.getVp() + 2);
			if(playerWithLongestRoad != null) playerWithLongestRoad.setHasLongestRoad(false);
			
			longestRoad = length;
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
		setCurrentPlayer(getPlayersAsList()[++current % NUM_PLAYERS]);
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
		for(Player p : getPlayersAsList())
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

	public Map<Colour, Player> getPlayers()
	{
		return players;
	}
	
	public Player[] getPlayersAsList()
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

	private String grantVpPoint()
	{
		currentPlayer.setVp(currentPlayer.getVp() + 1);
		return "ok";
	}

}
