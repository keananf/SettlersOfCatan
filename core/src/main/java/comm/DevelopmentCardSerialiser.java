package main.java.comm;

import java.lang.reflect.Type;

import main.java.game.build.DevelopmentCard;

import com.google.gson.*;

public class DevelopmentCardSerialiser implements JsonSerializer<DevelopmentCard>
{

	@Override
	public JsonElement serialize(DevelopmentCard card, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("type", card.getType().toString());
		obj.addProperty("colour", card.getColour().toString());
		
		return obj;
	}

	public static String serialise(DevelopmentCard c)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(DevelopmentCard.class, new DevelopmentCardSerialiser());
		Gson gson = builder.create();
		
		
		return gson.toJson(c);
	}
}
