package main.java.comm;

import java.lang.reflect.Type;
import main.java.enums.MoveType;

import com.google.gson.*;

/**
 * This class deserialises a Response
 * @author 140001596
 */
public class ResponseDeserialiser implements JsonDeserializer<Response>
{

	@Override
	public Response deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		Response response = new Response();
		
		// Instantiate the message object
		MoveType msgType = MoveType.valueOf(jObj.get("type").getAsString());
		response.setResponse(jObj.get("status").getAsString());
		response.setType(msgType);
		response.setMsg(jObj.get("message").getAsString());
		
		return response;
	}

}
