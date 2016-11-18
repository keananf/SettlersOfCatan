package board;

import game.enums.ResourceType;

/**
 * Class representing an individual hex 
 * @author 140001596
 */
public class Hex extends BoardElement
{
	private ResourceType resource;
	private int diceRoll;
	private boolean hasRobber;
	
	public Hex(int x, int y)
	{
		super(x, y);
		resource = ResourceType.None;
	}

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
	
	/**
	 * @return the diceRoll
	 */
	public int getChit() 
	{
		return diceRoll;
	}
	
	/**
	 * @param diceRoll the diceRoll to set
	 */
	public void setDiceRoll(int diceRoll) 
	{
		this.diceRoll = diceRoll;
	}
	
	public boolean hasRobber()
	{
		return hasRobber;
	}
	
	public void toggleRobber()
	{
		hasRobber = !hasRobber;
	}
}
