package game;

import board.Edge;
import board.HexGrid;
import board.Node;
import enums.Colour;
import enums.ResourceType;
import game.build.Road;
import game.players.Player;
import protocol.ResourceProtos.ResourceCount;

import java.util.HashMap;
import java.util.Map;

public abstract class GameState
{
	protected HexGrid grid;
	protected Map<Colour, Player> players;
	protected Colour currentPlayer;
	protected Colour playerWithLongestRoad;
	protected int longestRoad;
	public static final int NUM_PLAYERS = 4;
	public static final int MIN_ROAD_LENGTH = 5;

	public GameState()
	{
		grid = new HexGrid();
		players = new HashMap<Colour, Player>();
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
	 * Checks and updates who has the longest road
	 * @param broken if this method is being called after a road was broken
	 */
	protected void checkLongestRoad(boolean broken)
	{
		Player playerWithLongestRoad = players.get(this.playerWithLongestRoad);

		// Calculate who has longest road
		for(Colour c : Colour.values())
		{
			if(!players.containsKey(c))
				continue;

			Player player = players.get(c);
			int length = player.calcRoadLength();
			if(length > longestRoad || (broken && c.equals(currentPlayer)))
			{
				// Update victory points
				if(longestRoad >= MIN_ROAD_LENGTH)
				{
					playerWithLongestRoad.addVp(-2);
				}
				if (length >= MIN_ROAD_LENGTH) player.addVp(2);
				if(playerWithLongestRoad != null) playerWithLongestRoad.setHasLongestRoad(false);

				longestRoad = length;
				this.playerWithLongestRoad = currentPlayer;
				player.setHasLongestRoad(true);
			}
		}
	}

	/**
	 * This API is used for both the client and server when determining
	 * if a new settlement has broken a road chain.
	 * @param node
	 */
	protected void checkIfRoadBroken(Node node)
	{
		// Check all combinations of edges to check if a road chain was broken
		for(int i = 0; i < node.getEdges().size(); i++)
		{
			boolean broken = false;
			for(int j = 0; j < node.getEdges().size(); j++)
			{
				Edge e = node.getEdges().get(i), other = node.getEdges().get(j);
				Road r = e.getRoad(), otherR = other.getRoad();

				if(e.equals(other)) continue;

				// If this settlement is between two roads of the same colour
				if(r != null && otherR != null && r.getPlayerColour().equals(otherR.getPlayerColour()))
				{
					// retrieve owner of roads and break the road chain
					players.get(e.getRoad().getPlayerColour()).breakRoad(e, other);
					broken = true;
					break;
				}
			}
			if(broken)
			{
				checkLongestRoad(broken);
				break;
			}
		}
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

	/**
	 * @return the players
	 */
	public Map<Colour, Player> getPlayers()
	{
		return players;
	}
}
