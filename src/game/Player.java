package game;

import java.util.*;

import game.enums.*;

/**
 * Abstract class describing a player (human, AI, or network)
 * @author 140001596
 */
public abstract class Player 
{
	private int vp; // Victory points
	private Colour colour;
	private Map<ResourceType, Integer> resources;
	private HashSet<IBuildable> thingsBuilt; //TODO model this differently perhaps?
	private int knightsUsed;
	private boolean hasLongestRoad;

	private static final int THRESHHOLD = 10;
	
	
	public abstract void receiveMove(); //TODO think about how different players will interact with grid object
	
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
