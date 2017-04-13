package grid;

import enums.ResourceType;
import exceptions.BankLimitException;
import exceptions.CannotAffordException;
import game.Bank;
import game.players.Player;
import intergroup.board.Board;

import java.util.*;

/**
 * Class describing a port
 * 
 * @author 140001596
 */
public class Port extends Edge
{
	private ResourceType exchangeType;
	public static final int EXCHANGE_AMOUNT = 3, RETURN_AMOUNT = 1;

	public Port(Node x, Node y)
	{
		super(x, y);
	}

	/**
	 * Makes a new port between the given nodes, by randomly allocating the type
	 * 
	 * @param node one of the nodes uniquely describing this port
	 * @param neighbour one of the nodes uniquely describing this port
	 * @param availablePorts a list of total available ports
	 * @return
	 */
	public static Port makePort(Node node, Node neighbour, List<Port> availablePorts)
	{
		// Allocate port
		Random rand = new Random();
		int index = rand.nextInt(availablePorts.size());
		Port port = availablePorts.get(index);

		// Add in right order
		if (node.getX() + node.getY() < neighbour.getX() + neighbour.getY())
		{
			port.setX(node);
			port.setY(neighbour);
		}
		else
		{
			port.setX(neighbour);
			port.setY(node);
		}

		availablePorts.remove(index);
		return port;
	}

	public static List<Port> makePorts(List<Edge> edges, List<Edge> portLocations)
	{
		List<Port> availablePorts = getAvailablePorts();
		List<Port> ports = new ArrayList<Port>();

		// For each potential port
		for (Edge e : portLocations)
		{
			Port p = makePort(e.getX(), e.getY(), availablePorts);
			if (p != null)
			{
				e.getX().removeEdge(e);
				e.getY().removeEdge(e);
				edges.remove(e);

				e.getX().addEdge(p);
				e.getY().addEdge(p);
				edges.add(p);
				ports.add(p);
			}
		}

		// Return created ports
		return ports;
	}

	/**
	 * @return list of available ports
	 */
	private static List<Port> getAvailablePorts()
	{
		List<Port> ports = new LinkedList<Port>();

		// Default ports
		for (int i = 0; i < 4; i++)
		{
			Port p = new Port(new Node(0, 0), new Node(-1, -1));
			p.exchangeType = ResourceType.Generic; // signifies 'Any'

			ports.add(p);
		}

		// One port for each resource type
		for (ResourceType r : ResourceType.values())
		{
			if (r == ResourceType.Generic) continue;

			Port p = new Port(new Node(0, 0), new Node(-1, -1));
			p.exchangeType = r;

			ports.add(p);
		}

		return ports;
	}

	/**
	 * @return the version of this edge that can be sent across the network
	 */
	public Board.Harbour toPortProto()
	{
		Board.Harbour.Builder port = Board.Harbour.newBuilder();

		port.setLocation(toEdgeProto());
		port.setResource(ResourceType.toProto(exchangeType));

		return port.build();
	}

	/**
	 * @return the exchangeType
	 */
	public ResourceType getExchangeType()
	{
		return exchangeType;
	}

	/**
	 * @param exchangeType the exchangeType to set
	 */
	public void setExchangeType(ResourceType exchangeType)
	{
		this.exchangeType = exchangeType;
	}

	/**
	 * Performs the port trade
	 * 
	 * @param offerer the offerer
	 * @param offer the offer
	 * @param request the request
	 */
	public void exchange(Player offerer, Map<ResourceType, Integer> offer, Map<ResourceType, Integer> request,
			Bank bank) throws CannotAffordException, BankLimitException
	{
		offerer.spendResources(offer, bank);
		offerer.grantResources(request, bank);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Port port = (Port) o;

		return exchangeType == port.exchangeType;
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + (exchangeType != null ? exchangeType.hashCode() : 0);
		return result;
	}
}