package game.moves;

import enums.ResourceType;

public class PlayMonopolyCardMove extends Move
{
	private ResourceType resource;

	/**
	 * @return the resource
	 */
	public ResourceType getResource()
	{
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceType resource)
	{
		this.resource = resource;
	}
}
