package client;

import enums.ResourceType;

import java.util.Map;

/**
 * @author 140001596
 */
public class BankTrade
{
	private Map<ResourceType, Integer> offer, wanting;

	public BankTrade(Map<ResourceType, Integer> offer, Map<ResourceType, Integer> wanting)
	{
		this.offer = offer;
		this.wanting = wanting;
	}

	public Map<ResourceType, Integer> getOffer()
	{
		return offer;
	}

	public void setOffer(Map<ResourceType, Integer> offer)
	{
		this.offer = offer;
	}

	public Map<ResourceType, Integer> getWanting()
	{
		return wanting;
	}

	public void setWanting(Map<ResourceType, Integer> wanting)
	{
		this.wanting = wanting;
	}
}
