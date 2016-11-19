package game.build;

import game.enums.Colour;
import game.enums.ResourceType;

import java.util.*;

import board.Node;

/**
 * Class describing a city
 * @author 140001596
 */
public class City extends Building
{
	public City(Node node, Colour colour)
	{
		super(node, colour);
	}

	/**
	 * @return a map containing the total cost for all resources
	 */
	public Map<ResourceType, Integer> getCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		resources.put(ResourceType.Wheat, 2);
		resources.put(ResourceType.Stone, 3);
		
		return resources;
	}
}
