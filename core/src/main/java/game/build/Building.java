package game.build;

import java.util.*;

import enums.*;
import board.*;
import protocol.BuildProtos;
import protocol.BuildProtos.*;
import protocol.EnumProtos;

/**
 * Abstract class describing a building (either a settlement or city)
 * 
 * @author 140001596
 */
public abstract class Building implements IBuildable
{
	protected Node node;
	private Colour playerColour;

	public Building(Node node, Colour colour)
	{
		this.node = node;
		this.playerColour = colour;
	}

	protected Building()
	{
	}

	/**
	 * Calculates the total resources gained for this settlement
	 * 
	 * @return a map of resource types to number of cards gained.
	 */
	public Map<ResourceType, Integer> calculateResources(int diceRoll)
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();

		int numOfResource = this instanceof City ? 2 : 1;

		// For each hex this settlement borders
		for (Hex hex : node.getHexes())
		{
			if (!hex.hasRobber() && hex.getChit() == diceRoll) resources.put(hex.getResource(), numOfResource);
		}

		return resources;
	}

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

	/**
	 * @param playerColour the playerColour to set
	 */
	public void setPlayerColour(Colour playerColour)
	{
		this.playerColour = playerColour;
	}

	/**
	 * @return a serialisable form of this building compatible with protobufs
	 */
	public BuildingProto.Builder toProto()
	{
		BuildProtos.PointProto.Builder coords = BuildProtos.PointProto.newBuilder();
		BuildProtos.BuildingProto.Builder building = BuildProtos.BuildingProto.newBuilder();

		coords.setX(node.getX());
		coords.setY(node.getY());
		building.setP(coords.build());
		building.setPlayerId(Colour.toProto(getPlayerColour()));
		building.setType(
				this instanceof City ? EnumProtos.BuildingTypeProto.CITY : EnumProtos.BuildingTypeProto.SETTLEMENT);

		return building;
	}
}
