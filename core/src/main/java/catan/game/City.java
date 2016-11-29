package catan.game.build;

import catan.game.enums.Colour;
import catan.game.enums.ResourceType;

import java.util.*;

import catan.board.Node;

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
		
		resources.put(ResourceType.Wheat, 2);
		resources.put(ResourceType.Stone, 3);
		
		return resources;
	}
	
	public static Map<ResourceType, Integer> getCityCost()
	{
		return new City().getCost();
	}
}