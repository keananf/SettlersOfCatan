package main.java.comm;

import java.lang.reflect.Type;

import main.java.comm.messages.ResourceCount;
import main.java.comm.messages.TurnUpdateMessage;
import main.java.enums.Colour;

import com.google.gson.*;

public class TurnDeserialiser implements JsonDeserializer<TurnUpdateMessage>
{

	@Override
	public TurnUpdateMessage deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		// Set up resources deserialiser
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(ResourceCount.class, new ResourceDeserialiser());
		Gson gson = builder.create();
		
		// Instantiate the message object
		TurnUpdateMessage message = new TurnUpdateMessage();
		JsonObject jObj = json.getAsJsonObject();
		message.setDice(jObj.get("dice").getAsInt());
		message.setPlayer(Colour.valueOf(jObj.get("colour").getAsString()));
		message.setResources(gson.fromJson(jObj.get("resources"), ResourceCount.class));
		
		return message;
	}
}
