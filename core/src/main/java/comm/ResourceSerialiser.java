package main.java.comm;

import java.lang.reflect.Type;

import main.java.comm.messages.ResourceCount;

import com.google.gson.*;

public class ResourceSerialiser implements JsonSerializer<ResourceCount>
{

	@Override
	public JsonElement serialize(ResourceCount message, Type type, JsonSerializationContext context)
	{
		// Set up object
		JsonObject obj = new JsonObject();
		obj.addProperty("brick", message.getBrick());
		obj.addProperty("wool", message.getWool());
		obj.addProperty("grain", message.getGrain());
		obj.addProperty("ore", message.getOre());
		obj.addProperty("lumber", message.getLumber());
		
		return obj;
	}
}