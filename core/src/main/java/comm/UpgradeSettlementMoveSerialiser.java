package comm;

import java.lang.reflect.Type;

import game.moves.UpgradeSettlementMove;

import com.google.gson.*;

public class UpgradeSettlementMoveSerialiser implements JsonSerializer<UpgradeSettlementMove>
{

	@Override
	public JsonElement serialize(UpgradeSettlementMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty("x", move.getX());
		obj.addProperty("y", move.getY());
		obj.addProperty("colour", move.getPlayerColour().toString());
		
		return obj;
	}
}
