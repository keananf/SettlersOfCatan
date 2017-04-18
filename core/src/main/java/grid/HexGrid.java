package grid;

import intergroup.board.Board;
import enums.ResourceType;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class describing the hex board
 * 
 * @author 140001596
 */
public class HexGrid
{
	public final Hashtable<Point, Hex> grid; // Overall grid
	public final List<Edge> edges; // All edges
	public List<Port> ports; // All ports
	public final Hashtable<Point, Node> nodes; // All nodes
	private Hex hexWithRobber;
	private static final int SIZE_OF_GRID = 5;

	public HexGrid(boolean b)
	{
		grid = new Hashtable<>();
		nodes = new Hashtable<>();

		edges = new ArrayList<>();
		ports = new ArrayList<>();

		if (b) initGrid();
	}

	/**
	 * Instantiates a new hexgrid object
	 */
	private void initGrid()
	{
		Map<Integer, Integer> chitsAvailable = getChitsAvailable();
		Map<ResourceType, Integer> resourcesAvailable = getResourcesAvailable();

		// for each column
		for (int x = -SIZE_OF_GRID; x <= SIZE_OF_GRID; x++)
		{
			// for each row
			for (int y = -SIZE_OF_GRID; y <= SIZE_OF_GRID; y++)
			{
				// If in boundaries
				if (inBoundries(x, y))
				{

					// Condition for whether or not the coordinate is a hex.
					if (Math.abs(x + y) % 3 == 0 || x + y == 0) // TODO make
																// more generic.
																// NO magic
																// number
					{
						Hex hex = new Hex(x, y);

						allocateResource(hex, chitsAvailable, resourcesAvailable);
						grid.put(new Point(x, y), hex);
					}

					// Condition for whether or not the coordinate is a node
					else
						nodes.put(new Point(x, y), new Node(x, y));
				}
			}
		}
		setUpReferences();
		makePorts();
	}

	/**
	 * Finds all 3 neighbouring hexes and the two neighbouring nodes to this
	 * node.
	 * 
	 * This is used for initially setting up the board and all the different
	 * object's coorelations with one another.
	 * 
	 * @param node the node
	 * @return a list of neighbouring elements
	 */
	private List<GridElement> getNeighbours(Node node)
	{
		List<GridElement> neighbours = new LinkedList<>();

		// Find all 6 neighbours
		for (int i = -1; i <= 1; i++)
		{
			for (int j = -1; j <= 1; j++)
			{
				if (i == 0 && j == 0) continue;
				if (i == -1 && j == 1) continue;
				if (i == 1 && j == -1) continue;

				Point p = new Point(node.getX() + i, node.getY() + j);

				if (nodes.containsKey(p)) neighbours.add(nodes.get(p));

				if (grid.containsKey(p)) neighbours.add(grid.get(p));
			}
		}

		return neighbours;
	}

	/**
	 * Creates edges and relationships between the different board elements.
	 * Nodes are given their adjacent hexes, and edges are made between nodes.
	 */
	private void setUpReferences()
	{
		// for each node
		for (Node node : nodes.values())
		{
			List<Hex> adjacentHexes = new LinkedList<>();
			List<GridElement> neighbours = getNeighbours(node);

			// Create both edges AND find the adjacent hexes
			for (GridElement neighbour : neighbours)
			{
				// If neighbour is a node, create an edge
				if (neighbour instanceof Node)
				{
					Edge e = Edge.makeEdge(node, (Node) neighbour, edges);
					if (e != null)
					{
						e.getX().addEdge(e);
						e.getY().addEdge(e);
					}
				}

				// Otherwise add to this node's list of adjacent hexes.
				// Although inefficient just now, it will make future algorithms
				// a lot simpler.
				else
				{
					adjacentHexes.add((Hex) neighbour);
				}
			}

			node.setAdjacentHexes(adjacentHexes);
			for (Hex hex : adjacentHexes)
			{
				hex.addNode(node);
			}
		}

	}

	/**
	 * Given that setUpReferences() has been called, this makes all ports
	 */
	private void makePorts()
	{
		List<Edge> portLocations = new ArrayList<>();
		portLocations.add(getEdge(new Point(-5, -3), new Point(-4, -3)));
		portLocations.add(getEdge(new Point(-3, -5), new Point(-3, -4)));
		portLocations.add(getEdge(new Point(-1, -4), new Point(0, -4)));
		portLocations.add(getEdge(new Point(2, -3), new Point(3, -2)));
		portLocations.add(getEdge(new Point(4, 0), new Point(4, 1)));
		portLocations.add(getEdge(new Point(4, 3), new Point(4, 4)));
		portLocations.add(getEdge(new Point(2, 5), new Point(3, 5)));
		portLocations.add(getEdge(new Point(-1, 3), new Point(0, 4)));
		portLocations.add(getEdge(new Point(-4, 0), new Point(-3, 1)));

		ports = Port.makePorts(edges, portLocations);
	}

	/**
	 * @return map of available chits that can be allocated
	 */
	private Map<Integer, Integer> getChitsAvailable()
	{
		Map<Integer, Integer> chitsAvailable = new HashMap<>();
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

		Map<ResourceType, Integer> resourcesAvailable = new HashMap<>();
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
	 * 
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
		while (hex.getResource() == ResourceType.Generic)
		{
			int remaining = resourcesAvailable.get(resource);
			if (remaining > 0)
			{
				resourcesAvailable.put(resource, remaining - 1);
				hex.setResource(resource);
				if (resource == ResourceType.Generic)
				{
					hex.toggleRobber();

					hexWithRobber = hex;
					return; // Necessary to allow one hex to be 'none'
				}
			}

			// If we could not allocate this resource, randomly select a new
			// one.
			r = rand.nextInt(resourcesAvailable.size());
			resource = ResourceType.values()[r];
		}

		// Loop until we have a valid dice roll
		while (hex.getChit() == 0)
		{
			int dice = rand.nextInt(chitsAvailable.size() + 1) + 2;

			if (dice == 7) continue;

			int remaining = chitsAvailable.get(dice);
			if (remaining > 0)
			{
				chitsAvailable.put(dice, remaining - 1);
				hex.setDiceRoll(dice);
			}
		}

	}

	/**
	 * Swaps robbers with the current hex and the one at x, y
	 * 
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
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the hex, if found
	 */
	public Hex getHex(int x, int y)
	{
		Point p = new Point(x, y);
		return grid.getOrDefault(p, null);
	}

	/**
	 * Retrieve the Node at the given coordinates
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the Node, if found
	 */
	public Node getNode(int x, int y)
	{
		Point p = new Point(x, y);
		return nodes.getOrDefault(p, null);
	}

	/**
	 * @return all hexes on this board
	 */
	public List<Hex> getHexesAsList()
	{
		List<Hex> hexes = new ArrayList<>();
		hexes.addAll(this.grid.values());

		return hexes;
	}

	/**
	 * @return a list of all ports
	 */
	public List<Port> getPortsAsList()
	{
		List<Port> ports = new ArrayList<>();
		ports.addAll(this.ports);

		return ports;
	}

	/**
	 * @return list of all nodes
	 */
	public List<Node> getNodesAsList()
	{
		List<Node> nodes = new ArrayList<>();
		nodes.addAll(this.nodes.values());

		return nodes;
	}

	/**
	 * @return a list of all edges
	 */
	public List<Edge> getEdgesAsList()
	{
		List<Edge> edges = new ArrayList<>();
		edges.addAll(this.edges);

		return edges;

	}

	/**
	 * Finds the edge associated with the two points
	 * 
	 * @param p1 the first point
	 * @param p2 the second point
	 * @return the internal version of the road
	 */
	public Edge getEdge(Board.Point p1, Board.Point p2)
	{
		// Find nodes and edges
		Node n1 = getNode(p1.getX(), p1.getY());
		Node n2 = getNode(p2.getX(), p2.getY());

		return n1.findEdge(n2);
	}

	/**
	 * Finds the edge associated with the two points
	 *
	 * @param p1 the first point
	 * @param p2 the second point
	 * @return the internal version of the road
	 */
	private Edge getEdge(Point p1, Point p2)
	{
		// Find nodes and edges
		Node n1 = getNode((int) p1.getX(), (int) p1.getY());
		Node n2 = getNode((int) p2.getX(), (int) p2.getY());

		return n1.findEdge(n2);
	}

	public void setNodesAndHexes(List<Hex> hexes)
	{
		// Add hexes
		for (Hex h : hexes)
		{
			this.grid.put(new Point(h.getX(), h.getY()), h);

			// Set default robber
			if (h.getResource().equals(ResourceType.Generic)
					&& (hexWithRobber == null || hexWithRobber.getResource().equals(ResourceType.Generic)))
			{
				h.toggleHasRobber();
				hexWithRobber = h;
			}
		}

		// for each column
		for (int x = -SIZE_OF_GRID; x <= SIZE_OF_GRID; x++)
		{
			// for each row
			for (int y = -SIZE_OF_GRID; y <= SIZE_OF_GRID; y++)
			{
				if (inBoundries(x, y))
				{
					// Condition for whether or not the coordinate is a node.
					if (Math.abs(x + y) % 3 != 0 && x + y != 0)
					{
						nodes.put(new Point(x, y), new Node(x, y));
					}
				}
			}
		}

		setUpReferences();
	}

	/**
	 * Overwrites this grid's edges and ports
	 * 
	 * @param ports the ports
	 */
	public void setPorts(List<Port> ports)
	{
		List<Edge> es = new ArrayList<>();
		es.addAll(edges);
		this.ports = ports;

		// Set exchange resources
		for (Port p : ports)
		{
			if (p.getExchangeType().equals(ResourceType.Generic))
			{
				p.setExchangeAmount(3);
			}
			else
			{
				p.setExchangeAmount(2);
			}
		}

		// If edge is a port, overwrite
		for (Edge e : es)
		{
			for (Port p : ports)
			{
				if (e.equals(p))
				{
					e.getX().removeEdge(e);
					e.getY().removeEdge(e);
					edges.remove(e);

					e.getX().addEdge(p);
					e.getY().addEdge(p);
					edges.add(p);
					break;
				}
			}
		}

	}

	private boolean inBoundries(int x, int y)
	{
		Boolean terms[] = {
				y - 2 * x <= 8,
				2 * y - x <= 8,
				x + y <= 8,
				y - 2 * x >= -8,
				2 * y - x >= -8,
				x + y >= -8
		};
		return Stream.of(terms).reduce(true, (a, b) -> a && b);
	}
}
