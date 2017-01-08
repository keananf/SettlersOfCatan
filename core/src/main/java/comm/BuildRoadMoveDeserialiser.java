
package comm;

import java.lang.reflect.Type;
import enums.*;
import game.moves.BuildRoadMove;

import com.google.gson.*;

/**
 * This class deserialises a BuildRoadMoveRequest
 * @author 140001596
 */
public class BuildRoadMoveDeserialiser implements JsonDeserializer<BuildRoadMove>
{

	@Override
	public BuildRoadMove deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		BuildRoadMove move = new BuildRoadMove();
		
		// Instantiate the move object
		move.setX1((int) jObj.get("x1").getAsNumber());
		move.setY1((int) jObj.get("y1").getAsNumber());
		move.setX2((int) jObj.get("x2").getAsNumber());
		move.setY2((int) jObj.get("y2").getAsNumber());
		move.setPlayerColour(Colour.valueOf(jObj.get("colour").getAsString()));		
		
		return move;
	}

}