package comm;

import java.lang.reflect.Type;

import enums.Colour;
import game.moves.MoveRobberMove;

import com.google.gson.*;

public class MoveRobberMoveDeserialiser implements JsonDeserializer<MoveRobberMove>
{

	@Override
	public MoveRobberMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		MoveRobberMove move = new MoveRobberMove();
		
		// Instantiate the move object
		move.setX((int) jObj.get("x").getAsNumber());
		move.setY((int) jObj.get("y").getAsNumber());
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));
		move.setColourToTakeFrom(Colour.valueOf(jObj.get("colourToTakeFrom").getAsString()));
		
		return move;
	}

}
