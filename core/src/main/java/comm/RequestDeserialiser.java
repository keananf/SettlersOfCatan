package main.java.comm;

import java.lang.reflect.Type;

import main.java.enums.MoveType;
import main.java.game.moves.*;

import com.google.gson.*;

/**
 * This class deserialises a Request
 * @author 140001596
 */
public class RequestDeserialiser implements JsonDeserializer<Request>
{

	@Override
	public Request deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject jObj = json.getAsJsonObject();
		Request request = new Request();
		
		// Instantiate the message object
		MoveType msgType = MoveType.valueOf(jObj.get("type").getAsString());
		request.setType(msgType);
		request.setMsg(jObj.get("message").getAsString());
		
		return request;
	}

	/**
	 * Takes the raw bytes and deserialises them as a Request
	 * @param bytes the request
	 * @return the request object
	 */
	public Request deserialise(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(Request.class, this);
		Gson gson = builder.create();
		
		return gson.fromJson(bytes.toString(), Request.class);
	}

	/**
	 * Deserialises the bytes as a BuildRoadMove 
	 * @param bytes the move
	 * @return the move
	 */
	public BuildRoadMove getBuildRoadMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BuildRoadMove.class, new BuildRoadMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), BuildRoadMove.class);
	}

	/**
	 * Deserialises the bytes as a BuildSettlementMove 
	 * @param bytes the move
	 * @return the move
	 */
	public BuildSettlementMove getBuildSettlementMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BuildSettlementMove.class, new BuildSettlementMoveDeserialiser());
		builder.registerTypeHierarchyAdapter(UpgradeSettlementMove.class, new UpgradeSettlementMoveDeserialiser());
		builder.registerTypeHierarchyAdapter(MoveRobberMove.class, new MoveRobberMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), BuildSettlementMove.class);
	}

	/**
	 * Deserialises the bytes as a UpgradeSettlementMove
	 * @param bytes the move
	 * @return the move
	 */
	public UpgradeSettlementMove getUpgradeSettlementMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(UpgradeSettlementMove.class, new UpgradeSettlementMoveDeserialiser());
		builder.registerTypeHierarchyAdapter(MoveRobberMove.class, new MoveRobberMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), UpgradeSettlementMove .class);
	}
	
	/**
	 * Deserialises the bytes as a MoveRobberMove
	 * @param bytes the move
	 * @return the move
	 */
	public MoveRobberMove getMoveRobberMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(MoveRobberMove.class, new MoveRobberMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), MoveRobberMove.class);
	}
	
	/**
	 * Deserialises the bytes as a BuyDevelopmentCardMove
	 * @param bytes the move
	 * @return the move
	 */
	public BuyDevelopmentCardMove getBuyDevelopmentCardMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BuyDevelopmentCardMove.class, new BuyDevelopmentCardMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), BuyDevelopmentCardMove.class);
	}
}