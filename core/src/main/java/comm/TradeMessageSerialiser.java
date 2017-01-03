package main.java.comm;

import java.lang.reflect.Type;

import main.java.comm.messages.*;
import com.google.gson.*;

public class TradeMessageSerialiser  implements JsonSerializer<TradeMessage>
{

	@Override
	public JsonElement serialize(TradeMessage msg, Type type, JsonSerializationContext context)
	{
		// Gson serialser for internal ResourceCount Object
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(ResourceCount.class, new ResourceSerialiser());
		Gson gson = builder.create();

		// Set up object 
		JsonObject obj = new JsonObject();
		obj.addProperty("offererColour", msg.getPlayerColour().toString());
		obj.addProperty("recipientColour", msg.getRecipient().toString());
		obj.addProperty("offer", msg.getRecipient().toString());
		obj.addProperty("request", gson.toJson(msg.getRequest(), ResourceCount.class));
		obj.addProperty("message", msg.getMessage());
		obj.addProperty("status", msg.getStatus().toString());
		
		return obj;
	}
}