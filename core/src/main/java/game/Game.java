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
import main.java.comm.messages.TradeMessage;

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
				player.loseResources();
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
	 * Performs basic verification and then processes the move.
	 * Returns a response message, which is either "ok" or an 
	 * exception's message.
	 * @param move the move
	 * @param type the move's type
	 * @return the response message
	 */
	public String processMove(Move move, MoveType type)
	{
		// Maintain original in case roll back is necessary
		Player player = players.get(move.getPlayerColour()), copy = player.copy();
		String response = null;
		
		// Check to see this is the player's turn
		if(!currentPlayer.getColour().equals(move.getPlayerColour()))
		{
			return String.format("It is not %s player's turn", move.getPlayerColour().toString()); 
		}
		
		try
		{
			// Process the message by switching on its type
			switch(type)
			{
				case BuildRoad:
					response = buildRoad((BuildRoadMove) move);
					break;
				case BuildSettlement:
					response = buildSettlement((BuildSettlementMove) move);
					break;
				case MoveRobber:
					response = moveRobber((MoveRobberMove) move);
					break;
				case UpgradeSettlement:
					response = upgradeSettlement((UpgradeSettlementMove) move);
					break;
				case BuyDevelopmentCard:
					response = buyDevelopmentCard((BuyDevelopmentCardMove) move);
					break;
				case EndMove:
					response = changeTurn();			
					break;
				case TradeMove:
					response = processTrade((TradeMessage) move, player);
					break;
				default:
					break;
			}
		}
		catch(Exception e)
		{
			// Error. Reset player and return exception message
			players.get(copy.getColour()).restoreCopy(copy, null);
			
			return e.getMessage();
		}
		
		// Return message
		return response;
	}
	
	/**
	 * If trade was successful, exchange of resources occurs here
	 * @param move the move object detailing the trade
	 * @return the response status
	 */
	private String processTrade(TradeMessage move, Player offerer) throws IllegalTradeException
	{
		// Find the recipient and extract the trade's contents
		Map<ResourceType, Integer> offer = move.getOfferAsMap(), request = move.getRequestAsMap();
		Player recipient = players.get(move.getRecipient()), recipientCopy = recipient.copy();
		
		try
		{
			// Exchange resources
			offerer.spendResources(offer);
			recipient.grantResources(offer);
			
			recipient.spendResources(request);
			offerer.grantResources(request);
		}
		catch(Exception e)
		{
			// Reset recipient and throw exception. Offerer is reset in above method
			recipient.restoreCopy(recipientCopy, null);
					
			throw new IllegalTradeException(offerer.getColour(), recipient.getColour());
		}
		
		return "ok";
	}

	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 * @throws CannotUpgradeException 
	 * @throws CannotAffordException 
	 */
	private String upgradeSettlement(UpgradeSettlementMove move) throws CannotAffordException, CannotUpgradeException
	{
		Player p = players.get(move.getPlayerColour());
		Node n = grid.nodes.get(new Point(move.getX(), move.getY()));
		
		// Try to upgrade settlement
		p.upgradeSettlement(n);
		return "ok";
	}
	
	/**
	 * Checks that the player can build a settlement at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 * @throws IllegalPlacementException 
	 * @throws CannotAffordException 
	 * @throws SettlementExistsException 
	 */
	private String buildSettlement(BuildSettlementMove move) throws CannotAffordException, IllegalPlacementException, SettlementExistsException
	{
		Player p = players.get(move.getPlayerColour());
		Node node = grid.nodes.get(new Point(move.getX(), move.getY()));
		
		// Try to build settlement
		p.buildSettlement(node);
		
		// Check all combinations of edges to check if a road chain was broken
		for(int i = 0; i < node.getEdges().size(); i++)
		{
			boolean broken= false;
			for(int j = 0; j < node.getEdges().size(); j++)
			{
				Edge e = node.getEdges().get(i), other = node.getEdges().get(j);
				Road r = e.getRoad(), otherR = other.getRoad();
				
				if(e.equals(other)) continue;
				
				// If this settlement is between two roads of the same colour
				if(r != null && otherR != null && r.getPlayerColour().equals(otherR.getPlayerColour()))
				{
					// retrieve owner of roads and break the road chain
					players.get(e.getRoad().getPlayerColour()).breakRoad(e, other);
					broken = true;
					break;
				}
			}
			if(broken) break;
		}
		// TODO send out updated road counts
		
		return "ok";
	}
	
	/**
	 * Checks that the player can buy a development card
	 * @param move
	 * @return the response message to the client
	 */
	private String playDevelopmentCard(PlayDevelopmentCardMove move)
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
	 * @return the response message to the client
	 * @throws CannotAffordException 
	 */
	private String buyDevelopmentCard(BuyDevelopmentCardMove move) throws CannotAffordException
	{
		Player p = players.get(move.getPlayerColour());
		
		// Try to buy card
		// Return with "ok" status and set the card parameter
		return p.buyDevelopmentCard().toString(); //TODO fix
	}
	
	
	/**
	 * Atomically processes the type of development card being played
	 * @param move the move
	 * @return the response
	 */
	public String processDevelopmentCard(PlayDevelopmentCardMove move, Move internalMove)
	{
		// Copy old player state in case rollback is needed (individual component of move failing)
		Player copy = players.get(move.getPlayerColour()).copy();
		
		// Update player's inventory and ensure card can be played
		String response = playDevelopmentCard(move);
		if(response.equals("ok"))
		{
			// If valid, try to process internal message
			try
			{
				switch (move.getCard().getType())
				{
					case Knight:
						response = moveRobber((MoveRobberMove) internalMove);
						break;
					case Library:
						response = playLibraryCard();
						break;
					case University:
						response = playUniversityCard();
						break;
					case Monopoly:
						response = playMonopolyCard((PlayMonopolyCardMove) internalMove);
						break;
					case RoadBuilding:
						response = playBuildRoadsCard((PlayRoadBuildingCardMove) internalMove);
						break;
					case YearOfPlenty:
						response = playYearOfPlentyCard((PlayYearOfPlentyCardMove) internalMove);
						break;
					default:
						break;

				}
			}
			catch(Exception e) 
			{ 
				// Error. Reset player and return exception message
				players.get(copy.getColour()).restoreCopy(copy, move.getCard());
				
				return e.getMessage(); 
			}
		}
		
		return response;
	}
	
	/**
	 * Moves the robber and takes a card from the player
	 * who has a settlement on the hex
	 * @param move the move
	 * @return return message
	 * @throws CannotStealException if the specified player cannot provide a resource 
	 */
	public String moveRobber(MoveRobberMove move) throws CannotStealException
	{
		// Retrieve the new hex the robber will move to.
		Hex newHex = grid.grid.get(new Point(move.getX(), move.getY()));
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
		if(!valid) throw new CannotStealException(move.getPlayerColour(), other.getColour());
		
		// return success message
		grid.swapRobbers(move.getX(), move.getY());
		return "ok";
	}

	/**
	 * Process the playing of the 'Build Roads' development card.
	 * @param move the move to process
	 * @return return message
	 * @throws RoadExistsException 
	 * @throws CannotBuildRoadException 
	 * @throws CannotAffordException 
	 */
	public String playBuildRoadsCard(PlayRoadBuildingCardMove move) throws CannotAffordException, CannotBuildRoadException, RoadExistsException
	{
		buildRoad(move.getMove1());
		buildRoad(move.getMove2());
		
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
			
			try
			{
				// Give p's resources of type 'r' to currentPlayer
				int num = p.getResources().get(r);
				grant.put(r, num);
				p.spendResources(grant);
				sum += num;
			} 
			catch (CannotAffordException e) { /* Will never happen */ }
			currentPlayer.grantResources(grant);
		}
		
		// Return message is string showing number of resources taken
		return String.format("%d", sum); //TODO fix
	}
	
	/**
	 * Checks that the player can build a road at the desired location, and builds it.
	 * @param move
	 * @return the response message to the client
	 * @throws RoadExistsException 
	 * @throws CannotBuildRoadException 
	 * @throws CannotAffordException 
	 */
	private String buildRoad(BuildRoadMove move) throws CannotAffordException, CannotBuildRoadException, RoadExistsException
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
		
		// Try to build the road and update the longest road 
		p.buildRoad(edge);
		checkLongestRoad(); //TODO send updated road length
		
		// return success message
		return "ok";
	}
	
	/**
	 * Checks who has the longest road
	 */
	private void checkLongestRoad()
	{
		Player current = getCurrentPlayer();
		
		// Calculate who has longest road
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
