package main.java.comm;

import java.lang.reflect.Type;

import main.java.game.moves.PlayDevelopmentCardMove;

import com.google.gson.*;

public class PlayDevelopmentCardMoveSerialiser implements JsonSerializer<PlayDevelopmentCardMove>
{

	@Override
	public JsonElement serialize(PlayDevelopmentCardMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		obj.addProperty("colour", move.getPlayerColour().toString());
		obj.addProperty("card", move.getCard().getType().toString());
		
		return obj;
	}
}