package board;

import game.enums.ResourceType;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;

/**
 * Class describing the hex board
 * @author 140001596
 */
public class HexGrid
{
	public Hashtable<SimpleEntry<Integer, Integer>, Hex> grid; // Overall grid
	public HashSet<Edge> edges; // All edges
	public HashSet<Node> nodes; // All nodes
	public static final int SIZE_OF_GRID = 3;
	
	/**
	 * Instantiates a new hexgrid object
	 */
	public void initGrid()
	{
		grid = new Hashtable<SimpleEntry<Integer, Integer>, Hex>();
		edges = new HashSet<Edge>(); //TODO maybe have method to look up hex? then simple arithmetic to find adjacent ones
		nodes = new HashSet<Node>(); //TODO same as above
		
		// for each row
		for(int y = -SIZE_OF_GRID; y < SIZE_OF_GRID; y++)
		{
			// for each column
			for(int x = -SIZE_OF_GRID; x < SIZE_OF_GRID; x++)
			{
				// Condition for whether or not coordinates are valid.
				if(x + y < -SIZE_OF_GRID || x + y > SIZE_OF_GRID)
				{
					Hex hex = new Hex(x, y);
					SimpleEntry<Integer, Integer> coords = new SimpleEntry<Integer, Integer>(x, y);
					
					grid.put(coords, hex);
				}
			}
		}
		assignResourcesToHexes();
		assignEdges();
	}

	private List<Hex> getNeighbours(Hex hex)
	{
		List<Hex> neighbours = new LinkedList<Hex>();
		neighbours.addAll(grid.values());
		neighbours.removeIf((h) -> !hex.borders(h));
		
		return neighbours;
	}
	
	private void assignEdges()
	{
		// for each hex
		for(Hex hex : grid.values())
		{
			// Create edge 
			for(Hex neighbour : getNeighbours(hex))
			{
				//TODO add to hexes?
				new Edge(hex, neighbour);
			}
		}
	}

	/**
	 * Sets up the hexes in the grid to have resources and chits
	 */
	private void assignResourcesToHexes()
	{
		Map<Integer, Integer> diceAvailable = new HashMap<Integer, Integer>();
		diceAvailable.put(2, 1);
		diceAvailable.put(3, 2);
		diceAvailable.put(4, 2);
		diceAvailable.put(5, 2);
		diceAvailable.put(6, 2);
		diceAvailable.put(8, 2);
		diceAvailable.put(9, 2);
		diceAvailable.put(10, 2);
		diceAvailable.put(11, 2);
		diceAvailable.put(12, 1);
		
		Map<ResourceType, Integer> resourcesAvailable = new HashMap<ResourceType, Integer>();
		resourcesAvailable.put(ResourceType.Brick, 3);
		resourcesAvailable.put(ResourceType.Wheat, 4);
		resourcesAvailable.put(ResourceType.Stone, 3);
		resourcesAvailable.put(ResourceType.Sheep, 4);
		resourcesAvailable.put(ResourceType.Wood, 4);
		resourcesAvailable.put(ResourceType.None, 1);
		
		// For each hex
		for(Hex hex : grid.values())
		{
			allocate(hex, diceAvailable, resourcesAvailable);
		}
	}

	/**
	 * Dynamically allocate dice rolls and resources to the individual hexes.
	 * @param hex the hex to allocate
	 * @param diceAvailable a map of which chits are still available
	 * @param resourcesAvailable a map of which resources are still available
	 */
	private void allocate(Hex hex, Map<Integer, Integer> diceAvailable,
			Map<ResourceType, Integer> resourcesAvailable)
	{
		Random rand = new Random();
		ResourceType res = ResourceType.None;
		
		// Find a resource to allocate 
		while(hex.getResource() == ResourceType.None)
		{
			int remaining = resourcesAvailable.get(res);
			if(remaining > 0)
			{
				resourcesAvailable.put(res, remaining - 1);
				hex.setResource(res);
				if(res == ResourceType.None) hex.toggleRobber();
				
				break; // Necessary to allow one hex to be 'none'
			}
			
			// If we could not allocate this resource, randomly select a new one.
			int r = rand.nextInt(resourcesAvailable.size());
			res = ResourceType.values()[r];
		}
		
		// Loop until we have a valid dice roll
		while(hex.getDiceRoll() == 0)
		{
			int dice = rand.nextInt(diceAvailable.size()) + 1; 
			int remaining = diceAvailable.get(dice);
			if(remaining > 0)
			{
				diceAvailable.put(dice, remaining - 1);
				hex.setDiceRoll(dice);
			}
		}
		
	}
}
