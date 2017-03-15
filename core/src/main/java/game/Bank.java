package game;

import enums.DevelopmentCardType;
import enums.ResourceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing the bank limits
 * @author 140001596
 */
public class Bank
{
    private Map<ResourceType, Integer> availableResources;
    private Map<DevelopmentCardType, Integer> availableDevCards;
    private int availableCities = 16, availableSettlements = 20;
    private int availableRoads = 60;

    public Bank()
    {
        availableResources = new HashMap<ResourceType, Integer>();
        availableDevCards = new HashMap<DevelopmentCardType, Integer>();
        setUpAvailability();
    }

    private void setUpAvailability()
    {
        int resourceAmount = 19; // Adds up to 95 total resource cards

        // For each resource type
        for(ResourceType r : ResourceType.values())
        {
            availableResources.put(r, resourceAmount);
        }

        availableDevCards.put(DevelopmentCardType.Knight, 14);
        availableDevCards.put(DevelopmentCardType.University, 2);
        availableDevCards.put(DevelopmentCardType.Library, 3);
        availableDevCards.put(DevelopmentCardType.Monopoly, 2);
        availableDevCards.put(DevelopmentCardType.YearOfPlenty, 2);
        availableDevCards.put(DevelopmentCardType.RoadBuilding, 2);
    }

    public int getAvailableCities()
    {
        return availableCities;
    }

    public int getAvailableRoads()
    {
        return availableRoads;
    }

    public int getAvailableSettlements()
    {
        return availableSettlements;
    }

    public Map<DevelopmentCardType, Integer> getAvailableDevCards()
    {
        return availableDevCards;
    }

    public Map<ResourceType, Integer> getAvailableResources()
    {
        return availableResources;
    }
}
