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
	private Map<ResourceType, Integer> chosenResources;
	private Hex chosenHex;
	private Colour chosenColour;
	private DevelopmentCardType chosenCard;
	private String chatMessage;
	private BankTrade bankTrade;
	private PlayerTrade playerTrade;
	private Trade.Response tradeResponse;
	private Colour target;

	public Turn()
	{
		reset();
	}

	public void reset()
	{
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
		chosenResources = new HashMap<ResourceType, Integer>();
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

	public PlayerTrade getPlayerTrade()
	{
		return playerTrade;
	}

	public void setPlayerTrade(PlayerTrade playerTrade)
	{
		this.playerTrade = playerTrade;
	}

	public BankTrade getBankTrade()
	{
		return bankTrade;
	}

	public void setBankTrade(BankTrade bankTrade)
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
