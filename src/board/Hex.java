package board;

import game.enums.ResourceType;

/**
 * Class representing an individual hex 
 * @author 140001596
 */
public class Hex 
{
	private ResourceType resource;
	private int diceRoll;
	private int x; // x coordinate (column)
	private int y; // y coordinate (row)
	private boolean hasRobber;
	
	public Hex(int x, int y)
	{
		this.x = x;
		this.y = y;
		
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
	public int getDiceRoll() 
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
	
	/**
	 * @return the x
	 */
	public int getX()
	{
		return x;
	}
	
	/**
	 * @param x the x to set
	 */
	public void setX(int x)
	{
		this.x = x;
	}
	
	/**
	 * @return the y
	 */
	public int getY()
	{
		return y;
	}
	
	/**
	 * @param y the y to set
	 */
	public void setY(int y)
	{
		this.y = y;
	}

	public boolean hasRobber()
	{
		return hasRobber;
	}
	
	public void toggleRobber()
	{
		hasRobber = !hasRobber;
	}

	public boolean borders(Hex h)
	{
		int x2 = h.getX(), y2 = h.getY();
		
		// If the given hex is further away than one
		if(Math.abs(y2 - y) > 1 || Math.abs(x2 - x) > 1)
			return false;
		
		return true;
	}
}
