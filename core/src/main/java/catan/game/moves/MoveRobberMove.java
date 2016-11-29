package catan.game.moves;

import catan.game.enums.Colour;

public class MoveRobberMove extends Move
{
	private int x, y; // Hex's x and y coordinates
	private Colour colourToTakeFrom;
	
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

	/**
	 * @return the colourToTakeFrom
	 */
	public Colour getColourToTakeFrom()
	{
		return colourToTakeFrom;
	}

	/**
	 * @param colourToTakeFrom the colourToTakeFrom to set
	 */
	public void setColourToTakeFrom(Colour colourToTakeFrom)
	{
		this.colourToTakeFrom = colourToTakeFrom;
	}
	
}
