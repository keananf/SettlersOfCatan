package comm;

import java.lang.reflect.Type;

import enums.Colour;
import game.moves.BuildSettlementMove;

import com.google.gson.*;

public class BuildSettlementMoveDeserialiser implements JsonDeserializer<BuildSettlementMove>
{

	@Override
	public BuildSettlementMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		BuildSettlementMove move = new BuildSettlementMove();
		
		// Instantiate the move object
		move.setX((int) jObj.get("x").getAsNumber());
		move.setY((int) jObj.get("y").getAsNumber());
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));		
		
		return move;
	}

}
