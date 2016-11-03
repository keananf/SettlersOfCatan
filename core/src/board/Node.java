package board;

import java.util.*;

/**
 * Class representing an individual node in catan (intersection of three hexes)
 * @author 140001596
 */
public class Node extends BoardElement
{
	private List<Hex> hexes;
	
	protected Node(int x, int y)
	{
		super(x, y);
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
}
