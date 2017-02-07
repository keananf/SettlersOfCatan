package game.build;

import enums.Colour;
import enums.ResourceType;
import grid.Node;

import java.util.HashMap;
import java.util.Map;

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

	private City(){}

	/**
	 * @return a map containing the total cost for all resources
	 */
	public Map<ResourceType, Integer> getCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		resources.put(ResourceType.Grain, 2);
		resources.put(ResourceType.Ore, 3);
		
		return resources;
	}
	
	public static Map<ResourceType, Integer> getCityCost()
	{
		return new City().getCost();
	}
}
