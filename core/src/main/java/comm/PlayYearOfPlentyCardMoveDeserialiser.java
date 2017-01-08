package comm;

import java.lang.reflect.Type;

import enums.*;
import game.moves.PlayYearOfPlentyCardMove;

import com.google.gson.*;

public class PlayYearOfPlentyCardMoveDeserialiser implements JsonDeserializer<PlayYearOfPlentyCardMove>
{

	@Override
	public PlayYearOfPlentyCardMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		PlayYearOfPlentyCardMove move = new PlayYearOfPlentyCardMove();
		
		// Instantiate the message object
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));
		move.setResource1(ResourceType.valueOf(jObj.get("resource1").getAsString()));
		move.setResource2(ResourceType.valueOf(jObj.get("resource2").getAsString()));
		
		return move;
	}
}
