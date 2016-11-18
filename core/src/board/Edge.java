package board;

import java.util.HashSet;
import java.util.List;


/**
 * Class uniquely describing an edge (between two edges)
 * @author 140001596
 */
public class Edge //TODO extend BoardElement
{
	Node x, y; // way of uniquely describing an edge
	
	public Edge(Node x, Node y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the distance between the two edges
	 * @param other the edge to check
	 * @return the distance between thw two
	 */
	public int distance(Edge other)
	{
		Node otherX = other.x;
		Node otherY = other.y;
		int xDistance = 0, yDistance = 0;
		
		xDistance = Math.min(Math.abs(x.getX() - otherX.getX()), Math.abs(x.getY() - otherX.getY()));
		yDistance = Math.min(Math.abs(y.getX() - otherY.getX()), Math.abs(y.getY() - otherY.getY()));
		
		return Math.min(xDistance,  yDistance);
	}
	
	/**
	 * Makes a new edge between the given nodes, provided it is not a duplicate
	 * @param node one of the nodes uniquely describing this edge
	 * @param neighbour one of the nodes uniquely describing this edge
	 * @param edges the total set of edges so far
	 */
	public static void makeEdge(Node node, Node neighbour, List<Edge> edges)
	{
		Edge e;
		boolean duplicate = false;
		
		// Add in right order
		if(node.getX() + node.getY() < neighbour.getX() + neighbour.getY())
			e = new Edge(node, neighbour);
		else
			e = new Edge(neighbour, node);
		
		// Check this edge has not been created before
		for(Edge other : edges)
		{
			if(other.x.equals(e.x) && other.y.equals(e.y))
			{
				duplicate = true;
			}
		}
		if(!duplicate) edges.add(e);
	}
}
