package main.java.comm;

import java.lang.reflect.Type;

import main.java.enums.Colour;
import main.java.game.build.DevelopmentCard;
import main.java.game.moves.PlayDevelopmentCardMove;

import com.google.gson.*;

/**
 * Deserialises the json of a PlayDevelopmentCardMove received from across the network 
 * @author 140001596
 */
public class PlayDevelopmentCardMoveDeserialiser implements JsonDeserializer<PlayDevelopmentCardMove>
{

	@Override
	public PlayDevelopmentCardMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		PlayDevelopmentCardMove move = new PlayDevelopmentCardMove();
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(DevelopmentCard.class, new DevelopmentCardDeserialiser());
		Gson gson = builder.create();
		
		// Instantiate the move object
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));
		move.setCard(gson.fromJson(jObj.get("card").getAsString(), DevelopmentCard.class));		
		
		return move;
	}
}
