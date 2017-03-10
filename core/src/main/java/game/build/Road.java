package game.build;

import intergroup.board.Board;
import enums.Colour;
import enums.ResourceType;
import grid.Edge;
import grid.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Class describing a road
 * @author 140001596
 */
public class Road implements IBuildable
{
	private Edge edge;
	private Colour playerColour;
	
	public Road(Edge edge, Colour c)
	{
		playerColour = c;
		this.edge = edge;
	}
	private Road(){}

	/**
	 * @return a map containing the total cost for all resources
	 */
	public Map<ResourceType, Integer> getCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		resources.put(ResourceType.Brick, 1);
		resources.put(ResourceType.Lumber, 1);
		
		return resources;
	}
	
	public static Map<ResourceType, Integer> getRoadCost()
	{
		return new Road().getCost();
	}
	
	/**
	 * @return the playerColour
	 */
	public Colour getPlayerColour()
	{
		return playerColour;
	}

	/**
	 * @param playerColour the playerColour to set
	 */
	public void setPlayerColour(Colour playerColour)
	{
		this.playerColour = playerColour;
	}

	/**
	 * @return the edge
	 */
	public Edge getEdge()
	{
		return edge;
	}

	/**
	 * @param edge the edge to set
	 */
	public void setEdge(Edge edge)
	{
		this.edge = edge;
	}
	
	/**
	 * Checks to see if two roads are connected or not
	 * @param road the road to check
	 * @return if the two roads are connected
	 */
	public boolean isConnected(Road road)
	{
		Edge thisEdge = this.edge, otherEdge = road.edge;
		
		return thisEdge.isConnected(otherEdge);
	}

	/**
	 * Turns this road received from across the network into a road object used internally
	 * @param n1 the first node of the edge
	 * @param n2 the second node of the edge
	 * @param c the player's colour
	 */
    public static Road fromProto(Node n1, Node n2, Colour c)
	{
		Edge edge = n1.findEdge(n2);

		return new Road(edge, c);
    }

	/**
	 * @return A representation of this road compatible with protobufs for serialisation
	 */
	public Board.Edge toProto()
	{
		// TODO needs colour
		return edge.toEdgeProto();
	}
}
