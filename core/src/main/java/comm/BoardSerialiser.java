package main.java.comm;

import java.lang.reflect.Type;
import java.util.List;

import main.java.board.*;
import main.java.comm.messages.BoardMessage;

import com.google.gson.*;

public class BoardSerialiser implements JsonSerializer<BoardMessage>
{

	@Override
	public JsonElement serialize(BoardMessage message, Type type, JsonSerializationContext context)
	{
		// Set up object
		JsonObject obj = new JsonObject();
		obj.addProperty("hexes", serialiseHexes(message.getHexes()));
		obj.addProperty("nodes", serialiseNodes(message.getNodes()));
		obj.addProperty("ports", serialisePorts(message.getPorts()));
		obj.addProperty("edges", serialiseEdges(message.getEdges()));
		
		return obj;
	}

	/**
	 * Creates a coordinate object in JSON
	 * @param x the x coord
	 * @param y the y coord
	 * @return the JsonObject representing the coordinate object
	 */
	private JsonObject serialiseCoord(int x, int y)
	{
		JsonObject jObj = new JsonObject();
		jObj.addProperty("x", x);
		jObj.addProperty("y", y);
	
		return jObj;
	}
	
	/**
	 * Serialises a list of edges into a JSON object array
	 * @param edges the list to serialise
	 * @return the JSON object array
	 */
	private String serialiseEdges(List<Edge> edges)
	{
		JsonArray arr = new JsonArray();
		
		// for each edge
		for(Edge e : edges)
		{
			// Set up Json Object to add to the array
			JsonObject jObj = serialiseEdge(e);
			
			arr.add(jObj);
		}
		
		return arr.getAsString();
	}

	/**
	 * Serialises a list of ports into a JSON object array
	 * @param ports the list to serialise
	 * @return the JSON object array
	 */
	private String serialisePorts(List<Port> ports)
	{
		JsonArray arr = new JsonArray();
		
		// for each edge
		for(Port port : ports)
		{
			// Set up JsonObject
			JsonObject jObj = serialiseEdge(port.getRoad().getEdge());
			
			// Add info about resource exchange
			jObj.addProperty("exchangeAmount", port.getExchangeAmount());
			jObj.addProperty("exchangeType", port.getExchangeType().toString());
			jObj.addProperty("returnType", port.getReturnType().toString());
			jObj.addProperty("returnAmount", port.getReturnAmount());
			
			arr.add(jObj);
		}
		
		return arr.getAsString();
	}

	private JsonObject serialiseEdge(Edge e)
	{
		// Set up JsonObjects for node 1 and 2
		Node n1 = e.getX(), n2 = e.getY();
		JsonObject c1 = serialiseCoord(n1.getX(), n1.getY()), c2 = serialiseCoord(n2.getX(), n2.getY());
		
		// Set up Json Object to return
		JsonObject jObj = new JsonObject();
		jObj.addProperty("c1", c1.getAsString());
		jObj.addProperty("c2", c2.getAsString());
		
		return jObj;
	}
	
	/**
	 * Serialises a list of nodes into a JSON object array
	 * @param nodes the list to serialise
	 * @return the JSON object array
	 */
	private String serialiseNodes(List<Node> nodes)
	{
		JsonArray arr = new JsonArray();
		
		// for each node
		for(Node n : nodes)
		{
			// Set up Json Object to add to the array
			JsonObject jObj = serialiseCoord(n.getX(), n.getY());
			
			arr.add(jObj);
		}
		
		return arr.getAsString();
	}

	/**
	 * Serialises a list of hexes into a JSON object array
	 * @param hexes the list to serialise
	 * @return the JSON object array
	 */
	private String serialiseHexes(List<Hex> hexes)
	{
		JsonArray arr = new JsonArray();
		
		// for each hex
		for(Hex h : hexes)
		{
			// Set up Json Object to add to the array
			JsonObject jObj = serialiseCoord(h.getX(), h.getY());
			jObj.addProperty("resource", h.getResource().toString());
			
			arr.add(jObj);
		}
		
		return arr.getAsString();
	}

	/**
	 * Serialise a board message so it can be sent across the network
	 * @param msg the message
	 * @return the byte array representing the serialised object
	 */
	public byte[] serialise(BoardMessage msg)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BoardMessage.class, this);
		Gson gson = builder.create();
		
		return gson.toJson(msg, BoardMessage.class).getBytes();
	}
}