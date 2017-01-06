package comm;

import java.lang.reflect.Type;

import game.moves.BuildRoadMove;

import com.google.gson.*;


/**
 * This class serialises a BuildRoadMove request
 * @author 140001596
 */
public class BuildRoadMoveSerialiser implements JsonSerializer<BuildRoadMove>
{

	@Override
	public JsonElement serialize(BuildRoadMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty("x1", move.getX1());
		obj.addProperty("y1", move.getY1());
		obj.addProperty("x2", move.getX2());
		obj.addProperty("y2", move.getY2());
		obj.addProperty("colour", move.getPlayerColour().toString());
		
		return obj;
	}
}