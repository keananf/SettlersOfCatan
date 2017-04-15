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

	public Edge(Node x, Node y)
	{
		this.setX(x);
		this.setY(y);
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
