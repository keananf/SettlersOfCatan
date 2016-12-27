package main.java.comm;

import java.lang.reflect.Type;

import main.java.enums.Colour;
import main.java.enums.DevelopmentCardType;
import main.java.game.build.DevelopmentCard;

import com.google.gson.*;

public class DevelopmentCardDeserialiser implements JsonDeserializer<DevelopmentCard>
{

	@Override
	public DevelopmentCard deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		DevelopmentCard card = new DevelopmentCard();
		
		// Instantiate the move object
		card.setType(DevelopmentCardType.valueOf(jObj.get("type").getAsString()));
		card.setColour(Colour.valueOf(jObj.get("colour").getAsString()));
		
		return card;
	}
}
