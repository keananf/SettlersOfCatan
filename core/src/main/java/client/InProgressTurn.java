package client;

import enums.ClickObject;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import grid.Edge;
import grid.Hex;
import grid.Node;
import intergroup.Requests;

import java.util.ArrayList;
import java.util.List;

public class InProgressTurn
{
	private ClickObject initialClickObject;
	private List<Requests.Request.BodyCase> possibilities;
	private Requests.Request.BodyCase chosenMove;
	
	private Node chosenNode;
	private Edge chosenEdge;
	private Edge[] chosenEdges = new Edge[2];
	private ResourceType chosenResource;
	private List<ResourceType> chosenResources;
	private Hex chosenHex;
	private Colour chosenColour;
	private DevelopmentCardType chosenCard;
	private String chatMessage;

	public InProgressTurn()
	{
		possibilities = new ArrayList<Requests.Request.BodyCase>();
		chosenResources = new ArrayList<ResourceType>();
	}

	public ClickObject getInitialClickObject()
	{
		return initialClickObject;
	}

	public void setInitialClickObject(ClickObject initialClickObject)
	{
		this.initialClickObject = initialClickObject;
	}

	public List<Requests.Request.BodyCase> getPossibilities()
	{
		return possibilities;
	}

	public void addPossibility(Requests.Request.BodyCase possibility)
	{
		possibilities.add(possibility);
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

	public Edge[] getChosenEdges()
	{
		return chosenEdges;
	}

	public void setChosenEdges(Edge[] chosenEdges)
	{
		this.chosenEdges = chosenEdges;
	}

	public ResourceType getChosenResource()
	{
		return chosenResource;
	}

	public void setChosenResource(ResourceType chosenResource)
	{
		this.chosenResource = chosenResource;
	}

	public List<ResourceType> getChosenResources()
	{
		return chosenResources;
	}

	public void addChosenResource(ResourceType chosenResource)
	{
		chosenResources.add(chosenResource);
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
}
