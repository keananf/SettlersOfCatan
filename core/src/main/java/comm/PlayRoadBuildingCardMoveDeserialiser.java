package comm;

import java.lang.reflect.Type;

import enums.Colour;
import game.moves.*;

import com.google.gson.*;

public class PlayRoadBuildingCardMoveDeserialiser  implements JsonDeserializer<PlayRoadBuildingCardMove>
{

	@Override
	public PlayRoadBuildingCardMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		PlayRoadBuildingCardMove move = new PlayRoadBuildingCardMove();
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BuildRoadMove.class, new BuildRoadMoveDeserialiser());
		Gson gson = builder.create();

		// Instantiate the message object
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));
		move.setMove1(gson.fromJson(jObj.get("move1").getAsString(), BuildRoadMove.class));
		move.setMove2(gson.fromJson(jObj.get("move2").getAsString(), BuildRoadMove.class));
		
		return move;
	}
}
