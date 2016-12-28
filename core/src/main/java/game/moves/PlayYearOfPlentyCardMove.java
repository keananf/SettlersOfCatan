package main.java.game.moves;

import main.java.enums.ResourceType;

public class PlayYearOfPlentyCardMove extends Move
{
	private ResourceType resource1, resource2;

	/**
	 * @return the r1
	 */
	public ResourceType getResource1()
	{
		return resource1;
	}

	/**
	 * @param r1 the r1 to set
	 */
	public void setResource1(ResourceType r1)
	{
		this.resource1 = r1;
	}

	/**
	 * @return the r2
	 */
	public ResourceType getResource2()
	{
		return resource2;
	}

	/**
	 * @param r2 the r2 to set
	 */
	public void setResource2(ResourceType r2)
	{
		this.resource2 = r2;
	}
}
