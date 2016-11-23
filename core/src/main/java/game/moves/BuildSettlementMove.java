package main.java.game.moves;

import main.java.game.enums.Colour;

public class BuildSettlementMove extends Move
{
	private Colour colour;
	private int x, y;
	
	/**
	 * @return the colour
	 */
	public Colour getColour()
	{
		return colour;
	}
	/**
	 * @param colour the colour to set
	 */
	public void setColour(Colour colour)
	{
		this.colour = colour;
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
}
