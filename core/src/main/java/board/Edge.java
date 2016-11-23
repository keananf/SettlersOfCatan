package main.java.board;

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
		Node goal = null, start = null;
		Node otherX = other.x;
		Node otherY = other.y;
		int xyDistance = 0, yxDistance = 0, xxDistance = 0, yyDistance = 0;
		
		// Find raw differences between both nodes of this edge and the nodes of the other edge.
		// This will determine which is the goal node of the other edge, as well as which node of this edge
		// to start with.
		xyDistance = x.getCoordDistance(otherY);
		yxDistance = y.getCoordDistance(otherX);
		xxDistance = x.getCoordDistance(otherX);
		yyDistance = y.getCoordDistance(otherY);

		// Determine start and goal node
		if(xyDistance <= yxDistance && xyDistance <= xxDistance && xyDistance <= yyDistance)
		{
			start = x;
			goal = otherY;
		}
		else if(yxDistance <= xyDistance && yxDistance <= xxDistance && yxDistance <= yyDistance)
		{
			start = y;
			goal = otherX;
		}
		else if(xxDistance <= xyDistance && xxDistance <= yxDistance && xxDistance <= yyDistance)
		{
			start = x;
			goal = otherX;
		}
		else if(yyDistance <= xyDistance && yyDistance <= xxDistance && yyDistance <= yxDistance)
		{
			start = y;
			goal = otherY;
		}
		
		return navigate(start, goal);
	}
	
	/**
	 * Finds the shortest path to the other node by navigating along the edges
	 * @param node the given node
	 * @param goalNode the node to find
	 * @return
	 */
	private int navigate(Node node, Node goalNode)
	{
		int xDistance = Math.abs(node.getX() - goalNode.getX());
		int yDistance = Math.abs(node.getY() - goalNode.getY());
		
		// If we've reached it
		if(xDistance == 0 && yDistance == 0)
		{
			return 1;
		}
		
		// Find the next closest node adjacent to this one
		Edge next = findNextNode(node, goalNode);
		Node nextNode = next.x.equals(node) ? next.y : next.x;
		return 1 + next.navigate(nextNode, goalNode);
	}
	
	/**
	 * Based on node's edges, this find the next node that
	 * brings us closer to the goal
	 * @param node the node whose edge's we're checking
	 * @param goalNode the node we're trying to reach
	 * @return
	 */
	private Edge findNextNode(Node node, Node goalNode)
	{
		int distance1 = 5000, distance2 = 5000;
		Edge edge1 = null, edge2 = null; 
		
		// For each of node's edges except for 'this'
		for(Edge e : node.getEdges())
		{
			Node other = e.x.equals(node) ? e.y : e.x;
			if(e.equals(this)) continue;
			
			if(distance1 == 5000)
			{
				edge1 = e;
				distance1 = other.getCoordDistance(goalNode);
			}
			else if(distance2 == 5000)
			{
				edge2 = e;
				distance2 = other.getCoordDistance(goalNode);
			}
		}
		
		// Return the node with the smallest distance between the goal node.
		return Math.min(distance1, distance2) == distance1 ? edge1 : edge2;
	}
	
	/**
	 * Makes a new edge between the given nodes, provided it is not a duplicate
	 * @param node one of the nodes uniquely describing this edge
	 * @param neighbour one of the nodes uniquely describing this edge
	 * @param edges the total set of edges so far
	 * @return 
	 */
	public static Edge makeEdge(Node node, Node neighbour, List<Edge> edges)
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
		
		// Successful
		if(!duplicate)
		{
			edges.add(e);
			return e;
		}
		return null;
	}
	
	@Override
	public boolean equals(Object e)
	{
		return ((Edge)e).x.equals(x) && ((Edge)e).y.equals(y); 
	}
}
