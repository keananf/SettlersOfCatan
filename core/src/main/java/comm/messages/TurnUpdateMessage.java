package main.java.comm.messages;

import java.util.Map;

import main.java.enums.Colour;
import main.java.enums.ResourceType;

/**
 * Class describing a turn update message. Includes info about resources and dice
 * @author 140001596
 */
public class TurnUpdateMessage
{
	private ResourceCount resources;
	private int dice;
	private Colour player;
	
	/**
	 * @return the resources
	 */
	public ResourceCount getResources()
	{
		return resources;
	}
	/**
	 * @param resources the resources to set
	 */
	public void setResources(ResourceCount resources)
	{
		this.resources = resources;
	}
	/**
	 * @return the dice
	 */
	public int getDice()
	{
		return dice;
	}
	/**
	 * @param dice the dice to set
	 */
	public void setDice(int dice)
	{
		this.dice = dice;
	}
	/**
	 * @return the player
	 */
	public Colour getPlayer()
	{
		return player;
	}
	/**
	 * @param player the player to set
	 */
	public void setPlayer(Colour player)
	{
		this.player = player;
	}
	public void setResources(Map<ResourceType, Integer> map)
	{
		ResourceCount count = new ResourceCount();
		count.setBrick(map.get(ResourceType.Brick));
		count.setWool(map.get(ResourceType.Wool));
		count.setLumber(map.get(ResourceType.Lumber));
		count.setOre(map.get(ResourceType.Ore));
		count.setGrain(map.get(ResourceType.Grain));
		
		resources = count;
	}
}
