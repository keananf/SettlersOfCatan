package comm;

import java.lang.reflect.Type;
import game.moves.EndMove;
import com.google.gson.*;

public class EndMoveSerialiser implements JsonSerializer<EndMove>
{

	@Override
	public JsonElement serialize(EndMove move, Type type, JsonSerializationContext context)
	{
		JsonObject obj = new JsonObject();
		
		obj.addProperty("colour", move.getPlayerColour().toString());
		
		return obj;
	}
}
