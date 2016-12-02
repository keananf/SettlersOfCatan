package main.java.comm;

import java.lang.reflect.Type;

import main.java.enums.Colour;
import main.java.game.moves.BuyDevelopmentCardMove;

import com.google.gson.*;

/**
 * Class for serialising a development card
 * @author 140001596
 */
public class BuyDevelopmentCardMoveDeserialiser implements JsonDeserializer<BuyDevelopmentCardMove>
{

	@Override
	public BuyDevelopmentCardMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		BuyDevelopmentCardMove move = new BuyDevelopmentCardMove();
		
		// Instantiate the move object
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));		
		
		return move;
	}
}
