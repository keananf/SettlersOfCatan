package main.java.comm;

import java.lang.reflect.Type;

import main.java.enums.Colour;
import main.java.game.moves.EndMove;

import com.google.gson.*;

/**
 * Class for deserialising an end move turn
 * @author 140001596
 */
public class EndMoveDeserialiser implements JsonDeserializer<EndMove>
{

	@Override
	public EndMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		EndMove move = new EndMove();
		
		// Instantiate the move object
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));		
		
		return move;
	}
}