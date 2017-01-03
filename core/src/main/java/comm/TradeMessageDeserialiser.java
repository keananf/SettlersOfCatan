package main.java.comm;

import java.lang.reflect.Type;

import main.java.comm.messages.*;
import main.java.enums.*;

import com.google.gson.*;

public class TradeMessageDeserialiser implements JsonDeserializer<TradeMessage>
{

	@Override
	public TradeMessage deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		TradeMessage msg = new TradeMessage();
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(ResourceCount.class, new ResourceSerialiser());
		Gson gson = builder.create();
		
		// Instantiate the move object
		msg.setMessage(jObj.get("message").getAsString());
		msg.setStatus(TradeStatus.valueOf(jObj.get("status").getAsString()));
		msg.setPlayerColour(Colour.valueOf(jObj.get("offererColour").getAsString()));
		msg.setRecipient(Colour.valueOf(jObj.get("recipientColour").getAsString()));
		msg.setRequest(gson.fromJson(jObj.get("request").getAsString(), ResourceCount.class));
		msg.setOffer(gson.fromJson(jObj.get("offer").getAsString(), ResourceCount.class));		
		
		return msg;
	}
}