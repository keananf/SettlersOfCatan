package game.build;

import enums.Colour;
import grid.Node;

/**
 * Abstract class describing a building (either a settlement or city)
 * 
 * @author 140001596
 */
public abstract class Building
{
	protected Node node;
	private Colour playerColour;

	public Building(Node node, Colour colour)
	{
		this.node = node;
		this.playerColour = colour;
	}

	protected Building()
	{}

	/**
	 * @return the node
	 */
	public Node getNode()
	{
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(Node node)
	{
		this.node = node;
	}

	/**
	 * @return the playerColour
	 */
	public Colour getPlayerColour()
	{
		return playerColour;
	}
<<<<<<< HEAD

	/**
	 * @param playerColour the playerColour to set
	 */
	public void setPlayerColour(Colour playerColour)
	{
		this.playerColour = playerColour;
	}

=======
>>>>>>> 20a0aa4f32b028697b1ebe1aaaac03aa0f7861bd
}
