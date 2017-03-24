package client;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.Requests;
import intergroup.trade.Trade;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Turn
{
	private Requests.Request.BodyCase chosenMove;

	private Node chosenNode;
	private Edge chosenEdge;
	private ResourceType chosenResource;
	private Map<ResourceType, Integer> chosenResources;
	private Hex chosenHex;
	private Colour chosenColour;
	private DevelopmentCardType chosenCard;
	private String chatMessage;
	private Trade.WithBank bankTrade;
	private Trade.WithPlayer playerTrade;
	private Trade.Response tradeResponse;
	private Colour target;
	private ConcurrentLinkedQueue<Requests.Request.BodyCase> expectedMoves;
	private boolean tradePhase, turnStarted, initialPhase;
	private int roll;

	public Turn()
	{
		setUp();
	}

	public Turn(Requests.Request.BodyCase move)
	{
		this.chosenMove = move;
		setUp();
	}

	private void setUp()
	{
		expectedMoves = new ConcurrentLinkedQueue<Requests.Request.BodyCase>();
		chosenResources = new HashMap<ResourceType, Integer>();
		reset();
	}

	public void reset()
	{
		roll = 0;
		tradePhase = false;
		turnStarted = false;
		chosenCard = null;
		chosenColour = null;
		chosenEdge = null;
		chosenNode = null;
		chosenResource = null;
		chosenHex = null;
		chatMessage = null;
		bankTrade = null;
		playerTrade = null;
		tradeResponse = null;
		target = null;
		chosenResources.clear();
		expectedMoves.clear();
	}

	/**
	 * Same as above but leaves expected moves in tact
	 */
	public void resetInfo()
	{
		roll = 0;
		tradePhase = false;
		turnStarted = false;
		chosenCard = null;
		chosenColour = null;
		chosenEdge = null;
		chosenNode = null;
		chosenResource = null;
		chosenHex = null;
		chatMessage = null;
		bankTrade = null;
		playerTrade = null;
		tradeResponse = null;
		target = null;

	}

	public ConcurrentLinkedQueue<Requests.Request.BodyCase> getExpectedMoves()
	{
		return expectedMoves;
	}

	public Requests.Request.BodyCase getChosenMove()
	{
		return chosenMove;
	}

	public void setChosenMove(Requests.Request.BodyCase chosenMove)
	{
		this.chosenMove = chosenMove;
	}

	public Node getChosenNode()
	{
		return chosenNode;
	}

	public void setChosenNode(Node chosenNode)
	{
		this.chosenNode = chosenNode;
	}

	public Edge getChosenEdge()
	{
		return chosenEdge;
	}

	public void setChosenEdge(Edge chosenEdge)
	{
		this.chosenEdge = chosenEdge;
	}

	public ResourceType getChosenResource()
	{
		return chosenResource;
	}

	public void setChosenResource(ResourceType chosenResource)
	{
		this.chosenResource = chosenResource;
	}

	public Map<ResourceType, Integer> getChosenResources()
	{
		return chosenResources;
	}

	public void addChosenResource(ResourceType chosenResource)
	{
		int existing = chosenResources.containsKey(chosenResource) ? chosenResources.get(chosenResource) : 0;
		chosenResources.put(chosenResource, existing + 1);
	}

	public Hex getChosenHex()
	{
		return chosenHex;
	}

	public void setChosenHex(Hex chosenHex)
	{
		this.chosenHex = chosenHex;
	}

	public Colour getChosenColour()
	{
		return chosenColour;
	}

	public void setChosenColour(Colour chosenColour)
	{
		this.chosenColour = chosenColour;
	}

	public DevelopmentCardType getChosenCard()
	{
		return chosenCard;
	}

	public void setChosenCard(DevelopmentCardType chosenCard)
	{
		this.chosenCard = chosenCard;
	}

	public String getChatMessage()
	{
		return chatMessage;
	}

	public void setChatMessage(String chatMessage)
	{
		this.chatMessage = chatMessage;
	}

	public Trade.WithPlayer getPlayerTrade()
	{
		return playerTrade;
	}

	public void setPlayerTrade(Trade.WithPlayer playerTrade)
	{
		this.playerTrade = playerTrade;
	}

	public Trade.WithBank getBankTrade()
	{
		return bankTrade;
	}

	public void setBankTrade(Trade.WithBank bankTrade)
	{
		this.bankTrade = bankTrade;
	}

	public Trade.Response getTradeResponse()
	{
		return tradeResponse;
	}

	public void setTradeResponse(Trade.Response tradeResponse)
	{
		this.tradeResponse = tradeResponse;
	}

	public Colour getTarget()
	{
		return target;
	}

	public void setTarget(Colour target)
	{
		this.target = target;
	}

	public boolean isTradePhase()
	{
		return tradePhase;
	}

	public void setTradePhase(boolean tradePhase)
	{
		this.tradePhase = tradePhase;
	}

	public void setChosenResources(Map<ResourceType, Integer> chosenResources)
	{
		this.chosenResources = chosenResources;
	}

	public boolean hasTurnStarted()
	{
		return turnStarted;
	}

	public void setTurnStarted(boolean turnStarted)
	{
		this.turnStarted = turnStarted;
	}

	public boolean isInitialPhase()
	{
		return initialPhase;
	}

	public void setInitialPhase(boolean initialPhase)
	{
		this.initialPhase = initialPhase;
	}

	public int getRoll()
	{
		return roll;
	}

	public void setRoll(int roll)
	{
		this.roll = roll;
	}
}
