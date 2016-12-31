package main.java.comm;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import main.java.comm.messages.BoardMessage;
import main.java.enums.ResourceType;
import main.java.board.*;

import com.google.gson.*;

public class BoardDeserialiser implements JsonDeserializer<BoardMessage>
{

	@Override
	public BoardMessage deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		BoardMessage msg = new BoardMessage();
		
		// Instantiate the message object		
		msg.setHexes(deserialiseHexes(jObj.get("hexes").getAsJsonArray()));
		msg.setNodes(deserialiseNodes(jObj.get("nodes").getAsJsonArray()));
		msg.setPorts(deserialisePorts(jObj.get("ports").getAsJsonArray()));
		msg.setEdges(deserialiseEdges(jObj.get("edges").getAsJsonArray()));
		
		return msg;
	}
	
	/**
	 * Deserialises an individual edge
	 * @param jObj the edge as a JsonObject
	 * @return the edge
	 */
	private Edge deserialiseEdge(JsonObject jObj)
	{
		// Set up new edge and nodes
		JsonObject c1 = jObj.get("c1").getAsJsonObject(), c2 = jObj.get("c2").getAsJsonObject();
		Node n1 = new Node(c1.get("x").getAsInt(), c1.get("y").getAsInt());
		Node n2 = new Node(c2.get("x").getAsInt(), c2.get("y").getAsInt());
		
		Edge e = new Edge(n1, n2);
		return e;
	}
	
	/**
	 * Deserialises a list of edges
	 * @param arr the json array
	 * @return the list of deserialised edges
	 */
	private List<Edge> deserialiseEdges(JsonArray arr)
	{
		List<Edge> edges = new ArrayList<Edge>();
		
		// For each json object, deserialise and add to the list
		for(JsonElement elem : arr)
		{
			edges.add(deserialiseEdge(elem.getAsJsonObject()));
		}
		
		return edges;
	}

	/**
	 * Deserialises a list of ports
	 * @param arr the json array
	 * @return the list of deserialised ports
	 */
	private List<Port> deserialisePorts(JsonArray arr)
	{
		List<Port> ports = new ArrayList<Port>();
		
		// For each json object, deserialise and add to the list
		for(JsonElement elem : arr)
		{
			// Find edge coordinates 
			JsonObject jObj = elem.getAsJsonObject();
			Edge e = deserialiseEdge(jObj);
			Port p = new Port(e.getX(), e.getY());
			
			// Deserialise remaining fields
			p.setExchangeAmount(jObj.get("exchangeAmount").getAsInt());
			p.setReturnAmount(jObj.get("returnAmount").getAsInt());
			p.setReturnType(ResourceType.valueOf(jObj.get("returnType").getAsString()));
			p.setExchangeType(ResourceType.valueOf(jObj.get("exchangeType").getAsString()));
			
			ports.add(p);
		}
		
		return ports;
	}

	/**
	 * Deserialises a list of nodes
	 * @param arr the json array
	 * @return the list of deserialised nodes
	 */
	private List<Node> deserialiseNodes(JsonArray arr)
	{
		List<Node> nodes = new ArrayList<Node>();
		
		// For each json object, deserialise and add to the list
		for(JsonElement elem : arr)
		{
			// Deserialise and add each node to the list
			JsonObject c = elem.getAsJsonObject();
			Node n = new Node(c.get("x").getAsInt(), c.get("y").getAsInt());
			nodes.add(n);
		}
		
		return nodes;
	}

	/**
	 * Deserialises a list of hexes
	 * @param arr the json array
	 * @return the list of deserialised hexes
	 */
	private List<Hex> deserialiseHexes(JsonArray arr)
	{
		List<Hex> hexes = new ArrayList<Hex>();
		
		// For each json object, deserialise and add to the list
		for(JsonElement elem : arr)
		{
			// Deserialise and add each node to the list
			JsonObject jObj = elem.getAsJsonObject();
			Hex hex = new Hex(jObj.get("x").getAsInt(), jObj.get("y").getAsInt());
			hex.setResource(ResourceType.valueOf(jObj.get("resource").getAsString()));
			
			hexes.add(hex);
		}
		
		return hexes;
	}

	/**
	 * Deserialise a board message so it can be parsed from the network
	 * @param bytes the bytes received
	 * @return the BoardMessage
	 */
	public BoardMessage deserialise(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BoardMessage.class, this);
		Gson gson = builder.create();
		
		return gson.fromJson(bytes.toString(), BoardMessage.class);
	}
}
