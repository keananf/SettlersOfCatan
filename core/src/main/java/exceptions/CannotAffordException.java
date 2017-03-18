package exceptions;

import enums.ResourceType;
import intergroup.resource.Resource;

import java.util.*;

@SuppressWarnings("serial")
public class CannotAffordException extends Exception
{
	Map<ResourceType, Integer> resources, cost;

	public CannotAffordException(Map<ResourceType, Integer> resources, Map<ResourceType, Integer> cost)
	{
		this.resources = resources;
		this.cost = cost;
	}

	public CannotAffordException(Map<ResourceType, Integer> resources, Resource.Counts offer)
	{
		Map<ResourceType, Integer> cost = new HashMap<ResourceType, Integer>();
		cost.put(ResourceType.Brick, offer.getBrick());
		cost.put(ResourceType.Wool, offer.getWool());
		cost.put(ResourceType.Ore, offer.getOre());
		cost.put(ResourceType.Grain, offer.getGrain());
		cost.put(ResourceType.Lumber, offer.getLumber());

		this.resources = resources;
		this.cost = cost;
	}

	public String getMessage()
	{
		return String.format("Cannot afford due to resource: %s.", getInsufficientResource().toString());
	}

	private ResourceType getInsufficientResource()
	{
		for (ResourceType r : ResourceType.values())
		{
			if (cost.containsKey(r) && cost.get(r) > resources.get(r)) { return r; }
		}

		return ResourceType.Generic;
	}
}
