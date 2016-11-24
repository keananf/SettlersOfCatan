package main.java.game.players;

import java.awt.Point;
import java.util.*;

import main.java.board.Edge;
import main.java.board.Node;
import main.java.game.build.*;
import main.java.game.enums.*;
import main.java.game.exceptions.CannotAffordException;
import main.java.game.exceptions.CannotBuildRoadException;
import main.java.game.exceptions.CannotUpgradeException;
import main.java.game.moves.Move;

/**
 * Abstract class describing a player (AI, or network)
 * @author 140001596
 */
public abstract class Player 
{
	private int vp; // Victory points
	private Colour colour;
	private Map<ResourceType, Integer> resources;
	private List<List<Road>> roads; 
	private HashMap<Point, Building> settlements;
	private int knightsUsed;
	private boolean hasLongestRoad;
	private List<DevelopmentCard> cards;

	private static final int THRESHHOLD = 10;
	
	public Player(Colour colour)
	{
		this.colour = colour;
		roads = new ArrayList<List<Road>>();
		settlements = new HashMap<Point, Building>();
		resources = new HashMap<ResourceType, Integer>();
		cards = new ArrayList<DevelopmentCard>();
		
		// Initialise resources
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.None) continue;
			resources.put(r, 0);
		}
	}
	
	public abstract Move receiveMove();
	
	/**
	 * @return the length of this player's longest road
	 */
	public int calcRoadLength()
	{	
		int longest = 0;
		for(List<Road> list : roads)
		{
			if (list.size() > longest) 
				longest = list.size();
		}
		
		return longest;
	}

	/**
	 * Attempts to build a road for this player
	 * @param edge the edge to build the road on
	 * @throws CannotAffordException if the player cannot afford it
	 * @throws CannotBuildRoadException if it is not a valid place to build a road
	 */
	public void buildRoad(Edge edge) throws CannotAffordException, CannotBuildRoadException
	{
		boolean valid = false;
		List<Integer> listsAddedTo = new ArrayList<Integer>();
		Road r = new Road(edge, colour);

		// Check the location is valid for building and that the player can afford it
		if(r.getEdge().isNearSettlement(settlements))
		{
			canAfford(r.getCost());
		}
		else throw new CannotBuildRoadException(r);

		valid = checkRoadsAndAdd(r, listsAddedTo);
		
		// If valid place to put road
		if(valid || listsAddedTo.size() == 0)
		{
			spendResources(r.getCost());
			edge.setRoad(r);
			
			// If not connected to any other roads
			if (listsAddedTo.size() == 0)
			{
				List<Road> newList = new ArrayList<Road>();
				newList.add(r);
				roads.add(newList);
			}

			// merge lists if necessary
			else if(listsAddedTo.size() > 1)
				mergeRoads(r, listsAddedTo);
		}
	}

	/**
	 * Checks the new road against all other lists of connected roads.
	 * 
	 * If it is connected to one of the connected lists, then
	 * it is added and all the members' of that list are updated to reflect
	 * the new chain length
	 * @param r the new road
	 * @param listsAddedTo the list which records if more than one list was added to
	 * (i.e. this new road connects two previously separate ones)
	 * @return boolean dictating whether or not this method was successful
	 */
	private boolean checkRoadsAndAdd(Road r, List<Integer> listsAddedTo)
	{
		boolean isConnected = false, valid = false;
		int index = 0;
		
		// Check if this road is adjacent to any others
		for (List<Road> list : roads)
		{
			isConnected  = false;
			for(Road road : list)
			{
				// If they're connected, update the length
				if (r.isConnected(road))
				{
					isConnected = true;
					listsAddedTo.add(index++);
					break;
				}
			}
			
			// Update list
			if(isConnected)
			{
				list.add(r);
				valid = true;
			}
		}
		
		return valid;
	}

	/**
	 * Merges two roads together if a road was recently built which connects
	 * to previously separate ones
	 * @param r the new road
	 * @param listsAddedTo the number of lists (of adjacent roads) it was added to 
	 */
	private void mergeRoads(Road r, List<Integer> listsAddedTo)
	{
		for(int i = 1; i < listsAddedTo.size(); i++)
		{
			List<Road> last = roads.get(listsAddedTo.get(i - 1));
			List<Road> current = roads.get(listsAddedTo.get(i));
			
			// Remove the duplicate road in one of the lists and add it to the other one.
			last.remove(r);
			current.addAll(last);
			
			roads.remove(last);
		}
	}

	/**
	 * Attempts to build a settlement for this player
	 * @param node the node to build the settlement on
	 * @throws CannotAffordException
	 */
	public void buildSettlement(Node node) throws CannotAffordException
	{
		Settlement s = new Settlement(node, colour);
		spendResources(s.getCost());
		node.setSettlement(s);
		
		settlements.put(new Point(node.getX(), node.getY()), s);
	}
	

	/**
	 * Attempts to purchase a development card for this player
	 * @throws CannotAffordException
	 */
	public void buyDevelopmentCard() throws CannotAffordException
	{
		DevelopmentCard card = DevelopmentCard.chooseRandom();
		
		// Try to buy a development card
		spendResources(card.getCost());		
		cards.add(card);
	}
	
	public void spendDevelopmentCard()
	{
		
	}
	
	/**
	 * Attempts to upgrade a settlement for this player
	 * @param node the node to build the settlement on
	 * @throws CannotAffordException
	 */
	public void upgradeSettlement(Node node) throws CannotAffordException, CannotUpgradeException
	{
		Point p = new Point(node.getX(), node.getY());
		
		// If settlement doesn't yet exist
		if(!settlements.containsKey(p))
			throw new CannotUpgradeException(node.getX(), node.getY());
			
		// Otherwise build city
		City c = new City(node, colour);
		spendResources(c.getCost());
		node.setSettlement(c);
		
		settlements.put(p, c);
	}
	
	/**
	 * Grants resources to the player
	 * @param newResources a map of resources to give to the player
	 */
	public void grantResources(Map<ResourceType, Integer> newResources)
	{
		// Add each new resource and its amount to the player's resource bank
		for(ResourceType r : newResources.keySet())
		{
			int value = newResources.get(r);
			int existing = resources.get(r);
			
			// Add to overall resource bank
			resources.put(r, value + existing);
		}
	}	
	
	/**
	 * Spends resources to the player
	 * @param cost a map of resources describing the IBuildable that the player
	 * wants to construct
	 * @throws CannotAffordException if the player does not have enough resources
	 */
	public void spendResources(Map<ResourceType, Integer> cost) throws CannotAffordException
	{
		canAfford(cost);
		
		// Subtract each resource and its amount from the player's resource bank
		for(ResourceType r : cost.keySet())
		{
			int value = cost.get(r);
			int existing = resources.get(r);
			
			// Add to overall resource bank
			resources.put(r, existing - value);
		}
	}
	
	/**
	 * Checks to see if the user canAfford something
	 * @param cost
	 * @throws CannotAffordException
	 */
	private boolean canAfford(Map<ResourceType, Integer> cost) throws CannotAffordException
	{
		// Check if the player can afford this before initiating the purchase
		for(ResourceType r : cost.keySet())
		{
			if(resources.get(r) < cost.get(r))
				throw new CannotAffordException(r, resources.get(r), cost.get(r));
		}
		
		return true;
	}
	
	/**
	 * @return the victory points for this player
	 */
	public int getVp()
	{
		return vp;
	}

	/**
	 * This function increments a player's vp
	 */
	public void setVp(int amount)
	{
		vp += amount;
	}

	/**
	 * @return the colour
	 */
	public Colour getColour()
	{
		return colour;
	}

	/**
	 * @param colour the colour to set
	 */
	public void setColour(Colour colour)
	{
		this.colour = colour;
	}
	
	/**
	 * @return whether or not the player has enough victory points to win.
	 */
	public boolean hasWon()
	{
		return vp >= THRESHHOLD;
	}

	/**
	 * @return the player's resources
	 */
	public Map<ResourceType, Integer> getResources()
	{
		return resources;
	}
	
	/**
	 * @return the settlements the player has built
	 */
	public HashMap<Point, Building> getSettlements()
	{
		return settlements;
	}

	/**
	 * @return the development cards in this player's hand
	 */
	public List<DevelopmentCard> getDevelopmentCards()
	{
		return cards;
	}
	
	/**
	 * @return the roads the player has built
	 */
	public List<Road> getRoads()
	{
		List<Road> total = new ArrayList<Road>();
		
		for(List<Road> list : roads)
			total.addAll(list);
		
		return total;
	}

	/**
	 * @return the hasLongestRoad
	 */
	public boolean hasLongestRoad()
	{
		return hasLongestRoad;
	}

	/**
	 * @param hasLongestRoad the hasLongestRoad to set
	 */
	public void setHasLongestRoad(boolean hasLongestRoad)
	{
		this.hasLongestRoad = hasLongestRoad;
	}
}
