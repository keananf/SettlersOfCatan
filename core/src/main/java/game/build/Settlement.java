package main.java.game.build;

import main.java.enums.*;

import java.util.*;

import main.java.board.Edge;
import main.java.board.Node;

/**
 * Class describing a settlement
 * @author 140001596
 */
public class Settlement extends Building
{
	public Settlement(Node node, Colour colour)
	{
		super(node, colour);
	}

	private Settlement(){}

	/**
	 * @return a map containing the total cost for all resources
	 */
	public Map<ResourceType, Integer> getCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		resources.put(ResourceType.Brick, 1);
		resources.put(ResourceType.Lumber, 1);
		resources.put(ResourceType.Grain, 1);
		resources.put(ResourceType.Wool, 1);
		
		return resources;
	}

	/**
	 * Checks all nodes within two to check if any have a settlement
	 * @return true if this settlement is near another
	 */
	public boolean isNearSettlement()
	{
		// Check all paths from this node
		for(Edge e : node.getEdges())
		{
			// Get other node on this edge, check it and all its paths
			Node other = e.getX().equals(node) ? e.getY() : e.getX();

			// if the opposite end has a settlement, return true
			if(other.getSettlement() != null)
				return true;
			
		}
		
		// Not near any settlements
		return false;
	}
	
	public static Map<ResourceType, Integer> getSettlementCost()
	{
		return new Settlement().getCost();
	}
}
