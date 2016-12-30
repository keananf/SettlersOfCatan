package main.java.comm.messages;

import java.awt.Point;
import java.util.*;

import main.java.board.*;

public class BoardMessage
{
	private List<Hex> hexes;
	private List<Node> nodes;
	private List<Port> ports;
	private List<Edge> edges;
	
	/**
	 * @return the hexes
	 */
	public List<Hex> getHexes()
	{
		return hexes;
	}
	/**
	 * @param hexes the hexes to set
	 */
	public void setHexes(List<Hex> hexes)
	{
		this.hexes = hexes;
	}
	/**
	 * @param hexes the hexes to set
	 */
	public void setHexes(Hashtable<Point, Hex> hexes)
	{
		List<Hex> list = new ArrayList<Hex>();
		list.addAll(hexes.values());
		
		this.hexes = list;
	}
	/**
	 * @return the nodes
	 */
	public List<Node> getNodes()
	{
		return nodes;
	}
	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(List<Node> nodes)
	{
		this.nodes = nodes;
	}
	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(Hashtable<Point, Node> nodes)
	{
		List<Node> list = new ArrayList<Node>();
		list.addAll(nodes.values());
		
		this.nodes = list;
	}
	/**
	 * @return the ports
	 */
	public List<Port> getPorts()
	{
		return ports;
	}
	/**
	 * @param ports the ports to set
	 */
	public void setPorts(List<Port> ports)
	{
		this.ports = ports;
	}
	/**
	 * @return the edges
	 */
	public List<Edge> getEdges()
	{
		return edges;
	}
	/**
	 * @param edges the edges to set
	 */
	public void setEdges(List<Edge> edges)
	{
		this.edges = edges;
	}
}
