package comm;

import java.lang.reflect.Type;

import game.moves.MoveRobberMove;

import com.google.gson.*;

public class MoveRobberMoveSerialiser implements JsonSerializer<MoveRobberMove>
{

	@Override
	public JsonElement serialize(MoveRobberMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty("x", move.getX());
		obj.addProperty("y", move.getY());
		obj.addProperty("colour", move.getPlayerColour().toString());
		obj.addProperty("colourToTakeFrom", move.getColourToTakeFrom().toString());
		
		return obj;
	}
}
