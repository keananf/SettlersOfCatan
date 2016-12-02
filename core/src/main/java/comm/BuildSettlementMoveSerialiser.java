package main.java.comm;

import java.lang.reflect.Type;
import main.java.game.moves.BuildSettlementMove;
import com.google.gson.*;

public class BuildSettlementMoveSerialiser implements JsonSerializer<BuildSettlementMove>
{

	@Override
	public JsonElement serialize(BuildSettlementMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty("x", move.getX());
		obj.addProperty("y", move.getY());
		obj.addProperty("colour", move.getPlayerColour().toString());
		
		return obj;
	}
}
