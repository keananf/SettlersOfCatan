package main.java.game.moves;

import main.java.game.enums.*;

/**
 * Class describing an abstract move type
 * @author 140001596
 */
public abstract class Move
{
	private Colour playerColour;
	
	/**
	 * @return the playerColour
	 */
	public Colour getPlayerColour()
	{
		return playerColour;
	}

	/**
	 * @param playerColour the playerColour to set
	 */
	public void setPlayerColour(Colour playerColour)
	{
		this.playerColour = playerColour;
	}
}
