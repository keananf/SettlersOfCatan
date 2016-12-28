package main.java.comm;

import java.lang.reflect.Type;

import main.java.game.moves.*;

import com.google.gson.*;

public class PlayRoadBuildingCardMoveSerialiser implements JsonSerializer<PlayRoadBuildingCardMove>
{

	@Override
	public JsonElement serialize(PlayRoadBuildingCardMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BuildRoadMove.class, new BuildRoadMoveSerialiser());
		Gson gson = builder.create();
		
		obj.addProperty("colour", move.getPlayerColour().toString());
		obj.addProperty("move1", gson.toJson(move.getMove1()));
		obj.addProperty("move2", gson.toJson(move.getMove2().toString()));
		
		return obj;
	}
}
