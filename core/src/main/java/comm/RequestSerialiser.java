package comm;

import java.lang.reflect.Type;

import comm.messages.Request;

import com.google.gson.*;


/**
 * This class serialises a request
 * @author 140001596
 */
public class RequestSerialiser implements JsonSerializer<Request>
{

	@Override
	public JsonElement serialize(Request message, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty("type", message.getType().toString());
		obj.addProperty("message", message.getMsg());
		
		return obj;
	}
}
