package board;

import java.util.*;

/**
 * Class uniquely describing an edge (between two hexes)
 * @author 140001596
 */
public class Edge
{
	private List<Hex> hexes;

	public Edge(Hex hex, Hex neighbour)
	{
		hexes = new LinkedList<Hex>();
		hexes.add(hex);
		hexes.add(neighbour);
	}

	/**
	 * @return The adjacent hexes which uniquely define an edge
	 */
	public List<Hex> getHexes()
	{
		return hexes;
	}

	/**
	 * @param hexes the hexes to set
	 */
	public void setHexes(List<Hex> hexes)
	{
		this.hexes = hexes;
	}
}
