package catan.game.build;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import catan.game.enums.*;
import catan.board.*;

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
		resources.put(ResourceType.Wood, 1);
		
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
	 * @param road other road
	 */
	public boolean isConnected(Road other) {
		return false; // TODO implement
	}
}
