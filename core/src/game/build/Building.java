package game.build;

import java.util.*;

import game.enums.*;
import board.*;

/**
 * Abstract class describing a building (either a settlement or city)
 * @author 140001596
 */
public abstract class Building implements IBuildable
{
	private Node node;
	private Colour playerColour;
	
	public Building(Node node, Colour colour)
	{
		this.node = node;
		this.playerColour = colour;
	}
	
	protected Building(){}
	
	/**
	 * Calculates the total resources gained for this settlement
	 * @return a map of resource types to number of cards gained.
	 */
	public Map<ResourceType, Integer> calculateResources(int diceRoll)
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		int numOfResource = this instanceof City ? 2 : 1;
		
		// For each hex this settlement borders
		for(Hex hex : node.getHexes())
		{
			if(!hex.hasRobber() && hex.getChit() == diceRoll)
				resources.put(hex.getResource(), numOfResource);
		}
		
		return resources;
	}
	
	
	/**
	 * @return the node
	 */
	public Node getNode()
	{
		return node;
	}
	
	/**
	 * @param node the node to set
	 */
	public void setNode(Node node)
	{
		this.node = node;
	}

	/**
	 * @return the playerColour
	 */
	public Colour getPlayerColour()
	{
		return playerColour;
	}

	/**
	 * @param playerColour the playerColour to set
	 */
	public void setPlayerColour(Colour playerColour)
	{
		this.playerColour = playerColour;
	}
}
