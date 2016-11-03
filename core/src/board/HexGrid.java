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
	public Hashtable<SimpleEntry<Integer, Integer>, Node> nodes; // All nodes
	public static final int SIZE_OF_GRID = 8;
	
	public HexGrid()
	{
		initGrid();
	}
	
	/**
	 * Instantiates a new hexgrid object
	 */
	private void initGrid()
	{
		grid = new Hashtable<SimpleEntry<Integer, Integer>, Hex>();
		nodes = new Hashtable<SimpleEntry<Integer, Integer>, Node>();
		
		edges = new HashSet<Edge>();
		
		Map<Integer, Integer> chitsAvailable = getChitsAvailable();
		Map<ResourceType, Integer> resourcesAvailable = getResourcesAvailable();
		
		// for each column
		for(int x = -SIZE_OF_GRID; x < SIZE_OF_GRID; x++) // x goes from -8 to 8
		{
			// for each row
			for(int y = -(SIZE_OF_GRID - 3); y <= SIZE_OF_GRID - 3; y++) // y goes from -5 to 5
			{
				// Condition for whether or not the coordinate is a hex.
				if(x % 3 == 0) // TODO make more generic. NO make number
				{
					Hex hex = new Hex(x, y);
					SimpleEntry<Integer, Integer> coords = new SimpleEntry<Integer, Integer>(x, y);
					
					allocateResource(hex, chitsAvailable, resourcesAvailable);
					grid.put(coords, hex);
				}
				
				// Condition for whether or not the coordinate is a node
				else
				{
					Node node = new Node(x, y);
					SimpleEntry<Integer, Integer> coords = new SimpleEntry<Integer, Integer>(x, y);
					
					nodes.put(coords, node);
				}
			}
		}
		setUpEdgesAndNodes();
	}

	/**
	 * Finds all 3 neighbouring hexes and the two neighbouring nodes to this node.
	 * 
	 * This is used for initially setting up the board and all the different object's
	 * coorelations with one another. 
	 * @param node the node
	 * @return a list of neighbouring elements
	 */
	private List<BoardElement> getNeighbours(Node node)
	{
		List<BoardElement> neighbours = new LinkedList<BoardElement>();		
		Hashtable<SimpleEntry<Integer, Integer>, BoardElement> allBoardElements = new Hashtable<SimpleEntry<Integer, Integer>, BoardElement>();
		allBoardElements.putAll(nodes);
		allBoardElements.putAll(grid);
		
		//TODO MAKE MORE EFFICIENT. Just do coordinate arithmetic to retrieve specific elements
		allBoardElements.values().removeIf((b) -> b instanceof Node ? !node.isAdjacent((Node)b) 
													: !node.isAdjacent((Hex) b));
		
		neighbours.addAll(allBoardElements.values());
		return neighbours;
	}
	
	/**
	 * Creates edges and relationships between the different board elements
	 */
	private void setUpEdgesAndNodes()
	{
		// for each node
		for(Node node : nodes.values())
		{
			List<Hex> adjacentHexes = new LinkedList<Hex>();
			
			// Create both edges AND find the adjacent hexes
			for(BoardElement neighbour : getNeighbours(node))
			{
				// If neighbour is a node, create an edge
				if(neighbour instanceof Node)
				{					
					edges.add(new Edge(node, (Node)neighbour));
				}
				
				// Otherwise add to this node's list of adjacent hexes.
				// Although inefficient just now, it will make future algorithms a lot simpler.
				else
				{
					adjacentHexes.add((Hex) neighbour);
				}
			}
			node.setAdjacentHexes(adjacentHexes);
		}
	}

	/**
	 * @return map of available chits that can be allocated
	 */
	private Map<Integer, Integer> getChitsAvailable()
	{
		Map<Integer, Integer> chitsAvailable = new HashMap<Integer, Integer>();
		chitsAvailable.put(2, 1);
		chitsAvailable.put(3, 2);
		chitsAvailable.put(4, 2);
		chitsAvailable.put(5, 2);
		chitsAvailable.put(6, 2);
		chitsAvailable.put(8, 2);
		chitsAvailable.put(9, 2);
		chitsAvailable.put(10, 2);
		chitsAvailable.put(11, 2);
		chitsAvailable.put(12, 1);
		
		return chitsAvailable;
	}
	
	/**
	 * @return map of available resources that can be allocated.
	 */
	private Map<ResourceType, Integer> getResourcesAvailable()
	{
		
		Map<ResourceType, Integer> resourcesAvailable = new HashMap<ResourceType, Integer>();
		resourcesAvailable.put(ResourceType.Brick, 3);
		resourcesAvailable.put(ResourceType.Wheat, 4);
		resourcesAvailable.put(ResourceType.Stone, 3);
		resourcesAvailable.put(ResourceType.Sheep, 4);
		resourcesAvailable.put(ResourceType.Wood, 4);
		resourcesAvailable.put(ResourceType.None, 1);
		
		return resourcesAvailable;
	}

	/**
	 * Dynamically allocate dice rolls and resources to the individual hexes.
	 * @param hex the hex to allocate
	 * @param chitsAvailable a map of which chits are still available
	 * @param resourcesAvailable a map of which resources are still available
	 */
	private void allocateResource(Hex hex, Map<Integer, Integer> chitsAvailable,
			Map<ResourceType, Integer> resourcesAvailable)
	{
		Random rand = new Random();
		ResourceType resource = ResourceType.None;
		
		// Find a resource to allocate 
		while(hex.getResource() == ResourceType.None)
		{
			int remaining = resourcesAvailable.get(resource);
			if(remaining > 0)
			{
				resourcesAvailable.put(resource, remaining - 1);
				hex.setResource(resource);
				if(resource == ResourceType.None) hex.toggleRobber();
				
				break; // Necessary to allow one hex to be 'none'
			}
			
			// If we could not allocate this resource, randomly select a new one.
			int r = rand.nextInt(resourcesAvailable.size());
			resource = ResourceType.values()[r];
		}
		
		// Loop until we have a valid dice roll
		while(hex.getChit() == 0)
		{
			int dice = rand.nextInt(chitsAvailable.size()) + 1; 
			int remaining = chitsAvailable.get(dice);
			if(remaining > 0)
			{
				chitsAvailable.put(dice, remaining - 1);
				hex.setDiceRoll(dice);
			}
		}
		
	}
}
