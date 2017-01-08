package comm;

import java.lang.reflect.Type;

import enums.Colour;
import game.moves.UpgradeSettlementMove;

import com.google.gson.*;

public class UpgradeSettlementMoveDeserialiser implements JsonDeserializer<UpgradeSettlementMove>
{

	@Override
	public UpgradeSettlementMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		UpgradeSettlementMove move = new UpgradeSettlementMove();
		
		// Instantiate the move object
		move.setX((int) jObj.get("x").getAsNumber());
		move.setY((int) jObj.get("y").getAsNumber());
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));		
		
		return move;
	}

}
