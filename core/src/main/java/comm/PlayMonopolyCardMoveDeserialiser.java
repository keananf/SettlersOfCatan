package main.java.comm;

import java.lang.reflect.Type;

import main.java.enums.*;
import main.java.game.moves.*;

import com.google.gson.*;

public class PlayMonopolyCardMoveDeserialiser implements JsonDeserializer<PlayMonopolyCardMove>
{

	@Override
	public PlayMonopolyCardMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		PlayMonopolyCardMove move = new PlayMonopolyCardMove();
		
		// Instantiate the message object
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));
		move.setResource(ResourceType.valueOf(jObj.get("resource").getAsString()));
		
		return move;
	}
}
