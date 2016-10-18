package game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.enums.*;
import board.*;

/**
 * Class describing a road
 * @author 140001596
 */
public class Road implements IBuildable
{
	private Edge edge;
	private Colour playerColour;
	
	/**
	 * @return a map containing the total cost for all resources
	 */
	public Map<ResourceType, Integer> getCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		resources.put(ResourceType.Brick, 1);
		resources.put(ResourceType.Wood, 1);
		
		return resources;
	}
	
	/**
	 * Calculate the distance between two roads
	 * @param road the road the check
	 * @return the distance between 'this' and 'road'
	 */
	public boolean isBordering(Road road)
	{
		int x1, y1, x2, y2, x3, y3;
		
		List<Hex> borderingHexes = edge.getHexes();
		List<Hex> otherHexes = road.edge.getHexes();
		boolean valid = false;
		
		// Lists must have one element in common to be adjacent
		for(Hex hex : borderingHexes)
		{
			if(otherHexes.contains(hex))
			{
				valid = true;
				otherHexes.remove(hex);
			}
		}
		if(!valid) return false;
		
		x1 = borderingHexes.get(0).getX();
		y1 = borderingHexes.get(0).getY(); 
		
		x2 = borderingHexes.get(1).getX();
		y2 = borderingHexes.get(1).getY(); 
		
		x3 = otherHexes.get(0).getX();
		y3 = otherHexes.get(0).getY();
		
		// If any one of the hexes is more than one hex away from the other two
		if(Math.abs(y3 - y2) > 1 || Math.abs(y2 - y1) > 1
				|| Math.abs(x3 - x2) > 1 || Math.abs(x2 - x1) > 1)
			return false;
		
		return true;
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

	/**
	 * @return the edge
	 */
	public Edge getEdge()
	{
		return edge;
	}

	/**
	 * @param edge the edge to set
	 */
	public void setEdge(Edge edge)
	{
		this.edge = edge;
	}
}
