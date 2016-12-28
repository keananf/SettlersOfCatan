package main.java.exceptions;

import main.java.enums.ResourceType;

@SuppressWarnings("serial")
public class CannotAffordException extends Exception
{
	ResourceType resource;
	int cost, existing;
	
	public CannotAffordException(ResourceType r, int existing, int cost)
	{
		resource = r;
		this.existing = existing;
		this.cost = cost;
	}

	public String getMessage()
	{
		return String.format("Cannot afford due to resource: %s. Cost: %d, Player has: %d", resource.toString(), cost, existing);
	}
}
