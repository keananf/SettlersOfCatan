package main.java.comm;

import java.lang.reflect.Type;

import main.java.comm.messages.*;

import com.google.gson.*;

/**
 * This class represents a gson serialiser which serialises turn update messages.
 * 
 * Used for sending dice rolls and new resource allocations to players
 * @author 140001596
 */
public class TurnSerialiser implements JsonSerializer<TurnUpdateMessage>
{

	@Override
	public JsonElement serialize(TurnUpdateMessage message, Type type, JsonSerializationContext context)
	{
		// Set up resources serialiser
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(ResourceCount.class, new ResourceSerialiser());
		Gson gson = builder.create();
		
		// Set up object
		JsonObject obj = new JsonObject();
		obj.addProperty("dice", message.getDice());
		obj.addProperty("colour", message.getPlayer().toString());
		obj.addProperty("resources", gson.toJson(message.getResources(), ResourceCount.class));
		
		return obj;
	}

	/**
	 * Serialise a turn update message so it can be sent across the network
	 * @param msg the message
	 * @return the byte array representing the serialised object
	 */
	public byte[] serialise(TurnUpdateMessage msg)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(TurnUpdateMessage.class, this);
		Gson gson = builder.create();
		
		return gson.toJson(msg, TurnUpdateMessage.class).getBytes();
	}
}
