package grid;

import intergroup.board.Board;
import enums.ResourceType;
import intergroup.terrain.Terrain;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an individual hex 
 * @author 140001596
 */
public class Hex extends BoardElement
{
	private Terrain.Kind terrain;
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
		terrain = ResourceType.getTerrainFromResource(resource);
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
    public Board.Hex toHexProto()
	{
		int index = 0;
 		Board.Point.Builder coords = Board.Point.newBuilder();
 		coords.setX(getX());
 		coords.setY(getY());

		Board.Hex.Builder hexBuilder = Board.Hex.newBuilder();
		hexBuilder.setNumberToken(diceRoll);
		hexBuilder.setTerrain(terrain);
		hexBuilder.setLocation(coords.build());

		return hexBuilder.build();
    }

	/**
	 * Converts the hex proto back into an internal, usable representation
	 * @param h the protobuf hex
	 */
	public static Hex fromProto(Board.Hex h)
	{
		// Extract information and set up object
		Board.Point p = h.getLocation();
		int x = p.getX(), y = p.getY();
		Hex hex = new Hex(x, y);

		// Set remaining fields
		hex.setDiceRoll(h.getNumberToken());
		hex.terrain = h.getTerrain();
		hex.setResource(ResourceType.getResourceFromTerrain(hex.terrain));

		return hex;
    }

	@Override
	public boolean equals(Object o)
	{
		Hex h = (Hex) o;

		// Ensure properties are the same
		if(!(h.hasRobber == hasRobber))
		{
			return false;
		}
		if(!(h.getChit() == getChit()))
		{
			return false;
		}
		if(!(h.getResource() == getResource()))
		{
			return false;
		}

		// Check nodes for equivalence as well
		for(Node n : nodes)
		{
			// If the object doesn't have all of this hex's nodes
			if(!(h.getNodes().contains(n)))
			{
				return false;
			}
		}

		return true;
	}

	public void toggleHasRobber()
	{
		hasRobber = !hasRobber;
	}
}
