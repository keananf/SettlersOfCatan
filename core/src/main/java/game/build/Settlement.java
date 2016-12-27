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
		resources.put(ResourceType.Wood, 1);
		resources.put(ResourceType.Wheat, 1);
		resources.put(ResourceType.Sheep, 1);
		
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
			for(Edge e2 : other.getEdges())
			{
				// Skip 'e' as checking it is redundant
				if(e2.equals(e)) continue;
				
				// If either of these nodes have a settlement, return true
				if(e2.getX().getSettlement() != null || e2.getY().getSettlement() != null)
					return true;
			}
			
		}
		
		// Not near any settlements
		return false;
	}
	
	public static Map<ResourceType, Integer> getSettlementCost()
	{
		return new Settlement().getCost();
	}
}
