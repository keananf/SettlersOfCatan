package main.java.board;

import main.java.game.enums.ResourceType;

import java.util.*;

/**
 * Class describing a port
 * @author 140001596
 */
public class Port extends Edge 
{
	ResourceType exchangeType, receiveType;
	int exchangeAmount, receiveAmount;
	
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
			port.x = node;
			port.y = neighbour;
		}
		else
		{
			port.x = neighbour;
			port.y = node;				
		}			
	
		// Check this edge has not been created before
		for(Port other : currentPorts)
		{
			if(other.x.equals(port.x) && other.y.equals(port.y))
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
				Port p = makePort(e.x, e.y, ports, availablePorts);
				if (p != null)
				{
					e.x.removeEdge(e);
					e.y.removeEdge(e);
					edges.remove(e);

					e.x.addEdge(p);
					e.y.addEdge(p);
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
			p.exchangeType = ResourceType.None; // signifies 'Any'
			p.receiveAmount = 1;
			p.receiveType = ResourceType.None; // signifies 'Any'
			
			ports.add(p);
		}
			
		// One port for each resource type
		for(ResourceType r : ResourceType.values())
		{
			if(r == ResourceType.None) continue;
			
			Port p = new Port(new Node(0,0), new Node(-1, -1)); //default nodes
			p.exchangeAmount = 2;
			p.exchangeType = r;
			p.receiveAmount = 1;
			p.receiveType = r;
			
			ports.add(p);
		}
		
		return ports;
	}
}