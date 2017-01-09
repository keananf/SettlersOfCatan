 package board;

import java.util.ArrayList;
import java.util.List;

import enums.*;
import game.build.City;
import protocol.BoardProtos.*;
import protocol.BuildProtos.*;
import protocol.EnumProtos.*;

/**
 * Class representing an individual hex 
 * @author 140001596
 */
public class Hex extends BoardElement
{
	private ResourceType resource;
	private int diceRoll;
	private boolean hasRobber;
	private List<Node> nodes;
	
	public Hex(int x, int y)
	{
		super(x, y);
		resource = ResourceType.Generic;
		nodes = new ArrayList<Node>();
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

	public List<Node> getNodes()
	{
		return nodes;
	}

	public void addNode(Node node)
	{
		nodes.add(node);
	}

	/**
	 * @return a version of this object compatible with protobufs.
	 */
    public HexProto toHexProto()
	{
		int index = 0;
 		PointProto.Builder coords = PointProto.newBuilder();
 		coords.setX(getX());
 		coords.setY(getY());

		HexProto.Builder hexBuilder = HexProto.newBuilder();
		hexBuilder.setChitNumber(diceRoll);
		hexBuilder.setResource(ResourceType.toProto(resource));
		hexBuilder.setP(coords.build());

		// Add nodes
		for(Node n : nodes)
		{
			NodeProto.Builder nodeProto = NodeProto.newBuilder();
			if(n.getSettlement() != null)
			{
				PointProto.Builder point = PointProto.newBuilder();
				BuildingProto.Builder building = BuildingProto.newBuilder();
				point.setX(n.getX());
				point.setY(n.getY());
				building.setP(point.build());
				building.setPlayerId(Colour.toProto(n.getSettlement().getPlayerColour()));
				building.setType(n.getSettlement() instanceof City ? BuildingTypeProto.CITY : BuildingTypeProto.SETTLEMENT);

				nodeProto.setBuildingType(n.getSettlement() instanceof City ? BuildingTypeProto.CITY : BuildingTypeProto.SETTLEMENT);
				nodeProto.setBuilding(building.build());
			}
			hexBuilder.addNodes(index++, nodeProto.build());
		}

		return hexBuilder.build();
    }
}
