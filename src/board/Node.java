package board;

import java.util.*;

/**
 * Class representing an individual node in catan (intersection of three hexes)
 * @author 140001596
 */
public class Node 
{
	private List<Hex> hexes;
	
	/**
	 * @return the hexes
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
