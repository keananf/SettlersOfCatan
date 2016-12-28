package main.java.comm;

import java.lang.reflect.Type;

import main.java.game.moves.PlayYearOfPlentyCardMove;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PlayYearOfPlentyCardMoveSerialiser implements JsonSerializer<PlayYearOfPlentyCardMove>
{

	@Override
	public JsonElement serialize(PlayYearOfPlentyCardMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("colour", move.getPlayerColour().toString());
		obj.addProperty("resource1", move.getResource1().toString());
		obj.addProperty("resource2", move.getResource2().toString());
		
		return obj;
	}
}
	
