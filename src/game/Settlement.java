package game;

import game.enums.ResourceType;

import java.util.*;

/**
 * Class describing a settlement
 * @author 140001596
 */
public class Settlement extends Building
{
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

}
