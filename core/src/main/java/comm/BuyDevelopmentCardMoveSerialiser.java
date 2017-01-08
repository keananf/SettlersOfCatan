package comm;

import java.lang.reflect.Type;

import game.moves.BuyDevelopmentCardMove;

import com.google.gson.*;

public class BuyDevelopmentCardMoveSerialiser implements JsonSerializer<BuyDevelopmentCardMove>
{

	@Override
	public JsonElement serialize(BuyDevelopmentCardMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("colour", move.getPlayerColour().toString());
		
		return obj;
	}
}
