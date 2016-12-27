package main.java.game.build;

import main.java.enums.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Enum representing the different options of a development card
 * @author 140001596
 */
public class DevelopmentCard implements IBuildable //TODO change to normal inheritance?
{
	private DevelopmentCardType type;
	private Colour colour;
	private static Random rand;
	
	static
	{
		rand = new Random();
	}
	
	/**
	 * @return a map containing the total cost for all resources
	 */
	public Map<ResourceType, Integer> getCost()
	{
		Map<ResourceType, Integer> resources = new HashMap<ResourceType, Integer>();
		
		resources.put(ResourceType.Stone, 1);
		resources.put(ResourceType.Wheat, 1);
		resources.put(ResourceType.Sheep, 1);
		
		return resources;
	}
	
	public static Map<ResourceType, Integer> getCardCost()
	{
		return (new DevelopmentCard()).getCost();
	}

	public static DevelopmentCard chooseRandom(Colour colour)
	{
		DevelopmentCard c = new DevelopmentCard();

		// Randomly choose a development card to allocate
		c.type = DevelopmentCardType.values()[rand.nextInt(DevelopmentCardType.values().length)];
		c.colour = colour;
		
		return c;
	}

	/**
	 * @return the type
	 */
	public DevelopmentCardType getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(DevelopmentCardType type)
	{
		this.type = type;
	}

	/**
	 * @return the colour
	 */
	public Colour getColour()
	{
		return colour;
	}

	/**
	 * @param colour the colour to set
	 */
	public void setColour(Colour colour)
	{
		this.colour = colour;
	}
}
