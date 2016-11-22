package game.players;

import java.awt.Point;
import java.util.*;

import board.Edge;
import board.Node;
import game.build.*;
import game.enums.*;
import game.exceptions.CannotAffordException;
import game.exceptions.CannotUpgradeException;
import game.moves.Move;

/**
 * Abstract class describing a player (AI, or network)
 * @author 140001596
 */
public abstract class Player 
{
	private int vp; // Victory points
	private Colour colour;
	private Map<ResourceType, Integer> resources;
	private HashSet<Road> roads; 
	private HashMap<Point, Building> settlements;
	private int knightsUsed;
	private boolean hasLongestRoad;

	private static final int THRESHHOLD = 10;
	
	public Player(Colour colour)
	{
		this.colour = colour;
		roads = new HashSet<Road>();
		settlements = new HashMap<Point, Building>();
		resources = new HashMap<ResourceType, Integer>();
		
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
		//TODO
		return 0;
	}

	/**
	 * Attempts to build a road for this player
	 * @param edge the edge to build the road on
	 * @throws CannotAffordException
	 */
	public void buildRoad(Edge edge) throws CannotAffordException
	{
		Road r = new Road(edge, colour);
		spendResources(r.getCost());
		
		roads.add(r);
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
		
		settlements.put(new Point(node.getX(), node.getY()), s);
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
		// Check if the player can afford this before initiating the purchase
		for(ResourceType r : cost.keySet())
		{
			if(resources.get(r) < cost.get(r))
				throw new CannotAffordException(r, resources.get(r), cost.get(r));
		}
		
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
	 * @return the victory points for this player
	 */
	public int getVp()
	{
		return vp;
	}

	/**
	 * This function increments a player's vp
	 */
	public void incVp()
	{
		vp++;
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
	 * @return the roads the player has built
	 */
	public HashSet<Road> getRoads()
	{
		return roads;
	}
}
