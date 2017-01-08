package comm;

import java.lang.reflect.Type;

import comm.messages.Response;

import com.google.gson.*;


/**
 * This class serialises a response
 * @author 140001596
 */
public class ResponseSerialiser implements JsonSerializer<Response>
{

	@Override
	public JsonElement serialize(Response message, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty("status", message.getResponse());
		obj.addProperty("type", message.getType().toString());
		obj.addProperty("message", message.getMsg());
		
		return obj;
	}

	/**
	 * Serialise a response object
	 * @param resp the object to serialise
	 * @return the bytes
	 */
	public byte[] serialise(Response resp)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(Response.class, this);
		Gson gson = builder.create();
		
		return gson.toJson(resp, Response.class).getBytes();
	}
}
