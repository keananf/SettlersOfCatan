package comm;

import java.lang.reflect.Type;

import comm.messages.Request;
import comm.messages.TradeMessage;
import enums.MoveType;
import game.moves.*;

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
	 * Takes the raw bytes and deserialises them as a Move
	 * @param bytes the request
	 * @param type the move type
	 * @return the move object
	 */
	public Move deserialiseMove(byte[] bytes, MoveType type)
	{
		Move move = null;
		
		// Switch on message type to interpret the move
		switch(type)
		{
			case BuildRoad:
				move = getBuildRoadMove(bytes);
				break;
			case BuildSettlement:
				move = getBuildSettlementMove(bytes);
				break;
			case MoveRobber:
				move = getMoveRobberMove(bytes);
				break;
			case UpgradeSettlement:
				move = getUpgradeSettlementMove(bytes);
				break;
			case BuyDevelopmentCard:
				move = getBuyDevelopmentCardMove(bytes);
				break;
			case PlayDevelopmentCard:
				move = getPlayDevelopmentCardMove(bytes);
				break;
			case EndMove:
				move = getEndMove(bytes);
				break;
			case TradeMove:
				move = getTradeMessage(bytes);
				break;
		}
		
		return move;
	}

	/**
	 * Parses the specific kind of development card move
	 * @param move the development card move
	 * @return the internal move
	 */
	public Move getInternalDevCardMove(PlayDevelopmentCardMove move)
	{
		Move internalMove = null;
		
		switch (move.getCard().getType())
		{
			case Knight:
				internalMove = getMoveRobberMove(move.getMoveAsJson().getBytes());
				break;
			case Monopoly:
				internalMove = getPlayMonopolyCardMove(move.getMoveAsJson().getBytes());
				break;
			case RoadBuilding:
				internalMove = getPlayRoadBuildingCardMove(move.getMoveAsJson().getBytes());
				break;
			case YearOfPlenty:
				internalMove = getPlayYearOfPlentyCardMove(move.getMoveAsJson().getBytes());
				break;
			default:
				break;
		}
		
		return internalMove;
	}

	/**
	 * Takes the raw bytes and deserialises them as a Request
	 * @param bytes the request
	 * @return the request object
	 */
	public Request deserialiseRequest(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(Request.class, this);
		Gson gson = builder.create();
		
		return gson.fromJson(bytes.toString(), Request.class);
	}

	/**
	 * Deserialises the bytes as a PlayDevelopmentCardMove
	 * @param bytes the move
	 * @return the move
	 */
	public PlayDevelopmentCardMove getPlayDevelopmentCardMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(PlayDevelopmentCardMove.class, new PlayDevelopmentCardMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), PlayDevelopmentCardMove.class);
	}
	
	/**
	 * Deserialises the bytes as a BuildRoadMove 
	 * @param bytes the move
	 * @return the move
	 */
	private BuildRoadMove getBuildRoadMove(byte[] bytes)
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
	private BuildSettlementMove getBuildSettlementMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BuildSettlementMove.class, new BuildSettlementMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), BuildSettlementMove.class);
	}

	/**
	 * Deserialises the bytes as a UpgradeSettlementMove
	 * @param bytes the move
	 * @return the move
	 */
	private UpgradeSettlementMove getUpgradeSettlementMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(UpgradeSettlementMove.class, new UpgradeSettlementMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), UpgradeSettlementMove .class);
	}
	
	/**
	 * Deserialises the bytes as a MoveRobberMove
	 * @param bytes the move
	 * @return the move
	 */
	private MoveRobberMove getMoveRobberMove(byte[] bytes)
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
	private BuyDevelopmentCardMove getBuyDevelopmentCardMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(BuyDevelopmentCardMove.class, new BuyDevelopmentCardMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), BuyDevelopmentCardMove.class);
	}

	/**
	 * Deserialises the bytes as a PlayMonopolyCardMove
	 * @param bytes the move
	 * @return the move
	 */
	private PlayMonopolyCardMove getPlayMonopolyCardMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(PlayMonopolyCardMove.class, new PlayMonopolyCardMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), PlayMonopolyCardMove.class);
	}

	/**
	 * Deserialises the bytes as a PlayYearOfPlentyCardMove 
	 * @param bytes the move
	 * @return the move
	 */
	private PlayYearOfPlentyCardMove getPlayYearOfPlentyCardMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(PlayYearOfPlentyCardMove.class, new PlayYearOfPlentyCardMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), PlayYearOfPlentyCardMove.class);
	}
	
	/**
	 * Deserialises the bytes as a PlayRoadBuildingCardMove 
	 * @param bytes the move
	 * @return the move
	 */
	private PlayRoadBuildingCardMove getPlayRoadBuildingCardMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(PlayRoadBuildingCardMove.class, new PlayRoadBuildingCardMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), PlayRoadBuildingCardMove.class);
	}

	/**
	 * Deserialises the bytes as a TradeMessage
	 * @param bytes the move
	 * @return the move
	 */
	private Move getEndMove(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(EndMove.class, new EndMoveDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), EndMove.class);
	}
	
	/**
	 * Deserialises the bytes as a TradeMessage
	 * @param bytes the move
	 * @return the move
	 */
	private TradeMessage getTradeMessage(byte[] bytes)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(TradeMessage.class, new TradeMessageDeserialiser());
		Gson gson = builder.create();

		return gson.fromJson(bytes.toString(), TradeMessage.class);
	}
}