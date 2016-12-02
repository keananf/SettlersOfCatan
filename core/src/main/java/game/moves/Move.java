package main.java.game.moves;

import main.java.enums.*;

/**
 * Class describing an abstract move type
 * @author 140001596
 */
public abstract class Move
{
	private Colour playerColour;
	private MoveType type;
	
	/**
	 * @return the type
	 */
	public MoveType getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(MoveType type)
	{
		this.type = type;
	}

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
