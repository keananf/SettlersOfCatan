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

public class Turn
{
	private Requests.Request.BodyCase chosenMove;

	private Node chosenNode;
	private Edge chosenEdge;
	private ResourceType chosenResource;
	Map<ResourceType, Integer> chosenResources;
	private Hex chosenHex;
	private DevelopmentCardType chosenCard;
	private String chatMessage;
	private Trade.WithBank bankTrade;
	private Trade.WithPlayer playerTrade;
	private Trade.Response tradeResponse;
	private Colour target;

	public Turn()
	{
		setUp();
	}

	public Turn(Requests.Request.BodyCase move)
	{
		this.chosenMove = move;
		setUp();
	}

	void setUp()
	{
		chosenResources = new HashMap<>();
		reset();
	}

	void reset()
	{
		chosenCard = null;
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
	}

	/**
	 * Same as above but leaves expected moves in tact
	 */
	void resetInfo()
	{
		chosenCard = null;
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

	public void setChosenResources(Map<ResourceType, Integer> resources)
	{
		this.chosenResources = resources;
	}

	public Hex getChosenHex()
	{
		return chosenHex;
	}

	public void setChosenHex(Hex chosenHex)
	{
		this.chosenHex = chosenHex;
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
}
