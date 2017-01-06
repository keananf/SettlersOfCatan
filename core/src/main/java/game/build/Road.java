package game.build;

import java.util.*;

import enums.*;
import board.*;

/**
 * Class describing a road
 * @author 140001596
 */
public class Road implements IBuildable
{
	private Edge edge;
	private Colour playerColour;
	
	public Road(Edge edge, Colour c)
	{
		playerColour = c;
		this.edge = edge;
	}
	private Road(){}
	
	/**
	 * @return a map containing the total cost for all resources
	 */
	public Map<ResourceType, Integer> getCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		resources.put(ResourceType.Brick, 1);
		resources.put(ResourceType.Lumber, 1);
		
		return resources;
	}
	
	public static Map<ResourceType, Integer> getRoadCost()
	{
		return new Road().getCost();
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
	
	/**
	 * Checks to see if two roads are connected or not
	 * @param road the road to check
	 * @return if the two roads are connected
	 */
	public boolean isConnected(Road road)
	{
		Edge thisEdge = this.edge, otherEdge = road.edge;
		
		// If these two roads share a node
		if(thisEdge.getX().equals(otherEdge.getX()) || thisEdge.getX().equals(otherEdge.getY()) 
				|| thisEdge.getY().equals(otherEdge.getX()) || thisEdge.getY().equals(otherEdge.getY()))
		{
			return true;
		}
		
		return false;
	}
}
