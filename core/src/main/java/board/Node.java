package board;

import java.util.*;

import enums.Colour;
import enums.ResourceType;
import game.build.*;
import protocol.BoardProtos.*;
import protocol.BuildProtos;
import protocol.EnumProtos;

/**
 * Class representing an individual node in catan (intersection of three hexes)
 * @author 140001596
 */
public class Node extends BoardElement
{
	private List<Hex> hexes;
	private List<Edge> edges;
	private Building settlement;
	
	public Node(int x, int y)
	{
		super(x, y);
		edges = new ArrayList<Edge>(3);
	}

	/**
	 * Set list of adjacent hexes
	 * @param hexes the adjacent hexes
	 */
	public void setAdjacentHexes(List<Hex> hexes)
	{
		this.hexes = hexes;
	}
	
	/**
	 * @return the list of adjacent hexes
	 */
	public List<Hex> getHexes()
	{
		return hexes;
	}

	/**
	 * @return the list of adjacent edges
	 */
	public List<Edge> getEdges()
	{
		return edges;
	}
	
	public void addEdge(Edge e)
	{
		edges.add(e);
	}
	public void removeEdge(Edge e)
	{
		edges.remove(e);
	}

	/**
	 * Detects whether a given node is adjacent to this node or not
	 * @param n the node
	 * @return boolean indicating adjacency
	 */
	public boolean isAdjacent(Node n)
	{
		return (Math.abs(getX() - n.getX()) == 2 || Math.abs(getX() - n.getX()) == 1) 
				&& Math.abs(getY() - n.getY()) <= 1;
	}
	
	/**
	 * Detects whether a given hex is adjacent to this node or not
	 * @param h the hex
	 * @return boolean indicating adjacency
	 */
	public boolean isAdjacent(Hex h)
	{
		return (Math.abs(getX() - h.getX()) == 2 || Math.abs(getX() - h.getX()) == 1) 
				&& Math.abs(getY() - h.getY()) <= 1;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(getX() == ((Node)other).getX() && getY() == ((Node)other).getY()) return true;
		
		return false;
	}

	/**
	 * Gets the raw difference in coordinate values.
	 * 
	 * This is used as a metric when navigating along edges
	 * @param other the node to check
	 * @return the overall coord distance. A metric evaluating if how close
	 * a node is to another.
	 */
	public int getCoordDistance(Node other)
	{
		int xDistance = Math.abs(getX() - other.getX());
		int yDistance = Math.abs(getY() - other.getY());
		
		return xDistance + yDistance;
	}

	/**
	 * Determines if a node is on the boundaries of the board
	 * @return boolean indicating whether or not the node is on the board
	 */
	public boolean onBoundaries()
	{
		int x = getX();
		int y = getY();
		
		if(y - 2*x == 8 || 2*y - x == 8 || x + y == 8 ||
				   y - 2*x == -8 || 2*y - x == -8 || x + y == -8) // TODO fix magic numbers
		{
			return true;
		}
		
		return false;
	}

	/**
	 * @return the settlement
	 */
	public Building getSettlement()
	{
		return settlement;
	}

	/**
	 * @param settlement the settlement to set
	 */
	public void setSettlement(Building settlement)
	{
		this.settlement = settlement;
	}

	/**
	 * Searches through the adjacent edges for the one with the given node
	 * @param n2 the node to search for
	 */
    public Edge findEdge(Node n2)
	{
		for(Edge e : edges)
		{
			if(e.getX().equals(n2) || e.getY().equals(n2))
				return e;
		}

		return null;
    }

	/**
	 * @return converts this Node into a similar representation that is also compatible with protobufs
	 */
	public NodeProto toProto()
	{
		int index = 0;
		NodeProto.Builder nodeBuilder = NodeProto.newBuilder();
		BuildProtos.PointProto.Builder coords = BuildProtos.PointProto.newBuilder();

		coords.setX(getX());
		coords.setY(getY());
		nodeBuilder.setP(coords.build());

		// Add Building
		if(getSettlement() != null)
		{
			BuildProtos.BuildingProto.Builder building = BuildProtos.BuildingProto.newBuilder();
			building.setP(coords.build());
			building.setPlayerId(Colour.toProto(settlement.getPlayerColour()));
			building.setType(settlement instanceof City ? EnumProtos.BuildingTypeProto.CITY : EnumProtos.BuildingTypeProto.SETTLEMENT);
			nodeBuilder.setBuilding(building.build());
			nodeBuilder.setBuildingType(settlement instanceof City ? EnumProtos.BuildingTypeProto.CITY : EnumProtos.BuildingTypeProto.SETTLEMENT);
		}

		return nodeBuilder.build();
    }
}
