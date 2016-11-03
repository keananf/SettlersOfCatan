package game.players;

import java.util.*;

import game.build.IBuildable;
import game.build.Road;
import game.enums.*;
import game.exceptions.CannotAffordException;
import game.moves.Move;
import game.moves.Moves;

/**
 * Abstract class describing a player (human, AI, or network)
 * @author 140001596
 */
public abstract class Player 
{
	private int vp; // Victory points
	private Colour colour;
	private Map<ResourceType, Integer> resources;
	private HashSet<IBuildable> thingsBuilt; 
	private int knightsUsed;
	private boolean hasLongestRoad;

	private static final int THRESHHOLD = 10;
	
	
	public abstract Moves receiveMoves();
	
	/**
	 * @return the length of this player's longest road
	 */
	public int calcRoadLength()
	{
		HashSet<IBuildable> roads = new HashSet<IBuildable>();
		roads.addAll(thingsBuilt);
		
		// Filter out everything except the roads
		roads.removeIf((b) -> !(b instanceof Road));
		
		//TODO ensure that when building roads, they are inserted in such a way that adjacent ones are stored next to each other
		
		
		return 0;
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
}
