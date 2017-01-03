package main.java.comm.messages;


import java.util.HashMap;
import java.util.Map;

import main.java.enums.*;
import main.java.game.moves.Move;

public class TradeMessage extends Move
{
	private ResourceCount request, offer;
	private Colour recipient;
	private String message;
	private TradeStatus status;
	
	/**
	 * @return the request
	 */
	public ResourceCount getRequest()
	{
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(ResourceCount request)
	{
		this.request = request;
	}

	/**
	 * @return the offer
	 */
	public ResourceCount getOffer()
	{
		return offer;
	}

	/**
	 * @param offer the offer to set
	 */
	public void setOffer(ResourceCount offer)
	{
		this.offer = offer;
	}

	/**
	 * @return the receiver
	 */
	public Colour getRecipient()
	{
		return recipient;
	}

	/**
	 * @param receiver the receiver to set
	 */
	public void setRecipient(Colour receiver)
	{
		this.recipient = receiver;
	}

	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	/**
	 * @return the status
	 */
	public TradeStatus getStatus()
	{
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(TradeStatus status)
	{
		this.status = status;
	}

	public Map<ResourceType, Integer> getOfferAsMap()
	{
		Map<ResourceType, Integer> offer = new HashMap<ResourceType, Integer>();
		
		offer.put(ResourceType.Brick, this.offer.getBrick());
		offer.put(ResourceType.Grain, this.offer.getGrain());
		offer.put(ResourceType.Ore, this.offer.getOre());
		offer.put(ResourceType.Wool, this.offer.getWool());
		offer.put(ResourceType.Lumber, this.offer.getLumber());

		return offer;
	}

	public Map<ResourceType, Integer> getRequestAsMap()
	{
		Map<ResourceType, Integer> request = new HashMap<ResourceType, Integer>();
		
		request.put(ResourceType.Brick, this.request.getBrick());
		request.put(ResourceType.Grain, this.request.getGrain());
		request.put(ResourceType.Ore, this.request.getOre());
		request.put(ResourceType.Wool, this.request.getWool());
		request.put(ResourceType.Lumber, this.request.getLumber());

		return request;
	}
}
