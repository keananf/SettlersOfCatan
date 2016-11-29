package catan.board;

import java.awt.Point;
import java.util.*;

import catan.game.build.Building;
import catan.game.build.Road;
import catan.game.build.Settlement;

/**
 * Class uniquely describing an edge (between two edges)
 * @author 140001596
 */
public class Edge //TODO extend BoardElement
{
	private Node x, y; // way of uniquely describing an edge
	private Road  road;

	public Edge(Node x, Node y)
	{
		this.setX(x);
		this.setY(y);
	}

	/**
	 * Returns the distance between the two edges
	 * @param other the edge to check
	 * @return the distance between thw two
	 */
	public int distance(Edge other)
	{
		Node goal = null, start = null;
		Node otherX = other.getX();
		Node otherY = other.getY();
		int xyDistance = 0, yxDistance = 0, xxDistance = 0, yyDistance = 0;

		// Find raw differences between both nodes of this edge and the nodes of the other edge.
		// This will determine which is the goal node of the other edge, as well as which node of this edge
		// to start with.
		xyDistance = getX().getCoordDistance(otherY);
		yxDistance = getY().getCoordDistance(otherX);
		xxDistance = getX().getCoordDistance(otherX);
		yyDistance = getY().getCoordDistance(otherY);

		// Determine start and goal node
		if(xyDistance <= yxDistance && xyDistance <= xxDistance && xyDistance <= yyDistance)
		{
			start = getX();
			goal = otherY;
		}
		else if(yxDistance <= xyDistance && yxDistance <= xxDistance && yxDistance <= yyDistance)
		{
			start = getY();
			goal = otherX;
		}
		else if(xxDistance <= xyDistance && xxDistance <= yxDistance && xxDistance <= yyDistance)
		{
			start = getX();
			goal = otherX;
		}
		else if(yyDistance <= xyDistance && yyDistance <= xxDistance && yyDistance <= yxDistance)
		{
			start = getY();
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
		Node nextNode = next.getX().equals(node) ? next.getY() : next.getX();
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
			Node other = e.getX().equals(node) ? e.getY() : e.getX();
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
			if(other.getX().equals(e.getX()) && other.getY().equals(e.getY()))
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


	/**
	 * Checks that a road is within a distance of two to a settlement
	 * @param r the road that is attempting to be built
	 * @return true / false indicating if it is a valid move
	 */
	public boolean isNearSettlement(HashMap<Point, Building> settlements)
	{
		Node n1 = getX(), n2 = getY();
		List<Edge> edges = new ArrayList<Edge>();
		edges.addAll(n1.getEdges());
		edges.addAll(n2.getEdges());

		// If there is a settlement on one of its nodes
		if(settlements.containsKey(new Point(n1.getX(), n1.getY()))
				|| settlements.containsKey(new Point(n2.getX(), n2.getY())))
		{
			return true;
		}

		// Check if there is a settlement of distance one away from one of its nodes
		for(Edge e : edges)
		{
			if(e.equals(this)) continue;

			n1 = e.getX();
			n2 = e.getY();

			// If there is a settlement on one of its nodes
			if(settlements.containsKey(new Point(n1.getX(), n1.getY()))
					|| settlements.containsKey(new Point(n2.getX(), n2.getY())))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean equals(Object e)
	{
		return ((Edge)e).getX().equals(getX()) && ((Edge)e).getY().equals(getY());
	}

	/**
	 * @return the x
	 */
	public Node getX()
	{
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(Node x)
	{
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public Node getY()
	{
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(Node y)
	{
		this.y = y;
	}

	/**
	 * @return the road
	 */
	public Road getRoad()
	{
		return road;
	}

	/**
	 * @param road the road to set
	 */
	public void setRoad(Road road)
	{
		this.road = road;
	}
}
