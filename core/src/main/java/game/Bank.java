package game;

import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import exceptions.BankLimitException;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing the bank limits
 * 
 * @author 140001596
 */
public class Bank
{
	private final Map<ResourceType, Integer> availableResources;
	private final Map<DevelopmentCardType, Integer> availableDevCards;
	private final Map<Colour, Integer> availableCities;
	private final Map<Colour, Integer> availableSettlements;
	private final Map<Colour, Integer> availableRoads;

	public Bank()
	{
		availableResources = new HashMap<>();
		availableDevCards = new HashMap<>();
		availableSettlements = new HashMap<>();
		availableCities = new HashMap<>();
		availableRoads = new HashMap<>();
		setUpAvailability();
	}

	private void setUpAvailability()
	{
		int resourceAmount = 19; // Adds up to 95 total resource cards
		int cityAmount = 4, roadAmount = 15, settlementAmount = 5;

		// For each resource type
		for (ResourceType r : ResourceType.values())
		{
			if (r.equals(ResourceType.Generic)) continue;

			availableResources.put(r, resourceAmount);
		}

		// For each player
		for (Colour c : Colour.values())
		{
			availableCities.put(c, cityAmount);
			availableRoads.put(c, roadAmount);
			availableSettlements.put(c, settlementAmount);
		}

		availableDevCards.put(DevelopmentCardType.Knight, 14);
		availableDevCards.put(DevelopmentCardType.University, 1);
		availableDevCards.put(DevelopmentCardType.Chapel, 1);
		availableDevCards.put(DevelopmentCardType.Market, 1);
		availableDevCards.put(DevelopmentCardType.Palace, 1);
		availableDevCards.put(DevelopmentCardType.Library, 1);
		availableDevCards.put(DevelopmentCardType.Monopoly, 2);
		availableDevCards.put(DevelopmentCardType.YearOfPlenty, 2);
		availableDevCards.put(DevelopmentCardType.RoadBuilding, 2);
	}

	public int getAvailableCities()
	{
		int sum = 0;
		for (Colour c : Colour.values())
		{
			sum += availableCities.get(c);
		}
		return sum;
	}

	public int getAvailableRoads()
	{
		int sum = 0;
		for (Colour c : Colour.values())
		{
			sum += availableRoads.get(c);
		}
		return sum;
	}

	public int getAvailableSettlements()
	{
		int sum = 0;
		for (Colour c : Colour.values())
		{
			sum += availableSettlements.get(c);
		}
		return sum;
	}

	public int getAvailableRoads(Colour c)
	{
		return availableRoads.get(c);
	}

	public int getAvailableCities(Colour c)
	{
		return availableCities.get(c);
	}

	public int getAvailableSettlements(Colour c)
	{
		return availableSettlements.get(c);
	}

	public void setAvailableCities(Colour c, int availableCities)
	{
		this.availableCities.put(c, availableCities);
	}

	public void setAvailableSettlements(Colour c, int availableSettlements)
	{
		this.availableSettlements.put(c, availableSettlements);
	}

	public void subtractAvailableDevCards(DevelopmentCardType type)
	{
		int existing = availableDevCards.getOrDefault(type, 0);
		availableDevCards.put(type, existing - 1);
	}

	public void setAvailableRoads(Colour c, int availableRoads)
	{
		this.availableRoads.put(c, availableRoads);
	}

	/**
	 * Gives the resources back to the bank
	 * 
	 * @param grant
	 */
	public void grantResources(Map<ResourceType, Integer> grant)
	{
		for (ResourceType r : grant.keySet())
		{
			availableResources.put(r, availableResources.get(r) + grant.get(r));
		}
	}

	/**
	 * The bank gives the following resources
	 * 
	 * @param spend
	 */
	public void spendResources(Map<ResourceType, Integer> spend) throws BankLimitException
	{
		// Check enough resources
		for (ResourceType r : spend.keySet())
		{
			if (availableResources.get(r) < spend
					.get(r)) { throw new BankLimitException(String.format("Out of Resource: %s", r)); }
		}

		// Deduct from bank
		for (ResourceType r : spend.keySet())
		{
			availableResources.put(r, availableResources.get(r) - spend.get(r));
		}
	}

	public Map<ResourceType, Integer> getAvailableResources()
	{
		return availableResources;
	}

	public Map<DevelopmentCardType, Integer> getAvailableDevCards()
	{
		return availableDevCards;
	}

	public int getNumAvailableResources()
	{
		return availableResources.get(ResourceType.Brick) + availableResources.get(ResourceType.Grain)
				+ availableResources.get(ResourceType.Ore) + availableResources.get(ResourceType.Lumber)
				+ availableResources.get(ResourceType.Wool);
	}

	public int getNumAvailableDevCards()
	{
		int sum = 0;
		for (DevelopmentCardType type : DevelopmentCardType.values())
		{
			sum += availableDevCards.getOrDefault(type, 0);
		}
		return sum;
	}

	/**
	 * Checks to see if the bank canAfford something
	 * 
	 * @param cost the cost
	 */
	public boolean canAfford(Map<ResourceType, Integer> cost)
	{
		// Check if the player can afford this before initiating the purchase
		for (ResourceType r : cost.keySet())
		{
			if (availableResources.get(r) < cost.get(r)) return false;
		}

		return true;
	}
}
