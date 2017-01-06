package comm;

import java.lang.reflect.Type;

import comm.messages.ResourceCount;

import com.google.gson.*;

public class ResourceDeserialiser implements JsonDeserializer<ResourceCount>
{

	@Override
	public ResourceCount deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		// Instantiate the message object
		ResourceCount message = new ResourceCount();
		JsonObject jObj = json.getAsJsonObject();
		message.setBrick(jObj.get("brick").getAsInt());
		message.setWool(jObj.get("wool").getAsInt());
		message.setGrain(jObj.get("grain").getAsInt());
		message.setLumber(jObj.get("lumber").getAsInt());
		message.setOre(jObj.get("ore").getAsInt());
		
		return message;
	}
}