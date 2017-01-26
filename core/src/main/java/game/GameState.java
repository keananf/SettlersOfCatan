package game;

import board.HexGrid;
import enums.Colour;
import enums.ResourceType;
import protocol.ResourceProtos.ResourceCount;

import java.util.HashMap;
import java.util.Map;

public abstract class GameState
{
	protected HexGrid grid;
	protected Colour currentPlayer;
	protected Colour playerWithLongestRoad;
	protected int longestRoad;
	public static final int NUM_PLAYERS = 4;

	public GameState()
	{
		grid = new HexGrid();
	}

	/**
	 * Translates the protobuf representation of a resources allocation into a map.
	 * @param resources the resources received from the network
	 * @return a map of resources to number
	 */
	protected Map<ResourceType,Integer> processResources(ResourceCount resources)
	{
		Map<ResourceType,Integer> ret = new HashMap<ResourceType,Integer>();

		ret.put(ResourceType.Brick, resources.hasBrick() ? resources.getBrick() : 0);
		ret.put(ResourceType.Lumber, resources.hasLumber() ? resources.getLumber() : 0);
		ret.put(ResourceType.Grain, resources.hasGrain() ? resources.getGrain() : 0);
		ret.put(ResourceType.Ore, resources.hasOre() ? resources.getOre() : 0);
		ret.put(ResourceType.Wool, resources.hasWool() ? resources.getWool() : 0);

		return ret;
	}

	/**
	 * @return the grid
	 */
	public HexGrid getGrid()
	{
		return grid;
	}

	/**
	 * @return the currentPlayer
	 */
	public Colour getCurrentPlayer()
	{
		return currentPlayer;
	}

	/**
	 * @param currentPlayer the currentPlayer to set
	 */
	public void setCurrentPlayer(Colour currentPlayer)
	{
		this.currentPlayer = currentPlayer;
	}

	/**
	 * Sets the turn to the given colour
	 * @param colour the new turn
	 */
	public void setTurn(Colour colour)
	{
		setCurrentPlayer(colour);
	}
}
