package main.java.game.build;

import main.java.game.enums.*;

import java.util.*;

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

	public static Map<ResourceType, Integer> getSettlementCost()
	{
		return new Settlement().getCost();
	}
}
