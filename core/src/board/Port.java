package board;

import game.enums.ResourceType;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

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
	 */
	public static void makePort(Node node, Node neighbour, List<Port> currentPorts, List<Port> availablePorts)
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
		}
	}

	private boolean validCoordinates(List<Port> currentPorts)
	{
		boolean three = false, two = false;
			
		for(Port port : currentPorts)
		{
			// If this potential port shares a node with an existing one, not valid
			if(x.equals(port.x) || y.equals(port.y) ||
					y.equals(port.x) || x.equals(port.y) )
				return false;
			
			int distance = this.distance(port);
			
			if(distance == 0)
				return false;
			
			// If one edge away
			if(distance == 1)
			{
				// Can only be two edges away from one port.
				// The other must be three away
				if(!two) two = true;
				else return false;
			}
			
			if(distance == 2)
			{
				// Can only be three edges away from one port.
				// The other must be two  away
				if(!three) three = true;
				else return false;
			}
		}
		
		return true;
	}

}