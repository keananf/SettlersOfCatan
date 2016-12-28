package main.java.comm;

import java.lang.reflect.Type;
import main.java.game.moves.PlayMonopolyCardMove;
import com.google.gson.*;

public class PlayMonopolyCardMoveSerialiser implements JsonSerializer<PlayMonopolyCardMove>
{

	@Override
	public JsonElement serialize(PlayMonopolyCardMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("colour", move.getPlayerColour().toString());
		obj.addProperty("resource", move.getResource().toString());
		
		return obj;
	}
}
