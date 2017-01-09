package board;

import enums.ResourceType;
import protocol.BoardProtos.*;
import protocol.BuildProtos.*;

import java.util.*;

/**
 * Class describing a port
 * @author 140001596
 */
public class Port extends Edge 
{
	private ResourceType exchangeType, receiveType;
	private int exchangeAmount, receiveAmount;
	
	public Port(Node x, Node y)
	{
		super(x, y);
	}

	/**
	 * Makes a new port between the given nodes, provided it is not a duplicate
	 * @param node one of the nodes uniquely describing this port
	 * @param neighbour one of the nodes uniquely describing this port
	 * @param currentPorts the total set of ports so far
	 * @param availablePorts a list of total available ports
	 * @return 
	 */
	public static Port makePort(Node node, Node neighbour, List<Port> currentPorts, List<Port> availablePorts)
	{
		Port port = null;
		boolean duplicate = false;
		Random rand = new Random();
		
		// Allocate port
		int index = rand.nextInt(availablePorts.size());
		port = availablePorts.get(index);
		
		// Add in right order
		if(node.getX() + node.getY() < neighbour.getX() + neighbour.getY())
		{
			port.setX(node);
			port.setY(neighbour);
		}
		else
		{
			port.setX(neighbour);
			port.setY(node);				
		}			
	
		// Check this edge has not been created before
		for(Port other : currentPorts)
		{
			if(other.getX().equals(port.getX()) && other.getY().equals(port.getY()))
			{
				duplicate = true;
			}
		}
		
		// If successful, update collections
		if(!duplicate && port.validCoordinates(currentPorts))
		{
			availablePorts.remove(index);
			currentPorts.add(port);
			return port;
		}
		
		return null;
	}

	private boolean validCoordinates(List<Port> currentPorts)
	{
		boolean three = false;
			
		for(Port port : currentPorts)
		{
			// Find number of edges apart
			int distance = this.distance(port);
			
			if(distance == 0 || distance == 1)
				return false;
			
			if(distance == 3)
			{
				// Can only be three edges away from one port.
				// The other must be two  away
				if(!three) three = true;
				else return false;
			}
		}
		
		return true;
	}

	public static List<Port> makePorts(List<Edge> edges, List<Edge> potentialPorts)
	{
		List<Port> availablePorts = getAvailablePorts();
		List<Port> ports = new ArrayList<Port>();
		
		// For each potential port
		for(Edge e : potentialPorts)
		{
			if (availablePorts.size() > 0)
			{
				Port p = makePort(e.getX(), e.getY(), ports, availablePorts);
				if (p != null)
				{
					e.getX().removeEdge(e);
					e.getY().removeEdge(e);
					edges.remove(e);

					e.getX().addEdge(p);
					e.getY().addEdge(p);
					edges.add(p);
				}
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
		for(int i = 0; i < 4; i++)
		{
			Port p = new Port(new Node(0,0), new Node(-1, -1)); //default nodes
			p.exchangeAmount = 3;
			p.exchangeType = ResourceType.Generic; // signifies 'Any'
			p.receiveAmount = 1;
			p.receiveType = ResourceType.Generic; // signifies 'Any'
			
			ports.add(p);
		}
			
		// One port for each resource type
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.Generic) continue;
			
			Port p = new Port(new Node(0,0), new Node(-1, -1)); //default nodes
			p.exchangeAmount = 2;
			p.exchangeType = r;
			p.receiveAmount = 1;
			p.receiveType = r;
			
			ports.add(p);
		}
		
		return ports;
	}


	/**
	 * @return the version of this edge that can be sent across the network
	 */
	public PortProto toPortProto()
	{
		PortProto.Builder port = PortProto.newBuilder();
		PointProto.Builder p = PointProto.newBuilder();

		// Node 1
		p.setX(getX().getX());
		p.setY(getX().getY());
		port.setP1(p.build());

		// Node 2
		p.setX(getY().getX());
		p.setY(getY().getY());
		port.setP2(p.build());

		port.setExchangeAmount(exchangeAmount);
		port.setReturnAmount(receiveAmount);
		port.setExchangeResource(ResourceType.toProto(exchangeType));
		port.setReturnResource(ResourceType.toProto(receiveType));

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
	 * @return the receiveType
	 */
	public ResourceType getReturnType()
	{
		return receiveType;
	}

	/**
	 * @param receiveType the receiveType to set
	 */
	public void setReturnType(ResourceType receiveType)
	{
		this.receiveType = receiveType;
	}

	/**
	 * @return the exchangeAmount
	 */
	public int getExchangeAmount()
	{
		return exchangeAmount;
	}

	/**
	 * @param exchangeAmount the exchangeAmount to set
	 */
	public void setExchangeAmount(int exchangeAmount)
	{
		this.exchangeAmount = exchangeAmount;
	}

	/**
	 * @return the receiveAmount
	 */
	public int getReturnAmount()
	{
		return receiveAmount;
	}

	/**
	 * @param receiveAmount the receiveAmount to set
	 */
	public void setReturnAmount(int receiveAmount)
	{
		this.receiveAmount = receiveAmount;
	}

}