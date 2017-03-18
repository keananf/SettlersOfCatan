package client;

import enums.Colour;
import enums.ResourceType;

import java.util.Map;

/**
 * @author 140001596
 */
public class PlayerTrade
{
	private Map<ResourceType, Integer> offer, wanting;
	private Colour other;

	public PlayerTrade(Map<ResourceType, Integer> offer, Map<ResourceType, Integer> wanting, Colour other)
	{
		this.offer = offer;
		this.wanting = wanting;
		this.other = other;
	}

	public void reset()
	{
		offer = null;
		wanting = null;
		other = null;
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

	public Colour getOther()
	{
		return other;
	}

	public void setOther(Colour other)
	{
		this.other = other;
	}
}
