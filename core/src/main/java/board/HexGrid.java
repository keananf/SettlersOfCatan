package board;

import enums.ResourceType;
import protocol.BoardProtos.*;

import java.awt.Point;
import java.util.*;

/**
 * Class describing the hex board
 * @author 140001596
 */
public class HexGrid
{
	public Hashtable<Point, Hex> grid; // Overall grid
	public List<Edge> edges; // All edges
	public List<Port> ports; // All ports
	public Hashtable<Point, Node> nodes; // All nodes
	private Hex hexWithRobber;
	public static final int SIZE_OF_GRID = 5;
	
	public HexGrid()
	{
		initGrid();
	}
	
	/**
	 * Instantiates a new hexgrid object
	 */
	private void initGrid()
	{
		grid = new Hashtable<Point, Hex>();
		nodes = new Hashtable<Point, Node>();
		
		edges = new ArrayList<Edge>();
		ports = new ArrayList<Port>();
		
		Map<Integer, Integer> chitsAvailable = getChitsAvailable();
		Map<ResourceType, Integer> resourcesAvailable = getResourcesAvailable();
		
		// for each column
		for(int x = -SIZE_OF_GRID; x <= SIZE_OF_GRID; x++)
		{
			// for each row
			for(int y = -SIZE_OF_GRID; y <= SIZE_OF_GRID; y++)
			{
				// If in boundaries
				if(y - 2*x <= 8 && 2*y - x <= 8 && x + y <= 8 &&
				   y - 2*x >= -8 && 2*y - x >= -8 && x + y >= -8)
				{
					
					// Condition for whether or not the coordinate is a hex.
					if(Math.abs(x + y) % 3 == 0 || x + y == 0) // TODO make more generic. NO magic number
					{
						Hex hex = new Hex(x, y);
						
						allocateResource(hex, chitsAvailable, resourcesAvailable);
						grid.put(new Point(x, y), hex);
					}
					
					// Condition for whether or not the coordinate is a node
					else nodes.put(new Point(x, y), new Node(x, y));
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

		// Find all 6 neighbours
		for(int i = -1; i <= 1; i++)
		{
			for(int j = -1; j <= 1; j++)
			{
				if(i == 0 && j == 0) continue;
				if(i == -1 && j == 1) continue;
				if(i == 1 && j == -1) continue;
				
				Point p = new Point(node.getX() + i, node.getY() + j);
				
				if(nodes.containsKey(p)) neighbours.add(nodes.get(p));

				if(grid.containsKey(p)) neighbours.add(grid.get(p));
			}				
		}
		
		return neighbours;
	}
	
	/**
	 * Creates edges and relationships between the different board elements.
	 * Nodes are given their adjacent hexes, and edges are made between nodes.
	 * Ports are also made here
	 */
	private void setUpEdgesAndNodes()
	{
		List<Edge> potentialPorts = new ArrayList<Edge>();
		
		// for each node
		for(Node node : nodes.values())
		{
			List<Hex> adjacentHexes = new LinkedList<Hex>();
			List<BoardElement> neighbours = getNeighbours(node);
			
			// Create both edges AND find the adjacent hexes
			for(BoardElement neighbour : neighbours)
			{
				// If neighbour is a node, create an edge
				if (neighbour instanceof Node)
				{
					Edge e = Edge.makeEdge(node, (Node) neighbour, edges);
					if (e != null)
					{
						e.getX().addEdge(e);
						e.getY().addEdge(e);
						if (e.getX().onBoundaries() || e.getY().onBoundaries())
						{
							potentialPorts.add(e);
						}
					}
				}
				
				// Otherwise add to this node's list of adjacent hexes.
				// Although inefficient just now, it will make future algorithms a lot simpler.
				else
				{
					adjacentHexes.add((Hex) neighbour);
				}
			}
			node.setAdjacentHexes(adjacentHexes);
			for(Hex hex : adjacentHexes)
			{
				hex.addNode(node);
			}
		}
		
		ports = Port.makePorts(edges, potentialPorts);
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
		resourcesAvailable.put(ResourceType.Grain, 4);
		resourcesAvailable.put(ResourceType.Ore, 3);
		resourcesAvailable.put(ResourceType.Wool, 4);
		resourcesAvailable.put(ResourceType.Lumber, 4);
		resourcesAvailable.put(ResourceType.Generic, 1);
		
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
		int r = rand.nextInt(resourcesAvailable.size());
		ResourceType resource = ResourceType.values()[r];
		
		// Find a resource to allocate 
		while(hex.getResource() == ResourceType.Generic)
		{
			int remaining = resourcesAvailable.get(resource);
			if(remaining > 0)
			{
				resourcesAvailable.put(resource, remaining - 1);
				hex.setResource(resource);
				if(resource == ResourceType.Generic)
				{
					hex.toggleRobber();

					hexWithRobber = hex;
					return; // Necessary to allow one hex to be 'none'					
				}
			}
			
			// If we could not allocate this resource, randomly select a new one.
			r = rand.nextInt(resourcesAvailable.size());
			resource = ResourceType.values()[r];
		}
		
		// Loop until we have a valid dice roll
		while(hex.getChit() == 0)
		{
			int dice = rand.nextInt(chitsAvailable.size() + 1) + 2; 
			
			if(dice == 7) continue;
			
			int remaining = chitsAvailable.get(dice);
			if(remaining > 0)
			{
				chitsAvailable.put(dice, remaining - 1);
				hex.setDiceRoll(dice);
			}
		}
		
	}
	
	/**
	 * Swaps robbers with the current hex and the one at x, y
	 * @param hex the hex that is getting the robber
	 */
	public Hex swapRobbers(Hex hex)
	{
		// Swap robbers
		hexWithRobber.toggleRobber();
		hexWithRobber = hex;
		hexWithRobber.toggleRobber();
		
		return hexWithRobber;
	}

	/**
	 * @return the hexWithRobber
	 */
	public Hex getHexWithRobber()
	{
		return hexWithRobber;
	}

    /**
     * Retrieve the hex at the given coordinates
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the hex, if found
     */
    public Hex getHex(int x, int y)
    {
        return grid.get(new Point(x, y));
    }

    /**
     * Retrieve the Node at the given coordinates
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the Node, if found
     */
    public Node getNode(int x, int y)
    {
        return nodes.get(new Point(x, y));
    }

    /**
     * @return all hexes on this board
     */
    public List<Hex> getHexesAsList()
    {
        List<Hex> hexes = new ArrayList<Hex>();
        hexes.addAll(this.grid.values());

        return hexes;
    }

    /**
     * @return a list of all ports
     */
    public List<Port> getPortsAsList()
    {
        List<Port> ports = new ArrayList<Port>();
        ports.addAll(this.ports);

        return ports;
    }

    /**
     * @return list of all nodes
     */
    public List<Node> getNodesAsList()
    {
        List<Node> nodes = new ArrayList<Node>();
        nodes.addAll(this.nodes.values());

        return nodes;
    }

    /**
     * @return a list of all edges
     */
    public List<Edge> getEdgesAsList()
    {
        List<Edge> edges = new ArrayList<Edge>();
        edges.addAll(this.edges);

        return edges;

    }

	/**
	 * @param edge the protobuf version of the edge to find
	 * @return the internal version of the edge
	 */
	public Edge getEdge(EdgeProto edge)
	{
		// Find nodes and edges
		Node n1 = getNode(edge.getP1().getX(), edge.getP1().getY());
		Node n2 = getNode(edge.getP2().getX(), edge.getP2().getY());
		Edge e = n1.findEdge(n2);

		return e;
	}

	/**
	 * @param port the protobuf version of the port to find
	 * @return the internal version of the port
	 */
	public Port getPort(PortProto port)
	{
		// Find nodes and edges
		Node n1 = getNode(port.getP1().getX(), port.getP1().getY());
		Node n2 = getNode(port.getP2().getX(), port.getP2().getY());
		Edge e = n1.findEdge(n2);

		return (Port) e;
    }
}
