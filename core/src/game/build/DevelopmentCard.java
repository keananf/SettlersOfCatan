package game.build;

import game.enums.ResourceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing the different options of a development card
 * @author 140001596
 */
public enum DevelopmentCard implements IBuildable //TODO change to normal inheritance?
{
	Knight, // Steal 1 resource from 
	Library, // 1 VP
	University, // 1 VP
	YearOfPlenty, // Gain any 2 resources from the bank
	RoadBuilding, // Build two new roads
	Monopoly; // Every player must give over all resources of a particular type
	
	/**
	 * @return a map containing the total cost for all resources
	 */
	public Map<ResourceType, Integer> getCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		resources.put(ResourceType.Stone, 1);
		resources.put(ResourceType.Wheat, 1);
		resources.put(ResourceType.Sheep, 1);
		
		return resources;
	}
	
	public static Map<ResourceType, Integer> getCardCost()
	{
		return DevelopmentCard.Knight.getCost();
	}
}
