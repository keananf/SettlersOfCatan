package exceptions;

import enums.ResourceType;
import intergroup.resource.Resource;

import java.util.*;

@SuppressWarnings("serial")
public class CannotAffordException extends Exception
{
	private String msg;
	private Map<ResourceType, Integer> resources;
	private Map<ResourceType, Integer> cost;

	public CannotAffordException(Map<ResourceType, Integer> resources, Map<ResourceType, Integer> cost)
	{
		this.resources = resources;
		this.cost = cost;
	}

	public CannotAffordException(Map<ResourceType, Integer> resources, Resource.Counts offer)
	{
		Map<ResourceType, Integer> cost = new HashMap<>();
		cost.put(ResourceType.Brick, offer.getBrick());
		cost.put(ResourceType.Wool, offer.getWool());
		cost.put(ResourceType.Ore, offer.getOre());
		cost.put(ResourceType.Grain, offer.getGrain());
		cost.put(ResourceType.Lumber, offer.getLumber());

		this.resources = resources;
		this.cost = cost;
	}

	public CannotAffordException(String msg)
	{
		this.msg = msg;
	}

	public String getMessage()
	{
		if (msg != null) { return msg; }

		return String.format("Cannot afford due to resource: %s.", getInsufficientResource().toString());
	}

	private ResourceType getInsufficientResource()
	{
		for (ResourceType r : ResourceType.values())
		{
			if (cost.containsKey(r) && resources.containsKey(r) && cost.get(r) > resources.get(r)) { return r; }
		}

		return ResourceType.Generic;
	}
}
