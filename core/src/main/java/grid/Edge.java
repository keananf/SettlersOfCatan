package grid;

import com.badlogic.gdx.math.Vector3;
import game.build.Road;
import intergroup.board.Board;

import java.util.List;

/**
 * Class uniquely describing an edge (between two edges)
 * 
 * @author 140001596
 */
public class Edge implements BoardElement
{
	private Node x, y; // way of uniquely describing an edge
	private Road road;

	Edge(Node x, Node y)
	{
		this.setX(x);
		this.setY(y);
	}

	/**
	 * Returns the distance between the two edges
	 * 
	 * @param other the edge to check
	 * @return the distance between thw two
	 */
	public int distance(Edge other)
	{
		Node goal = null, start = null;
		Node otherX = other.getX();
		Node otherY = other.getY();
		int xyDistance, yxDistance, xxDistance, yyDistance;

		// Find raw differences between both nodes of this edge and the nodes of
		// the other edge.
		// This will determine which is the goal node of the other edge, as well
		// as which node of this edge
		// to start with.
		xyDistance = getX().getCoordDistance(otherY);
		yxDistance = getY().getCoordDistance(otherX);
		xxDistance = getX().getCoordDistance(otherX);
		yyDistance = getY().getCoordDistance(otherY);

		// Determine start and goal node
		if (xyDistance <= yxDistance && xyDistance <= xxDistance && xyDistance <= yyDistance)
		{
			start = getX();
			goal = otherY;
		}
		else if (yxDistance <= xyDistance && yxDistance <= xxDistance && yxDistance <= yyDistance)
		{
			start = getY();
			goal = otherX;
		}
		else if (xxDistance <= xyDistance && xxDistance <= yxDistance && xxDistance <= yyDistance)
		{
			start = getX();
			goal = otherX;
		}
		else if (yyDistance <= xyDistance && yyDistance <= xxDistance && yyDistance <= yxDistance)
		{
			start = getY();
			goal = otherY;
		}

		return navigate(start, goal);
	}

	/**
	 * Finds the shortest path to the other node by navigating along the edges
	 * 
	 * @param node the given node
	 * @param goalNode the node to find
	 * @return
	 */
	private int navigate(Node node, Node goalNode)
	{
		int xDistance = Math.abs(node.getX() - goalNode.getX());
		int yDistance = Math.abs(node.getY() - goalNode.getY());

		// If we've reached it
		if (xDistance == 0 && yDistance == 0) { return 1; }

		// Find the next closest node adjacent to this one
		Edge next = findNextNode(node, goalNode);
		Node nextNode = next.getX().equals(node) ? next.getY() : next.getX();
		return 1 + next.navigate(nextNode, goalNode);
	}

	/**
	 * Based on node's edges, this find the next node that brings us closer to
	 * the goal
	 * 
	 * @param node the node whose edge's we're checking
	 * @param goalNode the node we're trying to reach
	 * @return
	 */
	private Edge findNextNode(Node node, Node goalNode)
	{
		int distance1 = 5000, distance2 = 5000;
		Edge edge1 = null, edge2 = null;

		// For each of node's edges except for 'this'
		for (Edge e : node.getEdges())
		{
			Node other = e.getX().equals(node) ? e.getY() : e.getX();
			if (e.equals(this)) continue;

			if (distance1 == 5000)
			{
				edge1 = e;
				distance1 = other.getCoordDistance(goalNode);
			}
			else if (distance2 == 5000)
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
	 * 
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
		if (node.getX() + node.getY() < neighbour.getX() + neighbour.getY())
			e = new Edge(node, neighbour);
		else
			e = new Edge(neighbour, node);

		// Check this edge has not been created before
		for (Edge other : edges)
		{
			if (other.getX().equals(e.getX()) && other.getY().equals(e.getY()))
			{
				duplicate = true;
			}
		}

		// Successful
		if (!duplicate)
		{
			edges.add(e);
			return e;
		}
		return null;
	}

	/**
	 * Checks if there is a settlement on either of this edge's nodes
	 * 
	 * @return true / false indicating if it is a valid move
	 */
	public boolean hasSettlement()
	{
		Node n1 = getX(), n2 = getY();

		// If there is a settlement on one of its nodes
		return n1.getBuilding() != null || n2.getBuilding() != null;

	}

	@Override
	public boolean equals(Object e)
	{
		if (!(e instanceof Edge)) return false;

		return ((Edge) e).getX().equals(getX()) && ((Edge) e).getY().equals(getY());
	}

	@Override
	public int hashCode()
	{
		int result = x.hashCode();
		result = 31 * result + y.hashCode();
		result = 31 * result + (road != null ? road.hashCode() : 0);
		return result;
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
	void setX(Node x)
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
	void setY(Node y)
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

	/**
	 * @return the version of this edge that can be sent across the network
	 */
	public Board.Edge toEdgeProto()
	{
		Board.Point.Builder p = Board.Point.newBuilder();
		Board.Edge.Builder e = Board.Edge.newBuilder();

		// Node 1
		p.setX(getX().getX());
		p.setY(getX().getY());
		e.setA(p.build());

		// Node 2
		p.setX(getY().getX());
		p.setY(getY().getY());
		e.setB(p.build());

		return e.build();
	}

	/**
	 * Detects whether the two edges are connected
	 * 
	 * @param otherEdge the edge to check against
	 * @return if they're connected
	 */
	public boolean isConnected(Edge otherEdge)
	{
		// If these two edges share a node
		return getX().equals(otherEdge.getX()) || getX().equals(otherEdge.getY()) || getY().equals(otherEdge.getX())
				|| getY().equals(otherEdge.getY());

	}

	public Vector3 get3dVectorMidpoint()
	{
		Node a = getX();
		Node b = getY();
		int aX = a.getX();
		int bX = b.getX();
		int aY = a.getY();
		int bY = b.getY();

		Vector3 aVector = new Vector3((float) aX, 0.1f, (float) ((2 * (float) aY - (float) aX) / Math.sqrt(3)));
		Vector3 bVector = new Vector3((float) bX, 0.1f, (float) ((2 * (float) bY - (float) bX) / Math.sqrt(3)));

		float cartesianXMidpoint = (aVector.x + bVector.x) / 2;
		float cartesianYMidpoint = (aVector.z + bVector.z) / 2;

		return new Vector3(cartesianXMidpoint, 0.1f, cartesianYMidpoint);

	}

}
